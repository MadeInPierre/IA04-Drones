package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import agents.CommunicativeAgent;
import agents.drone.DroneAgent;
import agents.operator.OperatorAgent;
import environment.Environment;
import main.Constants;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;

public class SignalEdgePortrayal extends SimpleEdgePortrayal2D {
	private FileWriter fileWriter;
	private PrintWriter printWriter;
	
	public SignalEdgePortrayal() {
		super(null, Color.WHITE);
		
		try {
//			SimpleDateFormat format = new SimpleDateFormat("MM-dd-HH:mm:ss");
//			String dateString = format.format(new Date());
			
			fileWriter = new FileWriter("logs/2d_signals_" + Environment.get().getCurrentRun() + ".txt");
			printWriter = new PrintWriter(fileWriter);
		} catch(Exception e) {}
	}

	@Override
	public String getLabel(Edge e, EdgeDrawInfo2D edi) {
		CommunicativeAgent from = (CommunicativeAgent) e.getFrom();
		CommunicativeAgent to = (CommunicativeAgent) e.getTo();
		if (toDraw(from, to))
			return String.format("%.1f", e.getWeight());
		return "";
	}

	@Override
	public void draw(Object o, Graphics2D g, DrawInfo2D i) {
		CommunicativeAgent from = (CommunicativeAgent) ((Edge) o).getFrom();
		CommunicativeAgent to   = (CommunicativeAgent) ((Edge) o).getTo();
		double rawSignal = ((Edge) o).getWeight();
		double trueSignal = Environment.get().getSignalManager().getSignalLoss(from, to, false);

		if (toDraw(from, to)) {
			DroneAgent d = (from.getID() < to.getID()) ? (DroneAgent)from : (DroneAgent)to;
			
			if (Environment.get().schedule.getSteps() % 30 == 0)
				logSignals(from, to, rawSignal, d.getCommunicator().getFilteredStrengthFrom(d.getFollowerID()), trueSignal);
			
			if (rawSignal > Constants.DRONE_MAXIMUM_SIGNAL_LOSS) {
				setShape(SHAPE_THIN_LINE);
				this.fromPaint = this.toPaint = Color.black;
			} else {
				setShape(SHAPE_LINE_BUTT_ENDS);
				float f = (float) Math.max(Math.min((rawSignal / Constants.DRONE_MAXIMUM_SIGNAL_LOSS), 1), 0);
				this.fromPaint = this.toPaint = new Color(f, 1 - f, 0f);
			}
			super.draw(o, g, i);
		}

	}

	private boolean toDraw(CommunicativeAgent from, CommunicativeAgent to) {
		if (from instanceof OperatorAgent) {
			DroneAgent drone = (DroneAgent)to;
			OperatorAgent operator = (OperatorAgent)from;
			return drone.getID() == operator.getTail().getID();
		} else if (to instanceof OperatorAgent) {
			DroneAgent drone = (DroneAgent)from;
			OperatorAgent operator = (OperatorAgent)to;
			return drone.getID() == operator.getTail().getID();
		} else if (from instanceof DroneAgent && to instanceof DroneAgent) {
			if (to.getID() == ((DroneAgent) from).getLeaderID() || from.getID() == ((DroneAgent) to).getLeaderID())
				return true;
			else
				return false;
		}
		return false;
	}

	@Override
	protected double getPositiveWeight(Object o, EdgeDrawInfo2D i) {
		return Math.max(1 - ((Edge) o).getWeight() / Constants.DRONE_MAXIMUM_SIGNAL_LOSS, 0f);
	}
	
	private void logSignals(CommunicativeAgent from, CommunicativeAgent to, double rawSignal, double filteredSignal, double trueSignal) {
		int id = from.getID() > to.getID() ? to.getID() : from.getID();

		if(trueSignal < Constants.DRONE_MAXIMUM_SIGNAL_LOSS || filteredSignal < Constants.DRONE_MAXIMUM_SIGNAL_LOSS) 
	    	printWriter.printf("%d,%f,%d,%d,%f,%f,%f,%f,%f\n", id, 
	    											   		(float)(Environment.get().schedule.getSteps()) / 150.0,
	    											   		from.getID(), 
	    											   		to.getID(), 
	    											   		(from instanceof DroneAgent) ? ((DroneAgent)from).getDistanceInTunnel() : 0, 
	    											   		(to   instanceof DroneAgent) ? ((DroneAgent)to  ).getDistanceInTunnel() : 0, 
	    											   		rawSignal,
	    											   		filteredSignal,
	    											   		trueSignal);
	}

}
