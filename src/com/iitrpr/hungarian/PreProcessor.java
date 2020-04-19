
package com.iitrpr.hungarian;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PreProcessor {
	private String fileName;
        private double[][] costMatrix;
	//private ArrayList<ArrayList<Double>> costMatrix;
	private int demandNodes;
	private int serviceCenters;
	private int total;
	private String[] demandNodesLabels;
	private String[] serviceCenterLabels;
	
	public PreProcessor(String fileName){
		this.fileName = fileName;
		this.total = 0;
		this.demandNodes    = 0;
		this.serviceCenters = 0;
	} 
	
	public int getTotalNoOfDemandNodes(){
		return this.demandNodes;
	}
	
	public int getTotalNoOfServiceCenters(){
		return this.serviceCenters;
	}
	public int getDummyNodes(){
		
		return (this.demandNodes * this.serviceCenters) - this.demandNodes;
	}
	
	public double getDummyWeight(){
                double weight = this.costMatrix[this.demandNodes+1][0];
                //double weight = this.costMatrix.get(this.demandNodes+1).get(0);
		return weight;
	}
	
	public int getTotal() {
		return total;
	}


	public String getFileName(){
		return this.fileName;
	}
	
	public String[] getDemandNodeLabels(){
		return this.demandNodesLabels;
	}
	
	public String[] getServiceCenterLabels(){
		return this.serviceCenterLabels;
	}
	
	public void fillCostMatrixForISC(int distance, int penalty, int capacity, int ithDemandNode,int jthServiceCenter){
		int startIndex = (jthServiceCenter) * this.demandNodes; 
		int endIndex   = this.demandNodes + startIndex - 1;
		
		for(int j = startIndex;j <= endIndex;j++){
			if(j % demandNodes < capacity){
				costMatrix[ithDemandNode][j] = distance;
                              //  costMatrix.get(ithDemandNode).set(j,(double)distance);
			}
			else{
				costMatrix[ithDemandNode][j] = distance + penalty;
                                //costMatrix.get(ithDemandNode).set(j, (double)(distance+penalty));
			}
		}
	}
	
	public double[][] getCostMatrix() throws IOException{
	//public ArrayList<ArrayList<Double>> getCostMatrix() throws IOException{
            System.out.println("inside getCostMatrix");
		BufferedReader reader  = new BufferedReader(new FileReader(this.fileName));
		
		//read no of demand nodes and service centers from input.txt file
		
		String[] firstLine = reader.readLine().split(";");
		
		this.demandNodes    = Integer.parseInt(firstLine[0]);
		this.serviceCenters = Integer.parseInt(firstLine[1]);
		this.total = demandNodes * serviceCenters;
                System.out.println("total "+total);
                this.costMatrix = new double[total][total];
               //this.costMatrix=new ArrayList(total);
//                for (int r = 0; r < total; r++) {
//                    ArrayList<Double> ls = new ArrayList<Double>(total);
//                    costMatrix.add(ls);
//                }
                this.demandNodesLabels   = new String[this.demandNodes];
		this.serviceCenterLabels = new String[this.serviceCenters];
		
		System.out.println("No Of Demand Nodes " + this.demandNodes);
		System.out.println("No Of Service Centers " + this.serviceCenters);
		
		//read penalty and capacity associated with each service centers
		ArrayList<Integer> penaltyCost = new ArrayList<Integer>();
		ArrayList<Integer> capacity    = new ArrayList<Integer>();
		for(int i = 0;i < serviceCenters;i++){
			String[] penCap = reader.readLine().split(";");
			this.serviceCenterLabels[i] = penCap[0];
			capacity.add(Integer.parseInt(penCap[1]));
			penaltyCost.add(Integer.parseInt(penCap[2]));
		}
		
		//read shortest path distance for the demand node to all the service centers
                System.out.println("read shortest path distance for the demand node to all the service centers");
		int maxDistance = 0;
		for(int i=0;i<demandNodes;i++){
			String[] distanceVector = reader.readLine().split(";");
                        System.out.println("i "+i);
			this.demandNodesLabels[i] = distanceVector[0]; 
			for(int j=0;j<serviceCenters;j++){
				int distance = Integer.parseInt(distanceVector[j+1]);
				int penalty  = penaltyCost.get(j);
				
				if(maxDistance < distance + penalty){
					maxDistance = distance +penalty;
				}
				fillCostMatrixForISC(distance,penalty,capacity.get(j),i,j);
				
			}
			
			
		}
		
		int dummyNodeIndex = demandNodes;
		for(int i=dummyNodeIndex;i<total;i++){
				//assigning cost to dummy node
				for(int j=0;j<total;j++){
					costMatrix[i][j] = maxDistance + 1;
                                      // costMatrix.get(i).set(j, (double)(maxDistance + 1));
				}
		}
			
		reader.close();
		return this.costMatrix;
	}
	
	public void printMatrix(){
		for(int i=0;i<total;i++){
			for(int j=0;j<total;j++){
				System.out.print(costMatrix[i][j]+" ");
                               //System.out.print(costMatrix.get(i).get(j)+" ");
			}
			System.out.println();
		}
	}
 }