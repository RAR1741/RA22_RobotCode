package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Shooter implements Loggable {
    TalonSRX shooterMotor; // Motor is a Talon FX on 2022 Robot
    Shooter(int shooterID) {
        this.shooterMotor = new TalonSRX(shooterID);

        this.shooterMotor.setNeutralMode(NeutralMode.Coast);
    }

    public void setSpeed(double speed) {
        shooterMotor.set(ControlMode.Velocity, speed);
    }

    public void setPower(double power) {
        shooterMotor.set(ControlMode.PercentOutput, power);
    }
    
    public double getSpeed() {
        return shooterMotor.getSelectedSensorVelocity();
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
