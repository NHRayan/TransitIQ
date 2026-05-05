/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : AStarRouter.java – Generic A* search engine
 */

package com.transitiq.routing;

import com.transitiq.exception.InvalidNodeException;
import com.transitiq.exception.NoRouteException;
import com.transitiq.exception.StaleSnapshotException;
import com.transitiq.graph.CityGraph;
import com.transitiq.graph.TransitEdge;
import com.transitiq.graph.TransitNode;
import com.transitiq.state.GraphStateManager;
import java.util.*;

/**
 * Performs lock‑free A* route searches.
 *
 * <p>Each search captures a frozen snapshot from {@link GraphStateManager}
 * and then works entirely on local data structures.
 * No shared mutable state — safe for concurrent calls.</p>
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class AStarRouter {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private final CityGraph graph;
  private final GraphStateManager stateManager;

  /**
   * @param graph        the loaded city network
   * @param stateManager provides the current traffic snapshot
   */
  public AStarRouter(final CityGraph graph,
                     final GraphStateManager stateManager) {
    this.graph = graph;
    this.stateManager = stateManager;
  }

  /**
   * Main entry point — compute a route.
   *
   * @param fromId   origin node ID
   * @param toId     destination node ID
   * @param strategy FASTEST, CHEAPEST, or ECO
   * @return a fully described {@link RouteResult}
   * @throws NoRouteException        if the open set is exhausted without reaching the goal
   * @throws InvalidNodeException    if either node ID is not found in the graph
   * @throws StaleSnapshotException  if the traffic snapshot is {@code null} (retryable)
   */
  public RouteResult findRoute(final String fromId,
                               final String toId,
                               final RoutingStrategy strategy)
      throws NoRouteException, InvalidNodeException, StaleSnapshotException {

    // 1. grab an immutable snapshot — no lock
    Map<String, Double> snapshot = stateManager.getSnapshot();
    if (snapshot == null) {
      throw new StaleSnapshotException();
    }

    TransitNode start = graph.getNode(fromId);
    TransitNode goal  = graph.getNode(toId);

    if (start == null) {
      throw new InvalidNodeException(fromId);
    }
    if (goal == null) {
      throw new InvalidNodeException(toId);
    }

    // 2. pre‑compute edge lengths (km) for CO₂ calculations
    Map<String, Double> edgeLengthKm = new HashMap<>();
    for (TransitEdge e : getAllEdges()) {
      TransitNode fromNode = graph.getNode(e.from());
      TransitNode toNode   = graph.getNode(e.to());
      if (fromNode != null && toNode != null) {
        double dist = haversineKm(fromNode.lat(), fromNode.lon(),
                                  toNode.lat(), toNode.lon());
        edgeLengthKm.put(e.edgeKey(), dist);
      } else {
        edgeLengthKm.put(e.edgeKey(), 0.0);
      }
    }

    // 3. A* data structures
    PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
    Set<String> closedSet = new HashSet<>();
    Map<String, Double> gScore = new HashMap<>();
    Map<String, AStarNode> nodeMap = new HashMap<>();

    AStarNode startNode = new AStarNode(start, null, null,
                                        0.0, strategy.heuristic(start, goal));
    openSet.add(startNode);
    gScore.put(fromId, 0.0);
    nodeMap.put(fromId, startNode);

    // 4. main search loop
    while (!openSet.isEmpty()) {
      AStarNode current = openSet.poll();
      String currentId = current.node.id();

      if (currentId.equals(toId)) {
        return buildRoute(current, strategy, snapshot, edgeLengthKm);
      }

      if (closedSet.contains(currentId)) {
        continue;
      }
      closedSet.add(currentId);

      for (TransitEdge edge : graph.getEdges(currentId)) {
        String neighborId = edge.to();

        if (closedSet.contains(neighborId)) {
          continue;
        }

        TransitNode neighborNode = graph.getNode(neighborId);
        if (neighborNode == null) {
          continue;
        }

        double delay = snapshot.getOrDefault(edge.edgeKey(), 0.0);
        if (delay > 3.0) {
          continue;
        }

        double edgeCost;
        if (strategy instanceof FastestStrategy) {
          edgeCost = edge.baseTimeSeconds() * (1.0 + delay);
        } else if (strategy instanceof CheapestStrategy) {
          edgeCost = edge.cost();
        } else {
          double distKm = edgeLengthKm.getOrDefault(edge.edgeKey(), 1.0);
          edgeCost = edge.co2PerKm() * distKm;
        }

        double tentativeG = gScore.getOrDefault(currentId, Double.POSITIVE_INFINITY) + edgeCost;

        if (tentativeG < gScore.getOrDefault(neighborId, Double.POSITIVE_INFINITY)) {
          gScore.put(neighborId, tentativeG);
          double h = strategy.heuristic(neighborNode, goal);
          AStarNode nextNode = new AStarNode(neighborNode, edge, current, tentativeG, h);
          openSet.add(nextNode);
          nodeMap.put(neighborId, nextNode);
        }
      }
    }

    throw new NoRouteException(fromId, toId);
  }

  // ---------- private helpers ----------

  private RouteResult buildRoute(final AStarNode goalNode,
                                 final RoutingStrategy strategy,
                                 final Map<String, Double> snapshot,
                                 final Map<String, Double> lengthMap) {
    List<RouteSegment> segments = new ArrayList<>();
    AStarNode current = goalNode;
    while (current.edge != null) {
      double time  = current.edge.baseTimeSeconds()
                     * (1.0 + snapshot.getOrDefault(current.edge.edgeKey(), 0.0));
      double cost  = current.edge.cost();
      double dist  = lengthMap.getOrDefault(current.edge.edgeKey(), 0.0);
      double co2   = current.edge.co2PerKm() * dist;

      segments.add(new RouteSegment(current.edge, time, cost, co2));
      current = current.parent;
    }
    Collections.reverse(segments);
    return new RouteResult(segments);
  }

  private List<TransitEdge> getAllEdges() {
    List<TransitEdge> all = new ArrayList<>();
    for (String id : graph.getNodes().keySet()) {
      all.addAll(graph.getEdges(id));
    }
    return all;
  }

  private static double haversineKm(final double lat1, final double lon1,
                                    final double lat2, final double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
             + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
             * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  // ---------- inner class ----------

  static class AStarNode implements Comparable<AStarNode> {
    final TransitNode node;
    final TransitEdge edge;
    final AStarNode parent;
    final double g;
    final double f;

    AStarNode(final TransitNode node, final TransitEdge edge,
              final AStarNode parent, final double g, final double h) {
      this.node = node;
      this.edge = edge;
      this.parent = parent;
      this.g = g;
      this.f = g + h;
    }

    @Override
    public int compareTo(final AStarNode other) {
      return Double.compare(this.f, other.f);
    }
  }
}