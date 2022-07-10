# RA22_RobotCode Logger Documentation

Welcome to the docs for the Red Alert Robotics 2022 Robot Code Logger System (that was long, I know).

DISCLAIMER: I am assuming you know how Java works. If not, I strongly suggest you learn Java before utilizing the logger, or ask somebody who does know Java.

## Usage
To show how to use the logger system, I will demonstrate the creation of a `Loggable` object and how to register it in the log file.

### Create the Loggable
1. Go to ***/src/main/java/frc/robot/logging*** and create a new Java file. The naming system used for the loggables are like this: *Loggable*, followed immediately by the name of the system being logged. For demonstrations purposes, I will call it *LoggableTest.java*.

2. Inside this file, create a new class. You'll also need to have it implement *Loggable.java* (`public class LoggableTest implements Loggable`). This contains the methods needed to accumulate data to send to the log file. No import will be needed, as *Loggable.java* is in the same file as our *LoggableTest* class.
```java
package frc.robot.logging

public class LoggableTest implements Loggable {
  
}
```
<br>

3. Next you'll need to implement the methods from *Loggable.java*. The first method is `logHeaders()`. This method is only for logging headers. The second method is `logData()`. This is only for logging data. **Make sure to have the `@Override` decorator above both methods.**
```java
package frc.robot.logging

public class LoggableTest implements Loggable {
  @Override
  public void logHeaders(Logger logger) {
    
  }
  
  @Override
  public void logData(Logger logger) {
    
  }
}
```
<br>

4. In the `logHeaders()` method, we will have two headers: *first_name*, and *last_name*. So, we need to use the `logger` parameter to access the necessary method to add these headers.
```java
package frc.robot.logging

public class LoggableTest implements Loggable {
  @Override
  public void logHeaders(Logger logger) {
    logger.addHeader("first_name");
    logger.addHeader("last_name");
  }
  
  @Override
  public void logData(Logger logger) {
    
  }
}
```
<br>

5. Now let's add our data. Under `logData()`, use the `logger` parameter to access the needed methods. Make sure to specify the header you want the data to go under. Don't worry about converting the data to `string` type. The logger does that automatically.
```java
package frc.robot.logging

public class LoggableTest implements Loggable {
  @Override
  public void logHeaders(Logger logger) {
    logger.addHeader("first_name");
    logger.addHeader("last_name");
  }
  
  @Override
  public void logData(Logger logger) {
    logger.addData("first_name", "Jeff")
    logger.addData("last_name", "Bezos")
  }
}
```
<br>

6. There you go! Our *Loggable* object has been created. Don't think that you can't add other things as well. Constructors, other methods, those won't get in the way (except, of course, if you name other methods the same name). As long as your class has `logHeaders()` and `logData()`, you're good.
