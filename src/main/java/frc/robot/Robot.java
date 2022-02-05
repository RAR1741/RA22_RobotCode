// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
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

    LoggableController driver;
    LoggableController operator;

    LoggablePowerDistribution pdp;

    Climbing climb;

    boolean climberEnabled = true;

    /**
     * This function is run when the robot is first started up and should be used for any 
     * initialization code.
     */
    @Override
    public void robotInit() {
        pdp = new LoggablePowerDistribution(1, ModuleType.kRev);

        driver = new LoggableController("Driver", 0);
        operator = new LoggableController("Operator", 1);

        if (this.climberEnabled) {
            System.out.println("Initializing climber...");
            ClimbingSensors touchA = new ClimbingSensors("TouchA", 0, 1);
            ClimbingSensors touchB = new ClimbingSensors("TouchB", 2, 3);
            ClimbingSensors touchC = new ClimbingSensors("TouchC", 4, 5);
            climb = new Climbing(9, 10, 2, 3, 4, 5, touchA, touchB, touchC);

            logger.addLoggable(touchA);
            logger.addLoggable(touchB);
            logger.addLoggable(touchC);
            logger.addLoggable(climb);
        } else {
            System.out.println("Climber initialization disabled.");
        }

        logger = new Logger();
        timer = new LoggableTimer();

        logger.addLoggable(timer);
        logger.addLoggable(driver);
        logger.addLoggable(operator);
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
        logger.log();
    }

    @Override
    public void testInit() {
        resetLogging();
    }

    @Override
    public void testPeriodic() {
        // Robot code goes here
        if (climberEnabled) {
            climb.setPrestage(operator.getXButtonPressed());
            climb.setPower(operator.getRightY()); //Deadband
            climb.checkClimbingState();
        }

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
