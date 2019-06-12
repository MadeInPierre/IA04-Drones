package agents.operator;

import agents.CommunicativeAgent;
import agents.DroneMessage;
import agents.drone.DroneAgent;
import environment.Environment;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class OperatorAgent extends CommunicativeAgent implements Steppable, KeyListener {

	private Environment env;
	public static double x, y;
	public boolean forward;
	public DroneAgent tail;


	public OperatorAgent(){
		JFrame frame = new JFrame("Key Listener");
		Container contentPane = frame.getContentPane();
		JTextField textField = new JTextField();
		textField.addKeyListener(this);
		contentPane.add(textField, BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
	}


	public void keyPressed(KeyEvent key)
	{

		int codeDeLaTouche = key.getKeyCode();

		switch (codeDeLaTouche) // Les valeurs sont contenue dans KeyEvent. Elles commencent par VK_ et finissent par le nom de la touche
		{
			case KeyEvent.VK_UP: // si la touche enfoncée est celle du haut
				y = -1;
				break;
			case KeyEvent.VK_LEFT: // si la touche enfoncée est celle de gauche
				x = -1;
				break;
			case KeyEvent.VK_RIGHT: // si la touche enfoncée est celle de droite
				x = 1;
				break;
			case KeyEvent.VK_DOWN: // si la touche enfoncée est celle du bas
				y = 1;
				break;
		}
	}

	public void keyReleased(KeyEvent evt){
		if((evt.getKeyCode() == KeyEvent.VK_UP))
			forward = false;
	}

	public void keyTyped(KeyEvent evt) {

	}


	public void step(SimState state) {

		env = (Environment) state;
		String mesContent;

		if (y == -1){
			mesContent = "y " + "-1";
			y = 0;
		}
		else if (y == 1){
			mesContent = "y " + "1";
			y = 0;
		}
		else if(x == -1){
			mesContent = "x " + "-1";
			x = 0;
		}
		else if(x == 1){
			mesContent = "x " + "1";
			x = 0;
		}
		else {
			mesContent = "null";
		}

		if (mesContent != "null") {
			System.out.println(mesContent);
			DroneMessage msg = new DroneMessage(this, 0, DroneMessage.Performative.INFORM);
			msg.setTitle("moveHead");
			msg.setContent(mesContent);
			communicator.sendMessageToDrone(msg);
		}

	}

}
