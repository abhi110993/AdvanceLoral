
public class CascadePath {
	
	ServiceCenter serviceCenter;
	DemandNode demandNode;
	int deltaObj;
	int distance;
	
	public CascadePath(ServiceCenter sc, DemandNode dn, int deltaObj, int d) {
		serviceCenter = sc;
		demandNode = dn;
		this.deltaObj = deltaObj; 
		this.distance = d;
	}
}
