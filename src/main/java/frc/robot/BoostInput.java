
package frc.robot;

public class BoostInput implements InputScaler {
    private final double powerCap;
    private double boostScale;

    public BoostInput(double powerCap) {
        this.powerCap = powerCap;
        this.boostScale = 0;
    }

    public double scale(double input) {
        double scale = (1 - powerCap) * boostScale;
        return input * (powerCap + scale);
    }

    public void setScale(double boostScale) {
        this.boostScale = boostScale;
    }
}
