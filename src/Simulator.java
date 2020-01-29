import java.io.*;
import java.util.HashMap;

public class Simulator {

	public static void main(String[] args) throws IOException{
		PreProcessor preprocess = new PreProcessor();
		Loral.demandMap = new HashMap<String, DemandNode>();
		Loral.serviceMap = new HashMap<String, ServiceCenter>();
		Loral.edgeMap = new HashMap<String, HashMap<String,Integer>>();
		// Order of loading service center first and then demand node must not be changed.
		preprocess.loadServiceCenter();
		preprocess.loadDemandNode();
		preprocess.loadEdges();
		
		
		
	
	} 

}