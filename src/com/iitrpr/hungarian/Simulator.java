package com.iitrpr.hungarian;

import java.io.IOException;


public class Simulator {

	public static void main(String[] args) throws IOException {
		int[] demandToScRatio = { 100, 200, 300, 400, 500 };
		for (int r : demandToScRatio) {
			System.out.println("***********************************************************");
			String ratio = r + "";
			PreProcessor p = new PreProcessor("dataset/" + ratio + "/");
			double[][] costMatrix = p.getCostMatrix();
			Hungarian a = new Hungarian(costMatrix);
			double startTime = System.nanoTime();
			int[] result = a.execute();
			double endTime = System.nanoTime();
			double duration = (endTime - startTime);
			double minWeight = 0;
			String[] demandNodeLabels = p.getDemandNodeLabels();
			String[] serviceCenterLabels = p.getServiceCenterLabels();
			int totalDemandNodes = p.getTotalNoOfDemandNodes();
			//System.out.println("Demand node id--Service node id");
			for (int i = 0; i < result.length; i++) {
				//if (i < totalDemandNodes) {
				//	int index = result[i] / totalDemandNodes;
					// System.out.println(demandNodeLabels[i]+";"+serviceCenterLabels[index]);
					//System.out.println(demandNodeLabels[i] + "--" + serviceCenterLabels[index]);
				//}
				minWeight += costMatrix[i][result[i]];
			}
			minWeight -= p.getDummyNodes() * p.getDummyWeight();
			System.out.println("Objective function cost " + minWeight);
			System.out.println("Time taken in nano seconds " + duration);
		}
	}

}