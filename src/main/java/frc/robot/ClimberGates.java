package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class ClimberGates implements Loggable {
    DigitalInput aL, aR, b1L, b1R, b2L, b2R, cL, cR;

    public ClimberGates(int b1LID, int b1RID, int cLID, int cRID) {// int aLID, int aRID, int b1LID,
                                                                   // int b1RID, int b2LID, int
                                                                   // b2RID, int cLID, int cRID) {
        // this.aL = new DigitalInput(aLID);
        // this.aR = new DigitalInput(aRID);
        this.b1L = new DigitalInput(b1LID);
        this.b1R = new DigitalInput(b1RID);
        // this.b2L = new DigitalInput(b2LID);
        // this.b2R = new DigitalInput(b2RID);
        this.cL = new DigitalInput(cLID);
        this.cR = new DigitalInput(cRID);
    }

    public boolean getA() {
        return this.aL.get() && this.aR.get();
    }

    public boolean getB1() {
        return this.b1L.get() && this.b1R.get();
    }

    public boolean getB2() {
        return this.b2L.get() && this.b2R.get();
    }

    public boolean getC() {
        return this.cL.get() && this.cR.get();
    }

    @Override
    public void setupLogging(Logger logger) {
        // logger.log("GateA", this.getA());
        logger.log("GateB1", this.getB1());
        // logger.log("GateB2", this.getB2());
        logger.log("GateC", this.getC());
    }

    @Override
    public void log(Logger logger) {
        // logger.log("GateA", this.getA());
        logger.log("GateB1", this.getB2());
        // logger.log("GateB2", this.getB2());
        logger.log("GateC", this.getC());
    }
}
