package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import agents.DroneMessage.Performative;
import agents.drone.DroneAgent;
import environment.Environment;
import main.Constants;
import utils.KalmanFilter;

public class Communicator {
	private ArrayList<DroneMessage> inbox;
	
	private Map<Integer, DroneMessage> lastStatuses;
	private Map<Integer, KalmanFilter> filteredStatuses;
	
	private CommunicativeAgent owner;
	
	public Communicator(CommunicativeAgent owner) {
		inbox = new ArrayList<DroneMessage>();
		lastStatuses = new HashMap<>();
		filteredStatuses = new HashMap<>();
		this.owner = owner;
	}
	
	public void receiveMessage(DroneMessage msg) {
		if(msg.getTitle() == "status") {
			// register last status message
			lastStatuses.put(msg.getSenderID(), msg);
			
			// Calculate new filtered value
			if(filteredStatuses.containsKey(msg.getSenderID())) {
				double u = (owner instanceof DroneAgent) ? ((DroneAgent)owner).getSpeed() : 0f ;
				filteredStatuses.get(msg.getSenderID()).filter(msg.getStrength(), u);
			}
			else {
				double B = (owner instanceof DroneAgent) ? 3f : 0f ;
				filteredStatuses.put(msg.getSenderID(), new KalmanFilter(0.01, 3, 1, B, 1)); // 0.001, 3, 1, 2.5, 1
			}
		} else {
			if(inbox.size() > Constants.DRONE_MAX_INBOX_MSGS) 
				inbox.remove(0);
			inbox.add(msg);
		}
	}
	
	public ArrayList<DroneMessage> getMessages() {
		return inbox;
	}
	
	public DroneMessage getLastStatusFrom(int id) {
		return lastStatuses.get(id);
	}
	
	public float getFilteredStrengthFrom(int id) {
		if(!lastStatuses.containsKey(id) || !filteredStatuses.containsKey(id))
				return Float.NaN;
		
		long age = Environment.get().schedule.getSteps() - lastStatuses.get(id).getStep();
		if(age > Constants.DRONE_NOMSGS_DISCONNECT_STEPS) { 
			filteredStatuses.get(id).reset();
			return Float.NaN;
		}
		return (float)filteredStatuses.get(id).getLastMeasurement();
	}

	public float getSignalStrength(int id) {
		Environment env = Environment.get();

		for(CommunicativeAgent a : env.getAgents()) {
			if (a.getID() == id) {
				return env.getSignalManager().getSignalLoss(owner, a);
			}
		}
		return -1; // If invalid id
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
