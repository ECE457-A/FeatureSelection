package FS_Algorithms.aco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import OctaveBridge;

public class AntColonyOptimization {

    private double c = 1.0;
    private double alpha = 1;
    private double beta = 5;
    private double evaporation = 0.5;
    private double Q = 500;
    private double antFactor = 0.1;
    private double randomFactor = 0.05;

    private int maxIterations = 100;
    private int iterationsPerSubset = 10;

    private int numberOfFeatures;
    private int numberOfAnts;
    private double graph[][];
    private double trails[][];
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private double probabilities[];
    
    private List<Set<String>> smsData;
    private List<Boolean> isSpam;
    private String[] featureSet;
    private OctaveBridge octave = new OctaveBridge();
    private int incrementFactor = 0;
    private int currentSolutionSize = 0;

    private int currentIndex;

    private int[] bestFeatureSet;
    private double bestFeatureSetCost;

    public AntColonyOptimization(int numFeatures, List<Set<String>> smsData, List<Boolean> isSpam, Set<String> featureSet) {
    		numberOfFeatures = numFeatures;
        graph = generateRandomMatrix(numberOfFeatures);
        this.smsData = smsData;
        this.isSpam = isSpam;
        this.featureSet = new String[featureSet.size()];
        Iterator<String> it = featureSet.iterator();
        int i = 0;
        
        while (it.hasNext()) {
        		this.featureSet[i] = it.next();
        		i++;
        }
        
//        numberOfAnts = (int) (numberOfFeatures * antFactor);
        numberOfAnts = 20;
        incrementFactor = numberOfFeatures / (maxIterations/iterationsPerSubset);
        System.out.println("Increment Factor of: " + incrementFactor);
        
        trails = new double[numberOfFeatures][numberOfFeatures];
        probabilities = new double[numberOfFeatures];
        
        for (int a = 0; a < numberOfAnts; a++) {
        		ants.add(new Ant(numberOfFeatures));
        }
        
        System.out.println("Done init");
    }

