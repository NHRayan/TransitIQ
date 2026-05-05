/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : StaleSnapshotException.java – Thrown when the traffic snapshot is null
 */

package com.transitiq.exception;

/**
 * The traffic snapshot returned by {@code GraphStateManager}
 * was unexpectedly {@code null}. The system should retry once.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class StaleSnapshotException extends TransitIQException {

  public StaleSnapshotException() {
    super("Traffic snapshot is stale; retrying...");
  }
}