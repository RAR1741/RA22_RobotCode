package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.logging.Loggable;
import frc.robot.logging.LoggableFirstOrderFilter;
import frc.robot.logging.LoggableGyro;
import frc.robot.logging.LoggableTimer;
import frc.robot.logging.Logger;

public class Climber implements Loggable {
    enum ClimbingStates {
        RESTING(0, "Default resting"), PRE_STAGE(5,
                "Rotate climber and set pre-stage pin position (button)"), TOUCH_A(10,
                        "Pin A (button/sensor)"), ROTATE_B(15,
                                "Rotate to B bar (photogate)"), TOUCH_AB(20,
                                        "Pin B (high current/sensor)"), ROTATE_AB_DOWN(25,
                                                "Rotate down to plumb (photogate)"), RELEASE_A(30,
                                                        "Unpin A (gyro/accel)"), ROTATE_B_DOWN(35,
                                                                "Wait for swinging (photogate)"), ROTATE_C(
                                                                        40,
                                                                        "Rotate to C bar (gyro/accel)"), TOUCH_BC(
                                                                                50,
                                                                                "Pin C (high current/sensor)"), ROTATE_BC_DOWN(
                                                                                        55,
                                                                                        "Rotate down to plumb (photogate)"), RELEASE_B(
                                                                                                60,
                                                                                                "Unpin B (gyro/accel)"), ROTATE_C_DOWN(
                                                                                                        65,
                                                                                                        "Wait for swinging ()"), DONE(
                                                                                                                70,
                                                                                                                "Climbing is done"), ERROR(
                                                                                                                        100,
                                                                                                                        "Error");

        public int id;
        public String name;

        ClimbingStates(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // 00 RESTING: Default resting
    // 05 PRE_STAGE: Rotate climber and set pre-stage pin position (button)
    // 10 TOUCH_A: Pin A (button/sensor)
    // 15 ROTATE_B: Rotate to B bar (photogate)
    // 20 TOUCH_AB: Pin B (high current/sensor)
    // 25 ROTATE_AB_DOWN: Rotate down to plumb (photogate)
    // 30 RELEASE_A: Unpin A (gyro/accel)
    // 35 ROTATE_B_DOWN: Wait for swinging (photogate)
    // 40 ROTATE_C: Rotate to C bar (gyro/accel)
    // 50 TOUCH_BC: Pin C (high current/sensor)
    // 55 ROTATE_BC_DOWN: Rotate down to plumb (photogate)
    // 60 RELEASE_B: Unpin B (gyro/accel)
    // 65 ROTATE_C_DOWN: Wait for swinging ()
    // 70 DONE: Climbing is done
    // 100 ERROR: Error

    enum MotorStates {
        STATIC, ACTIVE;
    }

    public static double MAX_INSTANT_CURRENT = 70.0;
    public static double MAX_AVERAGE_CURRENT = 50.0;
    public static double NEXT_STATE_CURRENT = 50.0;

    TalonFX climbingMotor;
    TalonFX secondaryClimbingMotor;

    Solenoid climberSolenoidA;
    Solenoid climberSolenoidB1;
    Solenoid climberSolenoidB2;
    Solenoid climberSolenoidC;

    ClimberSensors touch;

    MotorStates currentState = MotorStates.STATIC;
    ClimbingStates currentStage = ClimbingStates.RESTING;
    LoggableTimer timer;
    LoggableGyro gyro;

    LoggableFirstOrderFilter leftFilter;
    LoggableFirstOrderFilter rightFilter;

    public Climber(int climbingMotorID, int secondaryClimbingMotorID, Solenoid climberSolenoidA,
            Solenoid climberSolenoidB1, Solenoid climberSolenoidB2, Solenoid climberSolenoidC,
            LoggableGyro gyro) {
        // ClimberSensors touch) {

        // TODO: figure out if the motors are inverted correctly
        this.climbingMotor = new TalonFX(climbingMotorID);
        this.secondaryClimbingMotor = new TalonFX(secondaryClimbingMotorID);

        this.climberSolenoidA = climberSolenoidA;
        this.climberSolenoidB1 = climberSolenoidB1;
        this.climberSolenoidB2 = climberSolenoidB2;
        this.climberSolenoidC = climberSolenoidC;

        // this.touch = touch;

        this.climbingMotor.setNeutralMode(NeutralMode.Coast);
        this.secondaryClimbingMotor.setNeutralMode(NeutralMode.Coast);

        this.climbingMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0,
                30);
        this.climbingMotor.config_kP(0, 0.1);
        this.climbingMotor.config_kI(0, 0.001);
        this.climbingMotor.config_kD(0, 5);

        secondaryClimbingMotor.setInverted(InvertType.InvertMotorOutput);
        secondaryClimbingMotor.follow(climbingMotor);
        this.timer = new LoggableTimer("Climber/Time");
        this.gyro = gyro;

        this.leftFilter = new LoggableFirstOrderFilter(10, "Climber/Left/Current");
        this.rightFilter = new LoggableFirstOrderFilter(10, "Climber/Right/Current");
    }

