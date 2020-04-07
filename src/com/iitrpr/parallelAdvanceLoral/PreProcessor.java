package com.iitrpr.parallelAdvanceLoral;
import java.util.*;

import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.DnToScToken;
import com.iitrpr.advanceLoral.ServiceCenter;

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
    		ParallelAdvanceLoral.serviceMap.put(lineSplit[0],serviceCenter);
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
    		if(lineSplit[0]!=null && !lineSplit[0].equals("") && !ParallelAdvanceLoral.serviceMap.containsKey(lineSplit[0])) {
    			//System.out.println("Load Demand Node line = " + line);
        		DemandNode dn = new DemandNode(lineSplit[0],null);
    			ParallelAdvanceLoral.demandMap.put(lineSplit[0], dn);
    			demandNodeIndexMapping.put(i,dn);
    			i++;
    		}
    	}
    	br.close();
    }
    
	public void loadEdges() throws IOException{
    	br = new BufferedReader(new FileReader(allEdgeDetails));
    	String line="";
    	while((line=br.readLine()) != null) {
    		String[] lineSplit = line.split(",");
    		
    		if(!ParallelAdvanceLoral.outgoingEdgeMap.containsKey(lineSplit[0])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    			ParallelAdvanceLoral.outgoingEdgeMap.put(lineSplit[0],edgeWeight);
    		}else {
    			ParallelAdvanceLoral.outgoingEdgeMap.get(lineSplit[0]).put(lineSplit[1],Integer.parseInt(lineSplit[2]));
    		}
    		if(!ParallelAdvanceLoral.incomingEdgeMap.containsKey(lineSplit[1])) {
    			HashMap<String,Integer> edgeWeight = new HashMap<String,Integer>();
    			edgeWeight.put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    			ParallelAdvanceLoral.incomingEdgeMap.put(lineSplit[1],edgeWeight);
    		}else {
    			ParallelAdvanceLoral.incomingEdgeMap.get(lineSplit[1]).put(lineSplit[0],Integer.parseInt(lineSplit[2]));
    		}
    	}
    	br.close();
    }
	
	public void distanceMatrixToDemandNodes() throws IOException{
		ParallelAdvanceLoral.demandNodeProcessQueue = new PriorityQueue<DnToScToken>();
		br = new BufferedReader(new FileReader(distanceMatrix));
    	String line="";
    	int i=0;
    	System.out.println("DemandNodeIndexMapSize : " + demandNodeIndexMapping.size());
    	System.out.println("ServiceNodeIndexMapSize : " + serviceCenterIndexMapping.size());
    	while((line=br.readLine()) != null && !line.equals("") &&(i<demandNodeIndexMapping.size())) {
    		String[] lineSplit = line.split(",");
    		DemandNode demandNode = demandNodeIndexMapping.get(i);
    		for(int j=0;j<ParallelAdvanceLoral.serviceMap.size();j++) {
    			//System.out.println("i=" +i+" j="+j + " val=" + lineSplit[j]);
    			/*
    			 * demandNodeIndexMapping and serviceCenterIndexMapping starts from 0. 
    			 * That's why j-size of total number of demand nodes.
    			 * For more understanding check loadServiceCenter function.
    			 * */
    			if(!lineSplit[j].contains("Infinite")) {
    				ServiceCenter sc = serviceCenterIndexMapping.get(j);
    				//System.out.println(sc.scid + " " + lineSplit[j].trim()+" "+demandNode + " i=" + i);
    				demandNode.addDistanceToSC(Integer.parseInt(lineSplit[j].trim()), sc);
    				ParallelAdvanceLoral.demandNodeProcessQueue.add(new DnToScToken(Integer.parseInt(lineSplit[j].trim()), sc, demandNode));
    			}
    		}
    		i++;
    	}
    	br.close();
	}
}
