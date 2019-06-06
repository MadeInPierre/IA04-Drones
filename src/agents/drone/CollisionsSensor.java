package agents.drone;

import environment.Environment;
import environment.CollisionManager;
import main.Constants;
import agents.Communicator;
import sim.util.Double2D;

import java.awt.geom.Point2D;
import java.lang.invoke.ConstantBootstraps;

public class CollisionsSensor {
	private float angle; // sensor angle in rad from the drone's front
	private float range;

	private DroneAgent agent;
	
	public CollisionsSensor(DroneAgent agent, float angle) {
		this.angle = angle;
		this.range = Constants.DRONE_COLLISION_SENSOR_RANGE;
		this.agent = agent;
	}

	public float getAngle() { return angle; }
	public float getRange() { return range; }
	
	// renvoie MAX_VALUE si aucune collision n'est detectée
	public double getDistance(Environment environment, Communicator communicator) {
		CollisionManager cm  = environment.getCollisionManager();
		Double2D p1 = environment.getDronePos(agent);
		float angle = environment.getDroneAngle(agent);

		Double2D p2 = p1.add(new Double2D(
				range * Math.cos(angle),
				range * Math.sin(angle)));

		Double2D collisionPoint = cm.firstPathPointColliding(p1, p2);

		return (collisionPoint != null) ? collisionPoint.subtract(p2).length() : Float.MAX_VALUE;
	}
}
