package com.turt2live.antishare.engine;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents the developer engine. This handles all debugging code
 * as well as integration with external services to ensure the operation
 * of this API Engine.
 *
 * @author turt2live
 */
public class DevEngine {

    private static final String FILE_EXCEPTIONS = "exceptions.log";
    private static final String FILE_LOGS = "log.log";

    private static boolean ENABLED = false;
    private static File LOG_FOLDER = new File("logs");
    private static DevEngine INSTANCE = null;

    // TODO: Make use of the DevTools API

    private DevEngine() {
    }

    private void write(String line, String filename, boolean writeErrors) {
        if (line == null || filename == null) throw new IllegalArgumentException("wtf developer");
        checkFolders();

        SimpleDateFormat format = new SimpleDateFormat();
        Date date = new Date(System.currentTimeMillis());
        format.setTimeZone(TimeZone.getDefault());

        String timestamp = format.format(date);
        Thread currentThread = Thread.currentThread();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(LOG_FOLDER, filename), true));
            writer.write("[" + currentThread.getName() + "/" + currentThread.getId() + "][" + timestamp + "] " + line);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            if (writeErrors) printError(e);
            else {
                System.err.println("Encountered an error writing a file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void printError(Throwable thrown) {
        StringBuilder data = new StringBuilder();

        data.append("Error:: ").append(thrown.getClass().getName()).append(" : ").append(thrown.getMessage()).append('\n');
        writeStacktrace(data, thrown.getStackTrace());

        writeCause(thrown, data);
        data.append("-----------------------\n");

        write(data.toString(), FILE_EXCEPTIONS, false); // Ensure we don't end up in a stack overflow
    }

    private void writeCause(Throwable throwable, StringBuilder builder) {
        if (throwable == null) return;

        Throwable cause = throwable.getCause();
        if (cause != null) {
            builder.append("Caused By: \n");
            writeStacktrace(builder, cause.getStackTrace());
            writeCause(cause, builder);
        }
    }

    private void writeStacktrace(StringBuilder builder, StackTraceElement[] elements) {
        for (StackTraceElement element : elements) {
            builder.append('\t').append(element.toString()).append('\n');
        }
    }

    private void checkFolders() {
        if (!LOG_FOLDER.exists())
            LOG_FOLDER.mkdirs();
    }

    private static DevEngine getInstance() {
        if (INSTANCE == null) INSTANCE = new DevEngine();
        return INSTANCE;
    }

    /**
     * Writes an error to the log file. Does nothing if {@link #isEnabled()} return false
     *
     * @param throwable the error to write, cannot be null
     */
    public static void writeError(Throwable throwable) {
        if (!isEnabled()) return;
        if (throwable == null) throw new IllegalArgumentException("throwable cannot be null");
        getInstance().printError(throwable);
    }

    /**
     * Writes to the log file. Does nothing if {@link #isEnabled()} return false
     *
     * @param line      the line to write, cannot be null
     * @param moreLines extra lines to include
     */
    public static void log(String line, String... moreLines) {
        if (!isEnabled()) return;
        if (line == null) throw new IllegalArgumentException("line cannot be null");

        getInstance().write(line, FILE_LOGS, true);
        if (moreLines != null) {
            for (String extra : moreLines) {
                getInstance().write(extra, FILE_LOGS, true);
            }
        }
    }

    /**
     * Sets this developer engine enabled or disabled
     *
     * @param enabled true for enabled, false otherwise
     */
    public static void setEnabled(boolean enabled) {
        ENABLED = enabled;
    }

    /**
     * Determines if this developer engine is enabled
     *
     * @return true for enabled, false otherwise
     */
    public static boolean isEnabled() {
        return ENABLED;
    }

    /**
     * Sets the log folder for this DevEngine
     *
     * @param directory the new directory, cannot be null
     */
    public static void setLogDirectory(File directory) {
        if (directory == null) throw new IllegalArgumentException();

        LOG_FOLDER = directory;
    }

    /**
     * Gets the current log directory
     *
     * @return the current log directory
     */
    public static File getLogDirectory() {
        return LOG_FOLDER;
    }

}