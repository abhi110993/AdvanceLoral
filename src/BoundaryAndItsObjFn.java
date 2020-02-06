
public class BoundaryAndItsObjFn implements Comparable<BoundaryAndItsObjFn>{
	int objFunction;
	DemandNode demandNode;
	ServiceCenter serviceCenter;
	
	public BoundaryAndItsObjFn(int obfn,DemandNode dn,ServiceCenter sc) {
		objFunction = obfn;
		demandNode = dn;
		serviceCenter = sc;
	}
	
	public int compareTo(BoundaryAndItsObjFn obj) {
		return this.objFunction - obj.objFunction;
	}
}
