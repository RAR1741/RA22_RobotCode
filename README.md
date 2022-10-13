# RA22_RobotCode Logger Documentation

Welcome to the docs for the Red Alert Robotics 2022 Robot Code Logger System.

DISCLAIMER: This document assumes you know how Java works. If not, it is strongly recommended that you learn Java before utilizing the logger, or ask somebody who does know Java.

## Usage
To show how to use the logger system, this document will demonstrate the creation of a `Loggable` object and how to register it in the log file.

### Create the Loggable
1. Go to ***/src/main/java/frc/robot/logging*** and create a new Java file. The naming system used for the loggables are like this: *Loggable*, followed immediately by the name of the system being logged. For demonstrations purposes, let's call it *LoggableTest.java*.

2. Inside this file, create a new class. You'll also need to have it implement *Loggable.java*. This contains the methods needed to accumulate data to send to the log file. No import will be needed, as *Loggable.java* is in the same file as our *LoggableTest* class.
```java
package frc.robot.logging

public class LoggableTest implements Loggable {
  
}
```
<br>

3. Next you'll need to implement the methods from *Loggable.java*. The first method is `logHeaders()`, which is for logging the type of data. It is called only when the robot starts. The second method is `logData()`. This is called approximately 30 times a second. **Make sure to have the `@Override` decorator above both methods.**
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

### Registering the Loggable
1. Go to the main *Robot.java* file in ***/src/main/java/frc/robot***. This is where all of the robot code is.

2. Import *LoggableTest.java*.
```java
import frc.robot.logging.LoggableTest;
```
<br>

3. Go to the top of the `Robot` class and create a new *LoggableTest* object.
```java
LoggableTest loggableTest;
```
<br>

4. Now go into the `robotInit()` method and create the new instance. This is what the logger will register. If there's a constructor, make sure to give the necessary values.
```java
loggableTest = new LoggableTest();
```
<br>

5. Go to the bottom of the `robotInit()` method and use the `logger.addLoggable()` method to register `loggableTest` (This is assuming that the *Loggable* is a standalone system. If this is part of another system, then make sure to add it to whatever if-statements belong to that system, if any.) This will allow the logger to call `logHeaders()` and `logData()`.
```java
logger.addLoggable(loggableTest);
```
<br>

6. You are now all set! You've added the *LoggableTest* object to the logger, so now, when you start the robot, the log file should contain the info being logged by your *Loggable* object.
