package frc.robot;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Drivetrain implements Loggable {

    private DriveModule left;
    private DriveModule right;

    Drivetrain(DriveModule left, DriveModule right) {
        this.left = left;
        this.right = right;
        right.setInverted(true);
    }

    public void drive(double leftSpeed, double rightSpeed) { //Probably implement deadbands later
        left.set(leftSpeed); 
        right.set(rightSpeed);
    }

    public void arcadeDrive(double turnInput, double speedInput) {
        this.drive(speedInput - turnInput, speedInput + turnInput);
    }

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
