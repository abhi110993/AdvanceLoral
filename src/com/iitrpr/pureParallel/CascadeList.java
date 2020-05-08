package com.iitrpr.pureParallel;
import com.iitrpr.advanceLoral.CascadePath;

public class CascadeList {
	
	public CascadePath[] list;
	public int size;
	
	public CascadeList() {
		size=0;
		list = new CascadePath[PureParallelLoral.serviceMap.size()+50];
	}
	
	public void insertAtEnd(CascadePath ele) {
		list[size++] = ele;
	}
	
	public void removeFromIndex(int k) {
		size=k;
	}
}
