/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TransitIQException.java – Base exception for the application
 */

package com.transitiq.exception;

/**
 * Root of all TransitIQ checked exceptions.
 * Caught at the CLI boundary for unified error display.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class TransitIQException extends Exception {

  public TransitIQException(final String message) {
    super(message);
  }

  public TransitIQException(final String message, final Throwable cause) {
    super(message, cause);
  }
}