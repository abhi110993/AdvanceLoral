package com.iitrpr.cost;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

class rearrange{
    String nodes;
    String edges;
    String servicenodes;
    String rearrangednodes;
    String nodes_service_costMatrix;
    String costMatrix;
    int d;
    int n;
    int s;
    int e;
    float ratioDemandToService;
    int penaltyRange;
    float ratioTotalCapacityToDemandNode;
    
    rearrange(String nodes, String edges, String servicenodes, int noOfNodes, float DtoSRatio, int noOfEdges, int penRange, float totalCapToDRatio){
        this.nodes = nodes;
        this.edges = edges;
        this.servicenodes = servicenodes;
        String ratiopath = ""+(int)DtoSRatio;
        this.rearrangednodes = "./dataset/"+ratiopath+"/rearrangednodes.txt";
        this.nodes_service_costMatrix = "./dataset/"+ratiopath+"/nodes_service_costMatrix.txt";
        this.costMatrix = "./dataset/"+ratiopath+"/cost_matrix.txt";
        n=noOfNodes;
        ratioDemandToService = DtoSRatio;
        e=noOfEdges;
        penaltyRange = penRange; //Should be greater than 10
        ratioTotalCapacityToDemandNode = totalCapToDRatio;
        
        s=(int) ((n)/(ratioDemandToService+1));
        d=n-s;
    }
    
    void customizedPickServiceNodes(int k) throws IOException{
    	ArrayList<String> nodeList = new ArrayList<>();
        Scanner read = new Scanner (new File(nodes));
        while(read.hasNextLine()){
            nodeList.add(read.nextLine());
        }
        int proportion = 1;
        BufferedWriter bw = new BufferedWriter(new FileWriter(servicenodes)); 
        int c = 0;
        for(int i=0; i<k; i++){
            //System.out.println("Picking-->"+c);
            bw.write(nodeList.get(c));
            c += proportion;
            if(i+1<k)
            	bw.newLine();
        }
        bw.close();
    }
    
    void pickServiceNodes(int k, int size) throws IOException{
        ArrayList<String> nodeList = new ArrayList<>();
        Scanner read = new Scanner (new File(nodes));
        while(read.hasNextLine()){
            nodeList.add(read.nextLine());
        }
        int proportion = (size/k)-1;
        BufferedWriter bw = new BufferedWriter(new FileWriter(servicenodes)); 
        int c = 0;
        for(int i=0; i<k; i++){
            //System.out.println("Picking-->"+c);
            bw.write(nodeList.get(c));
            c += proportion;
            bw.newLine();
        }
        bw.close();
    }
    
    void rearrangenodes() throws FileNotFoundException, IOException{
        ArrayList<String> Service = new ArrayList<>();
        Scanner read = new Scanner (new File(servicenodes));
        while(read.hasNextLine()){
            String st = read.nextLine();
            if(!Service.contains(st)){
                Service.add(st);
            }
        }
        System.out.println("ServiceNodes:"+s);
        read = new Scanner (new File(nodes));
        BufferedWriter clean = new BufferedWriter(new FileWriter(rearrangednodes)); 
        while(read.hasNextLine()){
            String st = read.nextLine();
            if(!Service.contains(st)){
                clean.write(st);
                clean.newLine();
            }
        }
        System.out.println("Nodes:"+n+"\tDemandNodes:"+d);
        for(String s:Service){
            clean.write(s);
            clean.newLine();
        }
        clean.close();
    }
    
    void prepareServiceNodesFile() throws FileNotFoundException, IOException{
        Scanner read = new Scanner (new File(servicenodes));
        String path = ""+(int)ratioDemandToService;
        BufferedWriter service = new BufferedWriter(new FileWriter("./dataset/"+path+"/finalservice.txt"));
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
    
    void getNode_Service_costMatrix() throws FileNotFoundException, IOException{
        long startTime = System.nanoTime();
        int old = 0;
        int new1=0;
        WeightedGraph<String, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Scanner read = new Scanner (new File(nodes));
        while(read.hasNextLine()){
            graph.addVertex(read.nextLine());
        }
        read = new Scanner (new File(edges));
        while(read.hasNextLine())
        {
            old++;
            String edgeinfo = read.nextLine();
            Scanner read1 = new Scanner (edgeinfo);
            read1.useDelimiter(","); 
            String src = read1.next();
            String dest = read1.next();
            double cost = Double.parseDouble(read1.next());
            if(graph.vertexSet().contains(src) && graph.vertexSet().contains(dest)){
                new1++;
                graph.addEdge(src, dest);
                graph.setEdgeWeight(graph.getEdge(src, dest), cost);
            }
        }
        // Prints the shortest path from vertex i to vertex c. This certainly
        // exists for our particular directed graph.
        BufferedWriter output1 = new BufferedWriter(new FileWriter(nodes_service_costMatrix));
        //BufferedWriter output2 = new BufferedWriter(new FileWriter(NextHopFile));
        read = new Scanner (new File(rearrangednodes));
        while(read.hasNextLine()){
            String src = read.nextLine();
            Scanner read1 = new Scanner (new File(servicenodes));
            boolean flag = false;
            while(read1.hasNextLine()){
                if(flag){
                    output1.write(",");
                    //output2.write(",");
                }
                String dest = read1.nextLine();
                DijkstraShortestPath dijkstraAlg = new DijkstraShortestPath(graph,src,dest);
                int cost = (int)dijkstraAlg.getPathLength();        
                output1.write("" + cost);
                //output2.write(graph.getEdgeTarget(hop));
                flag = true;
            }
            output1.newLine();
            //output2.write(";");
        }
        output1.close();
        //output2.close();
        //System.out.println("old Edges:"+old+"\tNew edges:"+new1);
        long endTime = System.nanoTime();
        System.out.println("\n\nTotal time taken to run Algo:"+(endTime - startTime) + " ns");

    }
    
    void getCostMatrix() throws FileNotFoundException, IOException{
        String str = "";
        for(int i=0; i<d; i++){
            str += "Infinity,";
        	//str += i+" ";
        }
       // System.out.println(str);
        Scanner read = new Scanner (new File(nodes_service_costMatrix));
        BufferedWriter output1 = new BufferedWriter(new FileWriter(costMatrix));
        int count =0 ;
        while(read.hasNextLine()){
        	//System.out.println("count"+count++);
            String old = read.nextLine();
            output1.write(str+old);
            output1.newLine();
        }
        output1.close();
    }
}
public class AllpairCostMatrix {

    public static void main(String[] args) throws IOException {
        String nodes = "./dataset/nodes.txt";
        String edges = "./dataset/edges.txt";
       
        
        int n=23839;
        int[] ratioDemandToService = {700,600,500,400,300};
        int e=51794;
        int penaltyRange = 20; //Should be greater than 10
        float ratioTotalCapacityToDemandNode = 0.7f;
        
        for(int ratio : ratioDemandToService) {
        	 String servicenodes = "./dataset/"+ratio+"/servicenodes.txt";
        	 
	        rearrange rearr = new rearrange(nodes,edges,servicenodes,n,ratio,e,penaltyRange,ratioTotalCapacityToDemandNode);
	        //Input= #serviceNodes, #TotalNodes
	        rearr.pickServiceNodes(rearr.s,rearr.n);
	        //rearr.customizedPickServiceNodes(rearr.s);
	        rearr.rearrangenodes();
	        rearr.getNode_Service_costMatrix();
	        rearr.getCostMatrix();
	        rearr.prepareServiceNodesFile();
        }
    }
}
