package daniyal.ece457;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;


public class OctaveBridge {
	private OctaveEngine octave;
	
	public OctaveBridge() {
		this.octave = new OctaveEngineFactory().getScriptEngine();
	}
	
	public double classify() {		
		octave.eval("clear;");
		octave.eval("run ~/workspace/457Classifier/Spam_LRC/classify.m;");
		OctaveDouble accuracy = octave.get(OctaveDouble.class, "accuracy");
		 
//		System.out.println("Completed a classification!");
		 
		return accuracy.get(1);
	}
	
	public void close() {
		this.octave.close();
	}
}
