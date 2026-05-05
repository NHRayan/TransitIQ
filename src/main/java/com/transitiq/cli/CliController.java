/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : CliController.java – Interactive command‑line interface
 */

package com.transitiq.cli;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.transitiq.exception.InvalidNodeException;
import com.transitiq.exception.NoRouteException;
import com.transitiq.exception.StaleSnapshotException;
import com.transitiq.graph.CityGraph;
import com.transitiq.graph.GraphLoader;
import com.transitiq.io.TripLogger;
import com.transitiq.io.TripRecord;
import com.transitiq.routing.*;
import com.transitiq.state.GraphStateManager;
import com.transitiq.state.TrafficSimulator;
import com.transitiq.validation.InputValidator;

/**
 * Bootstraps all TransitIQ components and provides an interactive command loop.
 *
 * <p>Supported commands:</p>
 * <ul>
 *   <li>{@code route STRATEGY FROM_ID TO_ID} — find a path</li>
 *   <li>{@code congestion} — print per‑district delay averages</li>
 *   <li>{@code trips} — list trip history (stub)</li>
 *   <li>{@code exit} — shut down cleanly</li>
 * </ul>
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public final class CliController {

  private CliController() { /* utility */ }

  /**
   * Application entry point.
   *
   * @param args command‑line arguments (not used)
   */
  public static void main(final String[] args) {
    System.out.println("============================================");
    System.out.println(" TransitIQ – Lock‑Free Multi‑Modal Router");
    System.out.println(" Group 4 – BOLEH | Nahid Hasan Rayan (NHR)");
    System.out.println("============================================");

    // ---- 1. Load graph ----
    Path nodesFile = Path.of("data", "nodes.csv");
    Path edgesFile = Path.of("data", "edges.csv");
    System.out.print("Loading graph... ");
    CityGraph graph = GraphLoader.loadGraph(nodesFile, edgesFile);
    System.out.println("OK (" + graph.nodeCount() + " nodes, "
                       + graph.edgeCount() + " edges)");

    // ---- 2. Compute min cost/CO₂ rates for admissible heuristics ----
    double minCostPerKm = computeMinCostPerKm(graph);
    double minCo2PerKm  = computeMinCo2PerKm(graph);

    // ---- 3. Initialise state and simulator ----
    Map<String, Double> initialCongestion = new HashMap<>();
    for (String id : graph.getNodes().keySet()) {
      for (var edge : graph.getEdges(id)) {
        initialCongestion.put(edge.edgeKey(), 0.0);
      }
    }
    GraphStateManager stateManager = new GraphStateManager(initialCongestion);
    TrafficSimulator simulator = new TrafficSimulator(stateManager, graph, 5);
    simulator.start();
    System.out.println("Traffic simulator started (5s interval)");

    // ---- 4. Create router ----
    AStarRouter router = new AStarRouter(graph, stateManager);

    // ---- 5. Trip logger ----
    TripLogger tripLogger = new TripLogger(Path.of("trip_history.json"));

    // ---- 6. Interactive loop ----
    Scanner scanner = new Scanner(System.in);
    System.out.println("\nType 'route STRATEGY FROM TO', 'congestion', 'trips', or 'exit'");

    while (true) {
      System.out.print("\n> ");
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) {
        continue;
      }

      String[] parts = line.split("\\s+");
      String command = parts[0].toLowerCase();

      try {
        switch (command) {
          case "route" -> {
            if (parts.length < 4) {
              System.out.println("Usage: route STRATEGY FROM_ID TO_ID");
              continue;
            }
            String strategyName = parts[1].toUpperCase();
            String fromId = parts[2].toUpperCase();
            String toId   = parts[3].toUpperCase();

            if (!InputValidator.isValidStrategy(strategyName)) {
              System.out.println("Unknown strategy: " + strategyName
                                 + ". Use FASTEST, CHEAPEST, or ECO.");
              continue;
            }
            if (!InputValidator.isValidStation(fromId)
                || !InputValidator.isValidStation(toId)) {
              System.out.println("Invalid station code. Format: two letters + two digits (e.g., J01).");
              continue;
            }

            RoutingStrategy strategy = switch (strategyName) {
              case "FASTEST" -> new FastestStrategy();
              case "CHEAPEST" -> new CheapestStrategy(minCostPerKm);
              case "ECO"      -> new EcoStrategy(minCo2PerKm);
              default -> throw new IllegalStateException("Unexpected strategy: " + strategyName);
            };

            RouteResult result = router.findRoute(fromId, toId, strategy);
            System.out.println(result);

            // log asynchronously
            TripRecord record = TripRecord.from(fromId, toId, strategyName, result);
            tripLogger.logAsync(record);
            System.out.println("Trip logged asynchronously.");
          }

          case "congestion" -> {
            Map<String, Double> snapshot = stateManager.getSnapshot();
            if (snapshot == null || snapshot.isEmpty()) {
              System.out.println("No traffic data available.");
              continue;
            }
            System.out.println("=== District Congestion Report ===");
            // group by district (extracted from edge key or graph edge district)
            // For simplicity, we print per‑edge delays in descending order.
            snapshot.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(15)
                .forEach(e -> System.out.printf("  %-20s delay = %.2f%n",
                                                e.getKey(), e.getValue()));
          }

          case "trips" -> {
            System.out.println("Trip history is at " + tripLogger.getLogFile().toAbsolutePath());
          }

          case "exit" -> {
            System.out.println("Shutting down...");
            simulator.stop();
            scanner.close();
            return;
          }

          default -> System.out.println("Unknown command: " + command);
        }
      } catch (NoRouteException e) {
        System.out.println("No route found: " + e.getMessage());
      } catch (InvalidNodeException e) {
        System.out.println("Invalid node: " + e.getMessage());
      } catch (StaleSnapshotException e) {
        System.out.println("Traffic snapshot stale, retrying...");
      } catch (Exception e) {
        System.out.println("Unexpected error: " + e.getMessage());
      }
    }
  }

  // ---------- helper methods ----------

  /** Scan all edges and return the smallest cost per km. */
  private static double computeMinCostPerKm(final CityGraph graph) {
    double min = Double.POSITIVE_INFINITY;
    for (String id : graph.getNodes().keySet()) {
      for (var edge : graph.getEdges(id)) {
        var fromNode = graph.getNode(edge.from());
        var toNode   = graph.getNode(edge.to());
        if (fromNode != null && toNode != null) {
          double dist = FastestStrategy.haversineKm(
              fromNode.lat(), fromNode.lon(),
              toNode.lat(), toNode.lon());
          if (dist > 0) {
            double rate = edge.cost() / dist;
            if (rate < min) {
              min = rate;
            }
          }
        }
      }
    }
    return Double.isFinite(min) ? min : 0.0;
  }

  /** Scan all edges and return the smallest CO₂ per km. */
  private static double computeMinCo2PerKm(final CityGraph graph) {
    double min = Double.POSITIVE_INFINITY;
    for (String id : graph.getNodes().keySet()) {
      for (var edge : graph.getEdges(id)) {
        var fromNode = graph.getNode(edge.from());
        var toNode   = graph.getNode(edge.to());
        if (fromNode != null && toNode != null) {
          double dist = FastestStrategy.haversineKm(
              fromNode.lat(), fromNode.lon(),
              toNode.lat(), toNode.lon());
          if (dist > 0) {
            double rate = edge.co2PerKm();  // already per‑km
            if (rate < min) {
              min = rate;
            }
          }
        }
      }
    }
    return Double.isFinite(min) ? min : 0.0;
  }
}