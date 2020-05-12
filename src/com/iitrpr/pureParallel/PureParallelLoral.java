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
import com.iitrpr.advanceLoral.DnToScToken;
import com.iitrpr.advanceLoral.ServiceCenter;

public class PureParallelLoral {

	static HashMap<String, DemandNode> demandMap;
	static HashMap<String, ServiceCenter> serviceMap;
	static HashMap<String, HashMap<String,Integer>> outgoingEdgeMap;
	static HashMap<String, HashMap<String,Integer>> incomingEdgeMap;
	static PriorityQueue<DnToScToken> demandNodeProcessQueue;
	static int threshold,bestK,noOfThreads;
	static long minCascadeCost;
	static CascadeList finalCascadeList;
	long objectiveFunction = 0,totalPenalizeCost = 0;
	// This variable is only for testing.
	int checkIndex = 1311;
	static int multiThreading;
	static int noOfActiveThreads;
	// To store the cascade list which gives out the minimum cascade cost.
	
	/*
	 *  This method performs the Loral Algorithm.
	 * */
	public void performLoral() throws InterruptedException{
		//int tokenIndex=1;
		int noOfTokensExecuted = 0;
		//For loop for demand nodes being unassigned to the service center.
		while(!demandNodeProcessQueue.isEmpty()) {
		//while(tokenIndex++<checkIndex+1) {
			// Token to get the service center and demand node with the minimum distance between them.
			DnToScToken token = demandNodeProcessQueue.poll();
			
			if(token==null || token.demandNode.isAllocated())
				continue;
			//System.out.println(token.serviceCenter.scid);
			//System.out.println(token.demandNode.dnid);
			//System.out.println(objectiveFunction);
			//System.out.println("Processing : DN = "+token.demandNode.dnid + " sc = " + token.serviceCenter.scid + " Dis=" + token.distance);
			System.out.println("Demand node in execution = " + noOfTokensExecuted++);
			
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
				//Cascading needs to be implemented here..
				int baseObjFn = token.distance + token.serviceCenter.penalty;
				PriorityQueue<BoundaryAndItsObjFn> bestKBoundaryVertices = new PriorityQueue<BoundaryAndItsObjFn>();
				//This hashmap is used to find the best demand node between the service centers
				HashMap<ServiceCenter,DemandNode> findBestDNodeForSC = new HashMap<ServiceCenter, DemandNode>();
				// This loop is to iterate over all the boundary vertices
				for(DemandNode demandNode : token.serviceCenter.boundaryVertices) {
					// This loop is to add the demand node and service center distance to the Tree Set.
					for(Map.Entry<ServiceCenter, Integer> distanceDetail : demandNode.distanceToSC.entrySet()) {
						// There's no point adding something whose distance is greater than the base objective function value
						if(baseObjFn>distanceDetail.getValue() && (demandNode.allocation!=distanceDetail.getKey())) {
							DemandNode prevBestDNode = findBestDNodeForSC.get(distanceDetail.getKey());
							if((prevBestDNode==null) || ((distanceDetail.getValue()-demandNode.distanceToAllocatedSC)<(prevBestDNode.getDistanceToSC(distanceDetail.getKey())-prevBestDNode.distanceToAllocatedSC))) {
								findBestDNodeForSC.put(distanceDetail.getKey(), demandNode);
							}
						}
					}
				}
				for(Map.Entry<ServiceCenter, DemandNode> entry : findBestDNodeForSC.entrySet()) {
					bestKBoundaryVertices.add(new BoundaryAndItsObjFn(entry.getValue().getDistanceToSC(entry.getKey())-entry.getValue().distanceToAllocatedSC, entry.getValue(), entry.getKey()));
				}
				
				findBestDNodeForSC.clear();
				
				// Initializing it to the base object function to campare it to all the cascading cost.
				minCascadeCost = baseObjFn;
				finalCascadeList = new CascadeList();
				ThreadPoolExecutor tpe = (ThreadPoolExecutor)Executors.newFixedThreadPool(noOfThreads);
				int k=0;
				multiThreading=0;
				while(!bestKBoundaryVertices.isEmpty() && (k++ < bestK)) {
					BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
					// This hash set to take care that the service center is not repeated.
					HashSet<ServiceCenter> visitedSC = new HashSet<ServiceCenter>();
					visitedSC.add(token.serviceCenter);
					
					// Since we are breaking the boundary vertex so we are subtracting the distance.
					int cascadeObjFn = token.distance + boundaryVertex.deltaDistance;
					// List to store the path through which the cascading proceeds.
					CascadeList currentCascadeDetail = new CascadeList();
					
					CascadeThread cascadePathThread = new CascadeThread();
					
					cascadePathThread.cascadeList = currentCascadeDetail;
					
					// Cascading Cost Calculation
					cascadePathThread.cascadePathCost = cascadeObjFn;
					cascadePathThread.visitedSC = visitedSC;
					cascadePathThread.serviceCenter = boundaryVertex.serviceCenter;
					cascadePathThread.demandNode = boundaryVertex.demandNode;
					tpe.execute(cascadePathThread);
				}
				
				
				//System.out.println("Final Task Count : " + tpe.getTaskCount() + "Completed Task : "+ tpe.getCompletedTaskCount());
				while(tpe.getTaskCount()!=tpe.getCompletedTaskCount()) {
					noOfActiveThreads = tpe.getActiveCount();
					//System.out.println("noOfActiveThreads="+noOfActiveThreads);
				}
				
				//Wait for all the threads to complete their execution
				tpe.shutdown();
				tpe.awaitTermination(7200, TimeUnit.SECONDS);
				while(!tpe.isTerminated()) {
					try {Thread.sleep(20);}catch(Exception e) {}
				}
				// Check if the cascading needs to happen or not.
				if(minCascadeCost<baseObjFn) { 
					// It means that cascading cost is less than the direct allocation of demand to service center.
					token.serviceCenter.addAllocation(token.demandNode,token.distance);
					updateBoundaryVertices(token.serviceCenter,token.demandNode);
					totalPenalizeCost+=minCascadeCost;
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
					performCascading(finalCascadeList);
					objectiveFunction += minCascadeCost;
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
		
		//System.out.println("*************The total objective cost is : " + objectiveFunction + "*************");
	}
	
	public static long cascadePath(long cascadePathCost, CascadeList cascadeList,HashSet<ServiceCenter> visitedSC, ServiceCenter serviceCenter, DemandNode demandNode) throws InterruptedException {
		// Cascading happens till the time the visited service center length becomes equal to the threshold.
		//System.out.println("Cascading Called for sc=" + serviceCenter.scid + " dn=" + demandNode.dnid);
		//System.out.println("Inside a cascade and here the cascade cost is = "+cascadePathCost);
		
		if(cascadePathCost> PureParallelLoral.minCascadeCost || PureParallelLoral.threshold==0) {
			return Integer.MAX_VALUE;
		}
		
		// Distance between demand node and service center.
		int distance = demandNode.getDistanceToSC(serviceCenter);
		
		// Update cascade list.
		cascadeList.insertAtEnd(new CascadePath(serviceCenter, demandNode, distance));
		
		if(!serviceCenter.isfull()) {
			PureParallelLoral.copyPathToFinalList(cascadePathCost,cascadeList);
			return cascadePathCost;
		} 
		else if(visitedSC.size() >= PureParallelLoral.threshold) {
			PureParallelLoral.copyPathToFinalList(cascadePathCost + serviceCenter.penalty,cascadeList);
			return cascadePathCost + serviceCenter.penalty;
		}
		else {
			// Adding the service center to the visited service center so that it is not further processed.
			visitedSC.add(serviceCenter);
			//Cascading needs to be implemented here.
			// Base condition to check if we go ahead with the penalty.
			long baseObjFn =  cascadePathCost + serviceCenter.penalty;
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
			
			//findBestDNodeForSC.clear();
			
			// Initializing it to the base object function to compare it to all the cascading cost.
			long localMinCascadeCost = baseObjFn;
			int k=0;
			long cascadeObjFn=0;
			//System.out.println("multithreading Value = "+multiThreading);
			if(multiThreading>20 || PureParallelLoral.noOfActiveThreads>(0.4*PureParallelLoral.noOfThreads)) {
				while((!bestKBoundaryVertices.isEmpty()) && (k++<PureParallelLoral.bestK)) {
					BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
					// Cascading Cost Calculation
					cascadeObjFn = cascadePathCost + boundaryVertex.deltaDistance;
					cascadeObjFn = cascadePath(cascadeObjFn, cascadeList, visitedSC, boundaryVertex.serviceCenter, boundaryVertex.demandNode);
					if(cascadeObjFn<localMinCascadeCost) {
						localMinCascadeCost = cascadeObjFn;
						PureParallelLoral.copyPathToFinalList(cascadeObjFn,cascadeList);
						cascadeList.removeFromIndex(visitedSC.size()-1);
					}else {
						// In my customized singly linked list the removal is done in constant time.
						cascadeList.removeFromIndex(visitedSC.size()-1);
						PureParallelLoral.copyPathToFinalList(baseObjFn,cascadeList);
					}
				}
			}else {
				addMultiThreadValue();
				ThreadPoolExecutor tpe = (ThreadPoolExecutor)Executors.newFixedThreadPool(PureParallelLoral.noOfThreads);
				ArrayList<CascadeThread> parallelThreads = new ArrayList<CascadeThread>();
				while((!bestKBoundaryVertices.isEmpty()) && (k++<PureParallelLoral.bestK)) {
					BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
					// Cascading Cost Calculation
					cascadeObjFn = cascadePathCost + boundaryVertex.deltaDistance;
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
				while(tpe.getTaskCount()!=tpe.getCompletedTaskCount()) {
					
				}
				//Wait for all the threads to complete their execution
				tpe.shutdown();
				tpe.awaitTermination(7200, TimeUnit.SECONDS);
				while(!tpe.isTerminated()) {
					try {Thread.sleep(20);}catch(Exception e) {}
				}
				for(CascadeThread c : parallelThreads) {
					localMinCascadeCost = Long.min(localMinCascadeCost,c.finalReturnValue);
				}
				cascadeList.removeFromIndex(visitedSC.size()-1);
				subtractMultiThreadValue();
			}
			visitedSC.remove(serviceCenter);
			return localMinCascadeCost;
		}
	}
	
	public void performCascading(CascadeList cascadeList) {
		//System.out.println("*********** Inside a perform Cascade function *********************");
		for(int i=0; i<cascadeList.size; i++) {
			CascadePath path = cascadeList.list[i];
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
	
	public static synchronized void addMultiThreadValue() {
		multiThreading++;
	}
	
	public static synchronized void subtractMultiThreadValue() {
		multiThreading--;
	}
	
	public static synchronized void copyPathToFinalList(long cascadeObjFn, CascadeList list) {
		//System.out.println("copyPathToFinalList initiated : cascadeObjFn = " + cascadeObjFn + " minCostAcrossAllThread = " + minCostAcrossAllThreads);
		if(cascadeObjFn<minCascadeCost) {
			minCascadeCost = cascadeObjFn;
			finalCascadeList.size=0;
			for(int i=0;i<list.size;i++) 
				finalCascadeList.insertAtEnd(list.list[i]);
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
