package com.iitrpr.threadExperiment;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Parallel {
	
	public static void main(String[] args) throws InterruptedException{
		double startTime = System.nanoTime();
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("#Cores = " + cores);
		
		ThreadPoolExecutor tpe = (ThreadPoolExecutor)Executors.newFixedThreadPool(12);
		
		for(int i = 0;i<20;i++) {
			MyThread t1 = new MyThread("Thread-" + i);
			tpe.execute(t1);
		}
		
		cores = Runtime.getRuntime().availableProcessors();
		System.out.println("#Cores = " + cores);
		/*
		try {
			tpe.awaitTermination(30,TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		tpe.shutdown();
		tpe.awaitTermination(50, TimeUnit.SECONDS);
		while(!tpe.isTerminated()) {
			System.out.println("Alive");
			try {Thread.sleep(1000);}catch(Exception e) {}
		}

		double endTime = System.nanoTime();
		System.out.println("Total Execution time in ns = " + (endTime - startTime));
	}
}
