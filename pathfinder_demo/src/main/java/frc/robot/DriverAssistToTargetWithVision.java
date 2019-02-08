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

        double angleRelativetoFieldRad = Pathfinder.d2r(15);
        double angleToTurnRad = 0.0;

        boolean rocketSides = false;

        if (rocketSides == true) {
            angleToTurnRad = (Pathfinder.d2r(45)) - (angleRelativetoFieldRad); //figure out the right angle the rocket side is at
        } else {
            angleToTurnRad = (Pathfinder.d2r(90)) - (angleRelativetoFieldRad);
        }

        System.out.println("RCVI: x: " + x);
        System.out.println("RCVI: y: " + y);
        System.out.println("RCVI: angleToTurnRad: " + angleToTurnRad);

        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH, 0.05, 1.7, 2.0, 60.0);
        
        Waypoint[] points = new Waypoint[] {

                // Initial position/heading
                new Waypoint(0, 0, 0),

                // Final position/heading in front of target
                new Waypoint(x,y,angleToTurnRad),
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
