/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TransportMode.java – Enum for edge transport modes
 */

package com.transitiq.graph;

/**
 * Possible modes of transport represented by graph edges.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public enum TransportMode {
  /** Driving a private car. */
  CAR,
  /** Public bus service. */
  BUS,
  /** Rapid transit (LRT/MRT). */
  TRAIN,
  /** Walking. */
  WALK
}