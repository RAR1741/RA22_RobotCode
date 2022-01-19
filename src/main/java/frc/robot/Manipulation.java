package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Manipulation implements Loggable {

    private CANSparkMax intakeWheel;
    private DoubleSolenoid intakePneumatics;
    private CANSparkMax indexLoad;

    private double speed;
    private boolean spinning;

    Manipulation(int pneumaticsForwardChannel, int pneumaticsReverseChannel, int intakeWheelID, int indexLoadID) {
        this.intakeWheel = new CANSparkMax(intakeWheelID, MotorType.kBrushless);
        this.indexLoad = new CANSparkMax(indexLoadID, MotorType.kBrushless);
        this.intakePneumatics = new DoubleSolenoid(PneumaticsModuleType.REVPH, pneumaticsForwardChannel, pneumaticsReverseChannel);

        intakeWheel.setInverted(true);
    }

    public void setIntakeSpin(boolean spin) {
        this.speed = spin ? -0.5 : 0.0;
        intakeWheel.set(speed);
        this.spinning = spin;
    }

    public void setIntakeExtend(boolean extend) {
        intakePneumatics.set(extend ? Value.kForward : Value.kReverse);
    }

    public void setIndexLoad(boolean load) {
        indexLoad.set(load ? 0.5 : 0.0);
    }

    public void shoot(boolean fire) {
        indexLoad.set(fire ? 0.75 : 0.0);
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("Manipulation/IntakeWheelSpeed");
        logger.addAttribute("Manipulation/IntakeWheelEnabled");       
    }

    @Override
    public void log(Logger logger) {
        logger.log("Manipulation/IntakeWheelSpeed", speed);
        logger.log("Manipulation/IntakeWheelEnabled", spinning);
    }
    
}
