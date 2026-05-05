/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : EcoStrategy.java – Optimize for lowest CO₂ emissions
 */

package com.transitiq.routing;

import com.transitiq.graph.TransitEdge;
import com.transitiq.graph.TransitNode;
import java.util.Map;

/**
 * Minimises CO₂ emissions. The heuristic uses the smallest CO₂‑per‑km
 * value across the whole graph (including 0 for walking).
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class EcoStrategy implements RoutingStrategy {

    private static final double R = 6371.0;
    private final double minCo2PerKm;

    /**
     * @param minCo2PerKm lowest CO₂ (kg/km) observed in the graph.
     */
    public EcoStrategy(final double minCo2PerKm) {
        this.minCo2PerKm = minCo2PerKm;
    }

    @Override
    public double computeEdgeCost(final TransitEdge edge,
            final Map<String, Double> congestionSnapshot) {
        // Estimate distance from base travel time and max speed
        double hours = edge.baseTimeSeconds() / 3600.0;
        double distKm = hours * edge.maxSpeedKph();
        return edge.co2PerKm() * distKm;
    }

    @Override
    public double heuristic(final TransitNode current, final TransitNode goal) {
        double distKm = FastestStrategy.haversineKm(current.lat(), current.lon(),
                goal.lat(), goal.lon());
        return distKm * minCo2PerKm;
    }
}