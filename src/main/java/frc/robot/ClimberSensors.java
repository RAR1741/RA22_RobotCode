package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class ClimberSensors implements Loggable {
    String name;
    DigitalInput a, b;

    public ClimberSensors(String name, int aID, int bID) {
        this.name = name;
        this.a = new DigitalInput(aID);
        this.b = new DigitalInput(bID);
    }

    public boolean get() {
        return this.a.get() && this.b.get();
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.log(this.name, this.get());

    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub

    }
}
