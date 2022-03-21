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
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.Solenoid;
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
    // LoggableGyro gyro;
    Shooter shooter;
    Manipulation manipulation;
    CANSparkMax tempClimber;

    LoggableController driver;
    LoggableController operator;

    LoggablePowerDistribution pdp;
    LoggableCompressor compressor;
    // ClimberGates climberGates;
    LoggableGyro gyro;

    boolean drivetrainEnabled = true;
    boolean tankDriveEnabled = false;
    boolean climberEnabled = false;
    boolean manipulationEnabled = false;
    double shootSpeed = 0.40;

    // private JsonAutonomous auto;

    private static final double DEADBAND_LIMIT = 0.01;
    private static final double SPEED_CAP = 0.45;
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

            // Solenoid climberSolenoidA = new Solenoid(PneumaticsModuleType.REVPH, 2);
            // Solenoid climberSolenoidB1 = new Solenoid(PneumaticsModuleType.REVPH, 3);
            // Solenoid climberSolenoidB2 = new Solenoid(PneumaticsModuleType.REVPH, 4);
            // Solenoid climberSolenoidC = new Solenoid(PneumaticsModuleType.REVPH, 5);

            // // ClimberSensors climberSensors = new ClimberSensors(0, 1, 2, 3, 4, 5);
            // climberGates = new ClimberGates(4, 5, 6, 7);
            // climber = new Climber(9, 10, climberSolenoidA, climberSolenoidB1, climberSolenoidB2,
            // climberSolenoidC, climberGates);// ,gyro, climberSensors);

            // // logger.addLoggable(climberSensors);
            // logger.addLoggable(climberGates);
            // logger.addLoggable(climber);
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

            logger.addLoggable(drive);
        } else {
            System.out.println("Drivetrain initialization disabled.");
        }

        // shooter = new Shooter(new TalonFX(6));

        if (manipulationEnabled) {
            System.out.println("Initializing manipulation...");
            manipulation = new Manipulation(0, 1, 7, 8);
        } else {
            System.out.println("Manipulation initialization disabled.");
        }

        tempClimber = new CANSparkMax(8, MotorType.kBrushless);
        tempClimber.setIdleMode(IdleMode.kBrake);

        System.out.print("Initializing compressor...");
        compressor = new LoggableCompressor(PneumaticsModuleType.REVPH);
        System.out.println("done");

        logger.addLoggable(driver);
        logger.addLoggable(operator);
        logger.addLoggable(compressor);
    }

    @Override
    public void robotPeriodic() {
        // Robot code goes here
        drive.update();
        // climber.update();
    }

    @Override
    public void autonomousInit() {
        gyro.reset();
        timer.reset();

        // auto = new JsonAutonomous("/home/lvuser/deploy/autos/autonomous.json", gyro, drive,
        // shooter,
        // manipulation);
        // System.out.println("Auto Initialized");
        // logger.addLoggable(auto);
        resetLogging();
    }

    @Override
    public void autonomousPeriodic() {
        // Robot code goes here

        if (timer.get() < 2) {
            drive.tankDrive(-0.3, -0.3);
        } else {
            drive.tankDrive(0, 0);
        }

        leftModule.updateCurrent();
        rightModule.updateCurrent();
        System.out.println("running - Robot");
        // auto.run();

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
            // if (tankDriveEnabled) {
            // double leftInput = deadband(-driver.getLeftY());
            // double rightInput = deadband(-driver.getRightY());
            // drive.tankDrive(leftInput, rightInput);
            // } else {
            double turnInput = deadband(driver.getRightX()) * -0.3;
            double speedInput = deadband(driver.getLeftY());
            boost.setScale(driver.getRightTriggerAxis());
            drive.arcadeDrive(turnInput, boost.scale(speedInput));
            // }
            if (driver.getXButtonPressed()) {
                tankDriveEnabled = !tankDriveEnabled;
            }
            // if (driver.getAButtonPressed()) {
            // drive.setShifter(!drive.getShifter());
            // }
            if (driver.getAButtonPressed()) {
                drive.setClimbMode();
            }

            leftModule.updateCurrent();
            rightModule.updateCurrent();
        }

        // if (this.climberEnabled) {
        // double climberInput = deadband(operator.getLeftY());
        // climber.setMotors(climberInput);
        // double tempClimberInput = deadband(operator.getRightY());
        // tempClimber.set(tempClimberInput);
        // if (operator.getLeftBumperPressed()) {
        // climber.setClimbingState(climber.getNextClimbingState());
        // }
        // // climber.checkClimbingState(operator.getAButtonPressed());

        // // TODO: Create a way for motors to go 'limp' when needed, reading loggableGyro
        // if (operator.getRightBumperPressed()) {
        // climber.setMotorState(
        // climber.getMotorState() == MotorStates.ACTIVE ? MotorStates.STATIC
        // : MotorStates.ACTIVE);
        // }

        // if (operator.getBButtonPressed() && climber.getClimberStateId() != 0) {
        // climber.setClimbingState(climber.getPreviousClimbingState());
        // }
        // // TODO: Enable this when we're ready to test the climber
        // }

        double tempClimberInput = deadband(operator.getRightY());
        tempClimber.set(tempClimberInput);

        // System.out.println((climberGates.getB1() ? "B1: true" : "B1: false") + " "
        // + (climberGates.getC() ? "C: true" : "C: false"));

        // if (operator.getYButtonPressed()) {
        // shootSpeed += 0.01;
        // System.out.println(shootSpeed);
        // } else if (operator.getAButtonPressed()) {
        // shootSpeed -= 0.01;
        // System.out.println(shootSpeed);
        // } else if (operator.getBButtonPressed()) {
        // shootSpeed = 0;
        // }

        // shooter.setPower(operator.getRightTriggerAxis());
        // shooter.setPower(operator.getRightTriggerAxis() > 0.5 ? shootSpeed : 0);

        if (this.manipulationEnabled) {
            if (driver.getRightBumperPressed()) {
                manipulation.setIntakeExtend(true);
            } else if (driver.getLeftBumperPressed()) {
                manipulation.setIntakeExtend(false);
            }
            manipulation.setIntakeSpin(operator.getYButton());
            manipulation.setIndexLoad(operator.getLeftTriggerAxis() > 0.5);
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
