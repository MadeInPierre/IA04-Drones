package agents;

import sim.engine.SimState;
import sim.engine.Steppable;

public class CommunicativeAgent implements Steppable {

	private static int idCounter = 0;
	private int agentID;
	private Communicator communicator;

	public CommunicativeAgent() {
		agentID = idCounter++;
		communicator = new Communicator();

	}
	
	@Override
	public void step(SimState state) {
		
	}
	
	public int getID() {
		return agentID;
	}
}
