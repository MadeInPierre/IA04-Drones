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
		FOLLOWER,
		RTH 		// Return to Home, searching for the base and landing when close
	};
	private DroneRole droneRole = DroneRole.FOLLOWER;
	
	public enum DroneState {
		IDLE,   	// Nothing to do, waiting for orders
		ARMED,		// Take off when the next drone is too far away
		FLYING,		// Drone flying and applying it's current FlyingState moving strategy.
		CRASHED		// Game over :(
	};
	private DroneState droneState = DroneState.IDLE;
	
	private int leaderID = -1;   // ID of the drone to follow
	private int followerID = -1; // ID of the drone that is following us 
	
	private CollisionsSensor[] collisionSensors;
	private DroneFlyingManager flyingManager;
	
	public void setDroneRole(DroneRole newRole) {
//		if(droneRole == DroneRole.HEAD && newRole == DroneRole.FOLLOWER)
//			flyingManager.setFlyingState(FlyingState.SEEK_TUNNEL_DIR);
		if(newRole == DroneRole.HEAD) {
			flyingManager.setFlyingState(FlyingState.HEAD_MOVE);
			setDroneState(DroneState.FLYING);
			setLeaderID(-1);
		}
		if(newRole == DroneRole.RTH) 
			if(getDroneState() == DroneState.FLYING)
				flyingManager.setFlyingStateForced(FlyingState.RTH);
		droneRole = newRole;
	}
	
	public DroneRole getDroneRole() { return droneRole; }
	
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
	
	public double getDistanceInTunnel() {
		return flyingManager.getDistanceInTunnel();
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
				communicator.sendMessageToDrone(newMsg);
					//setFlyingState(FlyingState.WAIT_RECONNECT);
				if(flyingManager.getFlyingState() != FlyingState.GOTO_STRAIGHT)
					garbageMessages.add(msg); // Don't remove the message when used by GotoStraightBehavior, removed there
			}
			
			if (!isHead() && msg.getTitle() == "tunnel_dist" && msg.getPerformative() == Performative.INFORM) {
				DroneMessage distmsg = new DroneMessage(this, getFollowerID(), Performative.INFORM);
				distmsg.setTitle(msg.getTitle());
				distmsg.setContent(msg.getContent());
				communicator.sendMessageToDrone(distmsg);
				garbageMessages.add(msg);
			}
			
			// Switch between leader and follower
			/*if (msg.getTitle() == "switch_chain" && msg.getPerformative() == Performative.REQUEST) { //TODO update
				DroneMessage newMsg = new DroneMessage(this, this.leaderID, msg.getPerformative());
				newMsg.setTitle(msg.getTitle());
				communicator.sendMessageToDrone(newMsg);
				
				// Switch leader and follower
				int tmp = getLeaderID();
				setLeaderID(getFollowerID());
				setFollowerID(tmp);
				
				// If the drone doesn't know who to follow (e.g. tail), follow the sender
				if(getLeaderID() == -1 && droneRole != DroneRole.HEAD)
					setLeaderID(msg.getSenderID());

				// If the drone is the head, change status and start seeking
				if(droneRole == DroneRole.HEAD)
					setDroneRole(DroneRole.FOLLOWER);
				
				// Seek for the follower
				if(getDroneState() == DroneState.FLYING) 
					flyingManager.setFlyingStateForced(FlyingState.GOTO_STRAIGHT); //update
				garbageMessages.add(msg);
			}*/

			if (msg.getTitle() == "rth" && msg.getPerformative() == Performative.REQUEST) {
				setLeaderID(Integer.parseInt(msg.getContent())); // Message was sent by the base, follow it
				setDroneRole(DroneRole.RTH);
				log("started rth, following agent=" + getLeaderID());
				garbageMessages.add(msg);
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
				if(leaderStatus.getStrength() > Constants.DRONE_ARMED_SIGNAL_LOSS) {
					DroneMessage armmsg = new DroneMessage(this, getFollowerID(), Performative.REQUEST);
					armmsg.setTitle("arm");
					communicator.sendMessageToDrone(armmsg);
					droneState = DroneState.FLYING;
					log("Detected signal low, now flying!");
					setFlyingState(FlyingState.GOTO_STRAIGHT);
				}
			}
			break;
		}
		case FLYING: {
//			DroneMessage leaderStatus = communicator.getLastStatusFrom(getLeaderID());
//			if(leaderStatus != null && leaderStatus.getStrength() > Constants.DRONE_DANGER_SIGNAL_LOSS) {
//				setFlyingState(FlyingState.WAIT_RECONNECT);
//			}
//			DroneMessage followerStatus = communicator.getLastStatusFrom(getFollowerID());
//			if(followerStatus != null && followerStatus.getStrength() > Constants.DRONE_DANGER_SIGNAL_LOSS) {
//				setFlyingState(FlyingState.ROLLBACK);
//			}
			if(getDroneRole() == DroneRole.HEAD) { // Get the distance back to the operator for stats
				DroneMessage distmsg = new DroneMessage(this, getFollowerID(), Performative.INFORM);
				distmsg.setTitle("tunnel_dist");
				distmsg.setContent(String.valueOf(flyingManager.getDistanceInTunnel()));
				communicator.sendMessageToDrone(distmsg);
			}
			break;
		}
		case CRASHED: {
			break;
		}
		}
		
		if(droneRole == DroneRole.RTH) {
			DroneMessage lastStatus = communicator.getLastStatusFrom(getLeaderID());
			float strength = Float.MAX_VALUE;
			if(lastStatus != null) strength = lastStatus.getStrength();
			
			if(droneState == DroneState.IDLE || droneState == DroneState.ARMED || 
			   strength <= Constants.DRONE_RTH_SIGNAL_LOSS + Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE) {
				setDroneState(DroneState.IDLE);
				
				DroneMessage rthmsg = new DroneMessage(this, getFollowerID(), Performative.REQUEST);
				rthmsg.setTitle("rth");
				rthmsg.setContent(String.valueOf(getLeaderID())); // Next drone will follow the base too
				communicator.sendMessageToDrone(rthmsg);
				
				setDroneRole(DroneRole.FOLLOWER);
				log("RTH Finished, landed!");
			}
		}
		
		// Update position
		if(droneState == DroneState.FLYING) flyingManager.stepTransform(communicator);
		
		// Cleanup messages
		communicator.clearStatuses();
		communicator.clearMessages();
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
