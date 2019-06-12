package agents.operator;

import agents.CommunicativeAgent;
import environment.Environment;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class OperatorAgent extends CommunicativeAgent implements Steppable, KeyListener {

	private Environment env;
	public static double x, y;
	public boolean forward;


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
				y = - 1;
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
		Double2D newPos;

		Double2D position = env.getYard().getObjectLocation(this);

		if (y != 0){
			newPos = new Double2D(position.getX(), position.getY() + y);
			y = 0;
		}
		else if(x != 0){
			newPos = new Double2D(position.getX() + x, position.getY());
			x = 0;
		}
		else {
			newPos = position;
		}

		env.getYard().setObjectLocation(this, newPos);

	}

}
