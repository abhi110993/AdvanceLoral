package com.iitrpr.advanceLoral;
import java.io.*;
import java.util.HashMap;

public class AdvanceSimulator {

	public static void main(String[] args) throws IOException{
		PreProcessor preprocess = new PreProcessor();
		AdvanceLoral loral = new AdvanceLoral();
		AdvanceLoral.demandMap = new HashMap<String, DemandNode>();
		AdvanceLoral.serviceMap = new HashMap<String, ServiceCenter>();
		AdvanceLoral.outgoingEdgeMap = new HashMap<String, HashMap<String,Integer>>();
		AdvanceLoral.incomingEdgeMap = new HashMap<String, HashMap<String,Integer>>();
		// Order of loading service center first and then demand node must not be changed.
		preprocess.loadServiceCenter();
		preprocess.loadDemandNode();
		preprocess.loadEdges();
		preprocess.distanceMatrixToDemandNodes();
		// Threshold is for limiting the cascading length
		AdvanceLoral.threshold = AdvanceLoral.serviceMap.size();
		//Loral.threshold = 0;
		
		// BestK is for limiting the boundary nodes to service node pairs for cascading. 
		//Loral.bestK = Loral.demandMap.size();
		AdvanceLoral.bestK=Integer.MAX_VALUE;
		
		//Initial Stage
		//loral.printAllInformation();
		
		//Time calculation after preprocessing
		//double startTime = System.currentTimeMillis();
		double startTime = System.nanoTime();
		
		loral.performLoral();
		
		double endTime = System.nanoTime();
		// This will print all the allocation which the service center has attained.
		loral.printAllInformation();
		System.out.println("Total Execution time in ns = " + (endTime - startTime));
		System.out.println("Total Objective Function = " + loral.objectiveFunction);
		System.out.println("Total Cost because of cascading = " + loral.totalPenalizeCost);
		
	} 
}