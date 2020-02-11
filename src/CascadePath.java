
public class CascadePath {
	
	ServiceCenter serviceCenter;
	DemandNode demandNode;
	int distance;
	
	public CascadePath(ServiceCenter sc, DemandNode dn, int d) {
		serviceCenter = sc;
		demandNode = dn;
		this.distance = d;
	}
}
