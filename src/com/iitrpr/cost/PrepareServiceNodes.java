package com.iitrpr.cost;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PrepareServiceNodes {
	
	static int noOfNodes=7000;
	static int noOfSC;
	static HashMap<String, Integer> nodesIndexMap;
	static float ratioTotalCapacityToDemandNode = 0.7f;
	static ArrayList<String> nodes;
	
	public static void main(String[] args) throws Exception{
		
		int[] ratioDemandToService = {700,600,500,400,300};
		//int[] ratioDemandToService = {300};
		for(int ratio : ratioDemandToService) {
			noOfSC=((noOfNodes)/(ratio+1));
			String path = "./dataset/"+ratio+"/ServiceCenter.txt";
			int capacity = Math.round(((noOfNodes-noOfSC)*ratioTotalCapacityToDemandNode)/noOfSC);
			int penaltyRange = 100;
			saveServiceNodesToFile(capacity,penaltyRange,path);
			System.out.println("Service Nodes are written to file");
		}
	}
	
	static void saveServiceNodesToFile(int capacity, int penaltyRange, String path) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		String[] serviceNodes = new String[noOfSC];
		for(int i = 0;i<noOfSC;i++) {
			line = br.readLine();
			serviceNodes[i] = line.split(",")[0];
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		Random random = new Random();
		int penalty=0;
		
		for(String sc : serviceNodes) {
			penalty = (random.nextInt(penaltyRange)+1);
			line = sc + "," + capacity + "," + penalty;
			bw.write(line);
			bw.newLine();
		}
		bw.close();
	}
}
