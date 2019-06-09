package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import environment.Environment;
import main.Constants;

public class Communicator {
	ArrayList<DroneMessage> inbox;
	Map<Integer, DroneMessage> lastStatuses;
	
	public Communicator() {
		inbox = new ArrayList<DroneMessage>();
		lastStatuses = new HashMap<>();
	}
	
	public void pushMessage(DroneMessage msg) {
		if(msg.getTitle() == "status") {
			//System.out.println("Adding status from " + msg.getSenderID());
			lastStatuses.put(msg.getSenderID(), msg);
		}
		else inbox.add(msg);
		//System.out.println("Agent " + msg.getDestinationID() + " got message title=" + msg.getTitle() + " at step=" + msg.getStep());
	}
	
	public ArrayList<DroneMessage> getMessages() {
		return inbox;
	}
	
	public DroneMessage getLastStatusFrom(int id) {
		return lastStatuses.get(id);
	}
	
	public void removeMessage(DroneMessage message) {
		for(DroneMessage m : inbox) {
			if(message == m) {
				inbox.remove(m);
				break;
			}
		}
	}
	
	public boolean sendMessageToDrone(Environment env, DroneMessage msg) {
		// Get the dest object
		boolean success = false;
		for(CommunicativeAgent a : env.getAgents()) {
			if(a.getID() == msg.getDestinationID() || msg.getDestinationID() == DroneMessage.BROADCAST) {
				msg.setStrength(env.getSignalManager().getSignalLoss(env.getDronePos(msg.getSender()), env.getDronePos(a)));
				msg.setStep(env.schedule.getSteps());
				
				if(msg.getStrength() < Constants.DRONE_MAXIMUM_SIGNAL_LOSS) {
					a.pushMessage(msg);
					success = true;
				}
				if(msg.getDestinationID() != DroneMessage.BROADCAST) break;
			}
		}
		return success;
	}
}
