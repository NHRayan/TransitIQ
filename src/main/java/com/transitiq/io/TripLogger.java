/*
 * TransitIQ – Smart City Traffic & Public Transit Router
 * Author : NAHID HASAN RAYAN (NHR)
 * Group  : 4 – BOLEH
 * Course : SCSE1224 Advanced Programming
 * File   : TripLogger.java – Asynchronous JSON trip persistence
 */

package com.transitiq.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

/**
 * Writes trip records to a JSON‑lines file off the main thread.
 * Failures are logged to stderr and never propagate to the caller.
 *
 * @author Nahid Hasan Rayan (NHR)
 */
public class TripLogger {

  private final Path logFile;

  /**
   * @param logFile path to the JSON‑lines file (e.g., {@code trip_history.json})
   */
  public TripLogger(final Path logFile) {
    this.logFile = logFile;
  }

  /**
   * Persist a trip record asynchronously.
   * The CLI remains responsive immediately after calling this method.
   *
   * @param record the completed trip to write
   */
  public void logAsync(final TripRecord record) {
    CompletableFuture.runAsync(() -> {
      try {
        String line = record.toJson() + System.lineSeparator();
        Files.writeString(logFile, line,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        System.err.println("Failed to log trip: " + e.getMessage());
      }
    });
  }

  /** Returns the path being written to. */
  public Path getLogFile() {
    return logFile;
  }
}