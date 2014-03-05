//package PerformanceTests;
//
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.util.Date;
//import java.util.Vector;
//
//import Client.client;
//import MiddleWareInterface.MiddleWare;
//import TransactionManager.InvalidTransactionException;
//import TransactionManager.TransactionAbortedException;
//
//public class PerformanceTest2 {
//	
//	private static double TotalTimeSpent = 0;
//	private static int operations = 0;
//	
//		
//private static final String MIDDLEWARE_SERVER = "g10-middleware";
//	
//	static String message = "blank";
//	static MiddleWare mw = null;
//
//	public static void main(String args[]) throws RemoteException, InvalidTransactionException, TransactionAbortedException
//	{
//		client obj = new client();
//		
//		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));			
//		
//		String command = "";
//		Vector arguments  = new Vector();
//		int Id, Cid;
//		int flightNum;
//		int flightPrice;
//		int flightSeats;
//		boolean Room;
//		boolean Car;
//		int price;
//		int numRooms;
//		int numCars;
//		String location;
//
//
//		String server = "localhost";
//		if (args.length == 1) 
//			server = args[0]; 
//		else if (args.length != 0 &&  args.length != 1) 	//The second condition is useless
//		{
//			System.out.println ("Usage: java client [rmihost]"); 
//			System.exit(1); 
//		}
//
//		
//		try 
//		{
//			// get a reference to the rmiregistry
//			Registry registry = LocateRegistry.getRegistry(server);
//			// get the proxy and the remote reference by rmiregistry lookup
//			mw = (MiddleWare) registry.lookup(MIDDLEWARE_SERVER);
//			if(mw!=null)
//			{
//				System.out.println("Successfully Connected to RM");
//			}
//			else
//			{
//				System.out.println("Unsuccessful in Connecting to RM");
//			}
//			// make call on remote method
//		} 
//		catch (Exception e) 
//		{	
//			System.err.println("Client exception: " + e.toString());
//			e.printStackTrace();
//		}
//		
//
//	
////	Id = mw.startTransaction();
////	int customer = 1;
////	if(mw.newCustomer(Id,customer))
////		System.out.println("Customer Added");
////	else
////		System.out.println("Customer Added");
////	mw.commit(Id);
//	
//	
//	int max =20;
//	for(int i=1;i<max;i++){
//		Id = mw.startTransaction();
//		double start = new Date().getTime();
//		try{
//			flightNum = Id;
//			flightSeats = 1;
//			flightPrice = 1;
//			if(mw.addFlight(Id,flightNum,flightSeats,flightPrice))
//				System.out.println("Flight added");
//			else
//				System.out.println("Flight could not be added");
//		}
//		catch(Exception e){
//			System.out.println("EXCEPTION:");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//		}	
//		operations++;
//		try{
//			location = "usa";
//			if(mw.addRooms(Id,location,max,10))
//				System.out.println("Room added");
//			else
//				System.out.println("Room could not be added");
//		}
//		catch(Exception e){
//			System.out.println("EXCEPTION:");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//		}
//		operations++;
//		try{
//			location = "usa";
//			numCars = 1;
//			price = 15;
//			if(mw.addCars(Id,location,numCars,price))
//				System.out.println("Cars added");
//			else
//				System.out.println("Cars could not be added");
//		}
//		catch(Exception e){
//			System.out.println("EXCEPTION:");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//		}
//		
//		operations++;
//		TotalTimeSpent+= (new Date().getTime()- start);
//		mw.commitMW(Id,0);
//		
//	}
//	double avg_response_time = TotalTimeSpent/operations;
//	System.out.println("The average response time was (in ms): " + avg_response_time);
//	}
//}
