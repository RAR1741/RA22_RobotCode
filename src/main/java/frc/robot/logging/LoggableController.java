package frc.robot.logging;

import edu.wpi.first.wpilibj.XboxController;

public class LoggableController extends XboxController implements Loggable {
    public String name;

    public LoggableController(String name, int port) {
        super(port);
        this.name = name;
    }

    @Override
    public void setupLogging(Logger logger) {}

    @Override
    public void log(Logger logger) {}

}