    public void update() {
        this.leftFilter.update(climbingMotor.getStatorCurrent());
        this.rightFilter.update(secondaryClimbingMotor.getStatorCurrent());

        // Make sure we're not pulling too much current instantly
        if (climbingMotor.getStatorCurrent() > MAX_INSTANT_CURRENT
                || secondaryClimbingMotor.getStatorCurrent() > MAX_INSTANT_CURRENT) {
            setClimbingState(ClimbingStates.ERROR);
        }

        // Make sure we're not pulling too much current over time
        if (leftFilter.get() > MAX_AVERAGE_CURRENT || rightFilter.get() > MAX_AVERAGE_CURRENT) {
            setClimbingState(ClimbingStates.ERROR);
        }

        switch (this.currentStage) {
            // 00 RESTING: Default resting
            case RESTING:
                break;

            // 05 PRE_STAGE: Rotate climber and set pre-stage pin position (button)
            case PRE_STAGE:
                this.timer.start();

                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(false);
                // TODO: set motor target here
                break;

            // 10 TOUCH_A: Pin A (button/sensor)
            case TOUCH_A:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                break;

            // 15 ROTATE_B: Rotate to B bar (photogate)
            case ROTATE_B:
                // TODO: set motor power here
                break;

            // 20 TOUCH_AB: Pin B (high current/sensor)
            case TOUCH_AB:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                // TODO: set motor target here
                break;

            // 25 ROTATE_AB_DOWN: Rotate down to plumb (photogate)
            case ROTATE_AB_DOWN:
                // TODO: set motor target here
                break;

            // 30 RELEASE_A: Unpin A (gyro/accel)
            case RELEASE_A:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                // TODO: set motor target here
                break;

            // 35 ROTATE_B_DOWN: Wait for swinging (photogate)
            case ROTATE_B_DOWN:
                // TODO: set motor target here
                break;

            // 40 ROTATE_C: Rotate to C bar (gyro/accel)
            case ROTATE_B_DOWN:
                // TODO: set motor target here
                break;

            // 50 TOUCH_BC: Pin C (high current/sensor)
            case TOUCH_BC:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(false);
                // TODO: set motor target here
                break;

            // 55 ROTATE_BC_DOWN: Rotate down to plumb (photogate)
            case ROTATE_BC_DOWN:
                // TODO: set motor target here
                break;

            // 60 RELEASE_B: Unpin B (gyro/accel)
            case RELEASE_B:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(true);
                climberSolenoidC.set(false);
                break;

            // 65 ROTATE_C_DOWN: Wait for swinging ()
            case ROTATE_C_DOWN:
                // TODO: set motor target here
                break;

            // 70 DONE: Climbing is done
            case DONE:
                // Success! \o/
                break;

            // 100 ERROR: Error
            case ERROR:
                System.out.println("Climber ERROR: something has gone wrong");
                break;

            default:
                System.out.println("Climber: Invalid state");
                break;
        }
    }

    public void setClimbingState(ClimbingStates climbingState) {
        this.currentStage = climbingState;
    }

    public ClimbingStates getNextClimbingState() {
        return ClimbingStates.values()[this.currentStage.ordinal() + 1];
    }

    public void disableClimber() {
        // Stop the motors
        this.climbingMotor.set(ControlMode.PercentOutput, 0);
        this.secondaryClimbingMotor.set(ControlMode.PercentOutput, 0);

        // Set the solenoids to their default extended positions
        climberSolenoidA.set(false);
        climberSolenoidB1.set(false);
        climberSolenoidB2.set(false);
        climberSolenoidC.set(false);

        // Set the current stage to the default
        setClimbingState(ClimbingStates.ERROR);
    }

    /**
     * Updates the current state of the climber.
     *
     * @return true when current state is RESTING
     */
    public void checkClimbingState(boolean advanceStage) {
        // Check whether or not the climber is done climbing during the current stage.
        switch (currentStage) {
            case PRE_STAGE:
                // Check if A is touching yet.
                if (advanceStage) { // touch.getA()
                    this.setClimbingState(ClimbingStates.TOUCH_A);
                }
                break;
            case TOUCH_A:
                // Check if B is touching yet.
                if (advanceStage) { // touch.getB()
                    this.setClimbingState(ClimbingStates.TOUCH_AB);
                }
                break;
            case TOUCH_AB:
                if (advanceStage) {
                    this.setMotorState(MotorStates.STATIC);
                    this.setClimbingState(ClimbingStates.TOUCH_B);
                }
                break;
            case TOUCH_B:
                // Check if C is touching yet.
                if (advanceStage) { // touch.getC()
                    this.setClimbingState(ClimbingStates.TOUCH_BC);
                }
                break;
            case TOUCH_BC:
                if (advanceStage) {
                    this.setMotorState(MotorStates.STATIC);
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

    public void setMotorState(MotorStates currentState) {
        this.currentState = currentState;
    }

    public MotorStates getMotorState() {
        return this.currentState;
    }

    public void setMotors(double value) {
        checkMotorState();
        if (value != 0) {
            setMotorState(MotorStates.ACTIVE);
        }
        switch (currentState) {
            case STATIC:
                setPower(0);
                break;

            case ACTIVE:
                setSpeed(value);
                break;

            default:
                setPower(0);
                break;
        }
    }

    public void checkMotorState() {
        switch (currentState) {
            case STATIC:
                if (Math.abs(gyro.getVelocityY()) < 2
                        && Math.abs(gyro.getWorldLinearAccelY()) < 0.1) {
                    setMotorState(MotorStates.ACTIVE);
                }
                break;

            case ACTIVE:
                break;

            default:
                break;
        }
    }

    @Override
    public void setupLogging(Logger logger) {
        this.timer.setupLogging(logger);

        logger.addLoggable(this.leftFilter);
        logger.addLoggable(this.rightFilter);
        this.leftFilter.setupLogging(logger);
        this.rightFilter.setupLogging(logger);

        logger.addAttribute("Climber/Left/Current");
        logger.addAttribute("Climber/Right/Current");

        logger.addAttribute("Climber/Speed");
    }

    @Override
    public void log(Logger logger) {
        this.timer.log(logger);
        logger.log("Climber/Left/Current", getLeftCurrent());
        logger.log("Climber/Right/Current", getRightCurrent());

        logger.log("Climber/Speed", getSpeed());
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
