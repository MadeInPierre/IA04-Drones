package agents.drone.communication;

public class DroneMessage {
	/*
	 * - Origin ID
	 * - Destination ID
	 * - Performative
	 * - Type
	 * - Content
	 */
	private int senderID;
	private int destinationID;
	
	public enum Performative {
		INFORM,
		REQUEST,
		REFUSE
	}
	private Performative performative;
	
	private String type;
	private String content;
	
	private float signalStrength; // set by the Environment, indicates the signal quality when this message has been sent
	
	public DroneMessage(int senderID, int destinationID, Performative perf) {
		this.senderID = senderID;
		this.destinationID = destinationID;
		this.performative = perf;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void send() {
		//TODO
	}
	
	public void setQuality(float strength) {
		this.signalStrength = strength;
	}
}
