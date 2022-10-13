package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Shooter implements Loggable {
    TalonFX shooterMotor;

    public Shooter(TalonFX s) {
        this.shooterMotor = s;

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
    public void logHeaders(Logger logger) {
        logger.addHeader("Shooter/speed");
    }

    @Override
    public void logData(Logger logger) {
        logger.addData("Shooter/speed", this.getSpeed());
    }
}