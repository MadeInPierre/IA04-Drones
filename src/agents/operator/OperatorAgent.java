package agents.operator;

import agents.CommunicativeAgent;
import agents.DroneMessage;
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

	}

}
