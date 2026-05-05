/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : DataCorruptionException.java – Fatal data integrity error (unchecked)
 */

package com.transitiq.exception;

/**
 * Indicates a fatal problem with the input data
 * (e.g., negative travel time, duplicate edges).
 * The application cannot safely continue.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class DataCorruptionException extends RuntimeException {

  public DataCorruptionException(final String message) {
    super(message);
  }

  public DataCorruptionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}