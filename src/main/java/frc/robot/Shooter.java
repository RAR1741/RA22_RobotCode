package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Shooter implements Loggable {
    TalonSRX indexFeed; // Motor is a Talon FX on 2022 Robot
    CANSparkMax shooterMotor;
    Shooter(int shooterID, int shooterMotorID) { // fly wheel is for shooting with Parsec
        this.indexFeed = new TalonSRX(shooterID);
        this.shooterMotor = new CANSparkMax(shooterMotorID, MotorType.kBrushless);

        this.indexFeed.setNeutralMode(NeutralMode.Coast);
    }

    public void setSpeed(double speed) {
        shooterMotor.set(speed);
    }

    public void setIndexSpeed(double speed) {
        indexFeed.set(ControlMode.Velocity, speed);
    }

    public void setIndexPower(double power) {
        indexFeed.set(ControlMode.PercentOutput, power);
    }
    
    public double getSpeed() {
        return indexFeed.getSelectedSensorVelocity();
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
