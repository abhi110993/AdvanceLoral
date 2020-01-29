import java.util.HashSet;

public class ServiceCenter {
	int penalty;
	String scid;
	int capacity;
	int curCapacity;
	HashSet<DemandNode> allocations;
	/*
	 * Boundary Vertices has at-least one of the following three properties:
	 * (a) an outgoing edge to a vertex allotted to a different service center sj
	 * (b) an outgoing edge to a different service center sj
	 * (c) an outgoing edge to an unprocessed demand vertex
	 * 
	 * */
	HashSet<DemandNode> boundaryVertices;
	
	/**
	 * @param Penalty
	 * @param scid
	 * @param maxCap
	 * */
	public ServiceCenter(int penalty, String scid, int maxCap) {
		this.penalty = penalty;
		this.scid = scid;
		this.capacity = maxCap;
		this.curCapacity = 0;
		allocations = new HashSet<DemandNode>();
	}
}
