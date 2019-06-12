package agents;

import sim.engine.SimState;
import sim.engine.Steppable;

public class CommunicativeAgent implements Steppable {

	protected static int idCounter = 0;
	protected int agentID;
	protected Communicator communicator;

	public CommunicativeAgent() {
		agentID = idCounter++;
		communicator = new Communicator(this);

	}
	
	public void step(SimState state) {
		System.out.println("ksjdfhg");
	}
	
	public int getID() {
		return agentID;
	}
	
	public void receiveMessage(DroneMessage msg) {
		communicator.receiveMessage(msg);
	}
}
