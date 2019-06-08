package agents;

import java.util.ArrayList;

import environment.Environment;
import main.Constants;

public class Communicator {
	ArrayList<DroneMessage> messages;
	
	public Communicator() {
		messages = new ArrayList<DroneMessage>();
	}
	
	public void pushMessage(DroneMessage msg) {
		messages.add(msg);
		//System.out.println("Agent " + msg.getDestinationID() + " got message title=" + msg.getTitle() + " at step=" + msg.getStep());
	}
	
	public ArrayList<DroneMessage> getMessages() {
		return messages;
	}
	
	public void removeMessage(DroneMessage message) {
		for(DroneMessage m : messages) {
			if(message == m) {
				messages.remove(m);
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
