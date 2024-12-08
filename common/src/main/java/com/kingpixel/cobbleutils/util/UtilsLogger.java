package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.util.concurrent.CompletableFuture;

/**
 * Class for logging.
 */
public class UtilsLogger {
  private Logger logger; // Log for the console.

  public void warn(String s) {
    logger.warn(s);
  }

  public void info(DayOfWeek dayOfWeek) {
    logger.info(dayOfWeek);
  }

  // Enums used for the log file.
  private enum Level {
    INFO,
    ERROR,
    WARN,
    FATAL
  }

  // Constructor that creates the logger.
  public UtilsLogger() {
    logger = LogManager.getLogger(CobbleUtils.MOD_NAME);
  }

  public UtilsLogger(String name) {
    logger = LogManager.getLogger(name);
  }

  /**
   * Info log method.
   *
   * @param message The message to log.
   */
  public void info(String message) {
    logger.info(message);
//		write(Level.INFO, message);
  }

  /**
   * Error log method.
   *
   * @param message The message to log.
   */
  public void error(String message) {
    logger.error(message);
//		write(Level.ERROR, message);
  }

  /**
   * Fatal log method.
   *
   * @param message The message to log.
   */
  public void fatal(String message) {
    logger.fatal(message);
//		write(Level.FATAL, message);
  }

  /**
   * Write method to save the logs to file.
   *
   * @param level   The level that the log is (INFO, ERROR or FATAL).
   * @param message The message to log.
   */
  private void write(Level level, String message) {
    // TODO Can't append to file.

    String output = "[" + level + "]: " + message;

    CompletableFuture<Boolean> future = Utils.writeFileAsync(CobbleUtils.PATH, "logs.txt", output);

    System.out.println(": " + future.join());
  }
}
