package environment;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import agents.drone.DroneAgent;
import main.Constants;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Double2D;

public class SignalManager implements Steppable {
	private double[][] originalLossField;
	private DoubleGrid2D signalLossField;
	private float step;
	private Network droneNetwork;
	private Environment env;

	public SignalManager(float width, float height, float step, String signalImage, Environment env) {
		int cellsW = (int) Math.ceil(width / step);
		int cellsH = (int) Math.ceil(height / step);
		this.originalLossField = new double[cellsW][cellsH];
		this.droneNetwork = new Network(false);
		this.step = step;
		initializeMap(signalImage);
		this.signalLossField = new DoubleGrid2D(originalLossField);
		this.env = env;
		System.out.println("Signal map initialized with " + cellsW + " x " + cellsH + " cells.");

		buildDroneNetwork();
	}

	public void buildDroneNetwork() {
		for (DroneAgent d : env.getDrones()) {
			droneNetwork.addNode(d);
			droneNetwork.addEdge(d, d, null); // will get reversed
		}
		// create complete graph
		droneNetwork = droneNetwork.getGraphComplement(true);
	}

	private float getQualityAtPoint(Double2D position) {
		int x = (int) Math.ceil(position.getX() / this.step);
		int y = (int) Math.ceil(position.getY() / this.step);
		return (float) this.signalLossField.get(x, y);
	}

	private void initializeMap(String image) {
		File imgPath = new File(image);
		BufferedImage img;
		try {
			img = ImageIO.read(imgPath);
			assert img.getWidth() == signalLossField.getWidth();
			assert img.getHeight() == signalLossField.getHeight();
			System.out.println(img.getWidth());
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					int r = (img.getRGB(x, y) >> 16) & 0xff;
					// map red 0->255 to signal MIN -> MAX
					float q = Constants.MIN_SIGNAL_LOSS
							+ (float) r / 255f * (Constants.MAX_SIGNAL_LOSS - Constants.MIN_SIGNAL_LOSS);
					this.originalLossField[x][y] = q;
				}
			}
		} catch (IOException e) {
			System.err.println("Unable to load signal map image !");
			e.printStackTrace();
		}
	}

	private float getPathLoss(float lossExponent, float distance) {
		return (float) (10 * lossExponent * Math.log10(distance));
	}

	private float getExponentLoss(Double2D pos1, Double2D pos2) {
		int n_points = (int) (pos1.distance(pos2) / this.step * 10);
		Point2D tmpPoint = new Point2D.Float();
		double x, y;
		float qualitySum = 0f;
		for (int i = 0; i < n_points; i++) {
			x = pos1.getX() + i * (pos2.getX() - pos1.getX()) / n_points;
			y = pos1.getY() + i * (pos2.getY() - pos1.getY()) / n_points;
			qualitySum += getQualityAtPoint(new Double2D(x, y));
		}

		return qualitySum / n_points;
	}

	public float getSignalLoss(Double2D pos1, Double2D pos2) {
		float lossExponent = getExponentLoss(pos1, pos2);
		float distance = (float) pos1.distance(pos2);
		return getPathLoss(lossExponent, distance);
	}

	public Map<DroneAgent, Float> getDronesInRange(Double2D dronePos, Set<DroneAgent> allDrones) {
		Map<DroneAgent, Float> ret = new HashMap<DroneAgent, Float>();
		for (DroneAgent d : allDrones) {
			float strength = Constants.EMITTER_SIGNAL_STRENGTH - getSignalLoss(dronePos, env.getDronePos(d));
			if (strength >= Constants.MINIMUM_SIGNAL_STRENGTH)
				ret.put(d, strength);
		}
		return ret;
	}

	public DoubleGrid2D getSignalLossField() {
		return signalLossField;
	}

	public Network getSignalNetwork() {
		return droneNetwork;
	}

	@Override
	public void step(SimState arg0) {
		// if (arg0.schedule.getSteps() % 50 == 0)
		// updateGaussianNoise();

		updateNetwork();
	}

	private void updateGaussianNoise() {
		Random r = new Random();
		for (int x = 0; x < signalLossField.getWidth(); x++) {
			for (int y = 0; y < signalLossField.getHeight(); y++) {
				double q = originalLossField[x][y] + r.nextGaussian() * Constants.SIGNAL_QUALITY_STD;
				if (q < Constants.MIN_SIGNAL_LOSS)
					q = Constants.MIN_SIGNAL_LOSS;
				else if (q > Constants.MAX_SIGNAL_LOSS)
					q = Constants.MAX_SIGNAL_LOSS;

				signalLossField.set(x, y, q);
			}
		}
	}

	private void updateNetwork() {
		Edge[][] edges = droneNetwork.getAdjacencyMatrix();

		for (int i = 0; i < edges.length; i++) {
			for (int j = i + 1; j < edges[0].length; j++) {
				Double2D pos1 = env.getDronePos((DroneAgent) edges[i][j].getFrom());
				Double2D pos2 = env.getDronePos((DroneAgent) edges[i][j].getTo());
				edges[i][j].setWeight(getSignalLoss(pos1, pos2));
			}
		}
	}
}
