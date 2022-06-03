// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import frc.robot.logging.LoggableCompressor;
import frc.robot.logging.LoggableController;
import frc.robot.logging.LoggableGyro;
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
    Manipulation manipulation;
    Shooter shooter;

    LoggablePowerDistribution pdp;
    LoggableCompressor compressor;

    boolean drivetrainEnabled = true;
    boolean tankDriveEnabled = true;
    boolean manipulationEnabled = true;
    boolean shooterEnabled = true;

    private JsonAutonomous auto;

    LoggableGyro gyro;

    private static final double DEADBAND_LIMIT = 0.01;
    private static final double SPEED_CAP = 0.25;
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
        pdp = new LoggablePowerDistribution(1, ModuleType.kCTRE); // rev

        driver = new LoggableController("Driver", 0);
        operator = new LoggableController("Operator", 1);
        logger = new Logger();

        timer = new LoggableTimer();
        logger.addLoggable(timer);

        gyro = new LoggableGyro();
        gyro.enableLogging(false);

        System.out.print("Initializing compressor...");
        compressor = new LoggableCompressor(2, PneumaticsModuleType.CTREPCM); // rev
        System.out.println("done");

        if (this.drivetrainEnabled) {
            System.out.println("Initializing drivetrain...");
            leftModule = new DriveModule("LeftDriveModule", 5, 7); // 2, 3
            leftModule.setEncoder(2, 3, false);

            rightModule = new DriveModule("RightDriveModule", 8, 10); // 4, 5
            rightModule.setEncoder(0, 1, true);

            drive = new Drivetrain(leftModule, rightModule, 6);
            drive.setNeutralMode(NeutralMode.Coast);

            logger.addLoggable(leftModule);
            logger.addLoggable(rightModule);
            logger.addLoggable(drive);
        } else {
            System.out.println("Drivetrain initialization disabled.");
        }
        if (this.manipulationEnabled) {
            System.out.println("Initializing manipulation...");
            manipulation = new Manipulation(1, 0, 13, 14); // 0, 1, 7, 8
        } else {
            System.out.println("Manipulation initialization disabled.");
        }
        if (this.shooterEnabled) {
            System.out.println("Initializing shooter");
            shooter = new Shooter(16, 11); // ?
            logger.addLoggable(shooter);
            System.out.println("Shooter done");
        } else {
            System.out.println("Shooter initialization disabled.");
        }

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
        gyro.reset();

        if (drivetrainEnabled) {
            drive.setNeutralMode(NeutralMode.Brake);
        }
        auto = new JsonAutonomous(JsonAutonomous.getAutoPath("parser-stress-test.json"), gyro, drive, shooter, manipulation);
        System.out.println("Auto Initialized");
        logger.addLoggable(auto);
        resetLogging();
    }

    @Override
    public void autonomousPeriodic() {
        // Robot code goes here
        leftModule.updateCurrent();
        rightModule.updateCurrent();
        auto.run();

        logger.log();
        logger.writeLine();
    }

    @Override
    public void teleopInit() {
        if (drivetrainEnabled) {
            drive.setNeutralMode(NeutralMode.Coast);
        }
        resetLogging();
    }

    @Override
    public void teleopPeriodic() {
        // Robot code goes here
        if (this.drivetrainEnabled) {
            if (tankDriveEnabled) {
                double leftInput = deadband(driver.getLeftY());
                double rightInput = deadband(driver.getRightY());
                //leftModule.set(leftInput);
                //rightModule.set(rightInput);
                drive.tankDrive(leftInput, rightInput);
            } else {
                double turnInput = deadband(driver.getRightX());
                double speedInput = deadband(driver.getLeftY()); // negative in 2022
                boost.setEnabled(driver.getRightTriggerAxis() > 0.5);
                drive.arcadeDrive(turnInput, boost.scale(speedInput));
            }
            if (driver.getXButtonPressed()) {
                tankDriveEnabled = !tankDriveEnabled;
            }
            // if (driver.getLeftBumperPressed()) {
            //     drive.setShifter(!drive.getShifter());
            // }

            leftModule.updateCurrent();
            rightModule.updateCurrent();
        }
        if (this.manipulationEnabled) {
            if (operator.getRightBumper()) {
                manipulation.setIntakeExtend(true); // down
            } else /* (operator.getLeftBumper()) */ {
                manipulation.setIntakeExtend(false); // up
            }
            manipulation.setIntakeSpin(operator.getYButton());
            manipulation.setIndexLoad(operator.getXButton());
        }
        if (this.shooterEnabled) {
            shooter.setIndexPower(operator.getBButton() ? 0.25: -operator.getRightTriggerAxis());
            shooter.setSpeed(operator.getLeftTriggerAxis()*0.5);
        }

        logger.log();
        logger.writeLine();
    }

    @Override
    public void disabledInit() {
        if (drivetrainEnabled) {
            drive.setNeutralMode(NeutralMode.Coast);
        }
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
        if (drivetrainEnabled) {
            drive.setNeutralMode(NeutralMode.Coast);
        }
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
