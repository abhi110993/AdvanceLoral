
public class BoundaryAndItsObjFn implements Comparable<BoundaryAndItsObjFn>{
	int deltaDistance;
	DemandNode demandNode;
	ServiceCenter serviceCenter;
	
	public BoundaryAndItsObjFn(int deltaDistance,DemandNode dn,ServiceCenter sc) {
		this.deltaDistance = deltaDistance;
		demandNode = dn;
		serviceCenter = sc;
	}
	
	public int compareTo(BoundaryAndItsObjFn obj) {
		return this.deltaDistance - obj.deltaDistance;
	}
}
