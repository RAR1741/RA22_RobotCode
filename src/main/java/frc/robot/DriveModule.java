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

    DriveModule(String moduleName, int mainID, int subID) {
        this.moduleName = moduleName;
        this.main = new TalonFX(mainID);
        this.sub = new TalonFX(subID);

        this.sub.follow(this.main);
    }

    public void setInverted(boolean isInverted) {
        main.setInverted(isInverted);
    }

    public void set(double input) {
        this.speed = input;
        main.set(TalonFXControlMode.PercentOutput, input);
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute(this.moduleName + "/MotorSpeed");
        
    }

    @Override
    public void log(Logger logger) {
        logger.log(this.moduleName + "/MotorSpeed", speed);
    }
}
