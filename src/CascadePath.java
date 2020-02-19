
public class CascadePath {
	
	ServiceCenter serviceCenter;
	DemandNode demandNode;
	// Remove this if not used - redundancy
	int distance;
	
	public CascadePath(ServiceCenter sc, DemandNode dn, int d) {
		serviceCenter = sc;
		demandNode = dn;
		this.distance = d;
	}
}
