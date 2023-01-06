package frc.robot.parsing;

import java.io.*;
import java.util.ArrayList;

public class JsonParser {

    private final ArrayList<String> LINES = new ArrayList<>();
    private final String[] BREAK_WORDS = {"type", "unit", "amount"};

    private final String path;

    public String type;
    public String unit;
    public Double amount;
    public ArrayList<Double> args = new ArrayList<>();

    public int instructionSize = 0;

    /**
     * Creates a parser to convert json data into data java classes can read
     *
     * @param path the path to the json instruction file
     */

    public JsonParser(String path) {
        this.path = path;
    }

    /**
     * Parses each autonomous instruction from the json file into java-readable data
     *
     * @param step the current instruction to parse (starting at 0). Step -1 initializes the parser
     */
    public void parse(int step) {
        int currentStep = -1;
        if (LINES.isEmpty()) {
            FileReader file = null;
            try {
                file = new FileReader(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BufferedReader reader;
            assert file != null;
            reader = new BufferedReader(file);

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    LINES.add(line);
                }

                for (int i = 0; i < LINES.size(); i++) {
                    LINES.set(i, LINES.get(i).replaceAll("[^a-zA-Z0-9.-]", ""));
                    if (LINES.get(i).equals("")) {
                        continue;
                    }
                    for (String j : BREAK_WORDS) {
                        if (LINES.get(i).contains(j)) {
                            LINES.set(i, LINES.get(i).replaceAll(j, j + " "));
                        }
                    }
                }

                for (String s : LINES) {
                    if (s.contains("type")) {
                        instructionSize++;
                    }
                }

                if(step != -1) {
                    for (int i = 0; i < LINES.size(); i++) {
                        if (LINES.get(i).contains("type")) {
                            currentStep++;
                        }
                        if (currentStep == step) {
                            type = LINES.get(i).replaceAll("type ", "");
                            unit = LINES.get(i + 1).replaceAll("unit ", "");
                            amount = LINES.get(i + 2).equals("") ? null : Double.parseDouble(LINES.get(i + 2).replaceAll("amount ", ""));
                            int j = i + 4;
                            while (!LINES.get(j).contains("type") && !LINES.get(j).equals("")) {
                                args.add(Double.valueOf(LINES.get(j)));
                                j++;
                            }
                            break;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            type = null;
            unit = null;
            amount = null;
            args.clear();
            for (int i = 0; i < LINES.size(); i++) {
                if (LINES.get(i).contains("type")) {
                    currentStep++;
                }
                if (currentStep == step) {
                    type = LINES.get(i).replaceAll("type ", "");
                    unit = LINES.get(i + 1).replaceAll("unit ", "");
                    amount = LINES.get(i + 2).equals("") ? null : Double.parseDouble(LINES.get(i + 2).replaceAll("amount ", ""));
                    int j = i + 4;
                    while(!LINES.get(j).contains("type") && !LINES.get(j).equals("")) {
                        args.add(Double.valueOf(LINES.get(j)));
                        j++;
                    }
                    break;
                }
            }
        }
    }

}