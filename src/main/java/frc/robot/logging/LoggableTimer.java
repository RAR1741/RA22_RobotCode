package frc.robot.logging;

import edu.wpi.first.wpilibj.Timer;

public class LoggableTimer extends Timer implements Loggable {

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("Time");
    }

    @Override
    public void log(Logger logger) {
        logger.log("Time", this.get());
    }

}
