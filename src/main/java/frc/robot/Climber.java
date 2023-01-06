package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
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
        RESTING(0, "Default resting"), //
        PRE_STAGE(5, "Rotate climber and set pre-stage pin position (button)"), //
        TOUCH_A(10, "Pin A (button/sensor)"), //
        ROTATE_B(15, "Rotate to B bar (photogate)"), //
        TOUCH_AB(20, "Pin B (high current/sensor)"), //
        ROTATE_AB_DOWN(25, "Rotate down to plumb (photogate)"), //
        // RELEASE_A(30, "Unpin A (gyro/accel)"), //
        ROTATE_B_DOWN(35, "Wait for swinging (photogate)"), //
        ROTATE_C(40, "Rotate to C bar (gyro/accel)"), //
        TOUCH_BC(50, "Pin C (high current/sensor)"), //
        ROTATE_BC_DOWN(55, "Rotate down to plumb (photogate)"), //
        RELEASE_B(60, "Unpin B (gyro/accel)"), //
        ROTATE_C_DOWN(65, "Wait for swinging ()"), //
        DONE(70, "Climbing is done"), //
        ERROR(100, "Error");

        public int id;
        public String name;

        ClimbingStates(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    enum MotorStates {
        STATIC, ACTIVE;
    }

    public static double MAX_INSTANT_CURRENT = 200.0;
    public static double MAX_AVERAGE_CURRENT = 120.0;
    public static double NEXT_AB_STATE_CURRENT = 65.0;
    public static double NEXT_BC_STATE_CURRENT = 45.0;
    public static int FILTER_FRAME_RANGE = 10;

    public static double ENCODER_DEADZONE = 100;

    public static double TOUCH_A_POSITION = 120000; // TBD
    public static double TOUCH_B_POSITION = -18000;
    public static double SWING_B_POSITION = -25000; // TBD
    public static double TOUCH_C_POSITION = -145000;

    private double motorSpeed;
    private double pError = 0;

    private double previousTime;

    TalonFX climbingMotor;
    TalonFX secondaryClimbingMotor;

    Solenoid climberSolenoidA;
    Solenoid climberSolenoidB1;
    Solenoid climberSolenoidB2;
    Solenoid climberSolenoidC;

    ClimberSensors touch;
    ClimberGates gates;

    MotorStates currentMotorState = MotorStates.STATIC;
    ClimbingStates currentClimberState = ClimbingStates.RESTING;
    LoggableTimer timer;
    LoggableGyro gyro;

    LoggableFirstOrderFilter leftFilter;
    LoggableFirstOrderFilter rightFilter;

    public Climber(int climbingMotorID, int secondaryClimbingMotorID, Solenoid climberSolenoidA,
            Solenoid climberSolenoidB1, Solenoid climberSolenoidB2, Solenoid climberSolenoidC) {
        // ClimberSensors touch) {

        this.climbingMotor = new TalonFX(climbingMotorID);
        this.secondaryClimbingMotor = new TalonFX(secondaryClimbingMotorID);

        this.climberSolenoidA = climberSolenoidA;
        this.climberSolenoidB1 = climberSolenoidB1;
        this.climberSolenoidB2 = climberSolenoidB2;
        this.climberSolenoidC = climberSolenoidC;

        this.touch = touch;

        this.climbingMotor.setNeutralMode(NeutralMode.Coast);
        this.secondaryClimbingMotor.setNeutralMode(NeutralMode.Coast);

        this.climbingMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0,
                30);
        this.climbingMotor.setSelectedSensorPosition(0);
        this.climbingMotor.config_kP(0, 0.1);
        this.climbingMotor.config_kI(0, 0.001);
        this.climbingMotor.config_kD(0, 5);

        secondaryClimbingMotor.setInverted(InvertType.InvertMotorOutput);
        secondaryClimbingMotor.follow(climbingMotor);

        this.timer = new LoggableTimer("Climber/Time");
        // this.gyro = gyro;

        this.leftFilter = new LoggableFirstOrderFilter(FILTER_FRAME_RANGE, "Climber/Left/Current");
        this.rightFilter =
                new LoggableFirstOrderFilter(FILTER_FRAME_RANGE, "Climber/Right/Current");
    }

    public void update() {
        this.leftFilter.update(climbingMotor.getStatorCurrent());
        this.rightFilter.update(secondaryClimbingMotor.getStatorCurrent());

        // // Make sure we're not pulling too much current instantly
        // if (climbingMotor.getStatorCurrent() > MAX_INSTANT_CURRENT
        // || secondaryClimbingMotor.getStatorCurrent() > MAX_INSTANT_CURRENT) {
        // setClimbingState(ClimbingStates.ERROR);
        // System.out.println("--------HIT MAX CURRENT--------");
        // }

        // // Make sure we're not pulling too much current over time
        // if (leftFilter.get() > MAX_AVERAGE_CURRENT || rightFilter.get() > MAX_AVERAGE_CURRENT) {
        // System.out.println("--------HIT MAX AVERAGE CURRENT--------");
        // setClimbingState(ClimbingStates.ERROR);
        // }

        switch (this.currentClimberState) {
            // 00 RESTING: Default resting
            case RESTING:
                climberSolenoidA.set(false);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(true);
                climberSolenoidC.set(false);
                break;

            // 05 PRE_STAGE: Rotate climber and set pre-stage pin position (button)
            case PRE_STAGE:
                this.timer.start();

                climberSolenoidA.set(true);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                // climbingMotor.set(ControlMode.Position, TOUCH_A_POSITION);
                break;

            // 10 TOUCH_A: Pin A (button/sensor)
            case TOUCH_A:
                setMotorState(MotorStates.ACTIVE);
                this.setMotors(0);
                climberSolenoidA.set(false);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);

                // Uncomment after testing
                // if (gates.getA()) {
                // setClimbingState(ClimbingStates.ROTATE_B);
                // }
                break;

            // 15 ROTATE_B: Rotate to B bar (photogate)
            case ROTATE_B:
                // TODO: set motor power here
                climberSolenoidA.set(false);
                climberSolenoidB1.set(true);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);

                pError = Math.abs(
                        (getPosition() - TOUCH_B_POSITION) / (TOUCH_B_POSITION - TOUCH_A_POSITION));
                this.setMotors(-(0.2 + 0.7 * Math.sqrt(pError)));
                // this.setMotors(-0.4);

                // if (climbingMotor.getStatorCurrent() > NEXT_AB_STATE_CURRENT
                // || secondaryClimbingMotor.getStatorCurrent() > NEXT_AB_STATE_CURRENT) {
                // setClimbingState(ClimbingStates.TOUCH_AB);
                // this.setSpeed(0);
                // }

                // if (touch.getB()) {
                // setClimbingState(ClimbingStates.TOUCH_AB);
                // previousTime = timer.get();
                // }
                break;

            // 20 TOUCH_AB: Pin B (high current/sensor)
            case TOUCH_AB:
                this.setMotors(-0.05);
                // if (touch.getB()) {
                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                // } else {
                // climberSolenoidA.set(false);
                // climberSolenoidB1.set(true);
                // climberSolenoidB2.set(false);
                // climberSolenoidC.set(true);
                // previousTime = timer.get();
                // }
                // if (!gates.getB1()) {
                // setClimbingState(ClimbingStates.ROTATE_B);
                // }
                // if (timer.get() - previousTime > 0.5) {
                // setClimbingState(ClimbingStates.ROTATE_AB_DOWN);
                // }
                break;

            // 25 ROTATE_AB_DOWN: Rotate down to plumb (photogate)
            case ROTATE_AB_DOWN:
                // this.setPower(0);
                climberSolenoidA.set(false);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                // if (climbingMotor.getSelectedSensorPosition() > SWING_AB_POSITION) {// Was less
                // then
                // // when broken
                // System.out.println("DONE SWINGING!");
                // // setClimbingState(ClimbingStates.RELEASE_A);
                // }
                break;

            // // 30 RELEASE_A: Unpin A (gyro/accel)
            // case RELEASE_A:
            // climberSolenoidA.set(true);
            // climberSolenoidB1.set(false);
            // climberSolenoidB2.set(false);
            // climberSolenoidC.set(true);
            // // this.setPower(0);
            // // TODO: set motor target here
            // // if (!gates.getA()) {
            // setClimbingState(ClimbingStates.ROTATE_B_DOWN);
            // // }
            // break;

            // 35 ROTATE_B_DOWN: Wait for swinging (photogate)
            case ROTATE_B_DOWN:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);
                // this.setPower(0);
                // if (Math.abs(climbingMotor.getSelectedSensorPosition() - SWING_B_POSITION) < 1000
                // && Math.abs(
                // climbingMotor.getSelectedSensorVelocity()) < SWING_MIN_VELOCITY) {
                // // Determine tolerance
                // System.out.println("DONE SWINGING!");
                // // setClimbingState(ClimbingStates.ROTATE_C);
                // }
                break;

            // 40 ROTATE_C: Rotate to C bar (gyro/accel)
            case ROTATE_C:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(true);

                pError = Math.abs(
                        (getPosition() - TOUCH_C_POSITION) / (TOUCH_C_POSITION - SWING_B_POSITION));
                this.setMotors(-(0.3 + 0.7 * Math.sqrt(pError)));
                // this.setMotors(-0.3);

                // if (climbingMotor.getStatorCurrent() > NEXT_BC_STATE_CURRENT
                // || secondaryClimbingMotor.getStatorCurrent() > NEXT_BC_STATE_CURRENT) {
                // setClimbingState(ClimbingStates.TOUCH_BC);
                // this.setSpeed(0);
                // }
                break;

            // 50 TOUCH_BC: Pin C (high current/sensor)
            case TOUCH_BC:
                this.setMotors(-0.05);
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(false);
                // TODO: set motor target here
                // if (gates.getC()) {
                // setClimbingState(ClimbingStates.ROTATE_BC_DOWN);
                // }
                break;

            // 55 ROTATE_BC_DOWN: Rotate down to plumb (photogate)
            case ROTATE_BC_DOWN:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(false);
                climberSolenoidC.set(false);
                // if (climbingMotor.getSelectedSensorPosition() < SWING_BC_POSITION) {// Determine
                // // tolerance
                // System.out.println("DONE SWINGING!");
                // // setClimbingState(ClimbingStates.RELEASE_B);
                // }
                break;

            // 60 RELEASE_B: Unpin B (gyro/accel)
            case RELEASE_B:
                // this.setPower(0);
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(true);
                climberSolenoidC.set(false);
                // if (!gates.getB2()) {
                // setClimbingState(ClimbingStates.ROTATE_C_DOWN);
                // }
                break;

            // 65 ROTATE_C_DOWN: Wait for swinging ()
            case ROTATE_C_DOWN:
                climberSolenoidA.set(true);
                climberSolenoidB1.set(false);
                climberSolenoidB2.set(true);
                climberSolenoidC.set(false);
                // this.setPower(0);
                break;

            // 70 DONE: Climbing is done
            case DONE:
                // Success! \o/
                break;

            // 100 ERROR: Error
            case ERROR:
                System.out.println("Climber ERROR: something has gone wrong");
                disableClimber();
                break;

            default:
                System.out.println("Climber: Invalid state");
                disableClimber();
                break;
        }

    }

    public void setClimbingState(ClimbingStates climbingState) {
        setMotorState(MotorStates.STATIC);
        this.currentClimberState = climbingState;
    }

    public ClimbingStates getNextClimbingState() {
        return ClimbingStates.values()[this.currentClimberState.ordinal() + 1];
    }

    public ClimbingStates getPreviousClimbingState() {
        return ClimbingStates.values()[this.currentClimberState.ordinal() - 1];
    }

    public int getClimberStateId() {
        return this.currentClimberState.ordinal();
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
    }

    /**
     * Updates the current state of the climber.
     *
     * @return true when current state is RESTING
     */
    // public void checkClimbingState(boolean advanceStage) {
    // // Check whether or not the climber is done climbing during the current stage.
    // switch (currentStage) {
    // case PRE_STAGE:
    // // Check if A is touching yet.
    // if (advanceStage) { // touch.getA()
    // this.setClimbingState(ClimbingStates.TOUCH_A);
    // }
    // break;
    // case TOUCH_A:
    // // Check if B is touching yet.
    // if (advanceStage) { // touch.getB()
    // this.setClimbingState(ClimbingStates.TOUCH_AB);
    // }
    // break;
    // case TOUCH_AB:
    // if (advanceStage) {
    // this.setMotorState(MotorStates.STATIC);
    // this.setClimbingState(ClimbingStates.TOUCH_B);
    // }
    // break;
    // case TOUCH_B:
    // // Check if C is touching yet.
    // if (advanceStage) { // touch.getC()
    // this.setClimbingState(ClimbingStates.TOUCH_BC);
    // }
    // break;
    // case TOUCH_BC:
    // if (advanceStage) {
    // this.setMotorState(MotorStates.STATIC);
    // this.setClimbingState(ClimbingStates.TOUCH_C);
    // }
    // break;
    // case TOUCH_C:
    // // Success! \o/
    // break;
    // default:
    // break;
    // }
    // }

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

    public double getPosition() {
        return climbingMotor.getSelectedSensorPosition();
    }

    public void setMotorState(MotorStates currentState) {
        this.currentMotorState = currentState;
    }

    public MotorStates getMotorState() {
        return this.currentMotorState;
    }

    public void setMotors(double value) {
        checkMotorState();
        motorSpeed = value;
        if (value != 0) {
            setMotorState(MotorStates.ACTIVE);
        }
        switch (currentMotorState) {
            case STATIC:
                setPower(0);
                break;

            case ACTIVE:
                setSpeed(motorSpeed);
                break;

            default:
                setPower(0);
                break;
        }
    }

    public void checkMotorState() {
        switch (currentMotorState) {
            case STATIC:
                // if (Math.abs(gyro.getVelocityY()) < 2
                // && Math.abs(gyro.getWorldLinearAccelY()) < 0.1) {
                // setMotorState(MotorStates.ACTIVE);
                // }
                break;

            case ACTIVE:
                break;

            default:
                break;
        }
    }

    /**
     * Checks whether or not the climber is climbing.
     *
     * @return true if the climber is climbing
     */
    public boolean isClimbing() {
        return this.currentClimberState != ClimbingStates.RESTING;
    }

    @Override
    public void setupLogging(Logger logger) {
        this.timer.setupLogging(logger);

        // logger.addLoggable(this.leftFilter);
        // logger.addLoggable(this.rightFilter);
        // this.leftFilter.setupLogging(logger);
        // this.rightFilter.setupLogging(logger);

        logger.addAttribute("Climber/Left/Current");
        logger.addAttribute("Climber/Right/Current");

        logger.addAttribute("Climber/Speed");
        logger.addAttribute("Climber/Position");
        logger.addAttribute("Climber/Error");

        logger.addAttribute("Climber/State/Name");
        logger.addAttribute("Climber/State/Id");
        logger.addAttribute("Climber/State/Ordinal");
    }

    @Override
    public void log(Logger logger) {
        this.timer.log(logger);
        logger.log("Climber/Left/Current", getLeftCurrent());
        logger.log("Climber/Right/Current", getRightCurrent());

        logger.log("Climber/Speed", getSpeed());
        logger.log("Climber/Position", getPosition());
        logger.log("Climber/Error", pError);

        logger.log("Climber/State/Name", this.currentClimberState.name);
        logger.log("Climber/State/Id", this.currentClimberState.id);
        logger.log("Climber/State/Ordinal", this.currentClimberState.ordinal());
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
