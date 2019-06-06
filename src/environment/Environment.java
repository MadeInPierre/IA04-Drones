package environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import agents.CommunicativeAgent;
import agents.drone.DroneAgent;
import agents.operator.OperatorAgent;
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
	private DroneAgent headDrone;
	private OperatorAgent operator;

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
		
		operator = new OperatorAgent();
		schedule.scheduleRepeating(operator);
		yard.setObjectLocation(operator, new Double2D(0, 30));
		headDrone = (DroneAgent) yard.getAllObjects().get(0);

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
		for(Object o : yard.getAllObjects())
			if(o instanceof DroneAgent)
				schedule.scheduleRepeating((DroneAgent)o);
	}

	public Double2D getDronePos(CommunicativeAgent drone) {
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
		Set<DroneAgent> s = (Set<DroneAgent>) yard.getAllObjects().stream().filter(obj -> obj instanceof DroneAgent).map(obj -> (DroneAgent) obj)
				.collect(Collectors.toSet());
		return s;
	}
	
	public Set<CommunicativeAgent> getAgents() {
		Set<CommunicativeAgent> s = (Set<CommunicativeAgent>) yard.getAllObjects().stream().filter(obj -> obj instanceof CommunicativeAgent).map(obj -> (CommunicativeAgent) obj)
				.collect(Collectors.toSet());
		return s;
	}

	public void translateDrone(DroneAgent drone, Double2D translation) {
		float angle = droneAngles.get(drone);
		float tx = (float) (Math.cos(angle) * translation.length());
		float ty = (float) (Math.sin(angle) * translation.length());
		
		Double2D oldPos = yard.getObjectLocation(drone);
		Double2D newPos = new Double2D(oldPos.getX() + tx, oldPos.getY() + ty);
		yard.setObjectLocation(drone, newPos);
	}
	
	public void rotateDrone(DroneAgent drone, float rotation) {
		float newAngle = (float) ((droneAngles.get(drone) + rotation) % (Math.PI * 2));
		droneAngles.put(drone, newAngle);
	}

	private void addDrone(Double2D pos) {
		DroneAgent d = new DroneAgent();
		yard.setObjectLocation(d, pos);
		droneAngles.put(d, 0f);
	}
}
