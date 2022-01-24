package frc.robot.logging;

/** A class for a loggable subsystem. */
public interface Loggable {
    /**
     * Sets up all the keys, getters, and setters in the given Logger object.
     * 
     * @param logger Logger class to setup keys in
     */
    public abstract void setupLogging(Logger logger);

    /**
     * Logs data in the given Logger object.
     * 
     * @deprecated See {@link Logger#addAttribute(String, Supplier<String>, Consumer<String>)}.
     * @param logger Logger class to log data to
     */
    @Deprecated(forRemoval = true)
    public void log(Logger logger);
}
