package agents.operator;

import agents.CommunicativeAgent;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneRole;
import agents.drone.DroneAgent.DroneState;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import main.Constants;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Optional;

public class OperatorAgent extends CommunicativeAgent implements Steppable, KeyListener {
	public static double x, y;
	public boolean rth = false; // Return to Home mode : all drones autonomous go back to land at home
	public boolean rth_fired = false;

	public OperatorAgent() {
		Frame[] frames = JFrame.getFrames();
		JFrame frame;
		for (int i = 0; i < frames.length; i++) {
			if (frames[i].getTitle() == "Environment Display") {
				frame = (JFrame) frames[i];
				JTextField textField = new JTextField();
				textField.addKeyListener(this);
				frame.add(textField, BorderLayout.NORTH);
				frame.addKeyListener(this);
				frame.pack();
				textField.requestFocus();
				textField.setText("CLICK HERE TO CONTROL HEAD DRONE");
				break;
			}
		}
	}

	public void keyPressed(KeyEvent key) {
		int codeDeLaTouche = key.getKeyCode();
		switch (codeDeLaTouche) // Les valeurs sont contenue dans KeyEvent. Elles commencent par VK_ et
								// finissent par le nom de la touche
		{
		case KeyEvent.VK_UP:
			y = -Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_LEFT:
			x = -Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_RIGHT:
			x = Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_DOWN:
			y = Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_H:
			rth = true;
			break;
		case KeyEvent.VK_Q:
			System.exit(1);
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {
		int k = evt.getKeyCode();
		if (k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN)
			y = 0;
		
		if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_LEFT)
			x = 0;
	}

	public void keyTyped(KeyEvent evt) {

	}

	public void step(SimState state) {
		Optional<CommunicativeAgent> tail = Environment.get().getSignalManager().getClosestAgent(this); // TODO real association
		
		// Process messages
		ArrayList<DroneMessage> garbageMessages = new ArrayList<DroneMessage>();
		for(DroneMessage msg : communicator.getMessages()) {
			if (msg.getTitle() == "tunnel_dist" && msg.getPerformative() == Performative.INFORM) {
//				System.out.println(msg.getContent());
				garbageMessages.add(msg);
			}
		}
		for(DroneMessage msg : garbageMessages) communicator.removeMessage(msg);
		
		if(tail.isPresent() && communicator.getLastStatusFrom(tail.get().getID()) != null) 
			log(String.valueOf(communicator.getLastStatusFrom(tail.get().getID()).getContent()));
		
		
		// Send usual status message (used by others for signal strength)
		DroneMessage statusmsg = new DroneMessage(this, DroneMessage.BROADCAST, Performative.INFORM);
		statusmsg.setTitle("status");
		communicator.sendMessageToDrone(statusmsg);

//		y = -Constants.DRONE_SPEED; // TODO remove
		String mesContent = "x " + x + ";y " + y;

		if (x != 0 || y != 0) {
			if (tail.isPresent()) {
				DroneMessage msg = new DroneMessage(this, tail.get().getID(), DroneMessage.Performative.REQUEST);
				msg.setTitle("moveHead");
				msg.setContent(mesContent);
				communicator.sendMessageToDrone(msg);
			}
		}
		
		if (rth == true && rth_fired == false) {
			if (tail.isPresent()) {
				// Ask for the whole chain to switch followers and leaders
				DroneMessage msg = new DroneMessage(this, tail.get().getID(), DroneMessage.Performative.REQUEST);
				msg.setTitle("switch_chain");
				communicator.sendMessageToDrone(msg);
				
				// Tell the tail drone to land near the base
				msg = new DroneMessage(this, tail.get().getID(), DroneMessage.Performative.REQUEST);
				msg.setTitle("rth");
				msg.setContent(String.valueOf(getID()));
				communicator.sendMessageToDrone(msg);
				System.out.println("BASE SENT RTH");
				rth_fired = true;
			}
		}

	}

}
