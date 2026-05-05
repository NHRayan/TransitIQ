package com.transitiq;

/**
 * TransitIQ – Lock‑Free Multi‑Modal Routing Engine.
 * Entry point. Delegates to CliController.
 *
 * @author Nahid Hasan Rayan (NHR) – Group 4 BOLEH
 */
public final class App {

  private App() {
    // utility class
  }

  public static void main(final String[] args) {
    com.transitiq.cli.CliController.main(args);
  }
}