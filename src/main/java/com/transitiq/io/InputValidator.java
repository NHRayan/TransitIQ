/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : InputValidator.java – Compiled regex validators
 */

package com.transitiq.validation;

import java.util.regex.Pattern;

/**
 * Pre‑compiled regular expressions for validating user input.
 * All patterns are static final constants — compiled once, reused forever.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public final class InputValidator {

  private InputValidator() { /* utility */ }

  /** Station code: two uppercase letters followed by exactly two digits. */
  private static final Pattern STATION_PAT =
      Pattern.compile("^[A-Z]{2}[0-9]{2}$");

  /** GPS coordinate: optional sign, 1‑3 integer digits, dot, 4‑6 decimal places. */
  private static final Pattern COORD_PAT =
      Pattern.compile("^-?[0-9]{1,3}\\.[0-9]{4,6}$");

  /** Malaysian‑style licence plate: 1‑3 letters, 1‑4 digits, 0‑2 trailing letters. */
  private static final Pattern PLATE_PAT =
      Pattern.compile("^[A-Z]{1,3}[0-9]{1,4}[A-Z]{0,2}$");

  /** 24‑hour HH:MM format (e.g., 09:30, 23:59). */
  private static final Pattern TIME_PAT =
      Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");

  /** Strategy names accepted by the router. */
  private static final Pattern STRATEGY_PAT =
      Pattern.compile("^(FASTEST|CHEAPEST|ECO)$", Pattern.CASE_INSENSITIVE);

  /** @return true if the string matches the node‑ID pattern */
  public static boolean isValidStation(final String input) {
    return input != null && STATION_PAT.matcher(input).matches();
  }

  /** @return true if the string is a valid GPS coordinate */
  public static boolean isValidCoord(final String input) {
    return input != null && COORD_PAT.matcher(input).matches();
  }

  /** @return true if the string matches the Malaysian plate format */
  public static boolean isValidPlate(final String input) {
    return input != null && PLATE_PAT.matcher(input).matches();
  }

  /** @return true if the string is a valid 24‑hour time */
  public static boolean isValidTime(final String input) {
    return input != null && TIME_PAT.matcher(input).matches();
  }

  /** @return true if the strategy name is one of FASTEST, CHEAPEST, ECO */
  public static boolean isValidStrategy(final String input) {
    return input != null && STRATEGY_PAT.matcher(input).matches();
  }
}