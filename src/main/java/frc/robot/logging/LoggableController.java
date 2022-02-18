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
        logger.addAttribute(name + "/LeftX");
        logger.addAttribute(name + "/LeftY");
        logger.addAttribute(name + "/RightX");
        logger.addAttribute(name + "/RightY");
    }

    @Override
    /**
     * Logs the controller's values.
     */
    public void log(Logger logger) {
        logger.log(name + "/LeftX", this.getLeftX());
        logger.log(name + "/LeftY", this.getLeftY());
        logger.log(name + "/RightX", this.getRightX());
        logger.log(name + "/RightY", this.getRightY());
    }

}
