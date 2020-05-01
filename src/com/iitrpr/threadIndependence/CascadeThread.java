package com.iitrpr.threadIndependence;

import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

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
	int minCascadeCost;
	int threshold;
	int bestK;
	
	public CascadeThread() {
		super();
	}
	
	public void run() {
		cascadePath(threshold,minCascadeCost,cascadePathCost, cascadeList, visitedSC, serviceCenter, demandNode);
	}
	
	public int cascadePath(int threshold,int minCascadeCost,int cascadePathCost, CascadeList cascadeList,HashSet<ServiceCenter> visitedSC, ServiceCenter serviceCenter, DemandNode demandNode) {
		// Cascading happens till the time the visited service center length becomes equal to the threshold.
		//System.out.println("Cascading Called for sc=" + serviceCenter.scid + " dn=" + demandNode.dnid);
		//System.out.println("Inside a cascade and here the cascade cost is = "+cascadePathCost);
		
		if(cascadePathCost>minCascadeCost || threshold==0) {
			return Integer.MAX_VALUE;
		}
		
		// Distance between demand node and service center.
		int distance = demandNode.getDistanceToSC(serviceCenter);
		
		// Update cascade list.
		cascadeList.insertAtEnd(new CascadePath(serviceCenter, demandNode, distance));
		
		if(!serviceCenter.isfull()) {
			ParallelAdvanceLoral.copyPathToFinalList(cascadePathCost,cascadeList);
			return cascadePathCost;
		} 
		else if(visitedSC.size() >= threshold) {
			ParallelAdvanceLoral.copyPathToFinalList(cascadePathCost + serviceCenter.penalty,cascadeList);
			return cascadePathCost + serviceCenter.penalty;
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
			
			// Initializing it to the base object function to compare it to all the cascading cost.
			int localMinCascadeCost = baseObjFn;
			int k=0;
			while((!bestKBoundaryVertices.isEmpty()) && (k++<bestK)) {
				BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
				// Cascading Cost Calculation
				int cascadeObjFn = cascadePathCost + boundaryVertex.deltaDistance;
				cascadeObjFn = cascadePath(threshold,localMinCascadeCost,cascadeObjFn, cascadeList, visitedSC, boundaryVertex.serviceCenter, boundaryVertex.demandNode);
				
				if(cascadeObjFn<localMinCascadeCost) {
					localMinCascadeCost = cascadeObjFn;
					ParallelAdvanceLoral.copyPathToFinalList(cascadeObjFn,cascadeList);
					cascadeList.removeFromIndex(visitedSC.size()-1);
				}else {
					// In my customized singly linked list the removal is done in constant time.
					cascadeList.removeFromIndex(visitedSC.size()-1);
					ParallelAdvanceLoral.copyPathToFinalList(baseObjFn,cascadeList);
				}
			}
			visitedSC.remove(serviceCenter);
			return localMinCascadeCost;
		}
	}
	
}
