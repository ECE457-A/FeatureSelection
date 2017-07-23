package FS_Algorithms.ga;
import java.util.*;
import java.io.*;

import OctaveBridge;

class Start{
	public static List<Set<String>> wordsinSMS = new ArrayList<>();
	public static List<Boolean> isSpamVector = new ArrayList<>();
	public static OctaveBridge octave = new OctaveBridge();
	
	public static void main(String[] args){
		String spamPath = "Youtube01-Psy.csv";
		File file = new File(spamPath);
		

		try{
			FileReader spamEmail = new FileReader(file.getName());
			Scanner scan = new Scanner(spamEmail);
			Map<String, HashNode> features = new HashMap<>();
			
//			scan.nextLine(); //skip first line since it has headers
			while(scan.hasNext()){
				// read through the file
				String line = scan.nextLine();
				Set<String> smsWordSet = new HashSet<>();
				// get words from lines
				line = line.replaceAll("[^A-Za-z0-9 ]", " ");
				String[] words = line.split("\\.|,|-|\\s|\\t|\\?|\\(|\\)|;|:|\"");
				//for each word add it into the features map
				boolean isSpam = false;
				for(int j = 0; j < words.length; j++){
					if(j == 0){
						if(words[j].equals("spam")){
							isSpam = true;
						}else{
							isSpam = false;
						}
						isSpamVector.add(isSpam);
					}else{

						if(features.containsKey(words[j])){
							
							HashNode temp = features.get(words[j]);
							if(isSpam){
								temp.spamCount++;
							}else{
								temp.hamCount++;
							}
						
						}else{
							
							HashNode temp = new HashNode();
							if(isSpam){
								temp.spamCount++;
							}else{
								temp.hamCount++;
							}
							features.put(words[j], temp);
						}

						smsWordSet.add(words[j]);
					}
				

				}

				wordsinSMS.add(smsWordSet);
			}
			
			List<String> keySet = new ArrayList<>(features.keySet());
			
//			Collections.sort(keySet);
//			for(String s : keySet){
//				HashNode temp = features.get(s);
//				System.out.println(s + "\t , " + temp.spamCount + ", " + temp.hamCount + ", " + (double)temp.spamCount/(double)(temp.spamCount+ temp.hamCount));
//			}
//			System.out.println(keySet.size());

			Collections.sort(keySet);
		    Classifier classifier = new Classifier(spamPath, keySet);
		    GeneticAlg gaObj = new GeneticAlg(keySet, classifier);
		    gaObj.runGA();
		
		}catch(IOException e){
			System.out.println("Failed at opening/parsing through input file for spam/ham classification" + e.getStackTrace());
		} finally {
			Start.octave.close();
		}
	}
}

class GeneticAlg{
	double uniformRate;
	double mutationRate;
	double tournamentSize;
	boolean elitism;
	int populationSize;
	int numbIterations;

	List<String> features;
	List<ParentNode> population;
	Classifier classifier;

	public GeneticAlg(List<String> features, Classifier classifier){
		this.population = new ArrayList<>();
		this.features = features;
		this.classifier = classifier;
		this.uniformRate = 0.5;
		this.mutationRate = 0.005;
		this.tournamentSize = 5;
		this.elitism = false;
		this.populationSize = 10;
		this.numbIterations = 15;
	}

	public boolean[] runGA(){
		System.out.println("Generating Initial Population");
		generateInitialSolutionPopulation(); //generate the initial population
		double maxFitness = 0.0;
		ParentNode bestParent = null;

		for(int i = 0; i < population.size(); i++){
			if(population.get(i).fitness > maxFitness){
				maxFitness = population.get(i).fitness;
				bestParent = population.get(i);
			}
		}
		System.out.println("Best Parent found so far from initial: " + maxFitness);

		int currGeneration = 0;

		while(maxFitness < 0.96 && currGeneration < numbIterations) {
			
			List<ParentNode> nextGeneration = new ArrayList<>();

			for(int i = 0; i < populationSize; i++) {
				
				ParentNode parent1 = tournamentSelection();
				ParentNode parent2 = tournamentSelection();

				boolean[] childChromosome = crossover(parent1.chromosome, parent2.chromosome);
				System.out.println("child fitness classify");
				double childFitness = classifier.classify(childChromosome); 
				ParentNode child = new ParentNode(childChromosome, childFitness); 
				
				System.out.println("child fit -> " + childFitness + " max fit -> " + maxFitness);
				if(childFitness > maxFitness){
					maxFitness = childFitness;
					bestParent = child;
				}

				nextGeneration.add(child);
			}
			System.out.println("Produced new population from cossover");
			population = mutation(nextGeneration);
			System.out.println("Mutated this population");

			currGeneration++;
			System.out.println("currGeneration is " + currGeneration);
		}
		

		System.out.println("Reducing best parent");
//		boolean[] returnVal = reduce(bestParent.chromosome);
		System.out.println("Done monicca");
//		return returnVal;
		return bestParent.chromosome;
	}

