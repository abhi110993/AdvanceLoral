package com.iitrpr.multipleCascade;

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
	static int minCostAcrossAllThreads;
	static CascadeList finalCascadeList;
	static PriorityQueue<CostCascades> allCascades;
	
	
	public CascadeThread() {
		super();
	}
	
	public void run() {
		// Cascading happens till the time the visited service center length becomes equal to the threshold.
		//System.out.println("Cascading Called for sc=" + serviceCenter.scid + " dn=" + demandNode.dnid);
		//System.out.println("Inside a cascade and here the cascade cost is = "+cascadePathCost);
		
		if(ParallelCascadeLoral.threshold==0) {
			finalReturnValue =  Integer.MAX_VALUE;
		//	System.out.println("cascadePathCost>minCostAcrossAllThreads so returned Cascade Cost : " + cascadePathCost + " minCostAcrossAllThread = " + minCostAcrossAllThreads);
			return;
		}
		
		// Distance between demand node and service center.
		int distance = demandNode.getDistanceToSC(serviceCenter);
		
		// Update cascade list.
		cascadeList.insertAtEnd(new CascadePath(serviceCenter, demandNode, distance));
		
		if(!serviceCenter.isfull()) {
		//	System.out.println(" !serviceCenter.isfull() Cascade Cost : " + cascadePathCost + " minCostAcrossAllThread = " + minCostAcrossAllThreads);
			if(cascadePathCost<minCostAcrossAllThreads) 
				copyPathToFinalList(cascadePathCost, cascadeList);
			
			finalReturnValue =  cascadePathCost;
			return;
		} 
		else if(visitedSC.size() >= ParallelCascadeLoral.threshold) {
		//	System.out.println("visitedSC.size() >= ParallelAdvanceLoral.threshold Cascade Cost : " + (cascadePathCost + serviceCenter.penalty) + " minCostAcrossAllThread = " + minCostAcrossAllThreads);
			if(cascadePathCost + serviceCenter.penalty<minCostAcrossAllThreads) 
				copyPathToFinalList(cascadePathCost + serviceCenter.penalty,cascadeList);
			finalReturnValue = cascadePathCost + serviceCenter.penalty;
			return;
		}
		else {
		//	System.out.println("Cascading can be implementated");
			// Adding the service center to the visited service center so that it is not further processed.
			visitedSC.add(serviceCenter);
			//Cascading needs to be implemented here.
			// Base condition to check if we go ahead with the penalty.
			int baseObjFn =  cascadePathCost + serviceCenter.penalty;
			
			
			
			//Should I change the minCostThroughout threads or not
			
			
			
			//System.out.println("Cascading again... + base ob fun= "+baseObjFn);
			// Priority Queue to find the best pair of demand node and service center
			PriorityQueue<BoundaryAndItsObjFn> bestKBoundaryVertices = new PriorityQueue<BoundaryAndItsObjFn>();
			//This hashmap is used to find the best demand node between the service centers
			HashMap<ServiceCenter,DemandNode> findBestDNodeForSC = new HashMap<ServiceCenter, DemandNode>();
			// This loop is to iterate over all the boundary vertices
			int k=0;
			for(DemandNode boundaryDemandNode : serviceCenter.boundaryVertices) {
				//System.out.println("** Boundary vertex processing "+ boundaryDemandNode.dnid +" **");
				// Only best k demand vertices are allowed.
				if(k++==ParallelCascadeLoral.bestK)
					break;
				
				// This loop is to add the demand node and service center distance to the Tree Set.
				for(Map.Entry<ServiceCenter, Integer> distanceDetail : boundaryDemandNode.distanceToSC.entrySet()) {
					// There's no point adding something whose distance is greater than the base objective function value
					//System.out.println("Boundary Detail but not yet added : sc="+ distanceDetail.getKey().scid + " dn="+boundaryDemandNode.dnid+" distanceDetail=" + distanceDetail.getValue());
					//System.out.println("Size of the boundary to sc map = "+ boundaryDemandNode.distanceToSC.size());
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
			
			//System.out.println("\nSize of bestKBoundaryVertices = " + bestKBoundaryVertices.size());
			
			// Initializing it to the base object function to compare it to all the cascading cost.
			int minCascadeCost = baseObjFn;
			
			while(!bestKBoundaryVertices.isEmpty()) {
				BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
				
				// Since we are breaking the boundary vertex so we are subtracting the distance.
				//System.out.println("Before Adding-cascade cost : " + cascadePathCost);
				//System.out.println("Before Adding-delta distance : " + boundaryVertex.deltaDistance);
				//System.out.println("Before Adding-distance to allocated SC : " + boundaryVertex.demandNode.distanceToAllocatedSC);
				//System.out.println("Before Adding-distance btw "+boundaryVertex.demandNode.dnid+" and "+boundaryVertex.serviceCenter.scid+" : " + boundaryVertex.demandNode.getDistanceToSC(boundaryVertex.serviceCenter));
				int cascadeObjFn = cascadePathCost + boundaryVertex.deltaDistance;
				//System.out.println("Initial Cascade Ob fn = " + cascadeObjFn + " B.V.=" + boundaryVertex.demandNode.dnid + " S.C.=" + boundaryVertex.serviceCenter.scid + " Allocation= "+boundaryVertex.demandNode.allocation.scid);
				// Cascading Cost Calculation
				int prevCascadeValue = cascadeObjFn;
				if(!visitedSC.contains(boundaryVertex.serviceCenter)) {
				//	System.out.println("Cascade Object Function is being created : Cascade Ob fn = " + cascadeObjFn + " B.V.=" + boundaryVertex.demandNode.dnid + " S.C.=" + boundaryVertex.serviceCenter.scid + " Allocation= "+boundaryVertex.demandNode.allocation.scid);
				//	System.out.println("Before cascade implementation : MinCostAcrossThreads = " + minCostAcrossAllThreads);
					CascadeThread cascadePathThread = new CascadeThread();
					cascadePathThread.cascadePathCost = prevCascadeValue;
					cascadePathThread.cascadeList = cascadeList;
					cascadePathThread.visitedSC = visitedSC;
					cascadePathThread.serviceCenter = boundaryVertex.serviceCenter;
					cascadePathThread.demandNode = boundaryVertex.demandNode;
					cascadePathThread.run();
					cascadeObjFn = cascadePathThread.finalReturnValue;
				//	System.out.println("After cascade implementation : MinCostAcrossThreads = " + minCostAcrossAllThreads);
					//System.out.println("Internal After Cascade Ob fn = " + cascadeObjFn + " B.V.=" + boundaryVertex.demandNode.dnid + " S.C.=" + boundaryVertex.serviceCenter.scid + " Allocation= "+boundaryVertex.demandNode.allocation.scid);
				}else 
					cascadeObjFn = Integer.MAX_VALUE;

				// Maintaining the minimum cascading list.
				if((cascadeObjFn<minCascadeCost) && copyPathToFinalList(cascadeObjFn,cascadeList)) {
					minCascadeCost = cascadeObjFn;
				//	System.out.println("Successfull Cascade Ob fn = " + cascadeObjFn + " B.V.=" + boundaryVertex.demandNode.dnid + " S.C.=" + boundaryVertex.serviceCenter.scid + " Allocation= "+boundaryVertex.demandNode.allocation.scid);
				}else {
					// In my customized singly linked list the removal is done in constant time.
					cascadeList.removeFromIndex(visitedSC.size()-1);
				}
			}
			visitedSC.remove(serviceCenter);
			finalReturnValue =  minCascadeCost;
			return;
		}
	}
	
	public static synchronized void copyPathToAllCascades(int cascadeObjFn, CascadeList list) {
		allCascades.add(new CostCascades(cascadeObjFn-ParallelCascadeLoral.token.distance, list));
	}
	
	public static synchronized boolean copyPathToFinalList(int cascadeObjFn, CascadeList list) {
		//System.out.println("copyPathToFinalList initiated : cascadeObjFn = " + cascadeObjFn + " minCostAcrossAllThread = " + minCostAcrossAllThreads);
		if(cascadeObjFn<minCostAcrossAllThreads) {
			minCostAcrossAllThreads = cascadeObjFn;
			finalCascadeList.size=0;
			for(int i=0;i<list.size;i++) {
				finalCascadeList.insertAtEnd(list.list[i]);
		//		System.out.println("List added : SC = " + list.list[i].serviceCenter.scid + " DN = " + list.list[i].demandNode.dnid);
			}
		//	System.out.println("Successfully copied : copyPathToFinalList done : cascadeObjFn = " + cascadeObjFn + " minCostAcrossAllThread = " + minCostAcrossAllThreads + " Final cascade size = " + finalCascadeList.size);
			
			return true;
		}
		return false;
	}
	
}
