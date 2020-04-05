package com.iitrpr.parallelAdvanceLoral;

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
import com.iitrpr.advanceLoral.DnToScToken;
import com.iitrpr.advanceLoral.ServiceCenter;

public class ParallelAdvanceLoral {

	static HashMap<String, DemandNode> demandMap;
	static HashMap<String, ServiceCenter> serviceMap;
	static HashMap<String, HashMap<String,Integer>> outgoingEdgeMap;
	static HashMap<String, HashMap<String,Integer>> incomingEdgeMap;
	static PriorityQueue<DnToScToken> demandNodeProcessQueue;
	static int threshold,bestK;
	static int noOfThreads;
	
	int objectiveFunction = 0,totalPenalizeCost = 0;
	// This variable is only for testing.
	int checkIndex = 16;
	// To store the cascade list which gives out the minimum cascade cost.
	
	/*
	 *  This method performs the Loral Algorithm.
	 * */
	public void performLoral() throws InterruptedException{
		int tokenIndex=1;
		//For loop for demand nodes being unassigned to the service center.
		while(!demandNodeProcessQueue.isEmpty()) {
		//while(tokenIndex<checkIndex+1) {
			// Token to get the service center and demand node with the minimum distance between them.
			DnToScToken token = demandNodeProcessQueue.poll();
			
			if(token.demandNode.isAllocated())
				System.out.println(tokenIndex+++" Already Allocated Token Processed : Demand Node = " + token.demandNode.dnid + " Service Center = " + token.serviceCenter.scid + " Distance = " + token.distance);
			else
				System.out.println(tokenIndex+++" Token Processed : Demand Node = " + token.demandNode.dnid + " Service Center = " + token.serviceCenter.scid + " Distance = " + token.distance);
			
			if(token==null || token.demandNode.isAllocated())
				continue;

			if(tokenIndex==checkIndex+1)
				System.out.println("Check-------------------------------CAP-----"+token.serviceCenter.curCapacity);
			// If the service center has the capacity then allocate the demand node to the service center.
			if(!token.serviceCenter.isfull()) {
				// Since the capacity is >0, so the increase in objective function is only because of the distance. 
				objectiveFunction+=token.distance;
				token.serviceCenter.addAllocation(token.demandNode,token.distance);
				updateBoundaryVertices(token.serviceCenter,token.demandNode);
				// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
				if(incomingEdgeMap.get(token.demandNode.dnid)!=null) {
					for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(token.demandNode.dnid).entrySet()) {
						if(!identifyServiceCenter(entry.getKey())) { 
							DemandNode dnode = demandMap.get(entry.getKey());
							// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
							if(dnode.isAllocated())
								updateBoundaryVertices(dnode.allocation,dnode);
						}
					}
				}
			}else {
				if(tokenIndex==checkIndex+1)
					System.out.println("Cascading is on...");
				CascadeThread.finalCascadeList = new CascadeList();
				//Cascading needs to be implemented here..
				int baseObjFn = token.distance + token.serviceCenter.penalty;

				if(tokenIndex==checkIndex+1)
					System.out.println("Base Obj Fn = " + baseObjFn);
				PriorityQueue<BoundaryAndItsObjFn> bestKBoundaryVertices = new PriorityQueue<BoundaryAndItsObjFn>();
				//This hashmap is used to find the best demand node between the service centers
				HashMap<ServiceCenter,DemandNode> findBestDNodeForSC = new HashMap<ServiceCenter, DemandNode>();
				// This loop is to iterate over all the boundary vertices
				int k=0;
				for(DemandNode demandNode : token.serviceCenter.boundaryVertices) {
					// Only best k demand vertices are allowed.
					if(k++==bestK)
						break;
					// This loop is to add the demand node and service center distance to the Tree Set.
					for(Map.Entry<ServiceCenter, Integer> distanceDetail : demandNode.distanceToSC.entrySet()) {
						
						// There's no point adding something whose distance is greater than the base objective function value
						if(baseObjFn>distanceDetail.getValue() && (demandNode.allocation!=distanceDetail.getKey())) {
							DemandNode prevBestDNode = findBestDNodeForSC.get(distanceDetail.getKey());
							if((prevBestDNode==null) || ((distanceDetail.getValue()-demandNode.distanceToAllocatedSC)<(prevBestDNode.getDistanceToSC(distanceDetail.getKey())-prevBestDNode.distanceToAllocatedSC))) {
								findBestDNodeForSC.put(distanceDetail.getKey(), demandNode);
								//if(tokenIndex==checkIndex+1)
								//	System.out.println("Hash addition : "+ demandNode.dnid+ "-" +distanceDetail.getKey().scid+"="+(distanceDetail.getValue()-demandNode.distanceToAllocatedSC));
							}
						}
					}
				}
				//System.out.println("findBestDNodeForSC Size: " + findBestDNodeForSC.size() + " Entry : "+findBestDNodeForSC);
				for(Map.Entry<ServiceCenter, DemandNode> entry : findBestDNodeForSC.entrySet()) {
					bestKBoundaryVertices.add(new BoundaryAndItsObjFn(entry.getValue().getDistanceToSC(entry.getKey())-entry.getValue().distanceToAllocatedSC, entry.getValue(), entry.getKey()));
					//if(tokenIndex==checkIndex+1)
					//	System.out.println("Best K boundary vertex addition : "+ entry.getValue().dnid+ "-" +entry.getKey().scid+"="+(entry.getValue().getDistanceToSC(entry.getKey())-entry.getValue().distanceToAllocatedSC));
				
				}
				
				findBestDNodeForSC.clear();
				
				
				
				if(tokenIndex==checkIndex+1)
					System.out.println("\nSize of bestKBoundaryVertices = " + bestKBoundaryVertices.size());
				
				
				// Initializing it to the base object function to campare it to all the cascading cost.
				CascadeThread.minCostAcrossAllThreads = baseObjFn;
				CascadeThread.finalCascadeList = new CascadeList();
				
				
				ThreadPoolExecutor tpe = (ThreadPoolExecutor)Executors.newFixedThreadPool(noOfThreads);
				
				while(!bestKBoundaryVertices.isEmpty()) {
					BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
					// This hash set to take care that the service center is not repeated.
					HashSet<ServiceCenter> visitedSC = new HashSet<ServiceCenter>();
					visitedSC.add(token.serviceCenter);
					
					// Since we are breaking the boundary vertex so we are subtracting the distance.
					int cascadeObjFn = token.distance + boundaryVertex.deltaDistance;
					
					int prevCascadeValue = cascadeObjFn;
					// Cascading Cost Calculation
					if(!visitedSC.contains(boundaryVertex.serviceCenter)) {
						if(tokenIndex==checkIndex+1)
							System.out.println("Cascade Ob fn = " + cascadeObjFn + " B.V.=" + boundaryVertex.demandNode.dnid + " S.C.=" + boundaryVertex.serviceCenter.scid);
						CascadeThread cascadePathThread = new CascadeThread();
						cascadePathThread.cascadePathCost = prevCascadeValue;
						// List to store the path through which the cascading proceeds.
						CascadeList currentCascadeDetail = new CascadeList();
						cascadePathThread.cascadeList = currentCascadeDetail;
						cascadePathThread.visitedSC = visitedSC;
						cascadePathThread.serviceCenter = boundaryVertex.serviceCenter;
						cascadePathThread.demandNode = boundaryVertex.demandNode;
						tpe.execute(cascadePathThread);
					}
				}
				
				//Wait for all the threads to complete their execution
				//tpe.awaitTermination(5, TimeUnit.SECONDS);
				tpe.shutdown();
				tpe.awaitTermination(7200, TimeUnit.SECONDS);
				while(!tpe.isTerminated()) {
					System.out.println("Alive");
					try {Thread.sleep(1000);}catch(Exception e) {}
				}
				
				// Check if the cascading needs to happen or not.
				if(CascadeThread.minCostAcrossAllThreads<baseObjFn) { 
					System.out.println("It means that cascading cost is less than the direct allocation of demand to service center.");
					// It means that cascading cost is less than the direct allocation of demand to service center.
					token.serviceCenter.addAllocation(token.demandNode,token.distance);
					updateBoundaryVertices(token.serviceCenter,token.demandNode);
					totalPenalizeCost+=CascadeThread.minCostAcrossAllThreads;
					// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
					if(incomingEdgeMap.get(token.demandNode.dnid)!=null) {
						for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(token.demandNode.dnid).entrySet()) {
							if(!identifyServiceCenter(entry.getKey())) { 
								DemandNode dnode = demandMap.get(entry.getKey());
								// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
								if(dnode.isAllocated())
									updateBoundaryVertices(dnode.allocation,dnode);
							}
						}
					}
					performCascading(CascadeThread.finalCascadeList);
					objectiveFunction+=CascadeThread.minCostAcrossAllThreads;
				}
				else {
					// It means that the base condition was the perfect choice.
					token.serviceCenter.addAllocation(token.demandNode,token.distance);
					updateBoundaryVertices(token.serviceCenter,token.demandNode);
					// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
					if(incomingEdgeMap.get(token.demandNode.dnid)!=null) {
						for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(token.demandNode.dnid).entrySet()) {
							if(!identifyServiceCenter(entry.getKey())) { 
								DemandNode dnode = demandMap.get(entry.getKey());
								// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
								if(dnode.isAllocated())
									updateBoundaryVertices(dnode.allocation,dnode);
							}
						}
					}
					objectiveFunction += baseObjFn;
				}
			}
		}
		
		System.out.println("*************The total objective cost is : " + objectiveFunction + "*************");
	}
	
	public void performCascading(CascadeList cascadeList) {
		System.out.println("*********** Inside a perform Cascade function *********************");
		for(int i=0; i<cascadeList.size; i++) {
			CascadePath path = cascadeList.list[i];
			
			System.out.println("---------Cascading performance between "+path.demandNode.dnid+" & "+path.serviceCenter.scid);
			
			// First remove the demand vertex previous allocation
			path.demandNode.allocation.removeAllocation(path.demandNode);
			// Now add allocation to the new service center
			path.serviceCenter.addAllocation(path.demandNode,path.distance);
			// Update the boundary vertices
			updateBoundaryVertices(path.serviceCenter,path.demandNode);
			// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
			if(incomingEdgeMap.get(path.demandNode.dnid)!=null) {
				for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(path.demandNode.dnid).entrySet()) {
					if(!identifyServiceCenter(entry.getKey())) { 
						DemandNode dnode = demandMap.get(entry.getKey());
						// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
						if(dnode.isAllocated())
							updateBoundaryVertices(dnode.allocation,dnode);
					}
				}
			}
		}
	}
	
	/*
	 * Boundary Vertices has at-least one of the following three properties:
	 * (a) an outgoing edge to a vertex allotted to a different service center sj
	 * (b) an outgoing edge to a different service center sj
	 * (c) an outgoing edge to an unprocessed demand vertex
	 * 
	 * */
	public void updateBoundaryVertices(ServiceCenter serviceCenter,DemandNode demandNode) {
		//Get the ID of the node:
		HashMap<String,Integer> idMap = outgoingEdgeMap.get(demandNode.dnid);
		boolean isBoundaryVertices = false;
		for(Map.Entry<String, Integer> idVal : idMap.entrySet()) {
			//Identifying whether the outgoing edge is to service center or demand node
			boolean isServiceCenter = identifyServiceCenter(idVal.getKey());
			//if it is a service center the case(b) needs to be identified.
			if(isServiceCenter && serviceMap.get(idVal.getKey())!=serviceCenter) {
				// it means that the outgoing edge is going to the different service center. So, Boundary vertex.
				isBoundaryVertices=true;
				break;
			}
			else if(!isServiceCenter) {
			// It's a demand node so condition needs to be verified.
				DemandNode outgoingDemandNode = demandMap.get(idVal.getKey());
				//Case C verification.
				if(!outgoingDemandNode.isAllocated()) {
				// the outgoing edge to the demand node id not allocated. So, Boundary Vertex
					isBoundaryVertices=true;
					break;
				}
				//Case A verification
				else {
					if(outgoingDemandNode.allocation!=serviceCenter) {
					// it means that the outgoing node is allocated to the different service center. So, Boundary vertex.
						isBoundaryVertices=true;
						break;
					}
				}
			}
		}
		if(isBoundaryVertices) 
			serviceCenter.boundaryVertices.add(demandNode);
		else if(serviceCenter.boundaryVertices.contains(demandNode))
			serviceCenter.boundaryVertices.remove(demandNode);
	}
	
	public boolean identifyServiceCenter(String key){
		if(serviceMap.containsKey(key))
			return true;
		else
			return false;
	}
	
	public void printAllInformation() {
		System.out.println("------------------------------------------------");
		for(Map.Entry<String, ServiceCenter> entry : serviceMap.entrySet()) {
			System.out.println("Service Center : " + entry.getKey() + " Current Capacity : " + entry.getValue().curCapacity + " Penalty : " + entry.getValue().penalty);
			for(DemandNode demandNode : entry.getValue().allocations) {
				System.out.println("Demand Node Allocated : " + demandNode.dnid);
			}
			System.out.println("Boundary Vertices :");
			for(DemandNode demandNode : entry.getValue().boundaryVertices) {
				System.out.println("Boundary Vertex : " + demandNode.dnid);
			}
			System.out.println("\n------------------------------------------------");
		}
	}
	
}
