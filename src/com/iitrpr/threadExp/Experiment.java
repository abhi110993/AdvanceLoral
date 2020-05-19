package com.iitrpr.threadExp;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Experiment {
	
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("No of threads:");
		int noT = Integer.parseInt(br.readLine());
		ThreadPoolExecutor tpe = (ThreadPoolExecutor)Executors.newFixedThreadPool(noT);
		for(int i=0;i<noT;i++) {
			tpe.execute(new Th());
		}
		while(tpe.getTaskCount()!=tpe.getCompletedTaskCount()) {
			//noOfActiveThreads = ;
			System.out.println("Running "+tpe.getActiveCount());
		}
		
		//Wait for all the threads to complete their execution
		tpe.shutdown();
		try {
			tpe.awaitTermination(7200, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(!tpe.isTerminated()) {
			try {Thread.sleep(20);}catch(Exception e) {}
		}
	}
	
	public static double call(int no) {
		if(no==0)
			return 0;
		double a=0;
		for(long i=0;i<Long.MAX_VALUE;i++)
			a = a + Integer.MAX_VALUE*Integer.MAX_VALUE*0.8;
		call(no-1);
		return a;
	}
}

class Th implements Runnable{
	
	public Th() {super();}
	
	@Override
	public void run() {
		//while(true) {
			Experiment.call(500);
		//}
		
	}
	
}