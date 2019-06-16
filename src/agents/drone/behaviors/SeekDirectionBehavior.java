package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneState;
import agents.drone.DroneFlyingManager.FlyingState;
import sim.util.Double3D;
import java.util.ArrayList;
import java.util.Collections;

public class SeekDirectionBehavior extends FlyingBehavior {
	private static final float CIRCLE_RADIUS  = .5f;
	private static final float N_GOTO_STEPS   = 20;  // Number of steps before reaching circle or center positions 
	private static final float N_CIRCLE_STEPS = 100; // Number of steps before making a full circle. One signal measure per step.
	
	private enum SeekState {
		BEGIN,		 // Notify the others we are seeking
		GOTO_CIRCLE, // Going to the circle's first point position
		IN_CIRCLE,   // Making the turn and collecting signals 
		GOTO_CENTER, // Going back to center position when circle finished 
		FINISHED,    // Mark as finished for FlyingManager
	}
	private SeekState seekState = SeekState.BEGIN;
	
	private int step = 0;
	private ArrayList<Float> strengths;
	private ArrayList<Integer> blocking_drone_ids; // keeps which drones are currently seeking
	
	public SeekDirectionBehavior(DroneAgent drone) {
		super(drone);
		strengths = new ArrayList<Float>();
		
		blocking_drone_ids = new ArrayList<Integer>();
	}
	
	// Make a full circle pattern
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D();
		
		switch(seekState) {
		case BEGIN: {
			// Check if nobody is doing a search right now
			ArrayList<DroneMessage> inbox = com.getMessages();
			ArrayList<DroneMessage> garbage = new ArrayList<DroneMessage>();
			for(DroneMessage msg : inbox) {
				if(msg.getTitle() == "seek") {
					if(msg.getContent() == "start" && (drone.getFollowerID() == msg.getSenderID() || drone.getLeaderID() == msg.getSenderID())) {
						blocking_drone_ids.add(msg.getSenderID());
						garbage.add(msg);
					}
					if(msg.getContent() == "end" && (drone.getFollowerID() == msg.getSenderID() || drone.getLeaderID() == msg.getSenderID())) {
						if(blocking_drone_ids.contains(msg.getSenderID())) {
							blocking_drone_ids.remove(blocking_drone_ids.indexOf(msg.getSenderID()));
						}
						garbage.add(msg);
					}
				}
			}
			for(DroneMessage msg : garbage) com.removeMessage(msg);
						
			if(blocking_drone_ids.size() == 0) { // seek when nobody near is
				DroneMessage msg = new DroneMessage(drone, DroneMessage.BROADCAST, Performative.REQUEST);
				msg.setTitle("seek");
				msg.setContent("start");
				com.sendMessageToDrone(msg);
				seekState = SeekState.GOTO_CIRCLE;
			}
			break;
		}
		case GOTO_CIRCLE: {
			double stepDist = (double)CIRCLE_RADIUS / N_GOTO_STEPS;
			transform = transform.add(new Double3D(stepDist, 0, 0));
			step++;
			
			if(step == N_GOTO_STEPS) {
				step = 0;
				seekState = SeekState.IN_CIRCLE;
			}
			break;
		}
		case IN_CIRCLE: {
			// Get signal strength compared to leader
			DroneMessage lastStatus = com.getLastStatusFrom(drone.getLeaderID());
			float strength = Float.MAX_VALUE;
			if(lastStatus != null) strength = lastStatus.getStrength();
			strengths.add(strength); // get current signal measure
			//System.out.println(strength);
			
			double da = 2 * Math.PI / N_CIRCLE_STEPS;
			double tx = CIRCLE_RADIUS - CIRCLE_RADIUS * Math.cos(da);
			double ty = CIRCLE_RADIUS * Math.sin(da);
			transform = transform.add(new Double3D(tx, ty, da));
			step++; 
			if(step == N_CIRCLE_STEPS) {
				step = 0;
				seekState = SeekState.GOTO_CENTER;
			}
			break;
		}
		case GOTO_CENTER: {
			double stepDist = -1.0 * (double)CIRCLE_RADIUS / N_GOTO_STEPS;
			transform = transform.add(new Double3D(stepDist, 0, 0));
			step++;
			
			if(step == N_GOTO_STEPS) {
				step = 0;
				seekState = SeekState.FINISHED;
				
				// Choose new direction 
				float dir = Collections.min(strengths);
				int i_dir = strengths.indexOf(dir);
				
				transform = transform.add(new Double3D(0, 0, i_dir * 2 * Math.PI / N_CIRCLE_STEPS));
				//System.out.println("drone=" + drone.getID() + " Found min=" + dir + ", i=" + i_dir + ", decided to turn=" + transform);
				
				// Tell the others we finished seeking
				DroneMessage msg = new DroneMessage(drone, DroneMessage.BROADCAST, Performative.REQUEST);
				msg.setTitle("seek");
				msg.setContent("end");
				com.sendMessageToDrone(msg);
			}
			break;
		}
		case FINISHED:
			break;
		default:
			break;
		}
		
		//System.out.println("[SeekDirectionBehaviour] chose to move by (" + transform.getX() + ", " + transform.getY() + ", " + transform.getZ() + "), step = " + step);
		return transform;
	}
	
	public FlyingState transitionTo() {
		if(seekState == SeekState.FINISHED) return FlyingState.KEEP_SIGNAL_DIST;
		return FlyingState.SEEK_SIGNAL_DIR;
	}
	
	public boolean enableCollisions() {
		return false;
	}
}
