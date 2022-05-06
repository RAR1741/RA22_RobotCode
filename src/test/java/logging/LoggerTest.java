package logging;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import frc.robot.logging.Logger;

public class LoggerTest {
    /**
     * Tests {@link Logger} generates a new log filename correctly
     */
    @Test
    public void newLogFilenameTest() {
        Calendar testDate = new GregorianCalendar(2022, GregorianCalendar.FEBRUARY, 19,
            14, 20, 30);

        assertEquals("log-2022-02-19_14-20-30.csv", Logger.getFilename(testDate));
    }
}
