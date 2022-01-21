package frc.robot.logging;

import java.util.function.LongBinaryOperator;

import edu.wpi.first.wpilibj.Timer;

public class LoggableTimer extends Timer implements Loggable {
    String name;
    public LoggableTimer() {
        this("Time");
    }
    public LoggableTimer(String name) {
        this.name = name;
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute(this.name);
    }

    @Override
    public void log(Logger logger) {
        logger.log(this.name, this.get());
    }

}
