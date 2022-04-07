package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class ClimberSensors implements Loggable {
    DigitalInput a1, a2, b1, b2, c1, c2;

    public ClimberSensors(int a1ID, int a2ID, int b1ID, int b2ID, int c1ID, int c2ID) {
        this.a1 = new DigitalInput(a1ID);
        this.a2 = new DigitalInput(a2ID);
        this.b1 = new DigitalInput(b1ID);
        this.b2 = new DigitalInput(b2ID);
        this.c1 = new DigitalInput(c1ID);
        this.c2 = new DigitalInput(c2ID);
    }

    public boolean getA() {
        return this.a1.get() && this.a2.get();
    }

    public boolean getB() {
        return this.b1.get() && this.b2.get();
    }

    public boolean getC() {
        return this.c1.get() && this.c2.get();
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.log("TouchA", this.getA());
        logger.log("TouchB", this.getB());
        logger.log("TouchC", this.getC());
    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub

    }
}
