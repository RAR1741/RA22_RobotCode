package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
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
    private double current[] = new double[20];
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

        indexCurrent = 0;
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

    /**
     * Sets the speed of the module.
     *
     * @param speed The speed to set the module to
     */
    public void setSpeed(double speed) {
        main.set(TalonFXControlMode.Velocity, speed / VELOCITY_COEFFICIENT * 6380);
    }

    /**
     * Get the velocity of the module.
     *
     * @return velocity (rpm) of the motor
     */
    public double getSpeed() {
        return main.getSelectedSensorVelocity() * VELOCITY_COEFFICIENT;
    }

    /**
     * Gets the average current drawn.
     *
     * @return The average current drawn by the motors
     */
    // public double getInstantCurrent() {
    // return (main.getStatorCurrent() + sub.getStatorCurrent())/2;
    // }

    /**
     * Gets the average current drawn over a number of cycles.
     *
     * @return The average current drawn by the motor
     */
    // public double getAccumulatedCurrent() {
    // current[indexCurrent] = getInstantCurrent();
    // indexCurrent = (indexCurrent + 1) % current.length;

    // int total = 0;
    // for(int i = 0; i < current.length; i++){
    // total += current[i];
    // }
    // return total/current.length;
    // }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute(this.moduleName + "/MotorPower");
        logger.addAttribute(this.moduleName + "/Distance");
        logger.addAttribute(this.moduleName + "/EncoderRate");
        // logger.addAttribute(this.moduleName + "/MotorCurrent");
    }

    @Override
    public void log(Logger logger) {
        System.out.println(this.moduleName + "/" + this.encoder.getRate());

        logger.log(this.moduleName + "/MotorPower", power);
        logger.log(this.moduleName + "/Distance", this.encoder.getDistance());
        logger.log(this.moduleName + "/EncoderRate", this.encoder.getRate());
        // logger.log(this.moduleName + "/MotorCurrent", getInstantCurrent());
    }
}
