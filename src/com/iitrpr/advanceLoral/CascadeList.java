package com.iitrpr.advanceLoral;
import com.iitrpr.advanceLoral.CascadePath;

public class CascadeList {
	
	public CascadePath[] list;
	public int size;
	
	public CascadeList() {
		size=0;
		list = new CascadePath[AdvanceLoral.serviceMap.size()+2];
	}
	
	public void insertAtEnd(CascadePath ele) {
		list[size++] = ele;
	}
	
	public void removeFromIndex(int k) {
		size=k;
	}
}
