/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TransitEdge.java – Immutable edge record
 */

package com.transitiq.graph;

/**
 * A directed edge in the multi‑modal city graph.
 * Immutable record — freely shareable between threads.
 *
 * @param from           origin node ID
 * @param to             destination node ID
 * @param mode           mode of transport (CAR, BUS, TRAIN, WALK)
 * @param baseTimeSeconds   free‑flow travel time in seconds
 * @param maxSpeedKph    maximum speed on this edge (km/h)
 * @param district       district name used for congestion reports
 * @param cost           monetary fare (MYR)
 * @param co2PerKm       CO₂ emissions per km
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public record TransitEdge(String from,
                          String to,
                          TransportMode mode,
                          double baseTimeSeconds,
                          double maxSpeedKph,
                          String district,
                          double cost,
                          double co2PerKm) {

  public TransitEdge {
    // NHR: any sanity checks can go here later
  }

  /**
   * Convenience key used for traffic‑snapshot lookups.
   * @return a String of the form {@code "from->to"}
   */
  public String edgeKey() {
    return from + "->" + to;
  }
}