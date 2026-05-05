package com.transitiq;

import com.transitiq.graph.*;
import com.transitiq.routing.*;
import com.transitiq.state.GraphStateManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceTest {

  private static CityGraph graph;
  private static AStarRouter router;

  @BeforeAll
  static void loadGraph() throws Exception {
    Path nodes500 = Path.of("data", "nodes_500.csv");
    Path edges500 = Path.of("data", "edges_500.csv");

    // Generate edges_500.csv if missing (pure Java, no Python)
    if (!Files.exists(edges500)) {
      System.out.println("Generating edges_500.csv (Java fallback) …");
      generateEdges500(nodes500, edges500);
      System.out.println("edges_500.csv created.");
    }

    graph = GraphLoader.loadGraph(nodes500, edges500);
    Map<String, Double> empty = new HashMap<>();
    for (String id : graph.getNodes().keySet()) {
      for (TransitEdge e : graph.getEdges(id)) {
        empty.put(e.edgeKey(), 0.0);
      }
    }
    router = new AStarRouter(graph, new GraphStateManager(empty));
  }

  /** Writes a 2000‑edge file connecting the 500 nodes. */
  private static void generateEdges500(Path nodesFile, Path edgesFile) throws Exception {
    // Load all node IDs and their lat/lon – fix: read as List<String>
    List<String> lines = Files.readAllLines(nodesFile);
    lines.remove(0); // skip header
    List<String> ids = new ArrayList<>();
    Map<String, double[]> coords = new HashMap<>();
    for (String line : lines) {
      String[] p = line.split(",");
      String id = p[0];
      double lat = Double.parseDouble(p[1]);
      double lon = Double.parseDouble(p[2]);
      ids.add(id);
      coords.put(id, new double[]{lat, lon});
    }

    // Ensure deterministic output
    Random rand = new Random(2026);
    int targetEdges = 2000;

    try (BufferedWriter w = Files.newBufferedWriter(edgesFile)) {
      w.write("from,to,mode,base_time_sec,max_speed_kph,district,cost,co2_per_km\n");
      int count = 0;
      while (count < targetEdges) {
        String from = ids.get(rand.nextInt(ids.size()));
        String to = ids.get(rand.nextInt(ids.size()));
        if (from.equals(to)) continue;

        double[] c1 = coords.get(from);
        double[] c2 = coords.get(to);
        double dist = haversineKm(c1[0], c1[1], c2[0], c2[1]);
        if (dist < 0.2) continue; // avoid tiny edges

        String mode = rand.nextBoolean() ? "CAR" : "BUS";
        int speed = mode.equals("CAR") ? 40 + rand.nextInt(40) : 20 + rand.nextInt(20);
        double baseTime = dist / speed * 3600;
        double cost = mode.equals("CAR") ? Math.round(dist * 1.5 * 100.0) / 100.0
                                         : Math.round(dist * 0.8 * 100.0) / 100.0;
        double co2 = mode.equals("CAR") ? 0.20 : 0.08;

        w.write(String.format("%s,%s,%s,%d,%d,DSTRCT%d,%.2f,%.2f%n",
            from, to, mode, (int) baseTime, speed, rand.nextInt(10), cost, co2));
        count++;
      }
    }
  }

  private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371.0;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
               Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
               Math.sin(dLon/2)*Math.sin(dLon/2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  }

  @Test
  void shouldComplete100RandomQueriesUnder150ms() throws Exception {
    List<String> ids = new ArrayList<>(graph.getNodes().keySet());
    Random rand = new Random(42);
    int queries = 100;
    long maxTime = 0;
    long total = 0;

    for (int i = 0; i < queries; i++) {
      String from = ids.get(rand.nextInt(ids.size()));
      String to = ids.get(rand.nextInt(ids.size()));
      long start = System.nanoTime();
      try {
        router.findRoute(from, to, new FastestStrategy());
      } catch (Exception e) { /* ignore no-route */ }
      long duration = (System.nanoTime() - start) / 1_000_000;
      total += duration;
      if (duration > maxTime) maxTime = duration;
    }

    double avg = total / (double) queries;
    System.out.printf("Performance: avg %.2f ms, max %d ms%n", avg, maxTime);
    assertThat(avg).isLessThan(150.0);
  }
}