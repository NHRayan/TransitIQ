/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : InvalidNodeException.java – Thrown when a node ID is not in the graph
 */

package com.transitiq.exception;

/**
 * User supplied a node ID that does not exist in the loaded graph.
 * The CLI prompts the user to re‑enter.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class InvalidNodeException extends TransitIQException {

  public InvalidNodeException(final String id) {
    super("Node not found: " + id);
  }
}