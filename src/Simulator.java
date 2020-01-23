
public class Simulator {

	public static void main(String[] args){
		PreProcessor preprocess = new PreProcessor();
		Loral.demandMap = preprocess.loadDemandNode();
		Loral.serviceMap = preprocess.loadServiceCenter();
		
	
	} 

}