    /**
     * Generate initial solution
     */
    public double[][] generateRandomMatrix(int n) {
        double[][] randomMatrix = new double[n][n];
        
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < n; j++) {
        		randomMatrix[i][j] = Math.abs(random.nextInt(100) + 1);
        	}
        }
        
        return randomMatrix;
    }

    /**
     * Perform ant optimization
     */
    public void startAntOptimization() {
    	for (int i = 1; i <= 3; i++) {
    		System.out.println("Attempt #" + i);
            solve();
    	}
    }

    /**
     * Use this method to run the main logic
     */
    public int[] solve() {
        clearTrails();
        
        for (int i = 0; i < maxIterations; i++) {
	        	if (i % iterationsPerSubset == 0) {
	        		System.out.println("changing solution size");
	        		currentSolutionSize += incrementFactor;
	        		
	        		if (currentSolutionSize > numberOfFeatures)
	        			currentSolutionSize = numberOfFeatures;
	        		
	        }
        		
        		setupAnts();
        		moveAnts();
        		System.out.println("iteration of move ants done");
            updateTrails();
            updateBest();
            
        }
        
        System.out.println("Best tour length: " + bestFeatureSetCost);
        System.out.println("Best tour order: " + Arrays.toString(bestFeatureSet));
        return bestFeatureSet.clone();
    }

    /**
     * Prepare ants for the simulation
     */
    private void setupAnts() {
	    	for (Ant ant : ants) {
	    		ant.clear();
	        ant.visitFeature(-1, random.nextInt(numberOfFeatures));
	    	}
    	
        currentIndex = 0;
    }

    /**
     * At each iteration, move ants
     */
    private void moveAnts() {
	    	for (int i = currentIndex; i < currentSolutionSize - 1; i++) {
	    		for (Ant ant : ants) {
	    			ant.visitFeature(currentIndex, selectNextFeature(ant));
	    		}
	    		
//	    		if (currentIndex % 100 == 0) {
//	    			System.out.println("This is the 100th feature, computing solution cost");
//	    			for (Ant ant : ants) {
//	    				double temp = ant.solutionCost(smsData, isSpam, featureSet, octave, currentIndex);
//	    				System.out.print(temp + " | ");
//	    				if (temp < 0.01) {
//	    					System.out.println("DONE");
//	    					int[] finalSolution = ant.solutionSet;
//	    					for (int j = 0; j <= currentIndex; j++) {
//	    						System.out.print(featureSet[finalSolution[j]] + ", ");
//	    					}
//	    					System.exit(0);
//	    				}
//	    			}
//	    			System.out.println();
//	    		}
	    		
	    		
	    		
	    		currentIndex++;
	    	}
    }

    /**
     * Select next city for each ant
     */
    private int selectNextFeature(Ant ant) {
        int t = random.nextInt(numberOfFeatures - currentIndex);
        if (random.nextDouble() < randomFactor) {
            OptionalInt featureIndex = IntStream.range(0, numberOfFeatures)
                .filter(i -> i == t && !ant.visited(i))
                .findFirst();
            if (featureIndex.isPresent()) {
                return featureIndex.getAsInt();
            }
        }
        calculateProbabilities(ant);
        double r = random.nextDouble();
        double total = 0;
        for (int i = 0; i < numberOfFeatures; i++) {
            total += probabilities[i];
            if (total >= r) {
                return i;
            }
        }

        throw new RuntimeException("There are no other cities");
    }

    /**
     * Calculate the next city picks probabilites
     */
    public void calculateProbabilities(Ant ant) {
        int i = ant.solutionSet[currentIndex];
        double pheromone = 0.0;
        for (int l = 0; l < numberOfFeatures; l++) {
            if (!ant.visited(l)) {
                pheromone += Math.pow(trails[i][l], alpha) * Math.pow(1.0 / 1.0, beta);
            }
        }
        for (int j = 0; j < numberOfFeatures; j++) {
            if (ant.visited(j)) {
                probabilities[j] = 0.0;
            } else {
                double numerator = Math.pow(trails[i][j], alpha) * Math.pow(1.0 / 1.0, beta);
                probabilities[j] = numerator / pheromone;
            }
        }
    }

    /**
     * Update trails that ants used
     */
    private void updateTrails() {
//        for (int i = 0; i < numberOfFeatures; i++) {
//            for (int j = 0; j < numberOfFeatures; j++) {
//                trails[i][j] *= evaporation;
//            }
//        }    	
    		for (Ant a : ants) {
    			for (int i = 0; i < currentSolutionSize - 1; i++) {
    				trails[a.solutionSet[i]][a.solutionSet[i + 1]] *= evaporation;
    			}
    			
    			trails[a.solutionSet[currentSolutionSize - 1]][a.solutionSet[0]] *= evaporation;
    		}
    	
        for (Ant a : ants) {
            double contribution = Q / a.solutionCost(smsData, isSpam, featureSet, octave, currentIndex);
            for (int i = 0; i < currentSolutionSize - 1; i++) {
                trails[a.solutionSet[i]][a.solutionSet[i + 1]] += contribution;
            }
            trails[a.solutionSet[currentSolutionSize - 1]][a.solutionSet[0]] += contribution;
        }
    }

    /**
     * Update the best solution
     */
    private void updateBest() {
        if (bestFeatureSet == null) {
            bestFeatureSet = ants.get(0).solutionSet;
            bestFeatureSetCost = ants.get(0)
                .solutionCost(smsData, isSpam, featureSet, octave, currentIndex);
        }
        double tempCost = 0.0;
        for (Ant a : ants) {
        		tempCost = a.solutionCost(smsData, isSpam, featureSet, octave, currentIndex);
        		
            if (tempCost < bestFeatureSetCost) {
                bestFeatureSetCost = tempCost;
                bestFeatureSet = a.solutionSet.clone();
            }
        }
        
        if (bestFeatureSetCost < 0.08) {
        		System.out.println("Found solution whose cost is " + bestFeatureSetCost);
			for (int j = 0; j <= currentIndex; j++) {
				System.out.print(featureSet[bestFeatureSet[j]] + ", ");
			}
        		System.out.println("Exiting now...");
        		System.exit(0);
        }
        
        System.out.println("current best cost is " + bestFeatureSetCost);
        
    }

    /**
     * Clear trails after simulation
     */
    private void clearTrails() {
    	for (int i = 0; i < numberOfFeatures; i++) {
    		for (int j = 0; j < numberOfFeatures; j++) {
    			trails[i][j] = c;
    		}
    	}
    }

}
