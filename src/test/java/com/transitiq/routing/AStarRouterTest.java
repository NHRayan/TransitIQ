package com.transitiq.routing;

import com.transitiq.graph.*;
import com.transitiq.state.GraphStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class AStarRouterTest {

  private AStarRouter router;
  private CityGraph graph;

  @BeforeEach
  void setUp() {
    // small triangle: J01 -> J02 -> TS01 ; J01 -> TS01 walking
    Map<String, TransitNode> nodes = new HashMap<>();
    nodes.put("J01", new TransitNode("J01", 3.14, 101.68, NodeType.JUNCTION));
    nodes.put("J02", new TransitNode("J02", 3.15, 101.69, NodeType.JUNCTION));
    nodes.put("TS01", new TransitNode("TS01", 3.147, 101.70, NodeType.TRAIN_STATION));

    List<TransitEdge> j01Out = List.of(
        new TransitEdge("J01","J02",TransportMode.CAR, 300, 40, "A", 3.0, 0.2),
        new TransitEdge("J01","TS01",TransportMode.WALK, 900, 5, "A", 0, 0)
    );
    List<TransitEdge> j02Out = List.of(
        new TransitEdge("J02","TS01",TransportMode.WALK, 300, 5, "A", 0, 0)
    );
    List<TransitEdge> ts01Out = List.of();

    Map<String, List<TransitEdge>> adj = new HashMap<>();
    adj.put("J01", j01Out);
    adj.put("J02", j02Out);
    adj.put("TS01", ts01Out);

    graph = new CityGraph(nodes, adj);
    Map<String,Double> emptyCongestion = new HashMap<>();
    // add all edge keys with 0 delay
    for (String fromId : nodes.keySet()) {
      for (TransitEdge e : adj.getOrDefault(fromId, List.of())) {
        emptyCongestion.put(e.edgeKey(), 0.0);
      }
    }
    GraphStateManager mgr = new GraphStateManager(emptyCongestion);
    router = new AStarRouter(graph, mgr);
  }

  @Test
  void shouldFindFastestRoute() throws Exception {
    RouteResult result = router.findRoute("J01", "TS01", new FastestStrategy());
    // shortest time is J01->J02->TS01: 300+300 = 600 sec, vs direct walk 900 sec
    assertThat(result.getTotalDuration()).isLessThan(700);
    assertThat(result.getSegments()).hasSize(2);
    assertThat(result.getSegments().get(0).mode()).isEqualTo(TransportMode.CAR);
    assertThat(result.getSegments().get(1).mode()).isEqualTo(TransportMode.WALK);
  }

  @Test
  void shouldThrowWhenNoRoute() {
    // unreachable node
    assertThatThrownBy(() -> router.findRoute("J01", "FAKE", new FastestStrategy()))
        .isInstanceOf(com.transitiq.exception.InvalidNodeException.class);
  }
}