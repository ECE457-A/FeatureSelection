package FS_Algorithms.aco;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import OctaveBridge;

public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Set<String>> smsData = new ArrayList<>();
		Set<String> featureSet = new HashSet<>();
		List<Boolean> isSpam = new ArrayList<>();
    	
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
					featureSet.add(words[i]);
				}
				
				smsData.add(smsWords);
        	}
        	
        	br.close();
        	
        } catch (Exception e){
        	System.out.println("Failed while trying to read in th data!");
        }
        
        
		AntColonyOptimization myColon = new AntColonyOptimization(featureSet.size(), smsData, isSpam, featureSet);
		myColon.solve();
	}

}

