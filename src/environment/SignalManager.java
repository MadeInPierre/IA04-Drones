package environment;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import agents.CommunicativeAgent;
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
	
	/* Signal calculation method : combines 5 different types of losses  
	 * 		- Map random noise :
	 * 			- random loss at each air pixel
	 * 			- wall pixels have their own loss
	 * 		- Path loss (Large scale fading): 
	 * 			- regular loss simply based on distance
	 * 		- Shadowing (Large scale fading) : 
	 * 			- Walls have a high loss exponent
	 * 			- Introduces an additional loss when we are turning 
	 * 		- Multipath (Small Scale fading) :
	 * 			- Sin wave based on the drone's distance in tunnel (quickly varies with a high amplitude)
	 * 		- Random noise : standard unknown noise (drones' movements, motor radiation...)
	 */

	public SignalManager(float width, float height, float step, String signalImage, Environment env) {
		int cellsW = (int) Math.ceil(width / step);
		int cellsH = (int) Math.ceil(height / step);
		this.originalLossField = new double[cellsW][cellsH];
		this.droneNetwork = new Network(false);
		this.step = step;
		this.env = env;
		initializeMap(signalImage);
		this.signalLossField = new DoubleGrid2D(originalLossField);
		System.out.println("Signal map initialized with " + cellsW + " x " + cellsH + " cells.");

		buildDroneNetwork();
		updateNetwork();
	}

	public void buildDroneNetwork() {
		for (CommunicativeAgent d : env.getAgents()) {
			droneNetwork.addNode(d);
			droneNetwork.addEdge(d, d, null); // will get reversed
		}
		// create complete graph
		droneNetwork = droneNetwork.getGraphComplement(true);
	}

	private float getQualityAtPoint(Double2D position) {
		int x = (int) Math.ceil(position.getX() / this.step);
		int y = (int) Math.ceil(position.getY() / this.step);
		x = signalLossField.stx(x);
		y = signalLossField.sty(y);

		return (float) this.signalLossField.get(x, y);
	}

	private void initializeMap(String image) {
		File imgPath = new File(image);
		BufferedImage img;
		try {
			img = ImageIO.read(imgPath);

			if (img.getWidth() != originalLossField.length || img.getHeight() != originalLossField[0].length) {
				Image tmp = img.getScaledInstance(originalLossField.length, originalLossField[0].length, Image.SCALE_DEFAULT);
				BufferedImage newImg = new BufferedImage(originalLossField.length, originalLossField[0].length, img.getType());
				Graphics2D g2d = newImg.createGraphics();
				g2d.drawImage(tmp, 0, 0, null);
				g2d.dispose();
				img = newImg;
			}

			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					int r = (img.getRGB(x, y) >> 16) & 0xff;
					// map red 0->255 to signal MIN -> MAX
					float q = Constants.SIGNAL_MIN_LOSS + (float) r / 255f * (Constants.SIGNAL_MAX_LOSS - Constants.SIGNAL_MIN_LOSS);
					if(r >= 254) { q = Constants.SIGNAL_WALL_LOSS; }
					this.originalLossField[x][y] = q;
				}
			}
		} catch (IOException e) {
			System.err.println("Unable to load signal map image !");
			e.printStackTrace();
		}
	}

	private float getPathLoss(float lossExponent, float distance) {
		if (distance <= this.step)
			return 0;
		return (float) Math.max((10 * lossExponent * Math.log10(distance)), 0f);
	}

	private float getExponentLoss(Double2D pos1, Double2D pos2) {
		int n_points = (int) (pos1.distance(pos2) / this.step * 10);
		double x, y;
		float qualitySum = 0f;
		for (int i = 0; i < n_points; i++) {
			x = pos1.getX() + i * (pos2.getX() - pos1.getX()) / n_points;
			y = pos1.getY() + i * (pos2.getY() - pos1.getY()) / n_points;
			float q = getQualityAtPoint(new Double2D(x, y));
			qualitySum += q;
		}

		return qualitySum / n_points;
	}

