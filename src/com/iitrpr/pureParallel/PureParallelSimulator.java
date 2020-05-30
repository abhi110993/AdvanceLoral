package com.iitrpr.pureParallel;

import java.io.*;
import java.io.IOException;
import java.util.HashMap;
import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.ServiceCenter;

public class PureParallelSimulator {

	public static void main(String[] args) throws IOException, InterruptedException {
		int[] demandToScRatio = {900,800};
		//int[] demandToScRatio = { 700};
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./output.txt")));
		for (int ratio : demandToScRatio) {
			for(int i=0;i<=20;i=i+5) {
				PureParallelLoral.percentThreadForChildSpawn = (float)i/100;
				for(int j=0;j<=20;j+=5) {
					PureParallelLoral.childSpawnLimit=j;
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
					PureParallelLoral.bestK = (int) (PureParallelLoral.serviceMap.size()*0.25f);
					// ParallelAdvanceLoral.noOfThreads =
					PureParallelLoral.noOfThreads = 80;
					System.out.println("******************** demandToScRatio = "+ratio+"***************************************");
					System.out.println("Total no of threads available are = " + PureParallelLoral.noOfThreads);
					System.out.println("percentThreadForChildSpawn = " + PureParallelLoral.percentThreadForChildSpawn);
					System.out.println("childSpawnLimit = " + PureParallelLoral.childSpawnLimit);
					
					double startTime = System.nanoTime();
					
					loral.performLoral();
	
					double endTime = System.nanoTime();
					// This will print all the allocation which the service center has attained.
					//loral.printAllInformation();
					
					System.out.println("Total Execution time in ns = " + (endTime - startTime));
					System.out.println("Total Objective Function = " + loral.objectiveFunction);
					
					bw.write("Total Execution time in ns = " + (endTime - startTime)+"\n"+"Total Objective Function = " + loral.objectiveFunction);
					//System.out.println("Total Cost because of cascading = " + loral.totalPenalizeCost);
				}
			}
			bw.close();
		}
	}
}