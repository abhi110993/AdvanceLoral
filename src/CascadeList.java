
public class CascadeList {
	
	CascadePath[] list;
	int size;
	
	public CascadeList() {
		size=0;
		list = new CascadePath[Loral.serviceMap.size()+2];
	}
	
	public void insertAtEnd(CascadePath ele) {
		list[size++] = ele;
	}
	
	public void removeFromIndex(int k) {
		size=k;
	}
}
