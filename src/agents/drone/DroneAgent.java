package agents.drone;

import java.util.ArrayList;
import agents.CommunicativeAgent;
import agents.Communicator;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import main.Constants;
import sim.engine.SimState;
import sim.portrayal.Oriented2D;
import sim.util.Double3D;

public class DroneAgent extends CommunicativeAgent {
	/* TODO
	 * 	- Fly manager 
	 *  - Communicator
	 * BUGS
	 * 	- 
	 */ 
	
	public enum DroneRole {
		HEAD,
		FOLLOWER
	};
	private DroneRole droneRole = DroneRole.FOLLOWER;
	
	public enum DroneState {
		IDLE,   			// Nothing to do, waiting for orders
		ARMED,				// Take off when the next drone is too far away
		FLYING,				// Drone flying and applying it's current FlyingState moving strategy.
		CRASHED				// Game over :(
	};
	private DroneState droneState = DroneState.IDLE;
	
	private int leaderID = -1;   // ID of the drone to follow
	private int followerID = -1; // ID of the drone that is following us 
	
	private CollisionsSensor[] collisionSensors;
	private DroneFlyingManager flyingManager;
	
	public void setDroneRole(DroneRole newRole) {
		if(droneRole == DroneRole.HEAD && newRole == DroneRole.FOLLOWER)
			flyingManager.setFlyingState(FlyingState.SEEK_SIGNAL_DIR);
		if(newRole == DroneRole.HEAD) {
			flyingManager.setFlyingState(FlyingState.HEAD_MOVE);
			setDroneState(DroneState.FLYING);
			setLeaderID(-1);
		}
		droneRole = newRole;
	}
	
	public void setDroneState(DroneState newState) {
		droneState = newState;
		if(newState == DroneState.IDLE) // force IDLE on both statuses
			flyingManager.setFlyingState(FlyingState.IDLE); 
	}
	
	public void setFlyingState(FlyingState newState) {
		flyingManager.setFlyingState(newState);
		if(newState == FlyingState.IDLE) // force IDLE on both statuses
			setDroneState(DroneState.IDLE);
	}

	public CollisionsSensor[] getCollisionSensors() {
		return collisionSensors;
	}
	
	public int getLeaderID() {
		return leaderID;
	}
	
	public void setLeaderID(int newID) {
		leaderID = newID;
	}
	
	public int getFollowerID() {
		return followerID;
	}
	
	public void setFollowerID(int newID) {
		followerID = newID;
	}
	
	// MAIN FUNCTIONS
	
	public DroneAgent() {
		super();

		flyingManager = new DroneFlyingManager(this);
		//flyingManager.setFlyingState(FlyingState.SEEK_SIGNAL_DIR); //TODO tmp
		System.out.println("New drone spawned, id=" + getID());

		collisionSensors = new CollisionsSensor[4];
		for (int i = 0; i < 4; i++) {
			collisionSensors[i] = new CollisionsSensor(this, (float) (2 * Math.PI * i / 4));
		}
	}

	public void step(SimState state) {
		
		// Process messages
		ArrayList<DroneMessage> garbageMessages = new ArrayList<DroneMessage>();
		for(DroneMessage msg : communicator.getMessages()) {
			if(msg.getTitle() == "arm") {
				setDroneState(DroneState.ARMED);
				log("Now armed!");
				garbageMessages.add(msg);
			}
			if (!isHead() && msg.getTitle() == "moveHead" && msg.getPerformative() == Performative.REQUEST) {
				DroneMessage newMsg = new DroneMessage(this, this.leaderID, msg.getPerformative());
				newMsg.setContent(msg.getContent());
				newMsg.setTitle(msg.getTitle());
				if (!communicator.sendMessageToDrone(newMsg))
					setFlyingState(FlyingState.WAIT_RECONNECT);
				garbageMessages.add(msg);
				//System.out.println("Drone" + this.agentID + " received moveeHad order, send to " + this.leaderID);
			}
		}
		for(DroneMessage msg : garbageMessages) communicator.removeMessage(msg);
		
		// Send usual status message (used by others for signal strength)
		DroneMessage msg = new DroneMessage(this, DroneMessage.BROADCAST, Performative.INFORM);
		msg.setTitle("status");
		communicator.sendMessageToDrone(msg);
		
		// Status behaviors
		switch(droneState) {
		case IDLE: {
			break;
		}
		case ARMED: {// listen for the leader's signal, fly if too low
			DroneMessage leaderStatus = communicator.getLastStatusFrom(getLeaderID());
			if(leaderStatus != null) {
				//System.out.println("Drone=" + agentID + " armed... signal=" + leaderStatus.getStrength());
				if(leaderStatus.getStrength() > Constants.DRONE_ARMED_SIGNAL_LOSS) {
					//System.out.println("Drone=" + agentID + " flying !");
					DroneMessage armmsg = new DroneMessage(this, getFollowerID(), Performative.REQUEST);
					armmsg.setTitle("arm");
					communicator.sendMessageToDrone(armmsg);
					droneState = DroneState.FLYING;
					log("Detected signal low, now flying!");
					setFlyingState(FlyingState.SEEK_SIGNAL_DIR);
				}
			}
			break;
		}
		case FLYING: {
			DroneMessage leaderStatus = communicator.getLastStatusFrom(getLeaderID());
			if(leaderStatus != null && leaderStatus.getStrength() > Constants.DRONE_DANGER_SIGNAL_LOSS) {
				setFlyingState(FlyingState.WAIT_RECONNECT);
			}
			DroneMessage followerStatus = communicator.getLastStatusFrom(getFollowerID());
			if(followerStatus != null && followerStatus.getStrength() > Constants.DRONE_DANGER_SIGNAL_LOSS) {
				setFlyingState(FlyingState.ROLLBACK);
			}
			break;
		}
		case CRASHED: {
			break;
		}
		}
		
		// Update position
		if(droneState == DroneState.FLYING) flyingManager.stepTransform(communicator);
		
		// Cleanup messages
		communicator.clearStatuses();
	}
	
	public void log(String text) {
		System.out.println("[Drone=" + getID() + "] " + text);
	}
	
	public boolean isHead() {
		return droneRole == DroneRole.HEAD;
	}
	
	public DroneState getDroneState() {
		return droneState;
	}
	
	public FlyingState getFlyingState() {
		return flyingManager.getFlyingState();
	}

	public Double3D popTrajectoryHistory() { return flyingManager.popTrajectoryHistory(); }
}
