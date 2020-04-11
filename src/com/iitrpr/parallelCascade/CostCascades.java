package com.iitrpr.parallelCascade;

public class CostCascades implements Comparable<CostCascades>{
	
	int cost;
	CascadeList cascadeList;
	
	public CostCascades(int cost, CascadeList cascadeList) {
		this.cost = cost;
		this.cascadeList = cascadeList;
	}
	
	public int compareTo(CostCascades cc) {
		return this.cost-cc.cost;
	}
	
}
