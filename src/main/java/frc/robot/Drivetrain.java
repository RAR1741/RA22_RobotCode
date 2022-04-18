package frc.robot;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.logging.Loggable;
import frc.robot.logging.LoggableGyro;
import frc.robot.logging.Logger;

public class Drivetrain implements Loggable {

    private final double SHIFT_CURRENT_HIGH = 80; // TODO Get actual values when we test drivetrain
    private final double SHIFT_CURRENT_LOW = 0;
    private final double SHIFT_VELOCITY = 0; // Wheel velocity
    private final double OFF_BALANCE_THRESHOLD = 15;
    private final double ON_BALANCE_THRESHOLD = 7;

    private boolean climbMode;
    private boolean autoBalanceMode;
    private boolean autoBalanceEnabled;

    private double balanceScale;

    private DriveModule left;
    private DriveModule right;
    private Solenoid shifter;
    private LoggableGyro gyro;

    /**
     * Constructor.
     *
     * @param left DriveModule of the drivetrain's left half
     * @param right DriveModule of the drivetrain's right half
     * @param shifter ID for the shifter solenoid
     */
    Drivetrain(DriveModule left, DriveModule right, int shifterID, LoggableGyro gyro) {
        this.left = left;
        this.right = right;

        this.shifter = new Solenoid(PneumaticsModuleType.REVPH, shifterID);

        this.gyro = gyro;

        right.setInverted(true);

        climbMode = false;
        autoBalanceMode = false;
        autoBalanceEnabled = false;
    }

    public void update() {
        this.left.update();
        this.right.update();
        this.autoBalance();
    }

    private void autoBalance() {
        if (!autoBalanceMode && Math.abs(gyro.getPitch()) >= Math.abs(OFF_BALANCE_THRESHOLD)) {
            autoBalanceMode = true;
        } else if (autoBalanceMode && Math.abs(gyro.getPitch()) <= Math.abs(ON_BALANCE_THRESHOLD)) {
            autoBalanceMode = false;
        }

        if (autoBalanceMode) {
            double pitchAngleRadians = gyro.getPitch() * (Math.PI / 180.0);
            balanceScale = Math.sin(pitchAngleRadians) * 0.5;
        } else {
            balanceScale = 0;
        }
    }

    public void toggleAutoBalance() {
        autoBalanceEnabled = !autoBalanceEnabled;
    }

    /**
     * Controls the power of the motors in the drivetrain.
     *
     * @param leftSpeed The speed of the left motors
     * @param rightSpeed The speed of the right motors
     */
    public void drive(double leftSpeed, double rightSpeed) { // Probably implement deadbands later
        left.setSpeed(leftSpeed);
        right.setSpeed(rightSpeed);
    }

    public void setClimbMode(boolean climb) {
        climbMode = climb;
    }

    /**
     * Drive with arcade-style controls.
     *
     * @param turnInput The speed to turn
     * @param speedInput The speed to drive
     */
    public void arcadeDrive(double turnInput, double speedInput) {
        if (!getShifter()) {
            turnInput *= 1.45;
        }
        speedInput =
                climbMode ? speedInput * 0.4 : speedInput + (autoBalanceEnabled ? balanceScale : 0);
        this.drive(speedInput - turnInput, speedInput + turnInput);
    }

    /**
     * Tank Drive.
     *
     * @param leftDrive The speed to set the left motors
     * @param rightDrive The speed to set the right motors
     */
    public void tankDrive(double leftDrive, double rightDrive) {
        this.drive(leftDrive, rightDrive);
    }

    /**
     * Changes gears for the drivetrain.
     *
     * @param highSpeed true if in high speed gearing, false if in low speed gearing
     */
    public void setShifter(boolean highSpeed) {
        shifter.set(highSpeed);
    }

    /**
     * Gets if the gear shift is engaged.
     *
     * @return true if in high gear, false if in low gear
     */
    public boolean getShifter() {
        return shifter.get();
    }

    /**
     * Shifts gears based on current.
     */
    public void checkGears() {
        if (getShifter()) {
            if (left.getAverageCurrent() > SHIFT_CURRENT_LOW
                    || right.getAverageCurrent() > SHIFT_CURRENT_LOW) {
                // if in high current, stay in low speed gear
                setShifter(true);
            } else if (left.getSpeed() > SHIFT_VELOCITY / 10.86
                    || Math.abs(right.getSpeed()) > SHIFT_VELOCITY / 10.86) {
                // if high velocity, shift to high speed gear
                setShifter(false);
            } else {
                // if in low velocity and in low current, stay in low speed gear
                setShifter(true);
            }
        } else {
            if (left.getAverageCurrent() > SHIFT_CURRENT_HIGH
                    || right.getAverageCurrent() > SHIFT_CURRENT_HIGH) {
                // if in high current, shift to low speed gear
                setShifter(true);
            } else if (left.getSpeed() < SHIFT_VELOCITY / 16.37
                    && Math.abs(right.getSpeed()) < SHIFT_VELOCITY / 16.37) {
                // if low velocity, shift to low speed gear
                setShifter(true);
            } else {
                // if in low current and in high velocity, stay in high speed gear
                setShifter(false);
            }
        }
    }

    @Override
    public void setupLogging(Logger logger) {
        // logger.addLoggable(left);
        // logger.addLoggable(right);
    }

    @Override
    public void log(Logger logger) {
        left.log(logger);
        right.log(logger);
    }
}
