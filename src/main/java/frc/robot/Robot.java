// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.Climber.MotorStates;
import frc.robot.logging.LoggableCompressor;
import frc.robot.logging.LoggableController;
import frc.robot.logging.LoggablePowerDistribution;
import frc.robot.logging.LoggableTimer;
import frc.robot.logging.Logger;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    Logger logger;
	Timer runTimer;

    LoggableTimer timer;

    Drivetrain drive;
    DriveModule leftModule;
    DriveModule rightModule;

    Climber climber;
    // LoggableGyro gyro;

    LoggableController driver;
    LoggableController operator;

    LoggablePowerDistribution pdp;
    LoggableCompressor compressor;

    boolean drivetrainEnabled = true;
    boolean tankDriveEnabled = true;
    boolean climberEnabled = true;

    private static final double DEADBAND_LIMIT = 0.01;
    private static final double SPEED_CAP = 0.3;
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
        logger = new Logger();
		runTimer = new Timer();

        timer = new LoggableTimer("Timer");
        pdp = new LoggablePowerDistribution(1, ModuleType.kRev);

		try {
			logger.createLog();
		} catch (IOException io) {
			io.printStackTrace();
		}

        driver = new LoggableController("Driver", 0);
        operator = new LoggableController("Operator", 1);

        if (this.climberEnabled) {
            System.out.println("Initializing climber...");

            Solenoid climberSolenoidA = new Solenoid(PneumaticsModuleType.REVPH, 2);
            Solenoid climberSolenoidB1 = new Solenoid(PneumaticsModuleType.REVPH, 3);
            Solenoid climberSolenoidB2 = new Solenoid(PneumaticsModuleType.REVPH, 4);
            Solenoid climberSolenoidC = new Solenoid(PneumaticsModuleType.REVPH, 5);

            ClimberGates climberGates = new ClimberGates(6, 7, 8, 9, 10, 11, 12, 13);
            climber = new Climber(9, 10, climberSolenoidA, climberSolenoidB1, climberSolenoidB2,
                    climberSolenoidC, climberGates);
        } else {
            System.out.println("Climber initialization disabled.");
        }

        if (this.drivetrainEnabled) {
            System.out.println("Initializing drivetrain...");
            leftModule = new DriveModule("LeftDriveModule", 2, 3); // 2, 3
            leftModule.setEncoder(2, 3, false);

            rightModule = new DriveModule("RightDriveModule", 4, 5); // 4, 5
            rightModule.setEncoder(0, 1, true);

            drive = new Drivetrain(leftModule, rightModule, 6);
        } else {
            System.out.println("Drivetrain initialization disabled.");
        }

        System.out.print("Initializing compressor...");
        compressor = new LoggableCompressor(PneumaticsModuleType.REVPH);

        logger.addLoggable(pdp);
        logger.addLoggable(climber);
        logger.addLoggable(drive);
        logger.addLoggable(timer);
        logger.addLoggable(driver);
        logger.addLoggable(operator);
        logger.addLoggable(compressor);

		logger.collectHeaders();
		try {
            logger.writeData("Initiation");
		} catch (IOException io) {
			io.printStackTrace();
		}

        System.out.println("Starting logger...");
		runTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.collectData();
                try {
                    logger.writeData(null);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }, 0, 33);
    }

    @Override
    public void robotPeriodic() {
        // Robot code goes here
        drive.update();
        climber.update();
    }

    @Override
    public void autonomousInit() {
        try {
            logger.writeData("Autonomous");
            logger.writeHeaders();
        } catch (IOException io) {
            io.printStackTrace();
        }

        resetTimer();
    }

    @Override
    public void autonomousPeriodic() {
        // Robot code goes here
    }

    @Override
    public void teleopInit() {
        try {
            logger.writeData("TeleOp");
            logger.writeHeaders();
        } catch (IOException io) {
            io.printStackTrace();
        }

        resetTimer();
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

        if (this.climberEnabled) {
            double climberInput = deadband(operator.getLeftY());
            climber.setMotors(climberInput);
            if (operator.getLeftBumperPressed()) {
                climber.setClimbingState(climber.getNextClimbingState());
            }
            // climber.checkClimbingState(operator.getAButtonPressed());

            // TODO: Create a way for motors to go 'limp' when needed, reading loggableGyro
            if (operator.getRightBumperPressed()) {
                climber.setMotorState(
                        climber.getMotorState() == MotorStates.ACTIVE ? MotorStates.STATIC
                                : MotorStates.ACTIVE);
            }
            // TODO: Enable this when we're ready to test the climber
        }
    }

    @Override
    public void disabledInit() {
        try {
            logger.writeData("Disabled");
            logger.writeHeaders();
        } catch (IOException io) {
            io.printStackTrace();
        }

        timer.stop();
		resetTimer();
    }

    @Override
    public void disabledPeriodic() {
        // Robot code goes here
    }

    @Override
    public void testInit() {
        try {
            logger.writeData("Testmode");
            logger.writeHeaders();
        } catch (IOException io) {
            io.printStackTrace();
        }
        
        resetTimer();
    }

    @Override
    public void testPeriodic() {
        // Robot code goes here
        // if (climberEnabled) {
        // climber.setPrestage(operator.getXButtonPressed());
        // climber.setPower(operator.getRightY()); // Deadband
        // climber.checkClimbingState();
        // }
    }

    private void resetTimer() {
        timer.reset();
        timer.start();
    }
}
