/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalSource;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port; // from rl
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a sample program demonstrating how to use an ultrasonic sensor and
 * proportional control to maintain a set distance from an object.
 */

public class Robot extends TimedRobot {
  // distance in inches the robot wants to stay from an object
  private static final double kHoldDistance = 12.0;

  // factor to convert sensor values to a distance in inches
  private static final double kValueToInches = 0.125;

  // proportional speed constant
  private static final double kP = 0.05;

  private static final int kLeftMotorPort = 0;
  private static final int kRightMotorPort = 1;
  private static final int kUltrasonicPort = 0;

  private final LIDARLite m_distanceSensor = new LIDARLite(I2C.Port.kOnboard);
  private final AnalogInput m_ultrasonic = new AnalogInput(kUltrasonicPort);
  private final DifferentialDrive m_robotDrive
      = new DifferentialDrive(new PWMVictorSPX(kLeftMotorPort),
      new PWMVictorSPX(kRightMotorPort));

  public static Navigator navigator;
  public static DigitalSource ds;
  public static LidarLitePWM lidarLitePWM;

  
   /*Tells the robot to drive to a set distance (in inches) from an object
    using proportional control.
   */
  @Override //has more than base class 
  public void teleopPeriodic() {
  
    // Take distance measurement from Lidar Lite
    /*
    Robot.navigator.startMeasuringDistance();
    int distance = Robot.navigator.getDistance();
    System.out.println("distance: " + distance);
    Robot.navigator.stopMeasuringDistance();
    */

    double distancePWM = Robot.lidarLitePWM.getDistancePWM();
    System.out.println(distancePWM );


    // The code below should not be needed but it may be the case
    // that the RoboRio complains if it is not there. (Check)

    double currentDistance = m_ultrasonic.getValue() * kValueToInches;

    // convert distance error to a motor speed
    double currentSpeed = (kHoldDistance - currentDistance) * kP;

    // drive robot
    m_robotDrive.arcadeDrive(currentSpeed, 0);
  } 
  @Override
  public void robotInit() {
    super.robotInit();
    navigator = new Navigator();
    ds = new DigitalInput(1);
    
    lidarLitePWM = new LidarLitePWM(ds);

    System.out.println("RCVI: Entered robotInit() ");

    double distancePWM = Robot.lidarLitePWM.getDistancePWM();
    System.out.println("distancePWM:" + distancePWM );

    /*
    // Test if Lidar Lite is communicating with I2C bus
    byte k_lidarLiteDeviceAddress = 0x62; // device address of Lidar Lite sensor
    I2C i2c = new I2C(I2C.Port.kOnboard, k_lidarLiteDeviceAddress); 
    boolean transferAborted = i2c.addressOnly(); // test if Lidar Lite is responding
    System.out.println("RCVI: transferAborted (expected: 0): " + transferAborted);
    i2c.close();
    */
    
    
    
    // Test reads from some registers on the Lidar Lite 

    /*
    // STATUS register (system status)
    int k_STATUS_reg = 0x01;
    int regVal = Robot.navigator.rlreadRegister(k_STATUS_reg);
    System.out.println("STATUS register (expected: 0x20):" + String.format("0x%08X", regVal));
    */
    /*
    // SIG_COUNT_VAL register (maximum acquisition count) 
    int k_SIG_COUNT_VAL_reg = 0x02;
    regVal = Robot.navigator.rlreadRegister(k_SIG_COUNT_VAL_reg);
    System.out.println("SIG_COUNT_VAL register (expected: 0x80):" + String.format("0x%08X", regVal));

    // ACQ_CONFIG_REG register (acquisition mode control) 
    int k_ACQ_CONFIG_REG_reg = 0x04;
    regVal = Robot.navigator.rlreadRegister(k_ACQ_CONFIG_REG_reg);
    System.out.println("ACQ_CONFIG_REG register (expected: 0x08):" + String.format("0x%08X", regVal));
     */

    // Take some distance measurements
    
    /*
    int numReads = 2;
    for (int i = 0; i < numReads; i++)
    {
        Robot.navigator.startMeasuringDistance();
        int distance = Robot.navigator.getDistance();
        Robot.navigator.stopMeasuringDistance();
        System.out.println("distance: " + distance);
        try
        {
            Thread.sleep(10, 0);
        } catch(InterruptedException e){System.out.println(e);}
    }
    */
    
    
    

  }
}


