import java.util.*;

/*
 * This class contains the tocken which contains service center and demand node and their distance.
 * */

public class DnToScTocken implements Comparator<DnToScTocken>{
	int distance;
	DemandNode demandNode;
	ServiceCenter serviceCenter;
	
	public DnToScTocken(int d, ServiceCenter sc, DemandNode dn){
		distance = d;
		serviceCenter = sc;
		demandNode = dn;
	}
	
	public int compare(DnToScTocken arg0, DnToScTocken arg1) {
		return arg0.distance-arg1.distance;
	}
}
