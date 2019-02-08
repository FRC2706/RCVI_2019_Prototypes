package frc.robot;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.LIDARLite;

public class Navigator {

    public static final int k_minDistance = 5;

    private final LIDARLite m_distanceSensor = new LIDARLite(I2C.Port.kOnboard);
    
    private int m_lidarFailCount = 0;
    private static final int k_maxFailCount = 5;
    
    public static final int k_invalidDistance = -1;

    public void startMeasuringDistance() {
        m_distanceSensor.startMeasuring();
    }

    public void stopMeasuringDistance() {
        m_distanceSensor.stopMeasuring();
    }

    public int getDistance() {
        int distance = m_distanceSensor.getDistance();
        SmartDashboard.putNumber("Distance (raw)", distance);
        if (distance < k_minDistance) {
            if (++m_lidarFailCount >= k_maxFailCount) {
                return k_invalidDistance;
            }
        } else {
            m_lidarFailCount = 0;
        }
        return distance;
    }

    public int rlreadRegister(int address) {
        return m_distanceSensor.rlreadRegister(address);
    }

    
    public double getDistanceToTarget(double gearHeight) {
        return (1211 * Math.pow(gearHeight, -0.6446));
    }

    public double getTargetCenterLine(double distanceToTarget) {
        return (-0.000606 * distanceToTarget * distanceToTarget * distanceToTarget
                + 0.1933 * distanceToTarget * distanceToTarget
                - 21.3 * distanceToTarget + 1050);
    }

}