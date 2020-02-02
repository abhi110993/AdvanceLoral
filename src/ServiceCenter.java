import java.util.HashSet;

public class ServiceCenter {
	int penalty;
	String scid;
	int curCapacity;
	HashSet<DemandNode> allocations;
	
	/*
	 * Boundary Vertices has at-least one of the following three properties:
	 * (a) an outgoing edge to a vertex allotted to a different service center sj
	 * (b) an outgoing edge to a different service center sj
	 * (c) an outgoing edge to an unprocessed demand vertex
	 * 
	 * */
	//@askSir : Best of k boudary vertices-- Implement it
	HashSet<DemandNode> boundaryVertices;
	
	/**
	 * @param Penalty
	 * @param scid
	 * @param maxCap
	 * */
	public ServiceCenter(int penalty, String scid, int maxCap) {
		this.penalty = penalty;
		this.scid = scid;
		this.curCapacity = maxCap;
		allocations = new HashSet<DemandNode>();
	}
	
	public void addAllocation(DemandNode demandNode) {
		allocations.add(demandNode);
		curCapacity--;
		demandNode.assignAllocation(this);
	}
	
	
	
}
