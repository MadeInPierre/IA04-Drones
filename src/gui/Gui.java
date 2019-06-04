package gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import environment.Environment;
import main.Constants;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.util.gui.SimpleColorMap;

public class Gui extends GUIState {

	public Display2D display;
	public JFrame displayFrame;
	FastValueGridPortrayal2D signalPortrayal = new FastValueGridPortrayal2D();
	NetworkPortrayal2D signalNetworkPortrayal = new NetworkPortrayal2D();
	FastValueGridPortrayal2D collisionPortrayal = new FastValueGridPortrayal2D();
	ContinuousPortrayal2D yardPortrayal;// = new ContinuousPortrayal2D();
	// protected Environment sim;

	public static void main(String[] args) {
		Environment model = new Environment(System.currentTimeMillis());
		Gui vid = new Gui(model);
		Console c = new Console(vid);

		c.setVisible(true);
	}

	public Gui() {
		super(new Environment(System.currentTimeMillis()));
	}

	public Gui(SimState state) {
		super(state);
		// sim = (Environment) state;
		this.yardPortrayal = new ContinuousPortrayal2D();
	}

	public void start() {
		super.start();
		setupPortrayals();
		setupPortrayals2();
	}

	public void load(SimState state) {
		super.load(state);
	}

	public void setupPortrayals() { // TODO la performance diminue beaucoup quand on appelle cette methode
		Environment env = (Environment) state;

		signalPortrayal.setField(env.getSignalManager().getSignalLossField());
		signalPortrayal.setMap(new SimpleColorMap(Constants.MIN_SIGNAL_LOSS, Constants.MAX_SIGNAL_LOSS,
				new Color(1f, 1f, 1f, 0f), new Color(1f, 0f, 0f, .5f)));
		signalNetworkPortrayal.setField(new SpatialNetwork2D(env.yard, env.getSignalManager().getSignalNetwork()));
		SimpleEdgePortrayal2D sep = new SimpleEdgePortrayal2D() {
			@Override public String getLabel(Edge e, EdgeDrawInfo2D edi) { return e.getWeight()+""; }
		    @Override public void draw(Object o, Graphics2D g, DrawInfo2D i) { 
		    	double w = ((Edge) o).getWeight();
		    	if (w > Constants.DRONE_MAXIMUM_SIGNAL_LOSS) {
		    		setShape(SHAPE_THIN_LINE);
		    		this.fromPaint = this.toPaint = new Color(1f, 0f, 0f, 0.5f);
		    	} else {
		    		setShape(SHAPE_LINE_BUTT_ENDS);
		    		float f = (float) (w / Constants.DRONE_MAXIMUM_SIGNAL_LOSS);
		    		this.fromPaint = this.toPaint = new Color(f, 1 - f, 0f);
		    	}
		    	super.draw(o, g, i);
		    }
		    @Override protected double getPositiveWeight(Object o, EdgeDrawInfo2D i) { return 1 - ((Edge) o).getWeight() / Constants.DRONE_MAXIMUM_SIGNAL_LOSS; }
		};
		sep.setBaseWidth(0.2f);
		signalNetworkPortrayal.setPortrayalForAll(sep);
		

		collisionPortrayal.setField(env.getCollisionManager().getCollisionMap());
		collisionPortrayal.setMap(new SimpleColorMap(0, 1, new Color(0, 0, 0, 0), Color.BLACK));

		display.reset();
		display.setBackdrop(Color.white);
		display.repaint();
	}

	public void setupPortrayals2() {
		Environment simulation = (Environment) state;
		yardPortrayal.setField(simulation.yard);
		display.reset();
		display.setBackdrop(Color.orange);
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
		display.attach(collisionPortrayal, "collision");
		display.attach(signalPortrayal, "signal");
		display.attach(yardPortrayal, "cave");
		display.attach(signalNetworkPortrayal, "signal network");
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
			String image = "img/cave.jpg";
			File imgPath = new File(image);
			BufferedImage b = ImageIO.read(imgPath);
			Graphics g = b.getGraphics();
			int w = 1000;
			int h = (int)(1000 * Constants.MAP_HEIGHT / Constants.MAP_WIDTH);
			//g.drawImage(b, 0, 0, w, h, null);
			//g.dispose();
			display.setBackdrop(new TexturePaint(b, new Rectangle(0, 0, w, h)));
		} catch (IOException e) {

		}
	}

}
