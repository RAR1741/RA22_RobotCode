package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Shooter implements Loggable {
    TalonFX shooterMotor;
    public Shooter(int shooterID) {
        this.shooterMotor = new TalonFX(shooterID);

        this.shooterMotor.setNeutralMode(NeutralMode.Coast);
    }

    public void setSpeed(double speed) {
        this.shooterMotor.set(ControlMode.Velocity, speed);
    }

    public void setPower(double power) {
        this.shooterMotor.set(ControlMode.PercentOutput, power);
    }
    
    public double getSpeed() {
        return this.shooterMotor.getSelectedSensorVelocity();
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("Shooter/speed");
    }

    @Override
    public void log(Logger logger) {
        logger.log("Shooter/speed", this.getSpeed());
    }
}
