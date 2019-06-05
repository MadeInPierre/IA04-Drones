package environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import agents.drone.DroneAgent;
import main.Constants;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.Double3D;

public class Environment extends SimState {
	private SignalManager signalManager;
	private CollisionManager collisionManager;
	private Continuous2D yard;
	private Map<DroneAgent, Float> droneAngles;

	public Environment(long seed) {
		super(seed);
		yard = new Continuous2D(.1d, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
		droneAngles = new HashMap<DroneAgent, Float>();

		// Add drones
		addDrone(new Double2D(6, 7));
		addDrone(new Double2D(12, 7));
		addDrone(new Double2D(6, 12));
		addDrone(new Double2D(12, 12));
		addDrone(new Double2D(30, 30));

		signalManager = new SignalManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.SIGNAL_MAP_STEP,
				Constants.SIGNAL_IMAGE, this);
		collisionManager = new CollisionManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.COLLISION_MAP_STEP,
				Constants.COLLISION_IMAGE);

		System.out.println("Environment is initialized.");
	}

	public static void main(String[] args) {
		doLoop(Environment.class, args);
		System.exit(0);
	}

	public SignalManager getSignalManager() {
		return signalManager;
	}

	public CollisionManager getCollisionManager() {
		return collisionManager;
	}

	public Continuous2D getYard() {
		return yard;
	}

	public void start() {
		super.start();
		schedule.scheduleRepeating(signalManager);
	}

	public Double2D getDronePos(DroneAgent drone) {
		return yard.getObjectLocation(drone);
	}

	public Double3D getDronePosAndAngle(DroneAgent drone) {
		Double2D pos = yard.getObjectLocation(drone);
		return new Double3D(pos.getX(), pos.getY(), this.droneAngles.get(drone));
	}
	
	public float getDroneAngle(DroneAgent drone) {
		return droneAngles.get(drone);
	}

	public Set<DroneAgent> getDrones() {
		Set<DroneAgent> s = (Set<DroneAgent>) yard.getAllObjects().stream().map(obj -> (DroneAgent) obj)
				.collect(Collectors.toSet());
		return s;
	}

	public void transformDrone(DroneAgent drone, Double2D translation, float rotation) {
		Double2D oldPos = yard.getObjectLocation(drone);
		Double2D newPos = new Double2D(oldPos.getX() + translation.getX(), oldPos.getY() + translation.getY());
		yard.setObjectLocation(drone, newPos);
		float newAngle = (float) ((droneAngles.get(drone) + rotation) % (Math.PI * 2));
		droneAngles.put(drone, newAngle);
	}

	private void addDrone(Double2D pos) {
		DroneAgent d = new DroneAgent();
		yard.setObjectLocation(d, pos);
		droneAngles.put(d, 0f);
	}
}
