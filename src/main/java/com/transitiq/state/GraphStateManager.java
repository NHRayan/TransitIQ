/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : GraphStateManager.java – Lock‑free traffic snapshot holder
 */

package com.transitiq.state;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the current traffic congestion map in an {@link AtomicReference}.
 * <p>
 * <b>Thread safety:</b> Writers call {@link #updateAll(Map)} which atomically
 * swaps in a new immutable map. Readers call {@link #getSnapshot()} which
 * performs a single volatile read and gets a consistent, never‑changing view.
 * No locks are used anywhere.
 * </p>
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class GraphStateManager {

  private final AtomicReference<Map<String, Double>> congestionRef;

  /**
   * @param initialCongestion the starting delay map (may be empty)
   */
  public GraphStateManager(final Map<String, Double> initialCongestion) {
    this.congestionRef = new AtomicReference<>(Map.copyOf(initialCongestion));
  }

  /**
   * Returns the current snapshot. The returned map is immutable and can be
   * safely read by multiple threads without any further synchronisation.
   *
   * @return current congestion map (never {@code null} after initialisation)
   */
  public Map<String, Double> getSnapshot() {
    return congestionRef.get();
  }

  /**
   * Atomically replaces the congestion map with a copy of {@code newCongestion}.
   * Observers are not notified here; that is done by the simulator.
   *
   * @param newCongestion the fresh delay factors
   */
  public void updateAll(final Map<String, Double> newCongestion) {
    congestionRef.set(Map.copyOf(newCongestion));
  }
}