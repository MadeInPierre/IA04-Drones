package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

import java.util.ArrayList;

public class HeadMoveBehavior extends FlyingBehavior {
	public HeadMoveBehavior(DroneAgent drone) {
		super(drone);
		
	}

	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		DroneMessage destroy = null;

		ArrayList<DroneMessage> inbox = com.getMessages();

		for (DroneMessage mes : inbox){
			if (mes.getTitle() == "moveHead" && mes.getPerformative() == Performative.REQUEST){
				destroy = mes;
				//Lecture du message de mouvement du drone de tête
				String move = mes.getContent();
				String delims = " ";
				String[] tokens = move.split(";");
				
				String sx = tokens[0];
				String sy = tokens[1];
				
				double x = Double.parseDouble(sx.replace("x ", ""));
				double y = Double.parseDouble(sy.replace("y ", ""));
				
				transform = new Double3D(x, y, 0);
				break;
			}
		}
		if (destroy != null)
			com.removeMessage(destroy);
		return transform;
	}

	public FlyingState transitionTo() {
		return FlyingState.HEAD_MOVE;
	}
}
