package com.iitrpr.advanceLoral;
import java.util.*;
import java.io.*;

public class PreProcessor {
    
	private String serviceDetails = "./Resource/ServiceCenter.txt";
	private String allNodesDetails = "./Resource/nodes.txt";
	private String allEdgeDetails = "./Resource/edges.txt";
	private String distanceMatrix = "./Resource/CostMatrix.txt";
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
    		AdvanceLoral.serviceMap.put(lineSplit[0],serviceCenter);
    		i++;
    	}
    }
    
	public void loadDemandNode() throws IOException{
    	br = new BufferedReader(new FileReader(allNodesDetails));
    	String line="";
    	int i=0;
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		if(lineSplit[0]!=null && !lineSplit[0].equals("") && !AdvanceLoral.serviceMap.containsKey(lineSplit[0])) {
    			DemandNode dn = new DemandNode(lineSplit[0],null);
    			AdvanceLoral.demandMap.put(lineSplit[0], dn);
    			demandNodeIndexMapping.put(i,dn);
    			i++;
    		}
    	}
    }
    
	public void loadEdges() throws IOException{
    	br = new BufferedReader(new FileReader(allEdgeDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		
    		if(!AdvanceLoral.outgoingEdgeMap.containsKey(lineSplit[0])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    			AdvanceLoral.outgoingEdgeMap.put(lineSplit[0],edgeWeight);
    		}else {
    			AdvanceLoral.outgoingEdgeMap.get(lineSplit[0]).put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    		}
    		if(!AdvanceLoral.incomingEdgeMap.containsKey(lineSplit[1])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    			AdvanceLoral.incomingEdgeMap.put(lineSplit[1],edgeWeight);
    		}else {
    			AdvanceLoral.incomingEdgeMap.get(lineSplit[1]).put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    		}
    	}
    }
	
	public void distanceMatrixToDemandNodes() throws IOException{
		AdvanceLoral.demandNodeProcessQueue = new PriorityQueue<DnToScToken>();
		br = new BufferedReader(new FileReader(distanceMatrix));
    	String line="";
    	int i=0;
    	System.out.println("DemandNodeIndexMapSize : " + demandNodeIndexMapping.size());
    	System.out.println("ServiceNodeIndexMapSize : " + serviceCenterIndexMapping.size());
    	while((line=br.readLine()) != null && !line.equals("") &&(i<demandNodeIndexMapping.size())) {
    		String[] lineSplit = line.split(",");
    		DemandNode demandNode = demandNodeIndexMapping.get(i);
    		for(int j=0;j<AdvanceLoral.serviceMap.size();j++) {
    			//System.out.println("i=" +i+" j="+j + " val=" + lineSplit[j]);
    			/*
    			 * demandNodeIndexMapping and serviceCenterIndexMapping starts from 0. 
    			 * That's why j-size of total number of demand nodes.
    			 * For more understanding check loadServiceCenter function.
    			 * */
    			if(!lineSplit[j].contains("Infinity")) {
    				ServiceCenter sc = serviceCenterIndexMapping.get(j);
    				System.out.println(sc.scid + " " + lineSplit[j].trim()+demandNode);
    				demandNode.addDistanceToSC(Integer.parseInt(lineSplit[j].trim()), sc);
    				AdvanceLoral.demandNodeProcessQueue.add(new DnToScToken(Integer.parseInt(lineSplit[j].trim()), sc, demandNode));
    			}
    		}
    		i++;
    	}
	}
}
