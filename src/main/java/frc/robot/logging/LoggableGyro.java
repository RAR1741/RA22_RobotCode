package frc.robot.logging;

import com.kauailabs.navx.frc.AHRS;

public class LoggableGyro extends AHRS implements Loggable {
    public LoggableGyro() {
        super();
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("AHRS/velocityX");
        logger.addAttribute("AHRS/yaw");
        logger.addAttribute("AHRS/accelerationX");
        logger.addAttribute("AHRS/velocityY");
        logger.addAttribute("AHRS/pitch");
        logger.addAttribute("AHRS/accelerationY");
        logger.addAttribute("AHRS/velocityZ");
        logger.addAttribute("AHRS/roll");
        logger.addAttribute("AHRS/accelerationZ");
    }

    @Override
    public void log(Logger logger) {
        logger.log("AHRS/velocityX", this.getVelocityX());
        logger.log("AHRS/yaw", this.getYaw());
        logger.log("AHRS/accelerationX", this.getWorldLinearAccelX());
        logger.log("AHRS/velocityY", this.getVelocityY());
        logger.log("AHRS/pitch", this.getPitch());
        logger.log("AHRS/accelerationY", this.getWorldLinearAccelY());
        logger.log("AHRS/velocityZ", this.getVelocityZ());
        logger.log("AHRS/roll", this.getRoll());
        logger.log("AHRS/accelerationZ", this.getWorldLinearAccelZ());
    }
}
