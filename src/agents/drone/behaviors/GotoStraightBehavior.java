package agents.drone.behaviors;

import java.util.ArrayList;

import agents.Communicator;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneRole;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import main.Constants;
import sim.util.Double3D;

public class GotoStraightBehavior extends FlyingBehavior {
	private boolean askForSeek = false;
	private ArrayList<Float> followerHistory;
	private ArrayList<Float> leaderHistory;
	float followerSignal, leaderSignal;
	
	private enum Mode {
		FOLLOW,
		GOTO
	}
	private Mode mode = Mode.FOLLOW;
	private float gotoPos = -1;
	
	private boolean balancing = false;
	
	public GotoStraightBehavior(DroneAgent drone) {
		super(drone);
		followerHistory = new ArrayList<Float>();
		leaderHistory   = new ArrayList<Float>();
	}
	
	private void updateHistory(float folSig, float leaSig) {
		followerHistory.add(folSig);
		if(followerHistory.size() > Constants.DRONE_SIGNAL_MEAN_STEPS)
			followerHistory.remove(0);
		leaderHistory.add(leaSig);
		if(leaderHistory.size() > Constants.DRONE_SIGNAL_MEAN_STEPS)
			leaderHistory.remove(0);
		followerSignal = (float)followerHistory.stream().mapToDouble(val -> val).average().orElse(0.0);
		leaderSignal   = (float)leaderHistory.stream().mapToDouble(val -> val).average().orElse(0.0);
	}
	
	private void resetHistory() {
		followerHistory.clear();
		leaderHistory.clear();
	}
	
	public Double3D stepTransform(Communicator com) {
		//if (Environment.get().schedule.getSteps() % 30 != 0)
			//return new Double3D();

		Double3D transform = new Double3D(0, 0, 0);
		
		// Stop going straight if there's an obstacle ahead and seek
		// CollisionsSensor[] sensors = drone.getCollisionSensors();
		// if(sensors[0].getDistance(com) <= Constants.DRONE_COLLISION_SENSOR_TRIGGER_DISTANCE) askForSeek = true;
		
		if (Environment.get().schedule.getSteps() % 30 == 0) {
//			updateHistory(com.getFilteredStrengthFrom(drone.getFollowerID()), com.getFilteredStrengthFrom(drone.getLeaderID()));
			
			followerSignal = com.getSignalStrength(drone.getFollowerID());
			leaderSignal   = com.getSignalStrength(drone.getLeaderID());
			
			//followerSignal = com.getFilteredStrengthFrom(drone.getFollowerID());
			//leaderSignal   = com.getFilteredStrengthFrom(drone.getLeaderID());
		}
		
		// Movement decision
		switch(mode) {
			case FOLLOW: { // Go where the signal is the lowest
				if(Math.abs(followerSignal - leaderSignal) > Constants.DRONE_EXPECTED_SIGNAL_STD) {
					balancing = true;
					
					double maxSignal = Math.max(followerSignal, leaderSignal);
					double minSignal = Math.min(followerSignal, leaderSignal);
//					double k = (Constants.SIGNAL_MAX_LOSS + Constants.SIGNAL_MIN_LOSS) / 2f;
					double k = Constants.SIGNAL_MAX_LOSS;
					
					double speed = (Math.exp(maxSignal / (10 * k)) - Math.exp(minSignal / (10 * k))) / (3f * 30f);
					
					//transform = transform.add(new Double3D((followerSignal < leaderSignal ? 1.0 : -1.0) * Constants.DRONE_SPEED, 0, 0));
					transform = transform.add(new Double3D(
							Math.max(-Constants.DRONE_SPEED, Math.min(Constants.DRONE_SPEED, ((followerSignal - leaderSignal < 0) ? 1f : -1f) * speed))
							, 0, 0));
//					transform = transform.add(new Double3D(((followerSignal - leaderSignal < 0) ? 1f : -1f) * speed, 0, 0));
				}
//				else {
////					if(balancing == true) { // if we just regained a stable middle position, move a bit more (goto the center of the zone)
////						gotoPos = drone.getDistanceInTunnel() + .5f;
////						mode = Mode.GOTO;
////						balancing = false;
////						break;
////					}
//					if(Math.abs(followerSignal - leaderSignal) > .5f) // small balancing for precise positioning
//						transform = transform.add(new Double3D((followerSignal < leaderSignal ? 1.0 : -1.0) * Constants.DRONE_SPEED, 0, 0));
//					
//					balancing = false;
//					// If the head moves, move too
//					for(DroneMessage msg : com.getMessages()) {
//						if (msg.getTitle() == "moveHead" && msg.getPerformative() == Performative.REQUEST) {
//							double move = -Double.parseDouble(msg.getContent().split(";")[1].replace("y ", ""));
//							transform = new Double3D(move/2f, 0, 0);
//							com.removeMessage(msg);
//							break;
//						}
//					}
//				}
				break;
			}
			case GOTO: { // Go forward or backward according to the goal position
				if(gotoPos == -1) { mode = Mode.FOLLOW; break; }
				
				if(drone.getDistanceInTunnel() < gotoPos - 0.05)
					transform = transform.add(new Double3D( Constants.DRONE_SPEED, 0, 0));
				else if(drone.getDistanceInTunnel() > gotoPos + 0.05)
					transform = transform.add(new Double3D(-Constants.DRONE_SPEED, 0, 0));
				else {
					gotoPos = -1;
					mode = Mode.FOLLOW;
				}
				break;
			}
		}
		
		
		
		// Avoid collisions between drones based on signal quality (can't be too good)
//		if(transform.getX() < 0 && com.getSignalStrength(drone.getFollowerID()) < Constants.DRONE_BEST_SIGNAL_LOSS)
//			transform = new Double3D(0, transform.getY(), transform.getZ());
//		if(transform.getX() > 0 && com.getSignalStrength(drone.getLeaderID())   < Constants.DRONE_BEST_SIGNAL_LOSS)
//			transform = new Double3D(0, transform.getY(), transform.getZ());
		
		return transform;
	}
	
	public FlyingState transitionTo() {
		//if(askForSeek) return FlyingState.SEEK_TUNNEL_DIR;
		return FlyingState.GOTO_STRAIGHT;
	}
	
	public boolean enableCollisions() {
		return true;
	}
}
