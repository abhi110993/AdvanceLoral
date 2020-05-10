package com.iitrpr.pureParallel;

import java.util.HashSet;
import com.iitrpr.advanceLoral.DemandNode;
import com.iitrpr.advanceLoral.ServiceCenter;

public class CascadeThread implements Runnable{
	
	int cascadePathCost;
	CascadeList cascadeList;
	HashSet<ServiceCenter> visitedSC;
	ServiceCenter serviceCenter;
	DemandNode demandNode;
	int finalReturnValue;
	
	public CascadeThread() {
		super();
	}
	
	public void run() {
			try {
				finalReturnValue = PureParallelLoral.cascadePath(cascadePathCost, cascadeList, visitedSC, serviceCenter, demandNode);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}
