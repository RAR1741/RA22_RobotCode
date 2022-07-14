package frc.robot.logging;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Logger {
	private BufferedWriter logger;
	public Map<String, String> logData;
	private List<Loggable> loggables;
	private String logName;

	public Logger() {
		logData = new HashMap<String, String>();
		loggables = new ArrayList<>();
	}

	/**
	* Creates a new log file.
	*
	* @throws IOException
	*/
	public void createLog() throws IOException {
		Calendar calendar = Calendar.getInstance();
        String dir = "\\home\\lvuser\\logs";
		Path path = Paths.get(dir + "\\" + calendar.get(Calendar.YEAR) + "-"
		+ (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "-"
		+ calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE) + "-"
		+ calendar.get(Calendar.SECOND) + ".csv");
		
		logName = Files.createFile(path).toString();
		logger = new BufferedWriter(new FileWriter(logName, true));
	}

	/**
	* Creates a header that values can be added under.
	*
	* @param header The name of the header to be created.
	*/
	public void addHeader(String header) {
		if (hasHeader(header)) {
			System.err.println("The header \"" + header + "\" already exists.");
		}
		else {
			logData.put(header, null);
		}
	}

	/**
	* Adds a value under the specified header.
	*
	* @param header The name of the header that the value will be put under.
	* @param data The value to be added under the header.
	*/
	public void addData(String header, Object data) {
		if (!hasHeader(header)) {
			System.err.println("The header \"" + header + "\" does not exist.");
		}
		else {
			if (hasData(header)) {
				System.err.println("The header \"" + header + "\" already has data.");
			}
			else {
				logData.put(header, data.toString());
			}
		}
	}

	/**
	* Writes all the headers created to the log file.
	*
	* @throws IOException
	*/
	public void writeHeaders() throws IOException {
		for (String header: logData.keySet()) {
			logger.write(header + ",");
		}

		logger.newLine();

		logger.close();
		logger = new BufferedWriter(new FileWriter(logName, true));
	}

	/**
	* Writes all the data added to the log file.
	*
	* @throws IOException
	*/
	public void writeData() throws IOException {
		for (Map.Entry<String, String> data: logData.entrySet()) {
			logger.write(data.getValue() + ",");
			logData.put(data.getKey(), null);
		}

		logger.newLine();

		logger.close();
		logger = new BufferedWriter(new FileWriter(logName, true));
	}

	/**
	* Creates a new Loggable object.
	*/
	public void addLoggable(Loggable loggable) {
		loggables.add(loggable);
	}

	/**
	* Logs all headers.
	*/
	public void logAllHeaders() {
		for (Loggable loggable: loggables) {
			loggable.logHeaders(this);
		}
	}

	/**
	* Logs all data.
	*/
	public void logAllData() {
		for (Loggable loggable: loggables) {
			loggable.logData(this);
		}
	}

	/**
	 * Sends all accumulated data to log file.
	 */
	public void logAll() {
		logAllData();
        try {
			writeData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	* Checks if the header being created exists already.
	*
	* @param header The header that trying to be created.
	*
	* @return Whether the header exists already or not.
	*/
	private boolean hasHeader(String header) {
		try {
			return logData.containsKey(header);
		}
		catch (NullPointerException exception) {
			return false;
		}
    }

	/**
	* Checks if the header already has current data under it.
	*
	* @param header The header being checked for data.
	*
	* @return Whether the header has data already or not.
	*/
	private boolean hasData(String header) {
		try {
			if (logData.get(header) == null) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (NullPointerException exception) {
			return false;
		}
	}
}