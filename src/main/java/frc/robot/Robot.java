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
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  Logger logger;
  LoggableTimer timer;

  LoggableController driver;
  LoggableController operator;

  LoggablePowerDistribution pdp;

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    pdp = new LoggablePowerDistribution(1, ModuleType.kRev);

    driver = new LoggableController("Driver", 0);
    operator = new LoggableController("Operator", 1);

    logger = new Logger();
    timer = new LoggableTimer();

    logger.addLoggable(timer);
    logger.addLoggable(driver);
    logger.addLoggable(operator);
  }

  @Override
  public void robotPeriodic() {
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
