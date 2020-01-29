
public class DemandNode {
	String dnid;
	ServiceCenter allocation;
	
	public DemandNode(String dnid, ServiceCenter allocation) {
		this.dnid = dnid;
		this.allocation = allocation;
	}
	
	public boolean isAllocated() {
		if(allocation==null)
			return false;
		else
			return true;
	}
}
