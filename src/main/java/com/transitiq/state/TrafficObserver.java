/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TrafficObserver.java – Callback for traffic updates
 */

package com.transitiq.state;

import java.util.Map;

/**
 * Functional interface for components that react to new traffic snapshots.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
@FunctionalInterface
public interface TrafficObserver {

  /**
   * Called when a fresh congestion map is published.
   *
   * @param congestion map of edgeKey → delay factor (0.0 = free flow)
   */
  void onTrafficUpdate(Map<String, Double> congestion);
}