package gui;

import java.awt.Color;
import java.awt.Graphics2D;

import agents.CommunicativeAgent;
import agents.drone.DroneAgent;
import agents.operator.OperatorAgent;
import main.Constants;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;

public class SignalEdgePortrayal extends SimpleEdgePortrayal2D {

	public SignalEdgePortrayal() {
		super(null, Color.WHITE);
	}

	@Override
	public String getLabel(Edge e, EdgeDrawInfo2D edi) {
		CommunicativeAgent from = (CommunicativeAgent) e.getFrom();
		CommunicativeAgent to = (CommunicativeAgent) e.getTo();
		if (from instanceof OperatorAgent || to instanceof OperatorAgent
				|| (from instanceof DroneAgent && to instanceof DroneAgent
						&& (to.getID() == ((DroneAgent) from).getLeaderID()
								|| from.getID() == ((DroneAgent) to).getLeaderID())))
			return String.format("%.1f", e.getWeight());
		return "";
	}

	@Override
	public void draw(Object o, Graphics2D g, DrawInfo2D i) {
		double w = ((Edge) o).getWeight();
		CommunicativeAgent from = (CommunicativeAgent) ((Edge) o).getFrom();
		CommunicativeAgent to = (CommunicativeAgent) ((Edge) o).getTo();
		if (from instanceof OperatorAgent || to instanceof OperatorAgent
				|| (from instanceof DroneAgent && to instanceof DroneAgent
						&& (to.getID() == ((DroneAgent) from).getLeaderID()
								|| from.getID() == ((DroneAgent) to).getLeaderID()))) {
			if (w > Constants.DRONE_MAXIMUM_SIGNAL_LOSS) {
				setShape(SHAPE_THIN_LINE);
				this.fromPaint = this.toPaint = Color.black;
			} else {
				setShape(SHAPE_LINE_BUTT_ENDS);
				float f = (float) Math.max(Math.min((w / Constants.DRONE_MAXIMUM_SIGNAL_LOSS), 1), 0);
				this.fromPaint = this.toPaint = new Color(f, 1 - f, 0f);
			}

			super.draw(o, g, i);
		}

	}

	@Override
	protected double getPositiveWeight(Object o, EdgeDrawInfo2D i) {
		return Math.max(1 - ((Edge) o).getWeight() / Constants.DRONE_MAXIMUM_SIGNAL_LOSS, 0f);
	}

}
