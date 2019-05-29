package gui;

import java.awt.Color;

import javax.swing.JFrame;

import environment.Environment;
import main.Constants;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;

public class Gui extends GUIState {

	public Display2D display;
	public JFrame displayFrame;
	FastValueGridPortrayal2D signalPortrayal = new FastValueGridPortrayal2D();

	public static void main(String[] args) {
		Gui vid = new Gui();
		Console c = new Console(vid);

		c.setVisible(true);
	}

	public Gui() {
		super(new Environment(System.currentTimeMillis()));
	}

	public Gui(SimState state) {
		super(state);
	}

	public void start() {
		super.start();
		setupPortrayals();
	}

	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals() {
		Environment env = (Environment) state;
		signalPortrayal.setField(env.getSignalManager().getSignalLossField());
		signalPortrayal.setMap(
				new SimpleColorMap(Constants.MIN_SIGNAL_LOSS, Constants.MAX_SIGNAL_LOSS, Color.WHITE, Color.RED));
		display.reset();
		display.setBackdrop(Color.white);
		display.repaint();
	}

	public void init(Controller c) {
		super.init(c);
		display = new Display2D(1000, 1000 * Constants.MAP_HEIGHT / Constants.MAP_WIDTH, this);
		display.setClipping(false);
		displayFrame = display.createFrame();
		displayFrame.setTitle("Environment Display");
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		display.attach(signalPortrayal, "signal");
	}

	public void quit() {
		super.quit();
		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;
	}

}
