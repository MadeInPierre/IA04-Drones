package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import agents.DroneMessage.Performative;
import agents.drone.DroneAgent;
import environment.Environment;
import main.Constants;

public class Communicator {
	private ArrayList<DroneMessage> inbox;
	private Map<Integer, DroneMessage> lastStatuses;
	private CommunicativeAgent owner;
	
	public Communicator(CommunicativeAgent owner) {
		inbox = new ArrayList<DroneMessage>();
		lastStatuses = new HashMap<>();
		this.owner = owner;
	}
	
	public void receiveMessage(DroneMessage msg) {
		if(msg.getTitle() == "status") {
			//System.out.println("Adding status from " + msg.getSenderID());
			lastStatuses.put(msg.getSenderID(), msg);
		} else {
			if(inbox.size() > Constants.DRONE_MAX_INBOX_MSGS) inbox.remove(0);
			inbox.add(msg);
		}
		//System.out.println("Agent " + msg.getDestinationID() + " got message title=" + msg.getTitle() + " at step=" + msg.getStep());
	}
	
	public ArrayList<DroneMessage> getMessages() {
		return inbox;
	}
	
	public DroneMessage getLastStatusFrom(int id) {
		//System.out.println(lastStatuses.keySet().toString());
		//System.out.println(id);
		return lastStatuses.get(id);
	}

	public float getSignalStrength(int id) {
		Environment env = Environment.get();

		for(CommunicativeAgent a : env.getAgents()) {
			if (a.getID() == id) {
				return env.getSignalManager().getSignalLoss(owner, a);
			}
		}

		return -1; // En cas d'id invalide;
	}
	
	public void clearStatuses() {
		lastStatuses.clear();
	}
	
	public void removeMessage(DroneMessage message) {
		for(DroneMessage m : inbox) {
			if(message.getMessageID() == m.getMessageID()) {
				inbox.remove(m);
				return;
			}
		}
		System.out.println("Warning: couldn't find message to delete " + message.getTitle());
	}
	
	public void clearMessages() {
		inbox.clear();
	}
	
	public boolean sendMessageToDrone(DroneMessage msg) {
		if(msg.getDestinationID() == -1) return false; // ignore invalid destination
		
		boolean success = false;
		Environment env = Environment.get();
		for(CommunicativeAgent a : env.getAgents()) {
			if(a.getID() == msg.getDestinationID() || (msg.getDestinationID() == DroneMessage.BROADCAST && msg.getSenderID() != a.getID())) {
				// Duplicate the message #javaRefs
				DroneMessage m = new DroneMessage(msg.getSender(), msg.getDestinationID(), msg.getPerformative());
				m.setTitle(msg.getTitle());
				m.setContent(msg.getContent());
				
				m.setStrength(env.getSignalManager().getSignalLoss(m.getSender(), a));
				//System.out.println("Setting strength to message strength=" + m.getStrength() + " between agent=" + m.getSenderID() + " to agent=" + m.getDestinationID() + "/" + a.getID());
				m.setStep(env.schedule.getSteps());
				
				if(m.getStrength() < Constants.DRONE_MAXIMUM_SIGNAL_LOSS) {
					a.receiveMessage(m);
					success = true;
				}
				if(m.getDestinationID() != DroneMessage.BROADCAST) break;
			}
		}
		return success;
	}
}
