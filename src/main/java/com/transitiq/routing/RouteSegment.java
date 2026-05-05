/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : RouteSegment.java – One edge in a computed route
 */

package com.transitiq.routing;

import com.transitiq.graph.TransitEdge;
import com.transitiq.graph.TransportMode;

/**
 * Immutable record capturing the actual cost of traversing a single edge.
 *
 * @param edge         the underlying graph edge
 * @param actualTimeSec time spent on this segment (including congestion)
 * @param actualCost   monetary cost (MYR)
 * @param actualCo2Kg  CO₂ emitted (kg)
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public record RouteSegment(TransitEdge edge,
                           double actualTimeSec,
                           double actualCost,
                           double actualCo2Kg) {

  /** Shortcut to the transport mode of this segment. */
  public TransportMode mode() {
    return edge.mode();
  }

  /** Shortcut to the origin node ID. */
  public String from() {
    return edge.from();
  }

  /** Shortcut to the destination node ID. */
  public String to() {
    return edge.to();
  }
}