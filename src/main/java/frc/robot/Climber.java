package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.logging.Loggable;
import frc.robot.logging.LoggableTimer;
import frc.robot.logging.Logger;

public class Climber implements Loggable {
    enum ClimbingStates {
        RESTING, PRE_STAGE, TOUCH_A, TRANS_AB, TOUCH_B, TRANS_BC, TOUCH_C;
    }

    TalonFX climbingMotor;
    TalonFX secondaryClimbingMotor;

    Solenoid climberSolenoidA;
    Solenoid climberSolenoidB1;
    Solenoid climberSolenoidB2;
    Solenoid climberSolenoidC;

    ClimberSensors touchA;
    ClimberSensors touchB;
    ClimberSensors touchC;

    ClimbingStates currentStage = ClimbingStates.RESTING;
    LoggableTimer timer;

    public Climber(int climbingMotorID, int secondaryClimbingMotorID, Solenoid climberSolenoidA,
            Solenoid climberSolenoidB1, Solenoid climberSolenoidB2, Solenoid climberSolenoidC) {
        // ClimberSensors touchA, ClimberSensors touchB, ClimberSensors touchC) {

        // TODO: figure out if the motors are inverted correctly
        this.climbingMotor = new TalonFX(climbingMotorID);
        this.secondaryClimbingMotor = new TalonFX(secondaryClimbingMotorID);

        this.climberSolenoidA = climberSolenoidA;
        this.climberSolenoidB1 = climberSolenoidB1;
        this.climberSolenoidB2 = climberSolenoidB2;
        this.climberSolenoidC = climberSolenoidC;

        // this.touchA = touchA;
        // this.touchB = touchB;
        // this.touchC = touchC;

        this.climbingMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0,
                30);
        this.climbingMotor.config_kP(0, 0.1);
        this.climbingMotor.config_kI(0, 0.001);
        this.climbingMotor.config_kD(0, 5);

        secondaryClimbingMotor.setInverted(InvertType.InvertMotorOutput);
        secondaryClimbingMotor.follow(climbingMotor);
        this.timer = new LoggableTimer("Climbing/Time");
    }

    public void setClimbingState(ClimbingStates climbingState) {
        this.currentStage = climbingState;

        switch (climbingState) {
            case PRE_STAGE:
                this.timer.start();
                break;
            case TOUCH_A:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                break;
            case TRANS_AB:
                this.timer.reset();
                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                break;
            case TOUCH_B:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                break;
            case TRANS_BC:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(false);
                break;
            case TOUCH_C:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(true);
                climberSolenoidC.set(false);
                break;
            case RESTING:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(false);
                break;
            default:
                break;
        }
    }

    /**
     * Updates the current state of the climber.
     *
     * @return true when current state is RESTING
     */
    public void checkClimbingState() {
        // Check whether or not the climber is done climbing during the current stage.
        switch (currentStage) {
            case PRE_STAGE:
                // Check if A is touching yet.
                if (touchA.get()) {
                    this.setClimbingState(ClimbingStates.TOUCH_A);
                }
                break;
            case TOUCH_A:
                // Check if B is touching yet.
                if (touchB.get()) {
                    this.setClimbingState(ClimbingStates.TRANS_AB);
                }
                break;
            case TRANS_AB:
                if (timer.hasElapsed(1)) {
                    this.setClimbingState(ClimbingStates.TOUCH_B);
                }
                break;
            case TOUCH_B:
                // Check if C is touching yet.
                if (touchC.get()) {
                    this.setClimbingState(ClimbingStates.TRANS_BC);
                }
                break;
            case TRANS_BC:
                if (timer.hasElapsed(1)) {
                    this.setClimbingState(ClimbingStates.TOUCH_C);
                }
                break;
            case TOUCH_C:
                // Success! \o/
                break;
            default:
                break;
        }
    }

    public void setPrestage(boolean stage) {
        if (stage) {
            this.setClimbingState(ClimbingStates.PRE_STAGE);
        }
    }

    public void setPower(double power) {
        climbingMotor.set(ControlMode.PercentOutput, power * 0.3);
    }

    public void setSpeed(double speed) {
        climbingMotor.set(ControlMode.Velocity, speed * 6000);
    }

    public double getSpeed() {
        return climbingMotor.getSelectedSensorVelocity();
    }

    @Override
    public void setupLogging(Logger logger) {
        this.timer.setupLogging(logger);
        logger.addAttribute("LeftClimberCurrent");
        logger.addAttribute("RightClimberCurrent");
        logger.addAttribute("ClimberSpeed");
    }

    @Override
    public void log(Logger logger) {
        this.timer.log(logger);
        logger.log("LeftClimberCurrent", getLeftCurrent());
        logger.log("RightClimberMotorCurrent", getRightCurrent());
        logger.log("ClimberSpeed", getSpeed());
    }

    public boolean getClimberSolenoidAState() {
        return climberSolenoidA.get();
    }

    public void setClimberSolenoidAState(Boolean state) {
        this.climberSolenoidA.set(state);
    }

    public boolean getClimberSolenoidB1State() {
        return climberSolenoidB1.get();
    }

    public void setClimberSolenoidB1State(Boolean state) {
        this.climberSolenoidB1.set(state);
    }

    public boolean getClimberSolenoidB2State() {
        return climberSolenoidB2.get();
    }

    public void setClimberSolenoidB2State(Boolean state) {
        this.climberSolenoidB2.set(state);
    }

    public boolean getClimberSolenoidCState() {
        return climberSolenoidC.get();
    }

    public void setClimberSolenoidCState(Boolean state) {
        this.climberSolenoidC.set(state);
    }

    public double getLeftCurrent() {
        return climbingMotor.getStatorCurrent();
    }

    public double getRightCurrent() {
        return secondaryClimbingMotor.getStatorCurrent();
    }
}
