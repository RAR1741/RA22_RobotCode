package frc.robot;

import java.util.List;

import com.google.gson.JsonElement;

public class JsonAutonomous { //extends Autonomous{

    private final static double TICKS_PER_ROTATION = 1; //TODO: Get values
    private final static double TICKS_PER_INCH = 1;
    private JsonElement auto;
    private List<AutoInstruction> instructions;
    private int step;
    private Timer timer;
    private double start;
    private double navxStart;
    private AHRS gyro;

    private FileReader fr;
    private JsonReader jr;
    private JsonParser jp;


    public static class AutoInstruction
    {
        public String type;
        public Unit unit;
        public double amount;
        public List<Double> args;

        
    
        public enum Unit { Seconds, Milliseconds, EncoderTicks, Rotations, Inches, Feet, Degrees, Invalid }

        public AutoInstruction(String type, List<Double> args)
        {
            this.type = type;
            this.args = args;
        }

        public AutoInstruction(String type, Unit unit, double amount, List<Double> args)
        {
            this.type = type;
            this.unit = unit;
            this.amount = amount;
            this.args = args;
        }
    }

    /**
     * Creates a JsonAutonomous from the specified file
     * @param file The location of the file to parse
    */
    public JsonAutonomous(String file, AHRS gyro, SwerveDrive drive) {
        this.swerve = drive;
        this.gyro = gyro;

        parseFile(file);
    }

    public void parseFile(String file)
    {
        step = -1;
        timer = new Timer();
        instructions = new ArrayList<AutoInstruction>();
        try
        {
            fr = new FileReader(file);
            jr = new JsonReader(fr);
            auto = JsonParser.parseReader(jr);
            JsonElement inner = auto.getAsJsonObject().get("auto");
            if(inner.isJsonArray())
            {
                for(JsonElement e : inner.getAsJsonArray())
                {
                    JsonObject o = e.getAsJsonObject();

                    List<Double> extraArgs = new ArrayList<Double>();
                    for(JsonElement e2 : o.get("args").getAsJsonArray())
                    {
                        extraArgs.add(e2.getAsDouble());
                    }

                    String type = o.get("type").getAsString();

                    String unitString = o.has("unit") ? o.get("unit").getAsString() : null;
                    AutoInstruction.Unit unit = unitString != null ? parseUnit(unitString) : null;

                    double amount = o.has("amount") ? o.get("amount").getAsDouble() : null;

                    AutoInstruction ai = unit == null ? new AutoInstruction(type, extraArgs) : new AutoInstruction(type, unit, amount, extraArgs);
                    instructions.add(ai);
                }
            }
        }
        catch (JsonIOException | JsonSyntaxException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static AutoInstruction.Unit parseUnit(String in)
    {
        return AutoInstruction.Unit.valueOf(in);
    }

    @Override
    public void run(){
        if(step == -1)
        {
            reset();
        }
        if(instructions.size() == step)
        {
            swerve.swerve(0, 0, 0, getAngle()-navxStart, false);
            return;
        }
        AutoInstruction ai = instructions.get(step);

        switch (ai.type) {
            case "drive":
                drive(ai, false);
                break;

            case "drive-fo":
                drive(ai, true);
                break;

            case "turnDeg":
                turnDegrees(ai, false);
                break;

            case "turnDeg-fo":
                turnDegrees(ai, true);
                break;

            case "wait":
                wait(ai);
                break;

            default:
                System.out.println("Invalid Command");
                reset();
                break;
        }
    }
    
}