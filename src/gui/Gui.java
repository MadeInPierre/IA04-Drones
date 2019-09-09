package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import agents.drone.DroneAgent;
import agents.operator.OperatorAgent;
import environment.Environment;
import main.Constants;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display.RateAdjuster;
import sim.engine.SimState;
import sim.portrayal.Portrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.gui.SimpleColorMap;

public class Gui extends GUIState {

	public Display2D display;
	public JFrame displayFrame;
	FastValueGridPortrayal2D signalPortrayal = new FastValueGridPortrayal2D();
	NetworkPortrayal2D signalNetworkPortrayal = new NetworkPortrayal2D();
	FastValueGridPortrayal2D collisionPortrayal = new FastValueGridPortrayal2D();
	CollisionSensorPortrayal collisionSensorPortrayal = new CollisionSensorPortrayal();
	ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();

	public static void main(String[] args) {
		Environment model = Environment.get();
		Gui vid = new Gui(model);
		Console c = new Console(vid);
		
		vid.scheduleRepeatingImmediatelyAfter(new RateAdjuster(100.0)); // FPS Cap
		
		c.setVisible(true);
		c.pressPlay();
	}

	public Gui() {
		super(Environment.get());
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
	}

	public void setupPortrayals() {
		Environment env = Environment.get();

		collisionSensorPortrayal.setField(env.getYard());
		collisionPortrayal.setMap(new SimpleColorMap(0, 1, new Color(0, 0, 0, 0), Color.BLACK));

		signalPortrayal.setField(env.getSignalManager().getSignalLossField());
		signalPortrayal.setMap(new SimpleColorMap(Constants.SIGNAL_MIN_LOSS, Constants.SIGNAL_MAX_LOSS,
				new Color(1f, 1f, 1f, 0f), new Color(1f, 0f, 0f, .5f)));

		signalNetworkPortrayal.setField(new SpatialNetwork2D(env.getYard(), env.getSignalManager().getSignalNetwork()));
		SignalEdgePortrayal edge = new SignalEdgePortrayal();
		edge.setBaseWidth(0.4f);
		edge.setLabelScaling(1);
		edge.setAdjustsThickness(true);
		signalNetworkPortrayal.setPortrayalForAll(edge);

		yardPortrayal.setField(env.getYard());
		yardPortrayal.setPortrayalForClass(DroneAgent.class, getDronePortrayal());
		yardPortrayal.setPortrayalForClass(OperatorAgent.class,
				new LabelledPortrayal2D(new OvalPortrayal2D(Color.blue), "operator", Color.blue, false));

		display.reset();
		display.setBackdrop(Color.white);
		addBackgroundImage();
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
		display.attach(collisionPortrayal, "collision", false);
		display.attach(signalPortrayal, "signal", false);
		display.attach(collisionSensorPortrayal, "collision sensors");
		display.attach(signalNetworkPortrayal, "signal network");
		display.attach(yardPortrayal, "agents");
	}

	public void quit() {
		super.quit();
		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;
	}

	private void addBackgroundImage() {
		// Image i = new ImageIcon(getClass().getResource("img/cave.jpg")).getImage();
		// int w = i.getWidth(null)/5;
		// int h = i.getHeight(null)/5;
		// BufferedImage b =
		// display.getGraphicsConfiguration().createCompatibleImage(w,h);
		try {
			File imgPath = new File(Constants.IMAGE);
			BufferedImage b = ImageIO.read(imgPath);
			Graphics g = b.getGraphics();
			int w = 1000;
			int h = (int) (1000 * Constants.MAP_HEIGHT / Constants.MAP_WIDTH);
			// g.drawImage(b, 0, 0, w, h, null);
			// g.dispose();
			display.setBackdrop(new TexturePaint(b, new Rectangle(0, 0, w, h)));
		} catch (IOException e) {

		}
	}

	private Portrayal2D getDronePortrayal() {
		return new DronePortrayal("img/drone.png", Color.white, true);
	}

}
