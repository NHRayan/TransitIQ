/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : GraphLoader.java – CSV parser that builds a CityGraph
 */

package com.transitiq.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.transitiq.exception.DataCorruptionException;

/**
 * Loads nodes and edges from CSV files and produces a {@link CityGraph}.
 * Fatal data errors result in a {@code DataCorruptionException}.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public final class GraphLoader {

  private GraphLoader() { /* utility */ }

  /**
   * @param nodesFile  CSV with columns {@code id,lat,lon,type}
   * @param edgesFile  CSV with columns
   *                   {@code from,to,mode,base_time_sec,max_speed_kph,district,cost,co2_per_km}
   * @return a fully constructed {@link CityGraph}
   * @throws DataCorruptionException if files are missing, contain negative
   *         times/speeds, or have duplicate node IDs
   */
  public static CityGraph loadGraph(final Path nodesFile, final Path edgesFile) {
    Map<String, TransitNode> nodeMap = new HashMap<>();

    // ---- load nodes ----
    try (BufferedReader br = Files.newBufferedReader(nodesFile)) {
      String header = br.readLine(); // skip header
      String line;
      int lineNum = 1;
      while ((line = br.readLine()) != null) {
        lineNum++;
        if (line.isBlank()) continue;
        String[] parts = line.split(",", -1);
        if (parts.length < 4) continue; // skip malformed lines (or throw)

        String id = parts[0].trim().toUpperCase();
        double lat = Double.parseDouble(parts[1].trim());
        double lon = Double.parseDouble(parts[2].trim());
        NodeType type = NodeType.valueOf(parts[3].trim().toUpperCase());

        if (nodeMap.containsKey(id)) {
          throw new DataCorruptionException("Duplicate node ID: " + id);
        }
        nodeMap.put(id, new TransitNode(id, lat, lon, type));
      }
    } catch (IOException e) {
      throw new DataCorruptionException("Failed to read nodes file: " + nodesFile, e);
    }

    // ---- load edges ----
    Map<String, List<TransitEdge>> adjMap = new HashMap<>();
    for (String id : nodeMap.keySet()) {
      adjMap.put(id, new ArrayList<>()); // ensure every node has a list
    }

    try (BufferedReader br = Files.newBufferedReader(edgesFile)) {
      String header = br.readLine();
      String line;
      int lineNum = 1;
      while ((line = br.readLine()) != null) {
        lineNum++;
        if (line.isBlank()) continue;
        String[] parts = line.split(",", -1);
        if (parts.length < 8) continue;

        String from = parts[0].trim().toUpperCase();
        String to   = parts[1].trim().toUpperCase();
        TransportMode mode = TransportMode.valueOf(parts[2].trim().toUpperCase());
        double baseTime = Double.parseDouble(parts[3].trim());
        double maxSpeed = Double.parseDouble(parts[4].trim());
        String district = parts[5].trim().toUpperCase();
        double cost     = Double.parseDouble(parts[6].trim());
        double co2      = Double.parseDouble(parts[7].trim());

        if (baseTime < 0 || maxSpeed < 0) {
          throw new DataCorruptionException(
              "Negative time/speed at line " + lineNum);
        }

        TransitEdge edge = new TransitEdge(from, to, mode, baseTime, maxSpeed, district, cost, co2);
        adjMap.computeIfAbsent(from, k -> new ArrayList<>()).add(edge);
      }
    } catch (IOException e) {
      throw new DataCorruptionException("Failed to read edges file: " + edgesFile, e);
    }

    // replace ArrayLists with unmodifiable lists inside the constructor
    Map<String, List<TransitEdge>> unmodAdj = new HashMap<>();
    for (var entry : adjMap.entrySet()) {
      unmodAdj.put(entry.getKey(), List.copyOf(entry.getValue()));
    }

    return new CityGraph(nodeMap, unmodAdj);
  }
}