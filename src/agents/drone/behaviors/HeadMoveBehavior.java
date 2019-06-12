package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

import java.util.ArrayList;

public class HeadMoveBehavior extends FlyingBehavior {
	public HeadMoveBehavior(DroneAgent drone) {
		super(drone);
		System.out.println("New head behavior drone=" + drone.getID());
	}

	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		DroneMessage destroy = null;

		ArrayList<DroneMessage> inbox = com.getMessages();

		for (DroneMessage mes : inbox){
			if (mes.getTitle() == "moveHead"){
				destroy = mes;
				System.out.println("get mes");
				//Lecture du message de mouvement du drone de tête
				String move = mes.getContent();
				String delims = " ";
				String[] tokens = move.split(delims);

				String s = tokens[0];
				String sx = "x";
				String sy = "y";
				if (s.equals(sx)){
					int x = Integer.parseInt(tokens[1]);
					transform = new Double3D(x, 0, 0);
				}
				else if(s.equals(sy)){
					int y = Integer.parseInt(tokens[1]);
					transform = new Double3D(0, y, 0);
				}
				System.out.println("transform" + transform);
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
