package com.iitrpr.cost;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class PrepareServiceNodes {
	
	static String serviceNodes = "./dataset/servicenodes.txt";
	static int penaltyRange;
	static int d,s,e;
	static float ratioTotalCapacityToDemandNode;
	
	public static void main(String[] args) throws Exception{
		int n=1000;
        float ratioDemandToService = 299;
        e=2254;
        penaltyRange = 20; //Should be greater than 10
        ratioTotalCapacityToDemandNode = 0.7f;
        
        s=(int) ((n)/(ratioDemandToService+1));
        d=n-s;
        prepareServiceNodesFile();
	}
	
	static void prepareServiceNodesFile() throws FileNotFoundException, IOException{
        Scanner read = new Scanner (new File(serviceNodes));
        BufferedWriter service = new BufferedWriter(new FileWriter("./dataset/finalservice.txt"));
        Random r_c = new Random();
        Random r_p = new Random();
        while(read.hasNextLine()){
            String st = read.nextLine();
            //int capacity = (r_c.nextInt((350 - 250) + 1) + 250);
            int capacity = Math.round((d*ratioTotalCapacityToDemandNode)/s);
            
            //int penalty = (r_p.nextInt((80 - 50) + 1) + 50);
            int penalty = (r_p.nextInt(penaltyRange-10) + 10);
            service.write("545641483,28.5978094,77.1808404,Secondary School,"+st+",28.5973249,77.181114,"+String.valueOf(capacity)+","+String.valueOf(penalty));
            service.newLine();
        }
       service.close();
    }
	
}
