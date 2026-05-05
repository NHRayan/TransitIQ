/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : RoutingStrategy.java – Strategy interface for cost functions
 */

package com.transitiq.routing;

import com.transitiq.graph.TransitEdge;
import com.transitiq.graph.TransitNode;
import java.util.Map;

/**
 * Defines a pluggable cost function for the A* router.
 * Each concrete strategy provides:
 * <ul>
 * <li>an admissible heuristic between two nodes, and</li>
 * <li>the actual edge cost under a given traffic snapshot.</li>
 * </ul>
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public interface RoutingStrategy {

    /**
     * Real cost of traversing this edge under current traffic conditions.
     *
     * @param edge               the edge being evaluated
     * @param congestionSnapshot map of edgeKey → delay factor (0.0 = free flow)
     * @return cost value (time, money, or CO₂ depending on strategy)
     */
    double computeEdgeCost(TransitEdge edge, Map<String, Double> congestionSnapshot);

    /**
     * Admissible heuristic – must never overestimate the remaining cost to goal.
     *
     * @param current current node
     * @param goal    destination node
     * @return estimated remaining cost (≤ actual optimal cost)
     */
    double heuristic(TransitNode current, TransitNode goal);
}