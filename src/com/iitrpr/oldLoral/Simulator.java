package com.iitrpr.oldLoral;
import java.io.*;
import java.util.HashMap;
import com.iitrpr.oldLoral.PreProcessor;
import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.oldLoral.Loral;
import com.iitrpr.advanceLoral.ServiceCenter;

public class Simulator {

	public static void main(String[] args) throws IOException{
		//int[] demandToScRatio = {400, 500,600,700 };
		int[] demandToScRatio = {30,60,90};
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./output.txt")));
		for(int ratio : demandToScRatio) {
			System.out.println("***********************************************************");
			PreProcessor.ratio=ratio;
			PreProcessor preprocess = new PreProcessor();
			Loral loral = new Loral();
			Loral.demandMap = new HashMap<String, DemandNode>();
			Loral.serviceMap = new HashMap<String, ServiceCenter>();
			Loral.outgoingEdgeMap = new HashMap<String, HashMap<String,Integer>>();
			Loral.incomingEdgeMap = new HashMap<String, HashMap<String,Integer>>();
			// Order of loading service center first and then demand node must not be changed.
			preprocess.loadServiceCenter();
			preprocess.loadDemandNode();
			preprocess.loadEdges();
			preprocess.distanceMatrixToDemandNodes();
			// Threshold is for limiting the cascading length
			Loral.threshold = Loral.serviceMap.size();
			// BestK is for limiting the boundary nodes to service node pairs for cascading. 
			Loral.bestK=Loral.serviceMap.size()/4;
			//Loral.bestK=Integer.MAX_VALUE;
			double startTime = System.nanoTime();
			
			loral.performLoral();
			
			double endTime = System.nanoTime();
			// This will print all the allocation which the service center has attained.
			//loral.printAllInformation();
			System.out.println("Total Execution time in ns = " + (endTime - startTime));
			System.out.println("Total Objective Function = " + loral.objectiveFunction);
			
			bw.write("Ratio= " + ratio+"\n");
			bw.write("Time= " + (endTime - startTime)+"\n"+"Obj fn = " + loral.objectiveFunction+"\n\n");
			
		}
		
		bw.close();
	} 

}