//	public float getSignalLoss(Double2D pos1, Double2D pos2) {
//		float lossExponent = getExponentLoss(pos1, pos2);
//		float distance = (float) pos1.distance(pos2);
//		return getPathLoss(lossExponent, distance);
//	}
	
	public float getSignalLoss(CommunicativeAgent agent1, CommunicativeAgent agent2, boolean enableGaussian) {
		// tmp variables
		Random r = new Random();
		float d1 = (agent1 instanceof DroneAgent) ? (float)((DroneAgent)agent1).getDistanceInTunnel() : 0f;
		float d2 = (agent2 instanceof DroneAgent) ? (float)((DroneAgent)agent2).getDistanceInTunnel() : 0f;
		
		// Usable distances for calculating losses
		float tunnel_distance  = Math.abs(d2 - d1); // Distance between two agents in the tunnel 1D dimension
		float direct_distance  = (float)env.getDronePos(agent1).distance(env.getDronePos(agent2)); // Simple Euclidean distance
		float turning_distance = Math.abs(tunnel_distance - direct_distance); // Rough estimate of the amount of turning there is 
		
		// Calculating Path Loss
		float pathLossExponent = getExponentLoss(env.getDronePos(agent1), env.getDronePos(agent2));
		float pathLoss = getPathLoss(pathLossExponent, (float)env.getDronePos(agent1).distance(env.getDronePos(agent2)));

		// Calculating Shadowing loss
		float shadowingLoss = Math.max(Constants.SIGNAL_SHADOWING_LOSS * turning_distance, 0f);
		
		// Calculating Multipath loss
		float multipathLoss = Constants.SIGNAL_MULTIPATH_LOSS_AMP * (float)Math.sin(Constants.SIGNAL_MULTIPATH_LOSS_PER * tunnel_distance);
		
		float loss = Constants.DRONE_BEST_SIGNAL_LOSS + pathLoss /*+ shadowingLoss + multipathLoss*/;
		
		if(enableGaussian)
			loss += Constants.SIGNAL_RANDOM_LOSS_STD * (float)r.nextGaussian();
		return (loss > 0) ? loss : 0f;
	}

	public Map<CommunicativeAgent, Float> getAgentsInRange(CommunicativeAgent agent) {
		Map<CommunicativeAgent, Float> ret = new HashMap<CommunicativeAgent, Float>();
		for (CommunicativeAgent d : Environment.get().getAgents()) {
			float loss = getSignalLoss(agent, d, true);
			if (loss < Constants.DRONE_MAXIMUM_SIGNAL_LOSS)
				ret.put(d, loss);
		}
		return ret;
	}
	
	public Optional<CommunicativeAgent> getClosestAgent(CommunicativeAgent agent) {
		return env.getAgents().stream().sorted(Comparator.comparing(o -> getSignalLoss(o, agent, true)))
		.filter(o -> o != agent).findFirst();

	}

	public DoubleGrid2D getSignalLossField() {
		return signalLossField;
	}

	public Network getSignalNetwork() {
		return droneNetwork;
	}

	@Override
	public void step(SimState arg0) {
		if (arg0.schedule.getSteps() % 50 == 0)
			;//updateGaussianNoise();
		updateNetwork();
	}

	private void updateGaussianNoise() {
		Random r = new Random();
		for (int x = 0; x < signalLossField.getWidth(); x++) {
			for (int y = 0; y < signalLossField.getHeight(); y++) {
				if(originalLossField[x][y] != Constants.SIGNAL_MIN_LOSS) {
					double q = originalLossField[x][y] + r.nextGaussian() * Constants.SIGNAL_STD_LOSS;
					if (q < Constants.SIGNAL_MIN_LOSS)
						q = Constants.SIGNAL_MIN_LOSS;
					else if (q > Constants.SIGNAL_MAX_LOSS)
						q = Constants.SIGNAL_MAX_LOSS;

					if(signalLossField.get(x,  y) < Constants.SIGNAL_WALL_LOSS - 0.1) signalLossField.set(x, y, q);
				}
			}
		}
	}

	private void updateNetwork() {
		Edge[][] edges = droneNetwork.getAdjacencyMatrix();
		for (int i = 0; i < edges.length; i++) {
			for (int j = i + 1; j < edges[0].length; j++) {
				edges[i][j].setWeight(getSignalLoss((CommunicativeAgent)edges[i][j].getFrom(), 
													(CommunicativeAgent)edges[i][j].getTo(), true));
			}
		}
	}
}
