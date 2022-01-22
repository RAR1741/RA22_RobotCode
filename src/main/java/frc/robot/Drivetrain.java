package frc.robot;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Drivetrain implements Loggable {

    private final double SHIFT_CURRENT_HIGH = 80; //TODO Get actual values when we test drivetrain
    private final double SHIFT_CURRENT_LOW = 0;

    private DriveModule left;
    private DriveModule right;
    private Solenoid shifter;

    /**
     * Constructor
     * 
     * @param left DriveModule of the drivetrain's left half
     * @param right DriveModule of the drivetrain's right half
     * @param shifter ID for the shifter solenoid
     * 
     */
    Drivetrain(DriveModule left, DriveModule right, int shifterID) {
        this.left = left;
        this.right = right;

        this.shifter = new Solenoid(PneumaticsModuleType.REVPH, shifterID);

        right.setInverted(true);
    }

    /**
     * Controls the power of the motors in the drivetrain
     * 
     * @param leftSpeed The speed of the left motors
     * @param rightSpeed The speed of the right motors
     * 
     */
    public void drive(double leftSpeed, double rightSpeed) { //Probably implement deadbands later
        left.set(leftSpeed); 
        right.set(rightSpeed);
    }

    /**
     * Drive with arcade-style controls
     * 
     * @param turnInput The speed to turn 
     * @param speedInput The speed to drive
     * 
     */
    public void arcadeDrive(double turnInput, double speedInput) {
        this.drive(speedInput - turnInput, speedInput + turnInput);
    }

    /**
     * Tank Drive
     * 
     * @param leftDrive The speed to set the left motors
     * @param rightDrive The speed to set the right motors
     * 
     */
    public void tankDrive(double leftDrive, double rightDrive) {
        this.drive(leftDrive, rightDrive);
    }

    /**
     * Changes gears for the drivetrain
     * 
     * @param lowSpeed true if in low speed gearing, false if in high speed gearing
     */
    public void setShifter(boolean lowSpeed) {
        shifter.set(lowSpeed);
    }

    /**
     * Gets if the gear shift is engaged
     * 
     * @return true if in low gear, false if in high gear
     */
    public boolean getShifter() {
        return shifter.get();
    }

    /**
     * Shifts gears based on current
     */
    public void checkGears() {
        if (getShifter()) {
            // if in low speed gear and in low current, shift to high speed gear
            setShifter(left.getCurrent() > SHIFT_CURRENT_LOW || right.getCurrent() > SHIFT_CURRENT_LOW);
        } else {
            // if in high speed gear and in high current, shift to low speed gear
            setShifter(left.getCurrent() > SHIFT_CURRENT_HIGH || right.getCurrent() > SHIFT_CURRENT_HIGH);            
        }
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
