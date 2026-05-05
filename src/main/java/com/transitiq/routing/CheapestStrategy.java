/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : CheapestStrategy.java – Optimize for lowest monetary cost
 */

package com.transitiq.routing;

import com.transitiq.graph.TransitEdge;
import com.transitiq.graph.TransitNode;
import java.util.Map;

/**
 * Minimises monetary fare. The heuristic is Haversine distance × the
 * smallest cost‑per‑km found anywhere in the graph (provided at construction).
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class CheapestStrategy implements RoutingStrategy {

    private static final double R = 6371.0;
    /** Minimum fare per km (RM) across all edges – must be ≤ any real edge. */
    private final double minCostPerKm;

    /**
     * @param minCostPerKm the smallest value of {@code edge.cost() / distance}
     *                     seen in the loaded graph. Use 0 if walking is free.
     */
    public CheapestStrategy(final double minCostPerKm) {
        this.minCostPerKm = minCostPerKm;
    }

    @Override
    public double computeEdgeCost(final TransitEdge edge,
            final Map<String, Double> congestionSnapshot) {
        // cost is independent of congestion (assumption: fares don't change)
        return edge.cost();
    }

    @Override
    public double heuristic(final TransitNode current, final TransitNode goal) {
        double distKm = FastestStrategy.haversineKm(current.lat(), current.lon(),
                goal.lat(), goal.lon());
        return distKm * minCostPerKm;
    }
}