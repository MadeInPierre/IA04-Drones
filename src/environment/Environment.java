package environment;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import agents.drone.DroneAgent;
import main.Constants;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;

public class Environment extends SimState {
    private SignalManager signalManager;
    private CollisionManager collisionManager;
    private Map<DroneAgent, Point2D> drones;
    public Continuous2D yard = new Continuous2D(1,20,20);

    public Environment(long seed) {
    	super(seed);
    	drones = new HashMap<DroneAgent, Point2D>();
        signalManager = new SignalManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.SIGNAL_MAP_STEP, Constants.SIGNAL_IMAGE);
        collisionManager = new CollisionManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.COLLISION_MAP_STEP, Constants.COLLISION_IMAGE);

        System.out.println("Environment is initialized.");
    }
    
    public static void main(String[] args) {
    	doLoop(Environment.class, args);
    	System.exit(0);
    }

    
    public SignalManager getSignalManager() {
    	return signalManager;
    }
    
    public void start() {
    	super.start();
    	
    	schedule.scheduleRepeating(signalManager);
    }
}
