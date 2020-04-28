package com.iitrpr.parallelAdvanceLoral;

import java.io.IOException;
import java.util.HashMap;
import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.ServiceCenter;

public class ParallelSimulator {

	public static void main(String[] args) throws IOException, InterruptedException {
		int[] demandToScRatio = { 400,500,600,700 };
		//int[] demandToScRatio = { 100};
		for (int ratio : demandToScRatio) {
			System.out.println("***********************************************************");
			PreProcessor.ratio = ratio;
			PreProcessor preprocess = new PreProcessor();
			ParallelAdvanceLoral loral = new ParallelAdvanceLoral();
			ParallelAdvanceLoral.demandMap = new HashMap<String, DemandNode>();
			ParallelAdvanceLoral.serviceMap = new HashMap<String, ServiceCenter>();
			ParallelAdvanceLoral.outgoingEdgeMap = new HashMap<String, HashMap<String, Integer>>();
			ParallelAdvanceLoral.incomingEdgeMap = new HashMap<String, HashMap<String, Integer>>();
			// Order of loading service center first and then demand node must not be
			// changed.
			preprocess.loadServiceCenter();
			preprocess.loadDemandNode();
			preprocess.loadEdges();
			preprocess.distanceMatrixToDemandNodes();
			// Threshold is for limiting the cascading length
			ParallelAdvanceLoral.threshold = ParallelAdvanceLoral.serviceMap.size();
			ParallelAdvanceLoral.bestK = ParallelAdvanceLoral.serviceMap.size();
			// ParallelAdvanceLoral.noOfThreads =
			// Runtime.getRuntime().availableProcessors()-3;
			ParallelAdvanceLoral.noOfThreads = 65;
			System.out.println("Total no of threads available are = " + ParallelAdvanceLoral.noOfThreads);
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