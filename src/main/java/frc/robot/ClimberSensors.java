package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class ClimberSensors implements Loggable {
    DigitalInput b1, b2;

    public ClimberSensors(int b1ID, int b2ID) {
        this.b1 = new DigitalInput(b1ID);
        this.b2 = new DigitalInput(b2ID);
    }

    public boolean getB() {
        return this.b1.get() && this.b2.get();
    }

    @Override
    public void logHeaders(Logger logger) {
        // logger.addHeader("TouchA");
        logger.addHeader("TouchB");
        // logger.addHeader("TouchC");
    }

    @Override
    public void logData(Logger logger) {
        // logger.addData("TouchA", this.getA());
        logger.addData("TouchB", this.getB());
        // logger.addData("TouchC", this.getC());
    }
}
