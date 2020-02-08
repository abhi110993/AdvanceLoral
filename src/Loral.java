import java.util.*;

public class Loral {
	static HashMap<String, DemandNode> demandMap;
	static HashMap<String, ServiceCenter> serviceMap;
	static HashMap<String, HashMap<String,Integer>> outgoingEdgeMap;
	static HashMap<String, HashMap<String,Integer>> incomingEdgeMap;
	static PriorityQueue<DnToScToken> dnNotAllocated;
	static int threshold,bestK;
	int objectiveFunction = 0;
	/*
	 *  This method performs the Loral Algorithm.
	 * */
	public void performLoral() {
		//For loop for demand nodes being unassigned to the service center.
		while(!dnNotAllocated.isEmpty()) {
			// Token to get the service center and demand node with the minimum distance between them.
			DnToScToken token = dnNotAllocated.poll();
			System.out.println("Token Processed : Demand Node = " + token.demandNode.dnid + " Service Center = " + token.serviceCenter.scid);
			if(token==null || token.demandNode.isAllocated())
				continue;
			// If the service center has the capacity then allocate the demand node to the service center.
			if(token.serviceCenter.curCapacity>0) {
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
				ArrayList<CascadePath> minCascadeDetail = null;
				for(BoundaryAndItsObjFn boundaryVertex : bestKBoundaryVertices) {
					// This hash set to take care that the service center is not repeated.
					HashSet<ServiceCenter> visitedSC = new HashSet<ServiceCenter>();
					visitedSC.add(token.serviceCenter);
					
					// Since we are breaking the boundary vertex so we are subtracting the distance.
					int cascadeObjFn = token.distance - boundaryVertex.demandNode.distanceToAllocatedSC;
					
					// List to store the path through which the cascading proceeds.
					ArrayList<CascadePath> currentCascadeDetail = new ArrayList<CascadePath>();
					
					// Cascading Cost Calculation
					cascadeObjFn += cascadePath(currentCascadeDetail, visitedSC, boundaryVertex.serviceCenter, boundaryVertex.demandNode);
					
					// Maintaining the minimum cascading list.
					if(cascadeObjFn<minCascadeCost) {
						minCascadeDetail = currentCascadeDetail;
						minCascadeCost = cascadeObjFn;
					}
				}
				
				// Check if the cascading needs to happen or not.
				if(minCascadeDetail!=null) { 
					// It means that cascading cost is less than the direct allocation of demand to service center.
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
	
	public int cascadePath(ArrayList<CascadePath> cascadeList,HashSet<ServiceCenter> visitedSC, ServiceCenter serviceCenter, DemandNode demandNode) {
		// Cascading happens till the time the visited service center length becomes equal to the threshold. 
		while(visitedSC.size()<threshold) {
			
		}
		visitedSC.add(serviceCenter);
		return 0;
	}
	
	public void performCascading(ArrayList<CascadePath> cascadeList) {
		for(CascadePath path : cascadeList) {
			path.serviceCenter.addAllocation(path.demandNode,path.distance);
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
			System.out.println("Service Center : " + entry.getKey());
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
