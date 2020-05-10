package com.iitrpr.pureParallel;
import java.util.*;

import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.DnToScToken;
import com.iitrpr.advanceLoral.ServiceCenter;

import java.io.*;

public class PreProcessor {
    
	static int ratio;
	private String serviceDetails = "./dataset/"+ratio+"/ServiceCenter.txt";
	private String allNodesDetails = "./dataset/"+ratio+"/nodes.txt";
	private String allEdgeDetails = "./dataset/"+ratio+"/edges.txt";
	private String distanceMatrix = "./dataset/"+ratio+"/CostMatrix.txt";
	private BufferedReader br;
	private HashMap<Integer, DemandNode> demandNodeIndexMapping = new HashMap<Integer, DemandNode>();
	private HashMap<Integer, ServiceCenter> serviceCenterIndexMapping = new HashMap<Integer, ServiceCenter>();
	
	public void loadServiceCenter() throws IOException{
    	br = new BufferedReader(new FileReader(serviceDetails));
    	String line="";
    	int i=0;
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		ServiceCenter serviceCenter = new ServiceCenter(lineSplit[0],Integer.parseInt(lineSplit[1]),Integer.parseInt(lineSplit[2]));
    		serviceCenterIndexMapping.put(i,serviceCenter);
    		PureParallelLoral.serviceMap.put(lineSplit[0],serviceCenter);
    		i++;
    	}
    	br.close();
    }
    
	public void loadDemandNode() throws IOException{
    	br = new BufferedReader(new FileReader(allNodesDetails));
    	String line="";
    	int i=0;
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		if(lineSplit[0]!=null && !lineSplit[0].equals("") && !PureParallelLoral.serviceMap.containsKey(lineSplit[0])) {
    			DemandNode dn = new DemandNode(lineSplit[0],null);
    			PureParallelLoral.demandMap.put(lineSplit[0], dn);
    			demandNodeIndexMapping.put(i,dn);
    		}
    		i++;
    	}
    	br.close();
    }
    
	public void loadEdges() throws IOException{
    	br = new BufferedReader(new FileReader(allEdgeDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		
    		if(!PureParallelLoral.outgoingEdgeMap.containsKey(lineSplit[0])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    			PureParallelLoral.outgoingEdgeMap.put(lineSplit[0],edgeWeight);
    		}else {
    			PureParallelLoral.outgoingEdgeMap.get(lineSplit[0]).put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    		}
    		if(!PureParallelLoral.incomingEdgeMap.containsKey(lineSplit[1])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    			PureParallelLoral.incomingEdgeMap.put(lineSplit[1],edgeWeight);
    		}else {
    			PureParallelLoral.incomingEdgeMap.get(lineSplit[1]).put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    		}
    	}
    	br.close();
    }
	
	public void distanceMatrixToDemandNodes() throws IOException{
		PureParallelLoral.demandNodeProcessQueue = new PriorityQueue<DnToScToken>();
		HashMap<DemandNode,DistToSCToken> map = new HashMap<DemandNode, DistToSCToken>();
		br = new BufferedReader(new FileReader(distanceMatrix));
    	String line="";
    	int i=0;
    	System.out.println("DemandNodeIndexMapSize : " + demandNodeIndexMapping.size());
    	System.out.println("ServiceNodeIndexMapSize : " + serviceCenterIndexMapping.size());
    	while((line=br.readLine()) != null && !line.equals("")) {
    		String[] lineSplit = line.split(",");
    		DemandNode demandNode = demandNodeIndexMapping.get(i);
    		if(demandNode==null) {i++;continue;}
    		for(int j=0;j<PureParallelLoral.serviceMap.size();j++) {
    			int cost = Integer.parseInt(lineSplit[j].trim());
    			if(!lineSplit[j].contains("Infinite")) {
    				ServiceCenter sc = serviceCenterIndexMapping.get(j);
    				demandNode.addDistanceToSC(cost, sc);
    				if(map.get(demandNode)==null) {
    					map.put(demandNode,new DistToSCToken(sc,cost));
    				}else if(map.get(demandNode).distance>cost) {
    					map.put(demandNode,new DistToSCToken(sc,cost));
    				}
    			//	PureParallelLoral.demandNodeProcessQueue.add(new DnToScToken(1, sc, demandNode));
    			}
    		}
    		i++;
    	}
    	br.close();
    	for(Map.Entry<DemandNode, DistToSCToken> entry : map.entrySet())
    		PureParallelLoral.demandNodeProcessQueue.add(new DnToScToken(entry.getValue().distance, entry.getValue().sc, entry.getKey()));
	}
}

class DistToSCToken{
	int distance;
	ServiceCenter sc;
	public DistToSCToken(ServiceCenter s,int d) {
		sc = s;
		distance = d;
	}
}