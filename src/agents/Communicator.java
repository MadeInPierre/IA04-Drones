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
		} else if(msg.getPerformative() == Performative.REQUEST && msg.getTitle() == "moveHead") {
			// command to move head
			if (owner instanceof DroneAgent  && ((DroneAgent)owner).isLeader()) {
				// TODO move !
			} else if (owner instanceof DroneAgent){
				int nextDroneId = ((DroneAgent) owner).getLeaderID();
				DroneMessage newMsg = new DroneMessage(owner, nextDroneId, msg.getPerformative());
				newMsg.setContent(msg.getContent());
				sendMessageToDrone(newMsg);
			}			
		}
		else inbox.add(msg);
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
	
	public void clearStatuses() {
		lastStatuses.clear();
	}
	
	public void removeMessage(DroneMessage message) {
		for(DroneMessage m : inbox) {
			if(message == m) {
				inbox.remove(m);
				break;
			}
		}
	}
	
	public boolean sendMessageToDrone(DroneMessage msg) {
		if(msg.getDestinationID() == -1) return false; // ignore invalid destination
		
		boolean success = false;
		Environment env = Environment.get();
		for(CommunicativeAgent a : env.getAgents()) {
			if(a.getID() == msg.getDestinationID() || msg.getDestinationID() == DroneMessage.BROADCAST) {
				// Duplicate the message #javaRefs
				DroneMessage m = new DroneMessage(msg.getSender(), msg.getDestinationID(), msg.getPerformative());
				m.setTitle(msg.getTitle());
				m.setContent(msg.getContent());
				
				m.setStrength(env.getSignalManager().getSignalLoss(env.getDronePos(m.getSender()), env.getDronePos(a)));
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
