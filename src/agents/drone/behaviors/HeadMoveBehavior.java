package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

import java.util.ArrayList;

public class HeadMoveBehavior extends FlyingBehavior {
	private boolean signalLost = false;
	private boolean askForSeek = false;

	public HeadMoveBehavior(DroneAgent drone) {
		super(drone);
	}

	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		DroneMessage destroy = null;
		float signalStrength = com.getSignalStrength(drone.getFollowerID());

		signalLost = signalStrength > Constants.DRONE_MAXIMUM_SIGNAL_LOSS;

		ArrayList<DroneMessage> inbox = com.getMessages();

		for (DroneMessage mes : inbox){
			if (mes.getTitle() == "moveHead" && mes.getPerformative() == Performative.REQUEST){
				destroy = mes;
				//Lecture du message de mouvement du drone de tête
				String move = mes.getContent();
				String[] tokens = move.split(";");

				String sx = tokens[0];
				String sy = tokens[1];

				double x = Double.parseDouble(sx.replace("x ", ""));
				double y = Double.parseDouble(sy.replace("y ", ""));
				
				double forward = -y;

//				transform = new Double3D(x, y, 0); // controls for 2D navigation
				transform = new Double3D(forward, 0, 0); // controls for 1D navigation
				
				if(forward > 0 && com.getSignalStrength(drone.getFollowerID()) > Constants.DRONE_DANGER_SIGNAL_LOSS) {
					transform = new Double3D(); // Do not let the user go forward if the local signal is too low (head too fast for the chain probably)
				}
				break;
			}
		}

		if (destroy != null) com.removeMessage(destroy);
		
		// Stop going straight if there's an obstacle ahead and seek
		CollisionsSensor[] sensors = drone.getCollisionSensors();
		if(sensors[0].getDistance(com) <= Constants.DRONE_COLLISION_SENSOR_TRIGGER_DISTANCE) askForSeek = true;

		return transform;
	}

	public FlyingState transitionTo() {
		if(signalLost) return FlyingState.ROLLBACK;
		//if(askForSeek) return FlyingState.SEEK_TUNNEL_DIR;
		return FlyingState.HEAD_MOVE;
	}
	
	public boolean enableCollisions() {
		return true;
	}
}
