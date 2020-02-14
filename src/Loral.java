import java.util.*;

public class Loral {
	static HashMap<String, DemandNode> demandMap;
	static HashMap<String, ServiceCenter> serviceMap;
	static HashMap<String, HashMap<String,Integer>> outgoingEdgeMap;
	static HashMap<String, HashMap<String,Integer>> incomingEdgeMap;
	static PriorityQueue<DnToScToken> demandNodeProcessQueue;
	static int threshold,bestK;
	int objectiveFunction = 0;
	/*
	 *  This method performs the Loral Algorithm.
	 * */
	public void performLoral() {
		int tokenIndex=1;
		//For loop for demand nodes being unassigned to the service center.
		//while(!demandNodeProcessQueue.isEmpty()) {
		while(tokenIndex<14) {
			// Token to get the service center and demand node with the minimum distance between them.
			DnToScToken token = demandNodeProcessQueue.poll();
			if(token.demandNode.isAllocated())
				System.out.println(tokenIndex+++" Already Allocated Token Processed : Demand Node = " + token.demandNode.dnid + " Service Center = " + token.serviceCenter.scid + " Distance = " + token.distance);
			else
				System.out.println(tokenIndex+++" Token Processed : Demand Node = " + token.demandNode.dnid + " Service Center = " + token.serviceCenter.scid + " Distance = " + token.distance);

			if(token==null || token.demandNode.isAllocated())
				continue;

			if(tokenIndex==14)
				System.out.println("Check-------------------------------CAP-----"+token.serviceCenter.curCapacity);
			// If the service center has the capacity then allocate the demand node to the service center.
			if(!token.serviceCenter.isfull()) {

				if(tokenIndex==14)
					System.out.println("OOPS::: ERROR");
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
				if(tokenIndex==14)
					System.out.println("Cascading is on...");
				//Cascading needs to be implemented here..
				int baseObjFn = token.distance + token.serviceCenter.penalty;

				if(tokenIndex==14)
					System.out.println("Base Obj Fn = " + baseObjFn);
				TreeSet<BoundaryAndItsObjFn> bestKBoundaryVertices = new TreeSet<BoundaryAndItsObjFn>();
				// This loop is to iterate over all the boundary vertices
				int k=0;
				for(DemandNode demandNode : token.serviceCenter.boundaryVertices) {
					// Only best k demand vertices are allowed.
					if(k++==bestK)
						break;
					// This loop is to add the demand node and service center distance to the Tree Set.
					for(Map.Entry<ServiceCenter, Integer> distanceDetail : demandNode.distanceToSC.entrySet()) {
						/*
						 * Verify whether it is correct or not;
						if(distanceDetail.serviceCenter.isfull())
							innerObjFn += distanceDetail.serviceCenter.penalty;
						*/
						// There's no point adding something whose distance is greater than the base objective function value
						if(baseObjFn>distanceDetail.getValue())
							bestKBoundaryVertices.add(new BoundaryAndItsObjFn(distanceDetail.getValue(), demandNode,distanceDetail.getKey()));
					}
				}
				
				// Initializing it to the base object function to campare it to all the cascading cost.
				int minCascadeCost = baseObjFn;
				SinglyLinkedList minCascadeDetail = null;
				// Extract minimum
				int iterationStep = 0;
				while((!bestKBoundaryVertices.isEmpty()) && (iterationStep++ < Loral.bestK)) {
					BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.pollFirst();
					// This hash set to take care that the service center is not repeated.
					HashSet<ServiceCenter> visitedSC = new HashSet<ServiceCenter>();
					visitedSC.add(token.serviceCenter);
					
					// Since we are breaking the boundary vertex so we are subtracting the distance.
					int cascadeObjFn = token.distance - boundaryVertex.demandNode.distanceToAllocatedSC;
					

					if(tokenIndex==13)
						System.out.println("Cascade Ob fn initial:" + cascadeObjFn);
					
					// List to store the path through which the cascading proceeds.
					SinglyLinkedList currentCascadeDetail = new SinglyLinkedList();
					
					// Cascading Cost Calculation
					if(!visitedSC.contains(boundaryVertex.serviceCenter))
						cascadeObjFn += cascadePath(currentCascadeDetail, visitedSC, boundaryVertex.serviceCenter, boundaryVertex.demandNode);
					else 
						cascadeObjFn = Integer.MAX_VALUE;
					// Maintaining the minimum cascading list.
					if(cascadeObjFn<minCascadeCost) {
						minCascadeDetail = currentCascadeDetail;
						minCascadeCost = cascadeObjFn;
					}
				}
				
				// Check if the cascading needs to happen or not.
				if(minCascadeCost<baseObjFn) { 
					// It means that cascading cost is less than the direct allocation of demand to service center.
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
					performCascading(minCascadeDetail);
					objectiveFunction+=minCascadeCost;
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
	
	public int cascadePath(SinglyLinkedList cascadeList,HashSet<ServiceCenter> visitedSC, ServiceCenter serviceCenter, DemandNode demandNode) {
		// Cascading happens till the time the visited service center length becomes equal to the threshold.
		int cascadeCost = demandNode.getDistanceToSC(serviceCenter);
		// Distance between demand node and service center.
		int distance = demandNode.getDistanceToSC(serviceCenter);
		
		// Update cascade list.
		cascadeList.insertAtEnd(new CascadePath(serviceCenter, demandNode,distance));
		// Adding the service center to the visited service center so that it is not further processed.
		visitedSC.add(serviceCenter);
		
		if(!serviceCenter.isfull()) {
			visitedSC.remove(serviceCenter);
			return cascadeCost;
		} 
		
		else if(visitedSC.size() == threshold) {
			visitedSC.remove(serviceCenter);
			return cascadeCost + serviceCenter.penalty;
		}
		
		else {
			//Cascading needs to be implemented here.
			// Base condition to check if we go ahead with the penalty.
			int baseObjFn =  distance + serviceCenter.penalty;
			
			// Priority Queue to find the best pair of demand node and service center
			PriorityQueue<BoundaryAndItsObjFn> bestKBoundaryVertices = new PriorityQueue<BoundaryAndItsObjFn>();
			
			// This loop is to iterate over all the boundary vertices
			int k=0;
			for(DemandNode boundaryDemandNode : serviceCenter.boundaryVertices) {
				// Only best k demand vertices are allowed.
				if(k++==bestK)
					break;
				
				// This loop is to add the demand node and service center distance to the Tree Set.
				for(Map.Entry<ServiceCenter, Integer> distanceDetail : boundaryDemandNode.distanceToSC.entrySet()) {
					/*
					 * Verify whether it is correct or not;
					if(distanceDetail.serviceCenter.isfull())
						innerObjFn += distanceDetail.serviceCenter.penalty;
					*/
					// There's no point adding something whose distance is greater than the base objective function value
					if((baseObjFn>distanceDetail.getValue()) && (!visitedSC.contains(distanceDetail.getKey())))
						bestKBoundaryVertices.add(new BoundaryAndItsObjFn(distanceDetail.getValue(), boundaryDemandNode, distanceDetail.getKey()));
				}
			}
			// Initializing it to the base object function to compare it to all the cascading cost.
			int minCascadeCost = baseObjFn;
			
			int iterationStep = 0;
			while((!bestKBoundaryVertices.isEmpty()) && (iterationStep++ < Loral.bestK)) {
				BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
				
				// Since we are breaking the boundary vertex so we are subtracting the distance.
				int cascadeObjFn = distance - boundaryVertex.demandNode.distanceToAllocatedSC;
				
				// Cascading Cost Calculation
				if(!visitedSC.contains(boundaryVertex.serviceCenter))
					cascadeObjFn += cascadePath(cascadeList, visitedSC, boundaryVertex.serviceCenter, boundaryVertex.demandNode);
				else 
					cascadeObjFn = Integer.MAX_VALUE;

				// Maintaining the minimum cascading list.
				if(cascadeObjFn<minCascadeCost) {
					//minCascadeDetail = currentCascadeDetail;
					minCascadeCost = cascadeObjFn;
				}else {
					// In my customized singly linked list the removal is done in constant time.
					for(int i=visitedSC.size(); i< cascadeList.size;i++)
						cascadeList.removeFromIndex(i);
				}
			}
			
			return minCascadeCost;
		}
	}
	
	public void performCascading(SinglyLinkedList cascadeList) {
		Node temp = cascadeList.head;
		while(temp!=null) {
			CascadePath path = temp.cascadePath;
			temp = temp.next;
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
