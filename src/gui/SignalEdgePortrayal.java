package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
			fileWriter = new FileWriter("2d_signals.txt");
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
		double w = ((Edge) o).getWeight();
		CommunicativeAgent from = (CommunicativeAgent) ((Edge) o).getFrom();
		CommunicativeAgent to   = (CommunicativeAgent) ((Edge) o).getTo();

		if (toDraw(from, to)) {
			DroneAgent d = (from.getID() > to.getID()) ? (DroneAgent)from : (DroneAgent)to;
			logSignals(from, to, w, d.getCommunicator().getFilteredStrengthFrom(d.getLeaderID()));
			
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

	private boolean toDraw(CommunicativeAgent from, CommunicativeAgent to) {
		if (from instanceof OperatorAgent) {
			Optional<CommunicativeAgent> closest = Environment.get().getSignalManager().getClosestAgent(from);
			if (closest.isPresent() && to == closest.get())
				return true;
			else
				return false;
		} else if (to instanceof OperatorAgent) {
			Optional<CommunicativeAgent> closest = Environment.get().getSignalManager().getClosestAgent(to);
			if (closest.isPresent() && from == closest.get())
				return true;
			else
				return false;
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
	
	private void logSignals(CommunicativeAgent from, CommunicativeAgent to, double signal, double filteredSignal) {
//		System.out.print("(" + from.getID() + "," + to.getID() + ",");
//		if(from instanceof DroneAgent) System.out.print(((DroneAgent)from).getDistanceInTunnel() + ","); else System.out.print("0.0,");
//		if(to   instanceof DroneAgent) System.out.print(((DroneAgent)to  ).getDistanceInTunnel() + ","); else System.out.print("0.0,");
//		System.out.println(signal + "," + filteredSignal + ")");
		
	    if(signal < Constants.DRONE_MAXIMUM_SIGNAL_LOSS || filteredSignal < Constants.DRONE_MAXIMUM_SIGNAL_LOSS) 
	    	printWriter.printf("%d,%d,%d,%f,%f,%f,%f\n", Environment.get().schedule.getSteps(),
		    										   from.getID(), 
		    										   to.getID(), 
		    										   (from instanceof DroneAgent) ? ((DroneAgent)from).getDistanceInTunnel() : 0, 
		    										   (to   instanceof DroneAgent) ? ((DroneAgent)to  ).getDistanceInTunnel() : 0, 
		    										   signal, filteredSignal);
	}

}
