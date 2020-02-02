import java.util.*;

public class Loral {
	static HashMap<String, DemandNode> demandMap;
	static HashMap<String, ServiceCenter> serviceMap;
	static HashMap<String, HashMap<String,Integer>> edgeMap;
	static PriorityQueue<DnToScTocken> dnNotAllocated;
	/*
	 *  This method performs the Loral Algorithm.
	 * */
	public void performLoral() {
		//For loop for demand nodes being unassigned to the service center.
		while(!dnNotAllocated.isEmpty()) {
			// Tocken to get the service center and demand node with the minimum distance between them.
			DnToScTocken token = dnNotAllocated.poll();
			if(token==null || token.demandNode.isAllocated())
				continue;
			// If the service center has the capacity then allocate the demand node to the service center.
			if(token.serviceCenter.curCapacity>0) {
				token.serviceCenter.addAllocation(token.demandNode);
				updateBoundaryVertices(token.serviceCenter,token.demandNode);
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
		HashMap<String,Integer> idMap = edgeMap.get(demandNode.dnid);
		for(Map.Entry<String, Integer> idVal : idMap.entrySet()) {
			//Identifying whether the outgoing edge is to service center or demand node
			boolean isServiceCenter = identifyServiceCenter(idVal.getKey());
			//if it is a service center the case(b) needs to be identified.
			if(isServiceCenter && serviceMap.get(idVal.getKey())!=serviceCenter) {
				// it means that the outgoing edge is going to the different service center. So, Boundary vertex.
				serviceCenter.boundaryVertices.add(demandNode);
				break;
			}
			else if(!isServiceCenter) {
			// It's a demand node so condition needs to be verified.
				DemandNode outgoingDemandNode = demandMap.get(idVal.getKey());
				//Case C verification.
				if(!outgoingDemandNode.isAllocated()) {
				// the outgoing edge to the demand node id not allocated. So, Boundary Vertex
					serviceCenter.boundaryVertices.add(demandNode);
					break;
				}
				//Case A verification
				else {
					if(outgoingDemandNode.allocation!=serviceCenter) {
					// it means that the outgoing node is allocated to the different service center. So, Boundary vertex.
						serviceCenter.boundaryVertices.add(demandNode);
						break;
					}
				}
				
				
			}
			
		}
	}
	
	public boolean identifyServiceCenter(String key){
		if(serviceMap.containsKey(key))
			return true;
		else
			return false;
	}
	
}
