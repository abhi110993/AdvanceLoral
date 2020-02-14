import java.io.*;
import java.util.HashMap;

public class Simulator {

	public static void main(String[] args) throws IOException{
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
		Loral.bestK = Loral.demandMap.size();
		
		//Initial Stage
		loral.printAllInformation();
		
		//Time calculation after preprocessing
		long startTime = System.currentTimeMillis();
		
		loral.performLoral();
		
		long endTime = System.currentTimeMillis();
		System.out.println("Total Execution time in ms" + String.valueOf(endTime - startTime));
		// This will print all the allocation which the service center has attained.
		loral.printAllInformation();
	} 

}