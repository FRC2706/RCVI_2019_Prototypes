package frc.robot;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DriverAssistToTargetWithVision {
    
    public DriverAssistToTargetWithVision() {
    
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable table = inst.getTable("PathFinder");
        String serverName = new String("10.27.6.100");
        int port = 1735;
        inst.startClient(serverName, port);

        table.addEntryListener("vectorToTarget", (table1, key, entry, value, flags) -> {
            System.out.println("vectorToTarget changed value: " + value.getValue());
            double[] vectorToTargetValue = value.getDoubleArray();
            double angleToTargetValue = vectorToTargetValue[0];
            double distanceToTargetValue = vectorToTargetValue[1];
            generateTrajectoryToTarget(distanceToTargetValue, angleToTargetValue);
        }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);

	}
    
    public void generateTrajectoryToTarget(double distanceToTarget, double angleToTarget) {
        System.out.println("RCVI: testTraj(): Entered");
        System.out.println("RCVI: distanceToTarget: " + distanceToTarget);
        System.out.println("RCVI: angleToTarget: " + angleToTarget);

        double angleToTargetRad = Pathfinder.d2r(angleToTarget);
        double x = distanceToTarget * Math.sin(angleToTargetRad);
        double y = distanceToTarget * Math.cos(angleToTargetRad);

        double angleRelativetoField = 15; //take out hard code for pigeon imu function
        
        double finalAngleRelativetoField = 0.0;

        boolean driverAssistCargo = false;
        boolean driverAssistRocket = false;

        if (driverAssistCargo == true) {
            
            if ((angleRelativetoField >= 0.0 && angleRelativetoField <= 45.0) || (angleRelativetoField >= 315.0 && angleRelativetoField <= 360.0)) {
                finalAngleRelativetoField = 0.0;
            } else if (angleRelativetoField >= 45.0 && angleRelativetoField <= 135.0) {
                finalAngleRelativetoField = 90.0;
            } else if (angleRelativetoField >= 135.0 && angleRelativetoField <= 225.0) {
                finalAngleRelativetoField = 180.0;
            } 
            
        }
        else if (driverAssistRocket ==true) {

            if (angleRelativetoField >= 90.0 && angleRelativetoField <= 150.0) {
                finalAngleRelativetoField = 120.0;
            } else if (angleRelativetoField >= 150.0 && angleRelativetoField <= 210.0) {
                finalAngleRelativetoField = 180.0;
            } else if (angleRelativetoField >= 210.0 && angleRelativetoField <= 270.0) {
                finalAngleRelativetoField = 240.0;
            } 
            
            else if (angleRelativetoField >= 30.0 && angleRelativetoField <= 90.0) {
                finalAngleRelativetoField = 60.0;
            } else if ((angleRelativetoField >= 0.0 && angleRelativetoField <= 30.0) || (angleRelativetoField >= 330.0 && angleRelativetoField <= 360.0)) {
                finalAngleRelativetoField = 0.0;
            } else if (angleRelativetoField >= 270.0 && angleRelativetoField <= 330.0) {
                finalAngleRelativetoField = 300.0;
            }
        }

        double angleToTurnRelativetoRobot = finalAngleRelativetoField - angleRelativetoField;


        System.out.println("RCVI: x: " + x);
        System.out.println("RCVI: y: " + y);
        System.out.println("RCVI: angleToTurnRelativetoRobot: " + angleToTurnRelativetoRobot);

        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH, 0.05, 1.7, 2.0, 60.0);
        
        Waypoint[] points = new Waypoint[] {

                // Initial position/heading
                new Waypoint(0, 0, Pathfinder.d2r(90)),

                // Final position/heading in front of target
                new Waypoint(x,y,angleToTurnRelativetoRobot),
        };

        Trajectory traj = Pathfinder.generate(points, config);
        
        // Do something with the new Trajectory...
        
        System.out.println("RCVI: testTraj(): trajectory length: " + traj.length());
        for (int i = 0; i < traj.length(); i++)
        {

            /*
            String str = 
                "  segment[" + i + "]: (" + 
                "dt=" + traj.segments[i].dt + "," + 
                "x="+ traj.segments[i].x + "," +
                "y=" + traj.segments[i].y + "," +
                "pos=" + traj.segments[i].position + "," +
                "vel=" + traj.segments[i].velocity + "," +
                "acc=" + traj.segments[i].acceleration + "," +
                "jerk=" + traj.segments[i].jerk + "," +
                "heading=" + traj.segments[i].heading + ")";
                */

              String str = 
                   traj.segments[i].x + "," +
                   traj.segments[i].y + "," +
                   traj.segments[i].heading;

            System.out.println(str);
        }

    }
    
}
