/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : CityGraph.java – Immutable container for the city network
 */

package com.transitiq.graph;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds the node lookup and adjacency list for the entire city.
 * After loading, the graph is effectively immutable; the adjacency lists
 * themselves are not modified by route searches.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class CityGraph {

  private final Map<String, TransitNode> nodes;
  private final Map<String, List<TransitEdge>> adjacency;

  /**
   * @param nodes      map from node ID to {@link TransitNode}
   * @param adjacency  map from origin node ID to a <b>non‑null</b> list of outgoing edges
   */
  public CityGraph(final Map<String, TransitNode> nodes,
                   final Map<String, List<TransitEdge>> adjacency) {
    this.nodes = Collections.unmodifiableMap(nodes);
    this.adjacency = Collections.unmodifiableMap(adjacency);
  }

  /** @return an unmodifiable view of all nodes */
  public Map<String, TransitNode> getNodes() {
    return nodes;
  }

  /** Convenience – get a single node by ID */
  public TransitNode getNode(final String id) {
    return nodes.get(id);
  }

  /** @return an unmodifiable view of outgoing edges for a given origin, or an empty list */
  public List<TransitEdge> getEdges(final String fromId) {
    return adjacency.getOrDefault(fromId, Collections.emptyList());
  }

  /** Total node count */
  public int nodeCount() {
    return nodes.size();
  }

  /** Total edge count */
  public int edgeCount() {
    return adjacency.values().stream().mapToInt(List::size).sum();
  }
}