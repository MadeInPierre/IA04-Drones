package agents;

import agents.drone.DroneAgent;

public class DroneMessage {
	/*
	 * Usage:
	 * - create message with senderID, destinationID
	 * - Origin ID
	 * - Destination ID
	 * - Performative
	 * - Type
	 * - Content
	 */
	
	public final static int BROADCAST = -42; // ID used in destinationID (sends to everyone in reach except the agent itself)
	protected static int idCounter = 0;
	
	private int destinationID;
	private CommunicativeAgent sender;
	private long step;
	private int messageID;
	
	public enum Performative {
		INFORM,
		REQUEST,
		REFUSE
	}
	private Performative performative;
	
	private String title;
	private String content;
	
	private float signalStrength; // set by the Environment, indicates the signal quality when this message has been sent
	
	public DroneMessage(CommunicativeAgent sender, int destinationID, Performative perf) {
		this.messageID = idCounter++;
		this.destinationID = destinationID;
		this.performative = perf;
		this.sender = sender;
		step = 0;
	}
	
	public int getMessageID() {
		return messageID;
	}
	
	public void setStep(long step) {
		this.step = step;
	}
	
	public long getStep() {
		return step;
	}
	
	public CommunicativeAgent getSender() {
		return sender;
	}
	
	public int getSenderID() {
		return sender.getID();
	}
	
	public int getDestinationID() {
		return destinationID;
	}
	
	public Performative getPerformative() {
		return performative;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setStrength(float strength) {
		this.signalStrength = strength;
	}
	
	public float getStrength() {
		return signalStrength;
	}
}
