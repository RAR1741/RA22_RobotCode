package frc.robot.logging;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

public class LoggableCompressor extends Compressor implements Loggable {
    public LoggableCompressor(int module, PneumaticsModuleType moduleType) {
        super(module, moduleType);
    }

    public LoggableCompressor(PneumaticsModuleType moduleType) {
        super(moduleType);
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("PH/pressure");
    }

    @Override
    public void log(Logger logger) {
        // logger.log("PH/pressure", this.getPressure());
    }
}
