package com.iitrpr.pureParallel;

import java.io.IOException;
import java.util.HashMap;
import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.ServiceCenter;

public class PureParallelSimulator {

	public static void main(String[] args) throws IOException, InterruptedException {
		int[] demandToScRatio = {500};
		//int[] demandToScRatio = { 700};
		for (int ratio : demandToScRatio) {
			System.out.println("***********************************************************");
			PreProcessor.ratio = ratio;
			PreProcessor preprocess = new PreProcessor();
			PureParallelLoral loral = new PureParallelLoral();
			PureParallelLoral.demandMap = new HashMap<String, DemandNode>();
			PureParallelLoral.serviceMap = new HashMap<String, ServiceCenter>();
			PureParallelLoral.outgoingEdgeMap = new HashMap<String, HashMap<String, Integer>>();
			PureParallelLoral.incomingEdgeMap = new HashMap<String, HashMap<String, Integer>>();
			// Order of loading service center first and then demand node must not be
			// changed.
			preprocess.loadServiceCenter();
			preprocess.loadDemandNode();
			preprocess.loadEdges();
			preprocess.distanceMatrixToDemandNodes();
			// Threshold is for limiting the cascading length
			PureParallelLoral.threshold = PureParallelLoral.serviceMap.size();
			PureParallelLoral.bestK = PureParallelLoral.serviceMap.size();
			// ParallelAdvanceLoral.noOfThreads =
			 
			PureParallelLoral.noOfThreads = Runtime.getRuntime().availableProcessors()-Runtime.getRuntime().availableProcessors()/10;
			
			System.out.println("Total no of threads available are = " + PureParallelLoral.noOfThreads);
			double startTime = System.nanoTime();
			
			loral.performLoral();

			double endTime = System.nanoTime();
			// This will print all the allocation which the service center has attained.
			//loral.printAllInformation();
			System.out.println("Total Execution time in ns = " + (endTime - startTime));
			System.out.println("Total Objective Function = " + loral.objectiveFunction);
			//System.out.println("Total Cost because of cascading = " + loral.totalPenalizeCost);
		}
	}
}