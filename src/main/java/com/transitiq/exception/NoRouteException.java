/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : NoRouteException.java – Thrown when the A* search cannot find a path
 */

package com.transitiq.exception;

/**
 * Indicates that no route exists between the requested origin and destination.
 * The user should try a different strategy or different endpoints.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class NoRouteException extends TransitIQException {

  public NoRouteException(final String from, final String to) {
    super("No route found from " + from + " to " + to);
  }
}