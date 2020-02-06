
public class CascadePath {
	
	ServiceCenter serviceCenter;
	DemandNode demandNode;
	int deltaObj;
	
	public CascadePath(ServiceCenter sc, DemandNode dn, int deltaObj) {
		serviceCenter = sc;
		demandNode = dn;
		this.deltaObj = deltaObj; 
	}
}
