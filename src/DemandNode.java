import java.util.*;

public class DemandNode{
	String dnid;
	ServiceCenter allocation;
	int distanceToAllocatedSC;
	//Redundancy: Remove it if not used
	ArrayList<DistanceDetail> distanceToSC;
	
	public DemandNode(String dnid, ServiceCenter allocation) {
		this.dnid = dnid;
		this.allocation = allocation;
		distanceToSC = new ArrayList<DistanceDetail>();
	}
	
	public void addDistanceToSC_Detail(int d, ServiceCenter sc) {
		distanceToSC.add(new DistanceDetail(d, sc));
	}
	
	public boolean isAllocated() {
		if(allocation==null)
			return false;
		else
			return true;
	}
	
	public void assignAllocation(ServiceCenter sc) {
		allocation = sc;
	}
	
}
