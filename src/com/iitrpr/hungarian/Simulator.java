package com.iitrpr.hungarian;

import java.io.IOException;

public class Simulator {

	public static void main(String[] args) throws IOException{

		int[] demandToScRatio = { 90 };
		for (int r : demandToScRatio) {
			System.out.println("***********************************************************");
			String ratio = r + "";
			PreProcessor p = new PreProcessor("./dataset/" + ratio + "/");
			double[][] costMatrix = p.getCostMatrix();
			Hungarian a = new Hungarian(costMatrix);
			double startTime = System.nanoTime();
			int[] result = a.execute();
			double endTime = System.nanoTime();
			double duration = (endTime - startTime);
			double minWeight = 0;
			for (int i = 0; i < result.length; i++) {
				minWeight += costMatrix[i][result[i]];
			}
			minWeight -= p.getDummyNodes() * p.getDummyWeight();
			System.out.println("Objective function cost " + minWeight);
			System.out.println("Time taken in nano seconds " + duration);
		}
	}

}