package frc.robot.logging;

import edu.wpi.first.wpilibj.PowerDistribution;

public class LoggablePowerDistribution extends PowerDistribution implements Loggable {
    public LoggablePowerDistribution() {
        super();
    }

    public LoggablePowerDistribution(int module, ModuleType moduleType) {
        super(module, moduleType);
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("PDH/voltage", this::getVoltage, null);
    }

    @Override
    public void log(Logger logger) {
    }

}
