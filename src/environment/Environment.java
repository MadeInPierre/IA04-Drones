package environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import agents.CommunicativeAgent;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneRole;
import agents.drone.DroneAgent.DroneState;
import agents.drone.DroneFlyingManager.FlyingState;
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

	private static Environment instance = new Environment(System.currentTimeMillis());

	public static Environment get() {
		return instance;
	}

	private Environment(long seed) {
		super(seed);
	}

	/*
	 * public static void main(String[] args) { doLoop(Environment.class, args);
	 * System.exit(0); }
	 */

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
		
		yard = new Continuous2D(.1d, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
		droneAngles = new HashMap<DroneAgent, Float>();

		// Add agents
		operator = new OperatorAgent();
		for(int i = 0; i < Constants.N_DRONES; i++) {
			addDrone(new Double2D(Constants.SPAWN_POS.getX(), Constants.SPAWN_POS.getY())); // Followers
			rotateDrone((DroneAgent)yard.getAllObjects().get(i), (float)Constants.SPAWN_POS.getZ());
		}

		headDrone = (DroneAgent) yard.getAllObjects().get(0);
		headDrone.setDroneRole(DroneRole.HEAD);
//		headDrone.setDroneState(DroneState.FLYING);
//		headDrone.setFlyingState(FlyingState.SEEK_TUNNEL_DIR);

		for (int i = 0; i < droneAngles.size() - 1; i++) {
			DroneAgent leader = (DroneAgent) yard.getAllObjects().get(i);
			DroneAgent follower = (DroneAgent) yard.getAllObjects().get(i + 1);
			linkDrones(follower, leader);
		}

		if(yard.getAllObjects().size() > 1) {
			DroneAgent d = (DroneAgent) yard.getAllObjects().get(1);
			d.setDroneState(DroneState.ARMED);
			DroneAgent tailDrone = (DroneAgent) yard.getAllObjects().get(yard.size() - 1);
			tailDrone.setFollowerID(operator.getID());
		}

		schedule.scheduleRepeating(operator);
		yard.setObjectLocation(operator, new Double2D(Constants.SPAWN_POS.getX(), Constants.SPAWN_POS.getY()));

		signalManager = new SignalManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.SIGNAL_MAP_STEP,
				Constants.SIGNAL_IMAGE, this);
		collisionManager = new CollisionManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.COLLISION_MAP_STEP,
				Constants.COLLISION_IMAGE);
		

		System.out.println("Environment is initialized.");
		
		schedule.scheduleRepeating(signalManager);
		schedule.scheduleRepeating(operator);
		for (Object o : yard.getAllObjects())
			if (o instanceof DroneAgent)
				schedule.scheduleRepeating((DroneAgent) o);
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
		Set<DroneAgent> s = (Set<DroneAgent>) yard.getAllObjects().stream().filter(obj -> obj instanceof DroneAgent)
				.map(obj -> (DroneAgent) obj).collect(Collectors.toSet());
		return s;
	}

	public Set<CommunicativeAgent> getAgents() {
		Set<CommunicativeAgent> s = (Set<CommunicativeAgent>) yard.getAllObjects().stream()
				.filter(obj -> obj instanceof CommunicativeAgent).map(obj -> (CommunicativeAgent) obj)
				.collect(Collectors.toSet());
		return s;
	}

	public void translateDrone(DroneAgent drone, Double2D translation) {
		Double2D pos = yard.getObjectLocation(drone);
		float angle = droneAngles.get(drone);

		double tx = Math.cos(angle) * translation.getX();
		double ty = Math.sin(angle) * translation.getX();
		angle += Math.PI / 2; // translation can have negative numbers, so treat X and Y separately
		tx += Math.cos(angle) * translation.getY();
		ty += Math.sin(angle) * translation.getY();
		pos = pos.add(new Double2D(tx, ty));
		pos = new Double2D(pos.x % yard.getWidth(), pos.y % yard.getHeight());
		yard.setObjectLocation(drone, pos);
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

	private void linkDrones(DroneAgent follower, DroneAgent leader) {
		follower.setLeaderID(leader.getID());
		leader.setFollowerID(follower.getID());
	}
	
	public boolean isPathFromOperatorToHead() {
		CommunicativeAgent a1 = operator;
		Optional<CommunicativeAgent> a2;

		a2 = Environment.get().getSignalManager().getClosestAgent(operator);

		while(true) {
			if (!a2.isPresent())
				return false;
			
			if (signalManager.getSignalLoss(a1, a2.get()) > Constants.DRONE_MAXIMUM_SIGNAL_LOSS)
				return false;
			
			if (((DroneAgent) a2.get()).isHead())
				return true;
			
			a1 = a2.get();
			int l = ((DroneAgent)a1).getLeaderID();
			a2 = getAgents().stream().filter(o -> o.getID() == l).findFirst();
		}
	}
}
