package frc.robot;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Drivetrain implements Loggable {

    private DriveModule left;
    private DriveModule right;

    /**
     * Constructor
     * 
     * @param left DriveModule of the drivetrain's left half
     * @param right DriveModule of the drivetrain's right half
     * 
     */
    Drivetrain(DriveModule left, DriveModule right) {
        this.left = left;
        this.right = right;
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

    @Override
    public void setupLogging(Logger logger) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub
        
    }
    
}
