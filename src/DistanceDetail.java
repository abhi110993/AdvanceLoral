
public class DistanceDetail implements Comparable<DistanceDetail>{
	int distance;
	ServiceCenter serviceCenter;
	
	public DistanceDetail(int d, ServiceCenter sc) {
		distance = d;
		serviceCenter = sc;
	}

	public int compareTo(DistanceDetail o) {
		return this.distance - o.distance; 
	}
	
}
