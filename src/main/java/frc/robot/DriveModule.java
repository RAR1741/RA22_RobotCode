package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import edu.wpi.first.wpilibj.Encoder;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class DriveModule implements Loggable {

    private final double VELOCITY_COEFFICIENT = 600 / 2048;

    private TalonFX main;
    private TalonFX sub;
    private String moduleName;
    private Encoder encoder;

    private double power;
    private double[] current = new double[30];
    private int indexCurrent;

    /**
     * Constructor.
     *
     * @param moduleName Name of the attribute to log speed
     * @param mainID CAN id of the main TalonFX
     * @param subID CAN id of the sub TalonFX
     */
    DriveModule(String moduleName, int mainID, int subID) {
        this.moduleName = moduleName;
        this.main = new TalonFX(mainID);
        this.sub = new TalonFX(subID);

        this.main.setNeutralMode(NeutralMode.Coast);
        this.sub.setNeutralMode(NeutralMode.Coast);

        this.sub.follow(this.main);

        main.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0, 30);
        main.config_kP(0, 0.1);
        main.config_kI(0, 0.001);
        main.config_kD(0, 5);

        indexCurrent = 0;
    }

    public void update() {
        updateCurrent();
    }

    public void setEncoder(int encoderPortA, int encoderPortB, boolean reverseDirection) {
        this.encoder = new Encoder(encoderPortA, encoderPortB);

        // 6in wheel, 3:1 encoder ratio, 360 CPR
        this.encoder.setDistancePerPulse((Math.PI * 6 * 3) / 360.0);
        this.encoder.setReverseDirection(reverseDirection);
    }

    /**
     * Inverts the module.
     *
     * @param isInverted True if the module should be inverted; false if not
     */
    public void setInverted(boolean isInverted) {
        main.setInverted(isInverted);
        sub.setInverted(isInverted);
    }

    /**
     * Sets the power of the module.
     *
     * @param input The power to set the module to
     */
    public void set(double input) {
        this.power = input;
        main.set(TalonFXControlMode.PercentOutput, input);
    }

    public void setNeutralMode(NeutralMode mode) {
        this.main.setNeutralMode(mode);
        this.sub.setNeutralMode(mode);
    }

    /**
     * Sets the speed of the module.
     *
     * @param speed The speed to set the module to
     */
    public void setSpeed(double speed) {
        main.set(TalonFXControlMode.Velocity, speed * 22000);
    }

    public void setPower(double power) {
        main.set(TalonFXControlMode.PercentOutput, power);
    }

    /**
     * Get the velocity of the module.
     *
     * @return velocity (rpm) of the motor
     */
    public double getSpeed() {
        return main.getSelectedSensorVelocity();// * VELOCITY_COEFFICIENT;
    }

    /**
     * Get the main sensor position (in raw sensor units).
     * 
     * @return Position of selected sensor (in raw sensor units).
     */
    public double getDriveEnc() {
        return main.getSelectedSensorPosition();
    }
 
    /**
     * Gets the average current drawn.
     *
     * @return The average current drawn by the motors
     */
    public double getCurrent() {
        return (main.getStatorCurrent() + sub.getStatorCurrent()) / 2;
    }

    /**
     * Updates the average current drawn for this cycle.
     */
    public void updateCurrent() {
        current[indexCurrent] = getCurrent();
        indexCurrent = (indexCurrent + 1) % current.length;
    }

    /**
     * Gets the average current drawn over a number of cycles.
     *
     * @return The average current drawn by the motor
     */
    public double getAverageCurrent() {
        double total = 0.0;
        for (int i = 0; i < current.length; i++) {
            total += current[i];
        }
        return total / current.length;
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute(this.moduleName + "/MotorPower");
        logger.addAttribute(this.moduleName + "/Distance");
        logger.addAttribute(this.moduleName + "/EncoderRate");
        logger.addAttribute(this.moduleName + "/MotorVelocity");
        logger.addAttribute(this.moduleName + "/MotorCurrent");
        logger.addAttribute(this.moduleName + "/MotorAverageCurrent");
    }

    @Override
    public void log(Logger logger) {
        logger.log(this.moduleName + "/MotorPower", power);
        logger.log(this.moduleName + "/Distance", this.encoder.getDistance());
        logger.log(this.moduleName + "/EncoderRate", this.encoder.getRate());
        logger.log(this.moduleName + "/MotorVelocity", getSpeed());
        logger.log(this.moduleName + "/MotorCurrent", getCurrent());
        logger.log(this.moduleName + "/MotorAverageCurrent", getAverageCurrent());
    }
}
