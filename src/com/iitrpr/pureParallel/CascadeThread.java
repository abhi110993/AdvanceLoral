package com.iitrpr.pureParallel;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.iitrpr.advanceLoral.BoundaryAndItsObjFn;
import com.iitrpr.advanceLoral.CascadePath;
import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.ServiceCenter;

public class CascadeThread implements Runnable{
	
	int cascadePathCost;
	CascadeList cascadeList;
	HashSet<ServiceCenter> visitedSC;
	ServiceCenter serviceCenter;
	DemandNode demandNode;
	int finalReturnValue;
	
	public CascadeThread() {
		super();
	}
	
	public void run() {
		try {
			cascadePath(cascadePathCost, cascadeList, visitedSC, serviceCenter, demandNode);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cascadePath(int cascadePathCost, CascadeList cascadeList,HashSet<ServiceCenter> visitedSC, ServiceCenter serviceCenter, DemandNode demandNode) throws InterruptedException {
		// Cascading happens till the time the visited service center length becomes equal to the threshold.
		//System.out.println("Cascading Called for sc=" + serviceCenter.scid + " dn=" + demandNode.dnid);
		//System.out.println("Inside a cascade and here the cascade cost is = "+cascadePathCost);
		
		if(cascadePathCost>PureParallelLoral.minCascadeCost || PureParallelLoral.threshold==0) {
			finalReturnValue = Integer.MAX_VALUE;
		}
		
		// Distance between demand node and service center.
		int distance = demandNode.getDistanceToSC(serviceCenter);
		
		// Update cascade list.
		cascadeList.insertAtEnd(new CascadePath(serviceCenter, demandNode, distance));
		
		if(!serviceCenter.isfull()) {
			PureParallelLoral.copyPathToFinalList(cascadePathCost,cascadeList);
			finalReturnValue = cascadePathCost;
		} 
		else if(visitedSC.size() >= PureParallelLoral.threshold) {
			PureParallelLoral.copyPathToFinalList(cascadePathCost + serviceCenter.penalty,cascadeList);
			finalReturnValue = cascadePathCost + serviceCenter.penalty;
		}
		else {
			// Adding the service center to the visited service center so that it is not further processed.
			visitedSC.add(serviceCenter);
			//Cascading needs to be implemented here.
			// Base condition to check if we go ahead with the penalty.
			int baseObjFn =  cascadePathCost + serviceCenter.penalty;
			//System.out.println("Cascading again... + base ob fun= "+baseObjFn);
			// Priority Queue to find the best pair of demand node and service center
			PriorityQueue<BoundaryAndItsObjFn> bestKBoundaryVertices = new PriorityQueue<BoundaryAndItsObjFn>();
			//This hashmap is used to find the best demand node between the service centers
			HashMap<ServiceCenter,DemandNode> findBestDNodeForSC = new HashMap<ServiceCenter, DemandNode>();
			// This loop is to iterate over all the boundary vertices
			for(DemandNode boundaryDemandNode : serviceCenter.boundaryVertices) {
				//System.out.println("** Boundary vertex processing "+ boundaryDemandNode.dnid +" **");
				// This loop is to add the demand node and service center distance to the Tree Set.
				for(Map.Entry<ServiceCenter, Integer> distanceDetail : boundaryDemandNode.distanceToSC.entrySet()) {
					if((baseObjFn>distanceDetail.getValue()) && (!visitedSC.contains(distanceDetail.getKey())) && (demandNode.allocation!=distanceDetail.getKey())) {
						DemandNode prevBestDNode = findBestDNodeForSC.get(distanceDetail.getKey());
						if((prevBestDNode==null) || ((distanceDetail.getValue()-boundaryDemandNode.distanceToAllocatedSC)<(prevBestDNode.getDistanceToSC(distanceDetail.getKey())-prevBestDNode.distanceToAllocatedSC))) {
							findBestDNodeForSC.put(distanceDetail.getKey(), boundaryDemandNode);
						}
					}
				}
			}
				
			for(Map.Entry<ServiceCenter, DemandNode> entry : findBestDNodeForSC.entrySet()) {
				bestKBoundaryVertices.add(new BoundaryAndItsObjFn(entry.getValue().getDistanceToSC(entry.getKey())-entry.getValue().distanceToAllocatedSC, entry.getValue(), entry.getKey()));
			}
			
			findBestDNodeForSC.clear();
			finalReturnValue = baseObjFn;
			
			// Initializing it to the base object function to compare it to all the cascading cost.
			int k=0;
			ThreadPoolExecutor tpe = (ThreadPoolExecutor)Executors.newFixedThreadPool(PureParallelLoral.noOfThreads);
			ArrayList<CascadeThread> parallelThreads = new ArrayList<CascadeThread>();
			//System.out.println("*****************Size = "+cascadeList.size);
			//System.out.println("*****************Visited SC Size = "+visitedSC.size());
			while((!bestKBoundaryVertices.isEmpty()) && (k++<PureParallelLoral.bestK)) {
				BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
				// Cascading Cost Calculation
				int cascadeObjFn = cascadePathCost + boundaryVertex.deltaDistance;
				CascadeThread cascadePathThread = new CascadeThread();
				cascadePathThread.cascadeList = new CascadeList();
				for(int i=0;i<cascadeList.size;i++)
					cascadePathThread.cascadeList.insertAtEnd(cascadeList.list[i]);
				parallelThreads.add(cascadePathThread);
				// Cascading Cost Calculation
				cascadePathThread.cascadePathCost = cascadeObjFn;
				HashSet<ServiceCenter> visitedServiceCenter = new HashSet<ServiceCenter>();
				for(ServiceCenter sc : visitedSC) 
					visitedServiceCenter.add(sc);
				cascadePathThread.visitedSC = visitedServiceCenter;
				cascadePathThread.serviceCenter = boundaryVertex.serviceCenter;
				cascadePathThread.demandNode = boundaryVertex.demandNode;
				tpe.execute(cascadePathThread);
			}
			//Wait for all the threads to complete their execution
			tpe.shutdown();
			tpe.awaitTermination(7200, TimeUnit.SECONDS);
			while(!tpe.isTerminated()) {
				try {Thread.sleep(20);}catch(Exception e) {}
			}
			for(CascadeThread c : parallelThreads) {
				finalReturnValue = Integer.min(finalReturnValue,c.finalReturnValue);
			}
			
			if(finalReturnValue<baseObjFn) {
				PureParallelLoral.copyPathToFinalList(finalReturnValue,cascadeList);
				cascadeList.removeFromIndex(visitedSC.size()-1);
			}else {
				// In my customized singly linked list the removal is done in constant time.
				cascadeList.removeFromIndex(visitedSC.size()-1);
				PureParallelLoral.copyPathToFinalList(baseObjFn,cascadeList);
			}
			visitedSC.remove(serviceCenter);
			
		}
	}
	
}
