package FS_Algorithms.aco;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;

import OctaveBridge;

public class Ant {

	protected int numFeatures;
	protected int solutionSet[];
	protected boolean visited[];

	public Ant(int numFeatures) {
		this.numFeatures = numFeatures;
		this.solutionSet = new int[numFeatures];
		this.visited = new boolean[numFeatures];
		
		for (int i = 0; i < solutionSet.length; i++) {
			solutionSet[i] = -1;
		}
	}

	protected void visitFeature(int currentIndex, int feature) {
		solutionSet[currentIndex + 1] = feature;
		visited[feature] = true;
	}

	protected boolean visited(int i) {
		return visited[i];
	}

	protected double solutionCost(List<Set<String>> smsData, List<Boolean> isSpam, String[] featureSet, OctaveBridge octave,
				int currentIndex) {

//		int[] solutionToTest = new int[currentIndex + 1];
//	    	for (int i = 0; i < solutionToTest.length; i++) {
//	    		solutionToTest[i] = solutionSet[i];
//	    	}
	    	
	    	try {
	    		FileWriter fw = new FileWriter("Spam_LRC/test.txt");
	    		BufferedWriter br = new BufferedWriter(fw);
	    		
	    		int smsCounter = 0;
	    		for (Set<String> s : smsData) {
	    			
	    			for (int i = 0; i <= currentIndex; i++) {
	    				if (s.contains(featureSet[solutionSet[i]]))
	    					br.append("1,");
	    				else
	    					br.append("0,");
	    			}
	    			
	    			if (isSpam.get(smsCounter))
	    				br.append("1");
	    			else
	    				br.append("0");
	    			
	    			smsCounter++;
	    			br.append("\n");
	    		}
	    		
	    		br.close();
	    		
	    	} catch (Exception e) {
	    		System.out.println("Could not write to file bro...");
	    	}
	    	
	    	return 1 - octave.classify();
		
	}

	protected void clear() {
		for (int i = 0; i < numFeatures; i++) {
			visited[i] = false;
			solutionSet[i] = -1;
		}
	}

}