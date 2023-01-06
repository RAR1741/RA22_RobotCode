package frc.robot;

import edu.wpi.first.wpilibj.Filesystem;
import frc.robot.JsonAutonomous.AutoInstruction.Unit;
import frc.robot.logging.Loggable;
import frc.robot.logging.LoggableGyro;
import frc.robot.logging.LoggableTimer;
import frc.robot.logging.Logger;
import frc.robot.parsing.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonAutonomous extends Autonomous implements Loggable {

    private static final double TICKS_PER_ROTATION = 16750; // TODO: Update value for 2022 robot
    private static final double TICKS_PER_INCH = TICKS_PER_ROTATION / (6 * Math.PI); // TODO: Update
                                                                                     // formula for
                                                                                     // 2022 robot
    private static final double SHOOTER_SPEED = -1;
    private List<AutoInstruction> instructions;
    private int step;
    private LoggableTimer timer;
    private double start;
    private double navxStart;
    private LoggableGyro gyro;

    private Drivetrain drive;

    private Shooter shooter;
    private Manipulation manipulation;

    public static class AutoInstruction {
        public String type;
        public Unit unit;
        public Double amount;
        public List<Double> args;

        public enum Unit {
            SECONDS, MILLISECONDS, ENCODER_TICKS, ROTATIONS, INCHES, FEET, CURRENT, DEGREES, SPEED, POWER, MOTOR, NOTMOTOR, INVALID
        }

        public AutoInstruction(String type, List<Double> args) {
            this.type = type;
            this.args = args;
        }

        public AutoInstruction(String type, Unit unit, Double amount, List<Double> args) {
            this.type = type;
            this.unit = unit;
            this.amount = amount;
            this.args = args;
        }

        public String toString() {
            return String.format("%s <%s %s>%n", this.type, this.amount, this.unit);
        }
    }

    /**
     * Creates a JsonAutonomous from the specified file
     * 
     * @param file The location of the file to parse
     */
    public JsonAutonomous(String file, LoggableGyro gyro, Drivetrain drive, Shooter shooter,
            Manipulation manipulation) {
        this.drive = drive;
        this.gyro = gyro;
        this.shooter = shooter;
        this.manipulation = manipulation;

        parseFile(file);
    }

    public void parseFile(String file) {
        step = -1;
        timer = new LoggableTimer();
        instructions = new ArrayList<>();
        JsonParser parser = new JsonParser(file);
        parser.parse(-1);
        for(int i = 0; i < parser.instructionSize; i++) {
            parser.parse(i);

            Double[] args = parser.args.toArray(new Double[0]);

            String type = parser.type;

            String unitString = parser.unit;
            AutoInstruction.Unit unit = unitString != null ? parseUnit(unitString) : null;

            Double amount = parser.amount;

            AutoInstruction ai = unit == null ? new AutoInstruction(type, Arrays.asList(args))
                    : new AutoInstruction(type, unit, amount, Arrays.asList(args));
            instructions.add(ai);
        }
    }

    public static AutoInstruction.Unit parseUnit(String in) {
        return AutoInstruction.Unit.valueOf(in);
    }

    @Override
    public void run() {
        if (step == -1) {
            reset();
        }
        if (instructions.size() == step) {
            drive.drive(0, 0, false);
            return;
        }
        AutoInstruction ai = instructions.get(step);

        switch (ai.type) {
            case "drive":
                drive(ai);
                break;

            case "turnDeg":
                turnDegrees(ai);
                break;

            case "wait":
                wait(ai);
                break;

            case "shoot":
                shoot(ai);
                break;

            case "intake":
                intake(ai);
                break;
            
            case "index":
                index(ai);
                break;

            default:
                System.out.println("Invalid Command");
                reset();
                break;
        }
    }

    private void drive(AutoInstruction ai) {
        AutoInstruction.Unit u = ai.unit;
        // ai args:
        // 0: leftPower
        // 1: rightPower
        if (u.equals(AutoInstruction.Unit.SECONDS) || u.equals(AutoInstruction.Unit.MILLISECONDS)) {
            // amount: (milli)seconds to drive
            if (driveTime(ai.args.get(0), ai.args.get(1),
                    (u.equals(AutoInstruction.Unit.SECONDS) ? ai.amount : ai.amount / 1000.0))) {
                reset();
            }
        } else if (u.equals(AutoInstruction.Unit.ENCODER_TICKS)
                || u.equals(AutoInstruction.Unit.ROTATIONS)) {
            // amount: rotations/encoder ticks to drive
            if (driveDistance(ai.args.get(0), ai.args.get(1),
                    (u.equals(AutoInstruction.Unit.ENCODER_TICKS) ? ai.amount
                            : ai.amount * TICKS_PER_ROTATION))) {
                reset();
            }
        } else if (u.equals(AutoInstruction.Unit.FEET) || u.equals(AutoInstruction.Unit.INCHES)) {
            // amount: feet/inches to drive
            if (driveDistance(ai.args.get(0), ai.args.get(0),
                    (u.equals(AutoInstruction.Unit.INCHES) ? ai.amount * TICKS_PER_INCH
                            : (ai.amount * TICKS_PER_INCH) * 12))) {
                reset();
            }
        } else if (u.equals(AutoInstruction.Unit.CURRENT)) {
            // amount: motor current to stop at
            if (driveCurrent(ai.args.get(0), ai.args.get(0), ai.amount)) {
                reset();
            }
        }
    }

    private boolean driveDistance(double leftPower, double rightPower, double distance) {
        if (Math.abs(drive.getEncoder() - start) < distance) {
            drive.drive(leftPower, rightPower, false);
        } else {
            return true;
        }
        return false;
    }

    private boolean driveTime(double leftPower, double rightPower, double time) {
        if (timer.get() < time) {
            drive.drive(leftPower, rightPower, false);
        } else {
            return true;
        }
        return false;
    }

    public boolean driveCurrent(double leftPower, double rightPower, double current) {
        if (drive.getAverageCurrent() < current) {
            drive.drive(leftPower, rightPower, false);
        } else {
            return true;
        }
        return false;
    }

    private boolean rotateDegrees(double leftSpeed, double rightSpeed, double deg) {
        System.out.println(getAngle());
        if (Math.abs(getAngle() - navxStart - deg) < 2) {
            return true;
        } else {
            drive.drive(leftSpeed, rightSpeed, false);
            return false;
        }
    }

    public void turnDegrees(AutoInstruction ai) {
        // ai args:
        // 0: leftPower
        // 1: rightPower
        // amount: degrees to turn
        if (rotateDegrees(ai.args.get(0), ai.args.get(1), ai.amount)) {
            drive.drive(0, 0, false); // Stop turning
            reset();
        }
    }

    private void wait(AutoInstruction ai) {
        if (ai.unit.equals(AutoInstruction.Unit.MILLISECONDS)) {
            if (timer.get() / 1000 >= ai.amount) {
                reset();
            }
        } else {
            if (timer.get() >= ai.amount) {
                reset();
            }
        }
    }

    private void shoot(AutoInstruction ai) {
        AutoInstruction.Unit u = ai.unit;

        if (u == AutoInstruction.Unit.SPEED) {
            if (shooter.getSpeed() < SHOOTER_SPEED) {
                shooter.setSpeed(SHOOTER_SPEED);
            } else {
                timer.reset();
                if (timer.get() < ai.amount) {
                    shooter.setSpeed(SHOOTER_SPEED);
                    manipulation.setIndexLoad(true);
                } else {
                    shooter.setSpeed(0);
                    manipulation.setIndexLoad(false);
                    reset();
                }
            }
        } else if (u == AutoInstruction.Unit.POWER) {
            /*if (shooter.getSpeed() < SHOOTER_SPEED) {
                shooter.setPower(SHOOTER_SPEED);
            } else {
                timer.reset();*/
                
                shooter.setSpeed(-SHOOTER_SPEED * 0.5);
                if (timer.get() > 2) { // setIndexPower should just be setPower
                    if (timer.get() < ai.amount + 2) {
                        shooter.setIndexPower(-0.25);
                    } else {
                        shooter.setIndexPower(0);
                        manipulation.setIndexLoad(false);
                        shooter.setSpeed(0);
                        reset();
                    }
                    //manipulation.setIndexLoad(true);
                } else {
                    shooter.setIndexPower(0);
                    manipulation.setIndexLoad(false);
                    // shooter.setSpeed(0);
                }
            //}
        }

    }

    private void intake(AutoInstruction ai) {
        AutoInstruction.Unit u = ai.unit;
        if(timer.get() < ai.amount) {
            if(u == Unit.MOTOR) {
                drive.drive(ai.args.get(0), ai.args.get(1), false);
                manipulation.setIntakeSpin(true);
            } else if(u == Unit.NOTMOTOR) {
                if(ai.args.get(0) == 1) {
                    manipulation.setIntakeExtend(true);
                } else {
                    manipulation.setIntakeExtend(false);
                }
            }
        } else {
            drive.drive(0, 0, false);
            manipulation.setIntakeSpin(false);
            reset();
        }
    }

    private void index(AutoInstruction ai) {
        AutoInstruction.Unit u = ai.unit;
        if(u == Unit.MILLISECONDS) {
            if(timer.get() < ai.amount / 1000) {
                manipulation.setIndexLoad(true);
            } else {
                manipulation.setIndexLoad(false);
                reset();
            }
        } else {
            if(timer.get() < ai.amount) {
                manipulation.setIndexLoad(true);
            } else {
                manipulation.setIndexLoad(false);
                reset();
            }
        }
    }

    private void reset() {
        drive.drive(0, 0, true);
        step++;
        navxStart = getAngle();
        start = drive.getEncoder();
        timer.reset();
        timer.start();
    }

    private double getAngle() {
        return gyro.getAngle();
    }

    @Override
    public void setupLogging(Logger logger) {
        logger.addAttribute("Autonomous/step");
    }

    @Override
    public void log(Logger logger) {
        logger.log("Autonomous/step", this.step);
    }

    public static String getAutoPath(String name) {
        return Filesystem.getDeployDirectory().listFiles((dir, filename) -> {
            return filename.contentEquals("autos");
        })[0].listFiles((dir, filename) -> {
            return filename.contentEquals(name);
        })[0].getAbsolutePath();
    }
}
