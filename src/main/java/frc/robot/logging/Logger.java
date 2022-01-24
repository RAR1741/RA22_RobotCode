package frc.robot.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.function.BooleanConsumer;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Manages NetworkTable and file logging. */
public class Logger {
    private static final String TABLE_PREFIX = "Logging";
    private String filename;
    private BufferedWriter log = null;
    private Map<String, Supplier<String>> suppliers;
    private Map<String, String> fields;
    private List<Loggable> loggables;
    @Deprecated(forRemoval = true)
    private NetworkTable table;

    public Logger() {
        fields = new LinkedHashMap<String, String>();
        loggables = new ArrayList<>();
        table = NetworkTableInstance.getDefault().getTable("logging");
        for (String s : table.getKeys()) {
            table.delete(s);
        }
    }

    /**
     * Opens a file with the name being the current date and time to log to.
     * @return Whether opening the file succeeded
     */
    public boolean open() {
        Calendar calendar = Calendar.getInstance();
        String dir = "/home/lvuser/logs";
        new File(dir).mkdirs();
        if (new File("/media/sda").exists()) {
            dir = "/media/sda";
        }
        String name = dir + "/log-" + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.HOUR_OF_DAY) + "-"
                + calendar.get(Calendar.MINUTE) + "-" + calendar.get(Calendar.SECOND) + ".csv";
        System.out.printf("Logging to file: '%s'%n", new File(name).getAbsolutePath());
        return this.open(name);
    }

    /**
     * Opens a file to log to.
     * @param filepath Path of the file to open
     * @return Whether opening the file succeeded
     */
    public boolean open(String filepath) {
        this.filename = filepath;
        try {
            log = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Closes the current log file.
     * @return Whether closing the file succeeded
     */
    public boolean close() {
        if (log != null) {
            try {
                log.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Resets the current log file.
     * @return true
     */
    public boolean reset() {
        close();
        open(this.filename);
        writeAttributes();
        return true;
    }

    /**
     * Checks to see if the logger already has a specific key.
     * @param name Key to check
     * @return Whether the key already exists
     */
    public boolean hasAttribute(String name) {
        return fields.containsKey(name);
    }

    /**
     * Adds an attribute to the logger.
     * @param field The key to add to the logger
     * @return Whether the attribute was added
     */
    public boolean addAttribute(String field) {
        if (hasAttribute(field)) {
            System.out.printf("Attribute \"%s\" already exists! Skipping!%n", field);
            return false; // We already have this attribute
        }
        fields.put(field, "");

        return true;
    }

    /**
     * Adds an attribute to the logger.
     * @param field The key to add to the logger
     * @param getter The function to get the value of the attribute
     * @param setter The function to set the value of the attribute
     * @return Whether the attribute was added
     */
    public boolean addAttribute(String field, Supplier<String> getter, Consumer<String> setter) {
        if (hasAttribute(field)) {
            System.out.printf("Attribute \"%s\" already exists! Skipping!%n", field);
            return false; // We already have this attribute
        }
        SmartDashboard.putData((SendableBuilder builder) -> {
            builder.setSmartDashboardType(TABLE_PREFIX);
            builder.addStringProperty(field, getter != null ? getter::get : null,
                    setter != null ? setter::accept : null);
        });

        suppliers.put(field, getter::get);
        fields.put(field, "");

        return true;
    }

    /**
     * Adds an attribute to the logger.
     * @param field The key to add to the logger
     * @param getter The function to get the value of the attribute
     * @param setter The function to set the value of the attribute
     * @return Whether the attribute was added
     */
    public boolean addAttribute(String field, DoubleSupplier getter, DoubleConsumer setter) {
        if (hasAttribute(field)) {
            System.out.printf("Attribute \"%s\" already exists! Skipping!%n", field);
            return false; // We already have this attribute
        }
        SmartDashboard.putData((SendableBuilder builder) -> {
            builder.setSmartDashboardType(TABLE_PREFIX);
            builder.addDoubleProperty(field, getter != null ? getter::getAsDouble : null,
                    setter != null ? setter::accept : null);
        });

        suppliers.put(field, () -> Double.toString(getter.getAsDouble()));
        fields.put(field, "");

        return true;
    }

    /**
     * Adds an attribute to the logger.
     * @param field The key to add to the logger
     * @param getter The function to get the value of the attribute
     * @param setter The function to set the value of the attribute
     * @return Whether the attribute was added
     */
    public boolean addAttribute(String field, BooleanSupplier getter, BooleanConsumer setter) {
        if (hasAttribute(field)) {
            System.out.printf("Attribute \"%s\" already exists! Skipping!%n", field);
            return false; // We already have this attribute
        }
        SmartDashboard.putData((SendableBuilder builder) -> {
            builder.setSmartDashboardType(TABLE_PREFIX);
            builder.addBooleanProperty(field, getter != null ? getter::getAsBoolean : null,
                    setter != null ? setter::accept : null);
        });

        suppliers.put(field, () -> Boolean.toString(getter.getAsBoolean()));
        fields.put(field, "");

        return true;
    }

    /**
     * Logs data to the Logger.
     * @deprecated Use {@link #addAttribute(String, DoubleSupplier, DoubleConsumer)} instead
     * @param field Key being logged
     * @param data Number data to log
     * @return Whether the operation succeeded
     */
    @Deprecated(forRemoval = true)
    public boolean log(String field, double d) {
        if (!hasAttribute(field))
            return false;
        fields.put(field, Double.toString(d));
        return true;
    }

    /**
     * Logs data to the Logger
     * @deprecated Use {@link #addAttribute(String, Supplier<String>, Consumer<String>)} instead
     * @param field key being logged
     * @param data String data to log
     * @return whether the operation succeeded
     */
    @Deprecated(forRemoval = true)
    public boolean log(String field, String data) {
        if (!hasAttribute(field))
            return false;

        fields.put(field, data);
        return true;
    }

    /**
     * Logs data to the Logger
     * @deprecated Use {@link #addAttribute(String, Supplier, Consumer)} instead
     * @param field key being logged
     * @param data data to log
     * @return whether the operation succeeded
     */
    @Deprecated(forRemoval = true)
    public boolean log(String field, Object data) {
        if (!hasAttribute(field))
            return false;

        table.getEntry(field).setValue(data);
        fields.put(field, data.toString());
        return true;
    }

    /**
     * Writes the headers to the file.
     * @return Whether the operation succeeded
     */
    public boolean writeAttributes() {
        try {
            for (Map.Entry<String, String> e : fields.entrySet()) {
                log.write(e.getKey() + ',');
            }
            log.write("\n");
            log.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Writes the current values to the file.
     * @return Whether the operation succeeded
     */
    public boolean writeLine() {
        try {
            for (Map.Entry<String, String> e : fields.entrySet()) {
                log.write(e.getValue() + ',');
            }
            log.write("\n");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Normalizes the name of a key.
     * @param str key name to normalize
     * @return normalized key name
     */
    String normalize(String str) {
        return str.toLowerCase();
    }

    /**
     * Registers a Loggable with the Logger.
     * @param l loggable to register
     */
    public void addLoggable(Loggable l) {
        loggables.add(l);
    }

    /**
     * Calls the setupLogging method of all currently registered Loggables.
     */
    public void setupLoggables() {
        for (Loggable l : loggables) {
            l.setupLogging(this);
        }
    }

    /**
     * Sets up all currently registered Loggables, along with writing the header to the file.
     */
    public void setup() {
        this.setupLoggables();
        this.writeAttributes();
    }

    /**
     * Calls the log method of all currently registered Loggables.
     */
    public void log() {
        for (Map.Entry<String, Supplier<String>> entry : this.suppliers.entrySet()) {
            this.fields.put(entry.getKey(), entry.getValue().get());
        }
        for (Loggable l : loggables) {
            l.log(this);
        }
    }
}