	public void generateInitialSolutionPopulation(){
		//based off of the populationsize, generate population 
		int featureSize = features.size();
		for(int i = 0; i < populationSize; i++){

			boolean[] parent = new boolean[featureSize];
			for(int k = 0; k < featureSize; k++){
				if(Math.random() > 0.5){
					parent[k] = true; //randomly set booleans to true
				}else{
					parent[k] = false;
				}
			}
			double fitness = classifier.classify(parent);
			population.add(new ParentNode(parent, fitness));
			System.out.println("population size currently: " + population.size());

		}
	}

	public ParentNode tournamentSelection() {
		ParentNode bestParent = null;
		double maxFitness = -1;

		for(int i = 0; i < tournamentSize; i++) {

			int individualIdx = (int)(Math.random() * populationSize);

			if(population.get(individualIdx).fitness > maxFitness){
				maxFitness = population.get(individualIdx).fitness;
				bestParent = population.get(individualIdx);
			}
		}

		return bestParent;

	}

	public boolean[] crossover(boolean[] parent1, boolean[] parent2){
		// perform Uniform crossover
		boolean[] newChild = new boolean[parent1.length];
		// double bestClassify = 0;

		for(int g1 = 0; g1 < parent1.length; g1++) {
			if(Math.random() <= uniformRate) {
				newChild[g1] = parent1[g1];
			} else {
				newChild[g1] = parent2[g1];
			}
		}

		
		return newChild;
	}

	public List<ParentNode> mutation(List<ParentNode> nextGeneration) {
		for(ParentNode individual : nextGeneration) {
			for(int g2 = 0; g2 < individual.chromosome.length; g2++) {
				if(Math.random() <= mutationRate) {
					individual.chromosome[g2] = !individual.chromosome[g2];
				}
			}
		}

		return nextGeneration;
	}

	public boolean[] reduce(boolean[] idealParent){	
		System.out.println("reducer classify");
	    double fitness = classifier.classify(idealParent);
	    double initFitness = fitness;

	    boolean[] bestReducedParent = idealParent;
	    int iterationNumb = 0;
	    while(fitness  > initFitness - 0.05 && iterationNumb < numbIterations){ //set the threshold value for mutation or run until at least length of words
	      //TODO
	      List<boolean[]> reducedList = new ArrayList<>();
	      //generate mutation population from bestReducedParent reduce by 1
	      for(int i =0; i < bestReducedParent.length; i++){
	        if(bestReducedParent[i] == true){
	          boolean[] temp = bestReducedParent.clone();
	          temp[i] = false;
	          reducedList.add(temp);
	        }
	      }
	      //find bestparent from mutated population
	      double bestClassify = 0;
	      boolean[] bestParent = null;
	      for(int i = 0; i < reducedList.size(); i++){
	    	  System.out.println("reduced list size is: " + reducedList.size() + " iteration " + i);
	        boolean[] parent = reducedList.get(i);
	        double temp = classifier.classify(parent);
	        if(temp > bestClassify){
	          bestClassify = temp;
	          bestParent = parent;
	        }
	      }
	      //set new fitness and bestReducedParent and repeat mutation iterations
	      fitness = bestClassify;
	      bestReducedParent = bestParent;
	      iterationNumb++;

	    }
	    //once while loop is completed, we have the most reduced/best mutation iteration, and so we can
	    //now go ahead and return this bestReducedParent 
	    System.out.println("WELL IT BROKE OUT / FINISHED");
	    return bestReducedParent;
	}
}

class Classifier{
	String spamPath;
	List<String> features;

	public Classifier(String spamPath, List<String> features){
		this.spamPath = spamPath;
		this.features = features;
	}

	public double classify(boolean[] parentFeatures){
		//takes the parents features of the set provided and tries to classify all of the different spam/ham to give fitness
		List<String> extractedFeatures = new ArrayList<>(); 
		for(int i = 0 ; i < parentFeatures.length; i++){
			if(parentFeatures[i]){
				extractedFeatures.add(features.get(i));
			}
		}
		StringBuilder fileContent = new StringBuilder();
		for(int j = 0; j < Start.wordsinSMS.size(); j++){
			Set<String> currSMS = Start.wordsinSMS.get(j);

			for(int k = 0; k < extractedFeatures.size(); k++){
				if(currSMS.contains(extractedFeatures.get(k))){
					fileContent.append(1 + ",");

				}else{
					fileContent.append(0 + ",");
				}
			}
			if(Start.isSpamVector.get(j)){
				fileContent.append(1);
			}else{
				fileContent.append(0);
			}
			fileContent.append("\n");
			
		}
		System.out.println("Starting Write");

		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("Spam_LRC/test.txt"), "utf-8"));
		    writer.write(fileContent.toString());
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		System.out.println("Completed Writing to file");

//		OctaveBridge octave = new OctaveBridge();
//		octave.classify();
		
//		while(true){}
		 return Start.octave.classify();
	}
}

class HashNode{
	public int spamCount;
	public int hamCount;

	public HashNode(){
		spamCount = 0;
		hamCount = 0;
	}
}

class ParentNode{
	public boolean[] chromosome;
	public double fitness;

	public ParentNode(boolean[] chromosome, double fitness){
		this.chromosome = chromosome;
		this.fitness = fitness;
	}
}