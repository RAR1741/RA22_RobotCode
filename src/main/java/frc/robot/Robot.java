// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.logging.LoggableCompressor;
import frc.robot.logging.LoggableController;
import frc.robot.logging.LoggablePowerDistribution;
import frc.robot.logging.LoggableTimer;
import frc.robot.logging.Logger;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

    Logger logger;
    LoggableTimer timer;

    Drivetrain drive;
    DriveModule leftModule;
    DriveModule rightModule;
    LoggableController driver;
    LoggableController operator;

    LoggablePowerDistribution pdp;
    LoggableCompressor compressor;

    boolean drivetrainEnabled = true;
    boolean tankDriveEnabled = true;

    private static final double DEADBAND_LIMIT = 0.01;
    private static final double SPEED_CAP = 0.6;
    InputScaler joystickDeadband = new Deadband(DEADBAND_LIMIT);
    InputScaler joystickSquared = new SquaredInput(DEADBAND_LIMIT);
    BoostInput boost = new BoostInput(SPEED_CAP);

    public double deadband(double in) {
        double out = joystickSquared.scale(in);
        return joystickDeadband.scale(out);
    }

    /**
     * This function is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        pdp = new LoggablePowerDistribution(1, ModuleType.kRev);

        driver = new LoggableController("Driver", 0);
        operator = new LoggableController("Operator", 1);
        logger = new Logger();

        timer = new LoggableTimer();
        logger.addLoggable(timer);

        if (this.drivetrainEnabled) {
            System.out.println("Initializing drivetrain...");
            leftModule = new DriveModule("LeftDriveModule", 2, 3); // 2, 3
            leftModule.setEncoder(2, 3, false);

            rightModule = new DriveModule("RightDriveModule", 4, 5); // 4, 5
            rightModule.setEncoder(0, 1, true);

            drive = new Drivetrain(leftModule, rightModule, 6);

            logger.addLoggable(leftModule);
            logger.addLoggable(rightModule);
            logger.addLoggable(drive);
        } else {
            System.out.println("Drivetrain initialization disabled.");
        }

        System.out.print("Initializing compressor...");
        compressor = new LoggableCompressor(PneumaticsModuleType.REVPH);
        System.out.println("done");

        logger.addLoggable(driver);
        // logger.addLoggable(operator);
        logger.addLoggable(compressor);
    }

    @Override
    public void robotPeriodic() {
        // Robot code goes here
    }

    @Override
    public void autonomousInit() {
        resetLogging();
    }

    @Override
    public void autonomousPeriodic() {
        // Robot code goes here
        logger.log();
        logger.writeLine();
    }

    @Override
    public void teleopInit() {
        resetLogging();
    }

    @Override
    public void teleopPeriodic() {
        // Robot code goes here
        if (this.drivetrainEnabled) {
            if (tankDriveEnabled) {
                double leftInput = deadband(-driver.getLeftY());
                double rightInput = deadband(-driver.getRightY());
                drive.tankDrive(leftInput, rightInput);
            } else {
                double turnInput = deadband(driver.getRightX());
                double speedInput = deadband(-driver.getLeftY());
                boost.setEnabled(driver.getRightTriggerAxis() > 0.5);
                drive.arcadeDrive(turnInput, boost.scale(speedInput));
            }
            if (driver.getXButtonPressed()) {
                tankDriveEnabled = !tankDriveEnabled;
            }
            if (driver.getLeftBumperPressed()) {
                drive.setShifter(!drive.getShifter());
            }

            leftModule.updateCurrent();
            rightModule.updateCurrent();
        }

        logger.log();
        logger.writeLine();
    }

    @Override
    public void disabledInit() {
        logger.close();
        timer.stop();
    }

    @Override
    public void disabledPeriodic() {
        // Robot code goes here
        // logger.log();
    }

    @Override
    public void testInit() {
        resetLogging();
    }

    @Override
    public void testPeriodic() {
        // Robot code goes here
        logger.log();
        logger.writeLine();
    }

    private void resetLogging() {
        logger.open();
        logger.setup();

        timer.reset();
        timer.start();
    }
}
