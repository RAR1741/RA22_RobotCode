// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.Climber.MotorStates;
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

    Climber climber;
    LoggableGyro gyro;
    Manipulation manipulation;
    CANSparkMax tempClimber;

    LoggableController driver;
    LoggableController operator;

    LoggablePowerDistribution pdp;
    LoggableCompressor compressor;
    ClimberSensors climberSensors;

    boolean drivetrainEnabled = true;
    boolean climberEnabled = true;
    boolean tempClimberEnabled = false;
    boolean manipulationEnabled = true;

    private JsonAutonomous auto;

    private static final double DEADBAND_LIMIT = 0.01;
    private static final double SPEED_CAP = 0.5;
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
        timer = new LoggableTimer();
        logger.addLoggable(timer);
        gyro = new LoggableGyro();
        gyro.enableLogging(false);

        pdp = new LoggablePowerDistribution(1, ModuleType.kRev);

        driver = new LoggableController("Driver", 0);
        operator = new LoggableController("Operator", 1);

        if (this.climberEnabled) {
            System.out.println("Initializing climber...");

            Solenoid climberSolenoidA = new Solenoid(PneumaticsModuleType.REVPH, 1);
            Solenoid climberSolenoidB1 = new Solenoid(PneumaticsModuleType.REVPH, 2);
            Solenoid climberSolenoidB2 = new Solenoid(PneumaticsModuleType.REVPH, 3);
            Solenoid climberSolenoidC = new Solenoid(PneumaticsModuleType.REVPH, 4);

            // climberSensors = new ClimberSensors(0, 0); // TODO: Add sensors and input ids
            climber = new Climber(10, 9, climberSolenoidA, climberSolenoidB1, climberSolenoidB2,
                    climberSolenoidC);

            // logger.addLoggable(climberSensors);
            logger.addLoggable(climber);
        } else {
            System.out.println("Climber initialization disabled.");
        }

        gyro = new LoggableGyro();
        gyro.enableLogging(false);

        System.out.print("Initializing compressor...");
        compressor = new LoggableCompressor(2, PneumaticsModuleType.REVPH);
        System.out.println("done");

        if (this.drivetrainEnabled) {
            System.out.println("Initializing drivetrain...");
            leftModule = new DriveModule("LeftDriveModule", 3, 2);
            leftModule.setEncoder(2, 3, false);

            rightModule = new DriveModule("RightDriveModule", 4, 5);
            rightModule.setEncoder(0, 1, true);

            drive = new Drivetrain(leftModule, rightModule, 0, gyro);
            drive.setNeutralMode(NeutralMode.Coast);

            logger.addLoggable(drive);
        } else {
            System.out.println("Drivetrain initialization disabled.");
        }

        if (manipulationEnabled) {
            System.out.println("Initializing manipulation...");
            manipulation = new Manipulation(5, 6, 7, 8);
        } else {
            System.out.println("Manipulation initialization disabled.");
        }

        if (tempClimberEnabled) {
            System.out.println("Initializing temporary climber...");
            tempClimber = new CANSparkMax(8, MotorType.kBrushless);
            tempClimber.setIdleMode(IdleMode.kBrake);
        } else {
            System.out.println("Temporary climber initialization disabled.");
        }

        System.out.print("Initializing compressor...");
        compressor = new LoggableCompressor(PneumaticsModuleType.REVPH);
        System.out.println("done");

        logger.addLoggable(driver);
        logger.addLoggable(operator);
        logger.addLoggable(compressor);
        logger.addLoggable(gyro);
    }

    @Override
    public void robotPeriodic() {
        // Robot code goes here
        if (drivetrainEnabled) {
            drive.update();
        }
        if (climberEnabled) {
            climber.update();
        }
    }

    @Override
    public void autonomousInit() {
        gyro.reset();

        if (drivetrainEnabled) {
            drive.setNeutralMode(NeutralMode.Brake);
        }
        auto = new JsonAutonomous(JsonAutonomous.getAutoPath("billiards-one.json"), gyro, drive, manipulation);
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
            double turnInput = deadband(driver.getRightX()) * -0.3;
            double speedInput = deadband(-driver.getLeftY());
            boost.setScale(driver.getRightTriggerAxis());
            drive.arcadeDrive(turnInput, boost.scale(speedInput));

            drive.setShifter(driver.getLeftTriggerAxis() < 0.5);

            drive.setClimbMode(driver.getAButton());
            // if (driver.getBButtonPressed()) {
            // drive.toggleAutoBalance();
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

        if (this.climberEnabled) {
            double climberInput = deadband(operator.getLeftY() * 0.8);
            climber.setMotors(climberInput);
            if (operator.getLeftBumperPressed()) {
                climber.setClimbingState(climber.getNextClimbingState());
            }

            // TODO: Create a way for motors to go 'limp' when needed, reading loggableGyro
            if (operator.getRightBumperPressed()) {
                climber.setMotorState(MotorStates.STATIC);
            }

            if (operator.getBButtonPressed() && climber.getClimberStateId() != 0) {
                climber.setClimbingState(climber.getPreviousClimbingState());
            }
        }

        if (tempClimberEnabled) {
            double tempClimberInput = deadband(operator.getRightY());
            tempClimber.set(tempClimberInput);
        }

        if (this.manipulationEnabled) {
            manipulation.setIntakeExtend(driver.getLeftBumper());

            if (operator.getYButton()) {
                manipulation.setCollect();
            } else if (operator.getXButton()) {
                manipulation.setSlowEject();
            } else if (operator.getAButton()) {
                manipulation.setEject();
            } else {
                manipulation.setCollection(0,0);
            }
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
        // if (climberEnabled) {
        // climber.setPrestage(operator.getXButtonPressed());
        // climber.setPower(operator.getRightY()); // Deadband
        // climber.checkClimbingState();
        // }

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
