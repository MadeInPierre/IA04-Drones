package environment;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import agents.drone.DroneAgent;
import main.Constants;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

public class Environment extends SimState {
	private SignalManager signalManager;
	private CollisionManager collisionManager;
	public Continuous2D yard;
	
	public Environment(long seed) {
		super(seed);
		yard = new Continuous2D(.1d, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
		
		// Add drones
		yard.setObjectLocation(new DroneAgent(), new Double2D(6, 7));
		yard.setObjectLocation(new DroneAgent(), new Double2D(12, 7));
		yard.setObjectLocation(new DroneAgent(), new Double2D(6, 12));
		yard.setObjectLocation(new DroneAgent(), new Double2D(12, 12));
		yard.setObjectLocation(new DroneAgent(), new Double2D(30, 30));


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

	public void start() {
		super.start();
		schedule.scheduleRepeating(signalManager);
	}

	public Double2D getDronePos(DroneAgent drone) {
		return yard.getObjectLocation(drone);
	}

	public Set<DroneAgent> getDrones() {
		Set<DroneAgent> s = (Set<DroneAgent>) yard.getAllObjects().stream().map(obj -> (DroneAgent) obj)
				.collect(Collectors.toSet());
		return s;
	}
}
