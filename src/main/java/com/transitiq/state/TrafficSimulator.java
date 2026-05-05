/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TrafficSimulator.java – Background traffic updater
 */

package com.transitiq.state;

import com.transitiq.graph.CityGraph;
import com.transitiq.graph.TransitEdge;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically generates randomised congestion values and publishes them
 * to the {@link GraphStateManager} and any registered {@link TrafficObserver}s.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class TrafficSimulator implements Runnable {

  private final GraphStateManager stateManager;
  private final CityGraph graph;
  private final List<TrafficObserver> observers = new CopyOnWriteArrayList<>();
  private ScheduledExecutorService scheduler;
  private final long intervalSeconds;

  /**
   * @param stateManager    the target to update
   * @param graph           used to discover all edge keys
   * @param intervalSeconds how often to publish new delays (typically 5)
   */
  public TrafficSimulator(final GraphStateManager stateManager,
                          final CityGraph graph,
                          final long intervalSeconds) {
    this.stateManager = stateManager;
    this.graph = graph;
    this.intervalSeconds = intervalSeconds;
  }

  /** Register a component that wants to be notified on every update. */
  public void addObserver(final TrafficObserver observer) {
    observers.add(observer);
  }

  /** Start the background thread. */
  public void start() {
    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "TrafficSimulator");
      t.setDaemon(true);
      return t;
    });
    scheduler.scheduleAtFixedRate(this, 0, intervalSeconds, TimeUnit.SECONDS);
  }

  /** Graceful shutdown. */
  public void stop() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdownNow();
    }
  }

  @Override
  public void run() {
    try {
      Map<String, Double> newCongestion = new HashMap<>();
      Random rand = new Random();

      for (String fromId : graph.getNodes().keySet()) {
        for (TransitEdge edge : graph.getEdges(fromId)) {
          // generate a delay between 0.0 (free flow) and 2.0 (200% longer)
          double delay = rand.nextDouble() * 2.0;
          newCongestion.put(edge.edgeKey(), delay);
        }
      }

      stateManager.updateAll(newCongestion);

      // notify all observers (e.g., a future JavaFX dashboard)
      for (TrafficObserver obs : observers) {
        obs.onTrafficUpdate(newCongestion);
      }
    } catch (Exception e) {
      System.err.println("TrafficSimulator pulse failed: " + e.getMessage());
    }
  }
}