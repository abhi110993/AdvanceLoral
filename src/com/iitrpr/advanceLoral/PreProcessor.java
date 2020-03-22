package com.iitrpr.advanceLoral;
import java.util.*;
import java.io.*;

public class PreProcessor {
    
	private String serviceDetails = "./Resource/finalservice.txt";
	private String allNodesDetails = "./Resource/rearrangednodes.txt";
	private String allEdgeDetails = "./Resource/edges.txt";
	private String distanceMatrix = "./Resource/cost_matrix.txt";
	private BufferedReader br;
	private HashMap<Integer, DemandNode> demandNodeIndexMapping = new HashMap<Integer, DemandNode>();
	private HashMap<Integer, ServiceCenter> serviceCenterIndexMapping = new HashMap<Integer, ServiceCenter>();
	
	public void loadServiceCenter() throws IOException{
    	br = new BufferedReader(new FileReader(serviceDetails));
    	String line="";
    	int i=0;
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		//System.out.println(lineSplit[4]);
    		//int penalty, String scid, int maxCap, int curCapacity
    		ServiceCenter serviceCenter = new ServiceCenter(Integer.parseInt(lineSplit[8]),lineSplit[4],Integer.parseInt(lineSplit[7]));
    		serviceCenterIndexMapping.put(i,serviceCenter);
    		Loral.serviceMap.put(lineSplit[4],serviceCenter);
    		i++;
    	}
    }
    
	public void loadDemandNode() throws IOException{
    	br = new BufferedReader(new FileReader(allNodesDetails));
    	String line="";
    	int i=0;
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		if(lineSplit[0]!=null && !lineSplit[0].equals("") && !Loral.serviceMap.containsKey(lineSplit[0])) {
    			DemandNode dn = new DemandNode(lineSplit[0],null);
    			Loral.demandMap.put(lineSplit[0], dn);
    			demandNodeIndexMapping.put(i,dn);
    		}
    		i++;
    	}
    }
    
	public void loadEdges() throws IOException{
    	br = new BufferedReader(new FileReader(allEdgeDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		if(!Loral.outgoingEdgeMap.containsKey(lineSplit[0])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    			Loral.outgoingEdgeMap.put(lineSplit[0],edgeWeight);
    		}else {
    			Loral.outgoingEdgeMap.get(lineSplit[0]).put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    		}
    		if(!Loral.incomingEdgeMap.containsKey(lineSplit[1])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    			Loral.incomingEdgeMap.put(lineSplit[1],edgeWeight);
    		}else {
    			Loral.incomingEdgeMap.get(lineSplit[1]).put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    		}
    	}
    }
	
	public void distanceMatrixToDemandNodes() throws IOException{
		Loral.demandNodeProcessQueue = new PriorityQueue<DnToScToken>();
		br = new BufferedReader(new FileReader(distanceMatrix));
    	String line="";
    	int i=0;
    	System.out.println("DemandNodeIndexMapSize : " + demandNodeIndexMapping.size());
    	System.out.println("ServiceNodeIndexMapSize : " + serviceCenterIndexMapping.size());
    	while((line=br.readLine()) != null && i<demandNodeIndexMapping.size()) {
    		String[] lineSplit = line.split(",");
    		DemandNode demandNode = demandNodeIndexMapping.get(i);
    		for(int j=demandNodeIndexMapping.size();j<Loral.serviceMap.size()+demandNodeIndexMapping.size();j++) {
    			//System.out.println("i=" +i+" j="+j + " val=" + lineSplit[j]);
    			/*
    			 * demandNodeIndexMapping and serviceCenterIndexMapping starts from 0. 
    			 * That's why j-size of total number of demand nodes.
    			 * For more understanding check loadServiceCenter function.
    			 * */
    			if(!lineSplit[j].contains("Infinity")) {
    				ServiceCenter sc = serviceCenterIndexMapping.get(j-demandNodeIndexMapping.size());
    				demandNode.addDistanceToSC(Integer.parseInt(lineSplit[j].trim()), sc);
    				Loral.demandNodeProcessQueue.add(new DnToScToken(Integer.parseInt(lineSplit[j].trim()), sc, demandNode));
    			}
    		}
    		i++;
    	}
	}
}
