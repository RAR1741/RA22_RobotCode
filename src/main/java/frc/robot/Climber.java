package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.logging.Loggable;
import frc.robot.logging.LoggableTimer;
import frc.robot.logging.Logger;

enum ClimbingStates {
    RESTING, PRE_STAGE, TOUCH_A, TRANS_AB, TOUCH_B, TRANS_BC, TOUCH_C;
}

public class Climber implements Loggable {
    TalonFX climbingMotor, secondaryClimbingMotor;
    Solenoid climberSolenoidA, climberSolenoidB1, climberSolenoidB2, climberSolenoidC;
    ClimberSensors touchA, touchB, touchC;
    ClimbingStates currentStage = ClimbingStates.RESTING;
    LoggableTimer timer;
    public Climber(int climbingMotorID, int secondaryClimbingMotorID,
                    int climberSolenoidAID, int climberSolenoidB1ID, int climberSolenoidB2ID, int climberSolenoidCID,
                    ClimberSensors touchA, ClimberSensors touchB, ClimberSensors touchC) {
        this.climbingMotor = new TalonFX(climbingMotorID);
        this.secondaryClimbingMotor = new TalonFX(secondaryClimbingMotorID);

        this.climberSolenoidA = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidAID);
        this.climberSolenoidB1 = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidB1ID);
        this.climberSolenoidB2 = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidB2ID);
        this.climberSolenoidC = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidCID);

        this.touchA = touchA;
        this.touchB = touchB;
        this.touchC = touchC;

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
     * @return true when current state is RESTING
     */
    public void checkClimbingState() {
        // Check whether or not the climber is done climbing during the current stage.
        switch (currentStage) {
            case PRE_STAGE:
                // Check if A is touching yet.
                if (touchA.get()){
                    this.setClimbingState(ClimbingStates.TOUCH_A);
                }
            case TOUCH_A:
                // Check if B is touching yet.
                if (touchB.get()){
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
                if (touchC.get()){
                    this.setClimbingState(ClimbingStates.TRANS_BC);
                }
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
        climbingMotor.set(ControlMode.PercentOutput, power);
    }

    @Override
    public void setupLogging(Logger logger) {
        this.timer.setupLogging(logger);

    }

    @Override
    public void log(Logger logger) {
        this.timer.log(logger);

    }
}
