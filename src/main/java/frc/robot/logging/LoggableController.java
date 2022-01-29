package frc.robot.logging;

import edu.wpi.first.wpilibj.XboxController;

public class LoggableController extends XboxController implements Loggable {
    public String name;

    public LoggableController(String name, int port) {
        super(port);
        this.name = name;
    }

    @Override
    /** 
     * Sets up logging for the controller.
     */
    public void setupLogging(Logger logger) {
        // No logging is currently required, so no setup is needed yet.
    }

    @Override
    /**
     * Logs the controller's values.
     */
    public void log(Logger logger) {
        // No logging is currently required.
    }

}
