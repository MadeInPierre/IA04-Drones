package agents.operator;

import agents.CommunicativeAgent;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.DroneAgent;
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
		case KeyEvent.VK_UP: // si la touche enfoncée est celle du haut
			y = -Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_LEFT: // si la touche enfoncée est celle de gauche
			x = -Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_RIGHT: // si la touche enfoncée est celle de droite
			x = Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_DOWN: // si la touche enfoncée est celle du bas
			y = Constants.DRONE_SPEED;
			break;
		case KeyEvent.VK_H: // si la touche enfoncée est H
			rth = true;
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
		// Send usual status message (used by others for signal strength)
		DroneMessage statusmsg = new DroneMessage(this, DroneMessage.BROADCAST, Performative.INFORM);
		statusmsg.setTitle("status");
		communicator.sendMessageToDrone(statusmsg);

		String mesContent = "x " + x + ";y " + y;

		if (x != 0 || y != 0) {
			Optional<CommunicativeAgent> tail = Environment.get().getSignalManager().getClosestAgent(this);
			if (tail.isPresent()) {
				DroneMessage msg = new DroneMessage(this, tail.get().getID(), DroneMessage.Performative.REQUEST);
				msg.setTitle("moveHead");
				msg.setContent(mesContent);
				communicator.sendMessageToDrone(msg);
			}
		}
		
		if (rth == true && rth_fired == false) {
			Optional<CommunicativeAgent> tail = Environment.get().getSignalManager().getClosestAgent(this);
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
