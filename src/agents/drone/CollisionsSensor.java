package agents.drone;

import agents.CommunicativeAgent;
import environment.Environment;
import environment.CollisionManager;
import main.Constants;
import agents.Communicator;
import sim.util.Double2D;

import java.awt.geom.Point2D;

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
	
	// renvoie -1 si aucune collision n'est detectée
	public double getDistance(Communicator communicator) {
		CollisionManager cm  = Environment.get().getCollisionManager();
		Double2D p1 = Environment.get().getDronePos((CommunicativeAgent) agent);
		float a = Environment.get().getDroneAngle(agent) + angle;

		Double2D p2 = p1.add(new Double2D(
				range * Math.cos(a),
				range * Math.sin(a)));

		Double2D collisionPoint = cm.firstPathPointColliding(p1, p2);

		return (collisionPoint != null) ? collisionPoint.subtract(p1).length() : -1;
	}
}
