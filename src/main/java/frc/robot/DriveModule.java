package frc.robot;

import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class DriveModule implements Loggable {
    
    private TalonFX main;
    private TalonFX sub;
    private String moduleName;

    private double speed;

    /**
     * Constructor
     * 
     * @param moduleName Name of the attribute to log speed
     * @param mainID CAN id of the main TalonFX
     * @param subID CAN id of the sub TalonFX
     * 
     */
    DriveModule(String moduleName, int mainID, int subID) {
        this.moduleName = moduleName;
        this.main = new TalonFX(mainID);
        this.sub = new TalonFX(subID);

        this.sub.follow(this.main);
    }

    /**
     * Inverts the main TalonFX
     * 
     * @param isInverted True if the TalonFX should be inverted; false if not
     * 
     */
    public void setInverted(boolean isInverted) {
        main.setInverted(isInverted);
    }

    /**
     * Sets the speed of the main TalonFX
     * 
     * @param input The speed to set the TalonFX to
     *
     */
    public void set(double input) {
        this.speed = input;
        main.set(TalonFXControlMode.PercentOutput, input);
    }

    /**
     * Gets the average current drawn
     * 
     * @return The average current drawn by the motors
     */
    public double getCurrent() {
        return (main.getStatorCurrent()+sub.getStatorCurrent())/2;
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute(this.moduleName + "/MotorSpeed");
        logger.addAttribute(this.moduleName + "/MotorCurrent");
    }

    @Override
    public void log(Logger logger) {
        logger.log(this.moduleName + "/MotorSpeed", speed);
        logger.log(this.moduleName + "/MotorCurrent", getCurrent());
    }
}
