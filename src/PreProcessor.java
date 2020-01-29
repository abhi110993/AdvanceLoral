import java.util.*;
import java.io.*;

public class PreProcessor {
    
	private String serviceDetails = "./Resource/finalservice.txt";
	private String allNodesDetails = "./Resource/nodes.txt";
	private String allEdgeDetails = "./Resource/edges.txt";
	private BufferedReader br;
	
	public void loadServiceCenter() throws IOException{
    	br = new BufferedReader(new FileReader(serviceDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		//System.out.println(lineSplit[4]);
    		//int penalty, String scid, int maxCap, int curCapacity
    		Loral.serviceMap.put(lineSplit[4],new ServiceCenter(Integer.parseInt(lineSplit[8]),lineSplit[4],Integer.parseInt(lineSplit[7])));
    	}
    }
    
	public void loadDemandNode() throws IOException{
    	br = new BufferedReader(new FileReader(allNodesDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		if(lineSplit[0]!=null && !lineSplit[0].equals("") && !Loral.serviceMap.containsKey(lineSplit[0]))
    			Loral.demandMap.put(lineSplit[0], new DemandNode(lineSplit[0],null));
    	}
    }
    
	public void loadEdges() throws IOException{
    	br = new BufferedReader(new FileReader(allEdgeDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		if(!Loral.edgeMap.containsKey(lineSplit[0])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    			Loral.edgeMap.put(lineSplit[0],edgeWeight);
    		}else {
    			Loral.edgeMap.get(lineSplit[0]).put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    		}
    		
    	}
    }
    
}
