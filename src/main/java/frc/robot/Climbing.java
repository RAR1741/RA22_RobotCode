package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

enum ClimbingStates {
    RESTING, PRE_STAGE, FIRST_STAGE, SECOND_STAGE, THIRD_STAGE;
}

public class Climbing implements Loggable {
    TalonFX climbingMotor, secondaryClimbingMotor;
    Solenoid climberSolenoidA, climberSolenoidB1, climberSolenoidB2, climberSolenoidC;
    ClimbingStates currentStage = ClimbingStates.RESTING;
    public Climbing(int climbingMotorID, int secondaryClimbingMotorID, 
                    int climberSolenoidAID, int climberSolenoidB1ID, int climberSolenoidB2ID, int climberSolenoidCID,
                    int collisionSensorA, int collisionSensorB) {
        this.climbingMotor = new TalonFX(climbingMotorID);
        this.secondaryClimbingMotor = new TalonFX(secondaryClimbingMotorID);

        this.climberSolenoidA = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidAID);
        this.climberSolenoidB1 = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidB1ID);
        this.climberSolenoidB2 = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidB2ID);
        this.climberSolenoidC = new Solenoid(PneumaticsModuleType.REVPH, climberSolenoidCID);

        secondaryClimbingMotor.setInverted(InvertType.InvertMotorOutput);
        secondaryClimbingMotor.follow(climbingMotor);
    }

    public void setClimbingState(ClimbingStates climbingState) {
        this.currentStage = climbingState;
        switch (climbingState) {
            case FIRST_STAGE:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                break;
            case SECOND_STAGE:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                break;
            case THIRD_STAGE:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(true);
                climberSolenoidC.set(false);
                break;
            default:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidC.set(false);
                break;
        }
    }
    /**
     * Updates the current state of the climber.
     * @return true when current state is RESTING
     */
    public boolean checkClimbingState() {
        // Check whether or not the climber is done climbing during the current stage.
        return this.currentStage == ClimbingStates.RESTING;
    }

    @Override
    public void setupLogging(Logger logger) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub
        
    }
}

//Upon lineup of Bar A shadow tape: 
/* 
- Press button to run climbing function (e.g. Press 'A' button) && When buttons triggered on climbing mechanism 
   [1] - Measure encoder count/total encoder counts = total rotations 
   [1] - Rotate to certain point to hit angle for gripping first bar
    - Activate first solenoid a very small time after the motor is at the correct rotation
    - Repeat [1] with different measurements
    - Activate second (double) solenoid a very small time after the motor is at the correct rotation
    - Repeat [1] with different measurements
    - Activate third solenoid a very small time after the motor is at the correct rotation
*/
