/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : NodeType.java – Enum for graph node categories
 */

package com.transitiq.graph;

/**
 * Represents the three possible types of a city node.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public enum NodeType {
  /** A road intersection (driving/walking junction). */
  JUNCTION,
  /** A bus stop (for bus boarding/alighting). */
  BUS_STOP,
  /** A train station (for train boarding/alighting). */
  TRAIN_STATION
}