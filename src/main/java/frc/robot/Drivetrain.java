package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Drivetrain implements Loggable {

    private final double SHIFT_CURRENT_HIGH = 80; // TODO Get actual values when we test drivetrain
    private final double SHIFT_CURRENT_LOW = 0;
    private final double SHIFT_VELOCITY = 0; // Wheel velocity

    private boolean climbMode = false;

    private DriveModule left;
    private DriveModule right;
    private Solenoid shifter;

    /**
     * Constructor.
     *
     * @param left DriveModule of the drivetrain's left half
     * @param right DriveModule of the drivetrain's right half
     * @param shifter ID for the shifter solenoid
     */
    Drivetrain(DriveModule left, DriveModule right, int shifterID) {
        this.left = left;
        this.right = right;

        this.shifter = new Solenoid(PneumaticsModuleType.REVPH, shifterID);

        right.setInverted(true);
    }

    public void update() {
        this.left.update();
        this.right.update();
    }

    /**
     * Controls the power of the motors in the drivetrain.
     *
     * @param leftSpeed The speed of the left motors
     * @param rightSpeed The speed of the right motors
     */
    public void drive(double leftSpeed, double rightSpeed) { // Probably implement deadbands later
        left.setPower(leftSpeed);
        right.setPower(rightSpeed);
    }

    public void setClimbMode() {
        climbMode = !climbMode;
    }

    /**
     * Drive with arcade-style controls.
     *
     * @param turnInput The speed to turn
     * @param speedInput The speed to drive
     */
    public void arcadeDrive(double turnInput, double speedInput) {
        speedInput = speedInput * (climbMode ? 0.3 : 1);
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
     * @param lowSpeed true if in low speed gearing, false if in high speed gearing
     */
    public void setShifter(boolean lowSpeed) {
        shifter.set(lowSpeed);
    }

    /**
     * Gets if the gear shift is engaged.
     *
     * @return true if in low gear, false if in high gear
     */
    public boolean getShifter() {
        return shifter.get();
    }

    /**
     * Gets the encoder information for the left DriveModule
     * 
     * @return Position of left sensor (in raw sensor units).
     */
    public double getEncoder() {
        return (left.getDriveEnc() + right.getDriveEnc()) / 2;
    }

    public double getAverageCurrent() {
        return (left.getAverageCurrent() + right.getAverageCurrent()) / 2;
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

    public void setNeutralMode(NeutralMode mode) {
        this.left.setNeutralMode(mode);
        this.right.setNeutralMode(mode);
    }

    @Override
    public void setupLogging(Logger logger) {
        // logger.addLoggable(left);
        // logger.addLoggable(right);
    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub
    }
}
