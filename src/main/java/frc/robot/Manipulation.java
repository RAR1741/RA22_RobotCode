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

    /**
     * Constructor
     * 
     * @param pneumaticsForwardChannel The Solenoid id for the forward channel for the intake
     * @param pneumaticsReverseChannel The Solenoid id for the reverse channel for the intake
     * @param intakeWheelID The CAN id of the spark for the intake 
     * @param indexLoadID The CAN id of the spark for the index loader
     * 
     */
    Manipulation(int pneumaticsForwardChannel, int pneumaticsReverseChannel, int intakeWheelID, int indexLoadID) {
        this.intakeWheel = new CANSparkMax(intakeWheelID, MotorType.kBrushless);
        this.indexLoad = new CANSparkMax(indexLoadID, MotorType.kBrushless);
        this.intakePneumatics = new DoubleSolenoid(PneumaticsModuleType.REVPH, pneumaticsForwardChannel, pneumaticsReverseChannel);

        intakeWheel.setInverted(true);
    }

    /**
     * Spins the intake motor
     * 
     * @param spin True if the motor should spin; false if not
     * 
     */
    public void setIntakeSpin(boolean spin) {
        this.speed = spin ? -0.5 : 0.0;
        intakeWheel.set(speed);
        this.spinning = spin;
    }

    /**
     * Moves the intake system
     * 
     * @param extend True if the pneumatics should extend; false if not
     *
     */
    public void setIntakeExtend(boolean extend) {
        intakePneumatics.set(extend ? Value.kForward : Value.kReverse);
    }
    /**
     * Moves power cells down indexing system
     * 
     * @param load True if it should load; false if not
     * 
     */
    public void setIndexLoad(boolean load) {
        indexLoad.set(load ? 0.5 : 0.0);
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
