/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TripRecord.java – Immutable record of a completed trip
 */

package com.transitiq.io;

import com.transitiq.routing.RouteResult;
import com.transitiq.routing.RouteSegment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A serialisable snapshot of a completed route search.
 * Written to {@code trip_history.json} by {@link TripLogger}.
 *
 * @param origin      starting node ID
 * @param destination ending node ID
 * @param strategy    FASTEST, CHEAPEST, or ECO
 * @param segments    the ordered list of traversed edges
 * @param totalTimeSec total travel time (seconds)
 * @param totalCost   total monetary fare (MYR)
 * @param totalCo2Kg  total CO₂ emitted (kg)
 * @param timestamp   when the trip was logged
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public record TripRecord(String origin,
                         String destination,
                         String strategy,
                         List<RouteSegment> segments,
                         double totalTimeSec,
                         double totalCost,
                         double totalCo2Kg,
                         LocalDateTime timestamp) {

  private static final DateTimeFormatter fmt =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Factory method from a RouteResult.
   *
   * @param fromId   origin node ID
   * @param toId     destination node ID
   * @param strategy human‑readable strategy name
   * @param result   the computed route
   * @return a new TripRecord stamped with the current time
   */
  public static TripRecord from(final String fromId,
                                final String toId,
                                final String strategy,
                                final RouteResult result) {
    return new TripRecord(fromId, toId, strategy,
                          result.getSegments(),
                          result.getTotalDuration(),
                          result.getTotalCost(),
                          result.getTotalCo2(),
                          LocalDateTime.now());
  }

  /** One‑line JSON representation for easy file appending. */
  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"origin\":\"").append(origin).append("\",");
    sb.append("\"destination\":\"").append(destination).append("\",");
    sb.append("\"strategy\":\"").append(strategy).append("\",");
    sb.append("\"segments\":[");
    for (int i = 0; i < segments.size(); i++) {
      RouteSegment s = segments.get(i);
      sb.append("{");
      sb.append("\"from\":\"").append(s.from()).append("\",");
      sb.append("\"to\":\"").append(s.to()).append("\",");
      sb.append("\"mode\":\"").append(s.mode()).append("\",");
      sb.append("\"timeSec\":").append(s.actualTimeSec()).append(",");
      sb.append("\"cost\":").append(s.actualCost()).append(",");
      sb.append("\"co2Kg\":").append(s.actualCo2Kg());
      sb.append("}");
      if (i < segments.size() - 1) sb.append(",");
    }
    sb.append("],");
    sb.append("\"totalTimeSec\":").append(totalTimeSec).append(",");
    sb.append("\"totalCost\":").append(totalCost).append(",");
    sb.append("\"totalCo2Kg\":").append(totalCo2Kg).append(",");
    sb.append("\"timestamp\":\"").append(timestamp.format(fmt)).append("\"");
    sb.append("}");
    return sb.toString();
  }
}