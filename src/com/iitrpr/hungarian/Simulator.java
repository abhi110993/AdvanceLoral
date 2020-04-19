package com.iitrpr.hungarian;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Simulator {

	public static final String CONSTFILENAME = "input";
	public static void main(String[] args) throws IOException  {
		// TODO Auto-generated method stub
	/*	double[][] cost = new double[][]{{5,5,10,10,10,9,9,24,24,24},{10,10,15,15,15,2,2,17,17,17},
						{5,5,10,10,10,3,3,18,18,18},{9,9,14,14,14,5,5,20,20,20}
						,{3,3,8,8,8,11,11,26,26,26},{100,100,100,100,100,100,100,100,100,100},
						{100,100,100,100,100,100,100,100,100,100},{100,100,100,100,100,100,100,100,100,100}
						,{100,100,100,100,100,100,100,100,100,100},{100,100,100,100,100,100,100,100,100,100}}; 
		
		//double [][]cost = new double[16000][16000];
		for(int i=0;i<16000;i++){
			
			for(int j=0;j<16000;j++){
				Random rn = new Random();
				cost[i][j] = rn.nextInt(i+j+1) + 3; 
				
			}
		} */
		String str = "2k";
		PreProcessor p = new PreProcessor("Experiment_1/"+CONSTFILENAME+str+".txt");
                double[][] costMatrix= p.getCostMatrix();
                //ArrayList<ArrayList<Double>> costMatrix=p.getCostMatrix();
		//p.printMatrix();
		System.out.println("Got Cost Matrix Size is "+costMatrix.length);
		Hungarian a = new Hungarian(costMatrix);
		long startTime = System.nanoTime();
		int[] result= a.execute();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime); 
		double minWeight = 0;
		String[] demandNodeLabels    = p.getDemandNodeLabels();
		String[] serviceCenterLabels = p.getServiceCenterLabels();
		int totalDemandNodes = p.getTotalNoOfDemandNodes();
                BufferedWriter writer = new BufferedWriter(new FileWriter("Experiment_1/hungarian2k.txt"));
                writer.write("Demand node id--Service node id");
                writer.newLine();
		for(int i=0;i<result.length;i++){
			//System.out.println("Demand Node "+i+" is assigned Service center: "+result[i]);
                       // writer.write("Demand Node "+i+" is assigned Service center: "+result[i]);
                        if(i < totalDemandNodes){
				
				int index = result[i]/totalDemandNodes;
				//System.out.println(demandNodeLabels[i]+";"+serviceCenterLabels[index]);
                                writer.write(demandNodeLabels[i]+"--"+serviceCenterLabels[index]);
                                writer.newLine();
			
			}
			minWeight += costMatrix[i][result[i]];
                      //  minWeight += costMatrix.get(i).get(result[i]);
		}
		minWeight -= p.getDummyNodes() * p.getDummyWeight();
                System.out.println(minWeight);
                writer.write("Objective function cost "+minWeight);
                writer.newLine();
                writer.write("Time taken in seconds "+duration);
		System.out.println("Time taken in seconds "+duration); //in seconds
                writer.close();
	}

}