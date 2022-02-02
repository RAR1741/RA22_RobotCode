package frc.robot.logging;

/** A class for a loggable subsystem. */
public interface Loggable {
    /**
     * Sets up all the keys, getters, and setters in the given Logger object.
     * 
     * Sets up all the keys in the given Logger object.
     * @param logger Logger class to setup keys in
     */
    public abstract void setupLogging(Logger logger);

    /**
     * Logs data in the given Logger object.
     * @param logger Logger class to log data to
     * @deprecated See {@link Logger#addAttribute(String, Supplier, Consumer)}.
     */
    @Deprecated(forRemoval = true)
    public void log(Logger logger);
}
