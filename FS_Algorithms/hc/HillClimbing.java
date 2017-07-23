package FS_Algorithms.hc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import OctaveBridge;

public class HillClimbing {
	public static List<Set<String>> smsData;
	public static String[] featureSet;
	public static List<Boolean> isSpam;
	public static OctaveBridge octave;
	public static Random random = new Random();
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		smsData = new ArrayList<>();
		Set<String> features = new HashSet<>();
		isSpam = new ArrayList<>();
		octave = new OctaveBridge();
    	
        try {
	        	FileReader fr = new FileReader("Youtube01-Psy.csv");
	        	BufferedReader br = new BufferedReader(fr);
	        	
	        	String line = "";
	        	
	        	while ((line = br.readLine()) != null) {
	        		Set<String> smsWords = new HashSet<>();
	        		line = line.replaceAll("[^A-Za-z0-9 ]", " ");
				String[] words = line.split("\\.|,|-|\\s|\\t|\\?|\\(|\\)|;|:|\"");
				
				if (words[0].equals("spam")) {
					isSpam.add(true);
				} else {
					isSpam.add(false);
				}
				
				for (int i = 1; i < words.length; i++) {
					smsWords.add(words[i]);
					features.add(words[i]);
				}
				
				smsData.add(smsWords);
	        	}
	        	
	        	br.close();
        	
        } catch (Exception e){
        		System.out.println("Failed while trying to read in th data!");
        }
        
        featureSet = new String[features.size()];
        Iterator<String> it = features.iterator();
        int index = 0;
        
        while (it.hasNext()) {
        		featureSet[index] = it.next();
        		index++;
        }
        
        
        int[] solution = new int[featureSet.length];
        
        for (int i = 0; i < solution.length; i++) {
        		if (ThreadLocalRandom.current().nextDouble(0, 1) > 0.5)
        			solution[i] = 1;
        		else
        			solution[i] = 0;
        }
        
        startHC(solution);
        
	}
	
	public static void startHC(int[] solution) {
		double goalAccuracy = 0.95;
		double currentAccuracy = calculateAccuracy(solution);
		double iterationAccuracy = 0.0;
		double delta = currentAccuracy - iterationAccuracy;
		int[] potentialSolution;
		
		while (currentAccuracy < goalAccuracy) {
			potentialSolution = flipBits(solution);
			iterationAccuracy = calculateAccuracy(potentialSolution);
			delta = currentAccuracy - iterationAccuracy;
			
			if (delta < 0) {
				//change only if the solution is a better one...
				currentAccuracy = iterationAccuracy;
				solution = potentialSolution;
				System.out.println("Allowed prev Solution " + currentAccuracy);
			}
				
		}
		
		System.out.println("Done, final accuracy was " + currentAccuracy);
	}
	
	public static int[] flipBits(int[] solution) {
		int[] result = new int[solution.length];

		int bitToFlip = ThreadLocalRandom.current().nextInt(0, solution.length);
		
		for (int i = 0; i < result.length; i++) {
			if (i % bitToFlip == 0) {
				if (solution[i] == 1)
					result[i] = 0;
				else
					result[i] = 1;
			} else {
				result[i] = solution[i];
			}
			
		}
		
		return result;
	}
	
	public static double calculateAccuracy(int[] solution) {
		StringBuilder sb = new StringBuilder();
		List<String> extractedFeatures = new ArrayList<>();
		
		for (int i = 0; i < solution.length; i++) {
			if (solution[i] == 1) {
				extractedFeatures.add(featureSet[i]);
//				System.out.println("Extracted feature: " + featureSet[i]);
			}
		}
		
    		int smsCounter = 0;
    		for (Set<String> s : smsData) {
    			
    			for (String f : extractedFeatures) {
    				if (s.contains(f))
    					sb.append(1 + ",");
    				else
    					sb.append(0 + ",");
    			}
    			
    			if (isSpam.get(smsCounter))
    				sb.append(1);
    			else
    				sb.append(0);
    			
    			smsCounter++;
    			sb.append("\n");
    		}
    		
    		Writer writer = null;
	    			
	    	try {
	    		 writer = new BufferedWriter(new OutputStreamWriter(
	   		          new FileOutputStream("Spam_LRC/test.txt"), "utf-8"));
	   		 writer.write(sb.toString());
	    		
	    	} catch (Exception e) {
	    		System.out.println("Could not write to file bro...");
	    	} finally {
	    		try {writer.close();} catch (Exception ex) {/*ignore*/}
	    	}
	    	
	    	return octave.classify();
	}

}
