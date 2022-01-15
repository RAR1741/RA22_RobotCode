package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import frc.robot.logging.Loggable;
import frc.robot.logging.Logger;

public class Climbing implements Loggable {
    TalonFX climbingMotor;
    public Climbing(int climbingMotorID) {
        this.climbingMotor = new TalonFX(climbingMotorID);
    }

    @Override
    public void setupLogging(Logger logger) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void log(Logger logger) {
        // TODO Auto-generated method stub
        
    }
}

//Upon lineup of Bar A shadow tape: 
/* 
- Press button to run climbing function (e.g. Press 'A' button)
   [1] - Measure encoder count/total encoder counts = total rotations
   [1] - Rotate to certain point to hit angle for gripping first bar
    - Activate first solenoid a very small time after the motor is at the correct rotation
    - Repeat [1] with different measurements
    - Activate second solenoid a very small time after the motor is at the correct rotation
    - Repeat [1] with different measurements
    - Activate third solenoid a very small time after the motor is at the correct rotation
*/