/*
 * This class contains the tocken which contains service center and demand node and their distance.
 * */
public class DnToScToken implements Comparable<DnToScToken>{
	int distance;
	DemandNode demandNode;
	ServiceCenter serviceCenter;
	
	public DnToScToken(int d, ServiceCenter sc, DemandNode dn){
		distance = d;
		serviceCenter = sc;
		demandNode = dn;
	}
	
	@Override
	public int compareTo(DnToScToken arg0) {
		return this.distance-arg0.distance;
		
	}
}
