package com.iitrpr.threadExperiment;

import java.nio.channels.ShutdownChannelGroupException;
import java.util.Random;

public class MyThread implements Runnable{
	
	static int minObjectiveFunction = Integer.MAX_VALUE;
	Random r;
	String name;
	
	public MyThread(String threadName) {
		super();
		name = threadName;
	} 
	
	public void run() {
		System.out.println("Thread Name = " + name);
		r= new Random();
		int n = r.nextInt(5)+10;
		int val = printAnInteger(n,0);
		if(val<minObjectiveFunction)
			changeObjectiveFunction(val);
		
	}
	
	public synchronized void changeObjectiveFunction(int val) {
		if(val<minObjectiveFunction) {
			minObjectiveFunction = val;
			System.out.println("Value changed = " + minObjectiveFunction);
		}
	}
	
	public int printAnInteger(int n, int prevCost) {
		if(n<0 || prevCost>minObjectiveFunction) {
			System.out.println("Thread Name = " + name + " n = " + n + " Returned = "+prevCost);
			return prevCost;
		}else {
			System.out.println("Thread Name = " + name + " n = " + n);
			try {
				Thread.sleep(1000);
			}catch (Exception e) {}
			n--;
			return printAnInteger(n, prevCost+r.nextInt(20));
		}
	}

}
