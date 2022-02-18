package frc.robot.logging;

import edu.wpi.first.wpilibj.Timer;

public class LoggableTimer extends Timer implements Loggable {

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("Time", this::get, null);
    }

    @Override
    public void log(Logger logger) {
    }

}
