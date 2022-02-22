package frc.robot.logging;

import com.kauailabs.navx.frc.AHRS;

public class LoggableGyro extends AHRS implements Loggable {
    public LoggableGyro() {
        super();
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("AHRS/velocityY");
        logger.addAttribute("AHRS/pitch");
        logger.addAttribute("AHRS/accelerationY");
    }

    @Override
    public void log(Logger logger) {
        logger.log("AHRS/velocityY", this.getVelocityY());
        logger.log("AHRS/pitch", this.getPitch());
        logger.log("AHRS/accelerationY", this.getWorldLinearAccelY());
    }
}
