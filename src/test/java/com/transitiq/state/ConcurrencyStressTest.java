package com.transitiq.state;

import com.transitiq.graph.*;
import com.transitiq.routing.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ConcurrencyStressTest {

  @Test
  void shouldHandleConcurrentSearchesWhileUpdating() throws Exception {
    // minimal graph: two nodes connected
    TransitNode n1 = new TransitNode("A1", 0, 0, NodeType.JUNCTION);
    TransitNode n2 = new TransitNode("B2", 1, 1, NodeType.BUS_STOP);
    Map<String, TransitNode> nodes = Map.of("A1", n1, "B2", n2);
    TransitEdge edge = new TransitEdge("A1", "B2", TransportMode.BUS, 100, 30, "X", 2.0, 0.08);
    Map<String, List<TransitEdge>> adj = Map.of(
        "A1", List.of(edge),
        "B2", List.of());
    CityGraph graph = new CityGraph(nodes, adj);

    // initial congestion
    Map<String, Double> initCongestion = new HashMap<>();
    initCongestion.put(edge.edgeKey(), 0.0);
    GraphStateManager stateMgr = new GraphStateManager(initCongestion);
    TrafficSimulator sim = new TrafficSimulator(stateMgr, graph, 1);
    sim.start();

    AStarRouter router = new AStarRouter(graph, stateMgr);
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    int tasks = 20;
    List<Future<RouteResult>> futures = new ArrayList<>();

    for (int i = 0; i < tasks; i++) {
      futures.add(executor.submit(() -> {
        return router.findRoute("A1", "B2", new FastestStrategy());
      }));
    }

    for (Future<RouteResult> f : futures) {
      RouteResult r = f.get(2, TimeUnit.SECONDS);
      assertThat(r).isNotNull();
      assertThat(r.getSegments()).hasSize(1);
    }

    executor.shutdown();
    sim.stop();
  }
}