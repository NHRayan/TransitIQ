/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : RouteResult.java – Full path result from A* search
 */

package com.transitiq.routing;

import java.util.Collections;
import java.util.List;

/**
 * Holds a sequence of segments returned by the A* router,
 * together with summary statistics.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class RouteResult {

  private final List<RouteSegment> segments;
  private final double totalDuration;
  private final double totalCost;
  private final double totalCo2;

  public RouteResult(final List<RouteSegment> segments) {
    this.segments = Collections.unmodifiableList(segments);
    this.totalDuration = segments.stream().mapToDouble(RouteSegment::actualTimeSec).sum();
    this.totalCost     = segments.stream().mapToDouble(RouteSegment::actualCost).sum();
    this.totalCo2      = segments.stream().mapToDouble(RouteSegment::actualCo2Kg).sum();
  }

  public List<RouteSegment> getSegments() {
    return segments;
  }

  public double getTotalDuration() {
    return totalDuration;
  }

  public double getTotalCost() {
    return totalCost;
  }

  public double getTotalCo2() {
    return totalCo2;
  }

  public int segmentCount() {
    return segments.size();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Route: %d segments | Duration %.1f min | Cost RM%.2f | CO2 %.2f kg\n",
            segmentCount(), totalDuration / 60.0, totalCost, totalCo2));
    for (RouteSegment s : segments) {
      sb.append(String.format("  %-5s → %-5s  %-6s  %5.1f min  RM%6.2f  %6.3f kg\n",
              s.from(), s.to(), s.mode(),
              s.actualTimeSec() / 60.0, s.actualCost(), s.actualCo2Kg()));
    }
    return sb.toString();
  }
}