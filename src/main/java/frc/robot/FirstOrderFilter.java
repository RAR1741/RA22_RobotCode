package frc.robot;

public class FirstOrderFilter {
    private double[] previousValues;
    private int index = 0;
    private int range;

    public FirstOrderFilter(int range) {
        this.range = range;
        this.previousValues = new double[range];
    }

    public double update(double value) {
        this.previousValues[this.index] = this.value;
        this.index = (this.index + 1) % this.range;
        return value;
    }

    public double get() {
        double total = 0.0;
        for (int i = 0; i < range; i++) {
            total += this.previousValues[i];
        }
        return total / this.range;
    }
}
