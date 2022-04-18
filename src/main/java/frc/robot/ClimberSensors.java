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
    public void setupLogging(Logger logger) {
        logger.log("TouchB", this.getB());
    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub

    }
}
