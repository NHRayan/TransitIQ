/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TransitNode.java – Immutable node record
 */

package com.transitiq.graph;

/**
 * A node (vertex) in the city graph.
 * Immutable — safe for concurrent reads with no synchronisation.
 *
 * @param id   unique node identifier matching {@code ^[A-Z]{2}[0-9]{2}$}
 * @param lat  WGS84 latitude
 * @param lon  WGS84 longitude
 * @param type junction, bus stop, or train station
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public record TransitNode(String id,
                          double lat,
                          double lon,
                          NodeType type) {

  /** Compact canonical constructor – records handle the assignment. */
  public TransitNode {
    // NHR: any validation can go here if required later
  }
}