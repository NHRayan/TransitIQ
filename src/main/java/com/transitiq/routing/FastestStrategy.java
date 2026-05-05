/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : FastestStrategy.java – Optimize for shortest travel time
 */

package com.transitiq.routing;

import com.transitiq.graph.TransitEdge;
import com.transitiq.graph.TransitNode;
import java.util.Map;

/**
 * Minimises total travel time.  The heuristic is Haversine distance
 * divided by the global maximum speed (120 km/h), converted to seconds.
 * This is admissible because no edge permits more than 120 km/h.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class FastestStrategy implements RoutingStrategy {

  /** Earth radius in km. */
  private static final double R = 6371.0;
  /** Upper bound of speed (km/h) used in heuristic – always ≥ any real edge. */
  private static final double MAX_SPEED_KPH = 120.0;

  @Override
  public double computeEdgeCost(final TransitEdge edge,
                                final Map<String, Double> congestionSnapshot) {
    double delay = congestionSnapshot.getOrDefault(edge.edgeKey(), 0.0);
    // delay is a multiplier: 0.0 = free flow, 1.0 = +100% time
    return edge.baseTimeSeconds() * (1.0 + delay);
  }

  @Override
  public double heuristic(final TransitNode current, final TransitNode goal) {
    double distKm = haversineKm(current.lat(), current.lon(), goal.lat(), goal.lon());
    double hours = distKm / MAX_SPEED_KPH;
    return hours * 3600.0;  // seconds
  }

  /**
   * Haversine distance between two lat/lon points.
   * @return distance in kilometres
   */
  public static double haversineKm(final double lat1, final double lon1,
                                   final double lat2, final double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
             + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
             * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}