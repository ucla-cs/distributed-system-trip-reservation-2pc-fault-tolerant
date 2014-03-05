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
//public class PerformanceTest3 {
//
//private static final String MIDDLEWARE_SERVER = "g10-middleware";
//	
//	static String message = "blank";
//	static MiddleWare mw1 = null;
//	static MiddleWare mw2 = null;
//
//	public static void main(String args[]) throws RemoteException, InvalidTransactionException, TransactionAbortedException
//	{
//		client obj = new client();
//		
//		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));			
//		
//		String command = "";
//		Vector arguments  = new Vector();
//		
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
//			mw1 = (MiddleWare) registry.lookup(MIDDLEWARE_SERVER);
//			if(mw1!=null)
//			{
//				System.out.println("Successfully Connected to MW1");
//			}
//			else
//			{
//				System.out.println("Unsuccessful in Connecting to MW1");
//			}
//			// make call on remote method
//		} 
//		catch (Exception e) 
//		{	
//			System.err.println("Client exception: " + e.toString());
//			e.printStackTrace();
//		}
//		
//		try 
//		{
//			// get a reference to the rmiregistry
//			Registry registry = LocateRegistry.getRegistry(server);
//			// get the proxy and the remote reference by rmiregistry lookup
//			mw2 = (MiddleWare) registry.lookup(MIDDLEWARE_SERVER);
//			if(mw2!=null)
//			{
//				System.out.println("Successfully Connected to MW2");
//			}
//			else
//			{
//				System.out.println("Unsuccessful in Connecting to MW2");
//			}
//			// make call on remote method
//		} 
//		catch (Exception e) 
//		{	
//			System.err.println("Client exception: " + e.toString());
//			e.printStackTrace();
//		}
//
//		 ThreadClient t1, t2;
//			t1 = new ThreadClient ( 1,mw1);
//			t2 = new ThreadClient ( 2,mw2);
//			t1.start ();
//			t2.start ();
//			
//			while(!ThreadClient.done1 & !ThreadClient.done2);
//			
//			double avg_response_time = ThreadClient.TotalTimeSpent/ThreadClient.operations;
//			System.out.println("The average response time was (in ms): " + avg_response_time);
//	}	
//}
//
//class ThreadClient extends Thread {
//	public static double TotalTimeSpent = 0;
//	public static int operations = 0;
//	public static boolean done1 = false;
//	public static boolean done2 = false;
//
//	public MiddleWare mw;
//	
//    int threadId;
//
//    public ThreadClient (int threadId,MiddleWare middleware) {
//        this.threadId = threadId;
//        mw = middleware;
//    }
//
//    public void run () {
//        if (threadId == 1) {
//        	
//        	int Id = 0, Cid;
//    		int flightNum;
//    		int flightPrice;
//    		int flightSeats;
//    		boolean Room;
//    		boolean Car;
//    		int price;
//    		int numRooms;
//    		int numCars;
//    		String location;
//    	
//
//    		int max =20;
//    		for(int i=1;i<max;i++){
//    			try {
//					Id = mw.startTransaction();
//				} catch (RemoteException e1) {
//					e1.printStackTrace();
//				}
//    			double start = new Date().getTime();
//    			try{
//    				flightNum = Id;
//    				flightSeats = 1;
//    				flightPrice = 1;
//    				if(mw.addFlight(Id,flightNum,flightSeats,flightPrice))
//    					System.out.println("Flight added");
//    				else
//    					System.out.println("Flight could not be added");
//    			}
//    			catch(Exception e){
//    				System.out.println("EXCEPTION:");
//    				System.out.println(e.getMessage());
//    				e.printStackTrace();
//    			}	
//    			operations++;
//    			try{
//    				location = "usa";
//    				if(mw.addRooms(Id,location,max,10))
//    					System.out.println("Room added");
//    				else
//    					System.out.println("Room could not be added");
//    			}
//    			catch(Exception e){
//    				System.out.println("EXCEPTION:");
//    				System.out.println(e.getMessage());
//    				e.printStackTrace();
//    			}
//    			operations++;
//    			try{
//    				location = "usa";
//    				numCars = 1;
//    				price = 15;
//    				if(mw.addCars(Id,location,numCars,price))
//    					System.out.println("Cars added");
//    				else
//    					System.out.println("Cars could not be added");
//    			}
//    			catch(Exception e){
//    				System.out.println("EXCEPTION:");
//    				System.out.println(e.getMessage());
//    				e.printStackTrace();
//    			}
//    			TotalTimeSpent+= (new Date().getTime()- start);
//    			operations++;
//    			try {
//					mw.commitMW(Id,0);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				} catch (InvalidTransactionException e) {
//					e.printStackTrace();
//				} catch (TransactionAbortedException e) {
//					e.printStackTrace();
//				}
//    			
//    		}
//    	done1 = true;
//        
//        }
//        else if (threadId == 2) {
//        	int Id, Cid;
//    		int flightNum;
//    		int flightPrice;
//    		int flightSeats;
//    		boolean Room;
//    		boolean Car;
//    		int price;
//    		int numRooms;
//    		int numCars;
//    		String location;
//    	
//    	Id = 0;
//    	int customer = 2;
//    	try {
//    		boolean newCustomer = false;
//			try {
//				newCustomer = mw.newCustomer(Id,customer);
//			} catch (TransactionAbortedException e) {
//				e.printStackTrace();
//			} catch (InvalidTransactionException e) {
//				e.printStackTrace();
//			}
//			if(newCustomer)
//				System.out.println("Customer Deleted");
//			else
//				System.out.println("Customer could not be deleted");
//		} catch (RemoteException e1) {
//			e1.printStackTrace();
//		}
//    	
//    	
//		int max =20;
//		for(int i=1;i<max;i++){
//			try {
//				Id = mw.startTransaction();
//			} catch (RemoteException e1) {
//				e1.printStackTrace();
//			}
//			double start = new Date().getTime();
//			try{
//				flightNum = Id;
//				flightSeats = 1;
//				flightPrice = 1;
//				if(mw.addFlight(Id,flightNum,flightSeats,flightPrice))
//					System.out.println("Flight added");
//				else
//					System.out.println("Flight could not be added");
//			}
//			catch(Exception e){
//				System.out.println("EXCEPTION:");
//				System.out.println(e.getMessage());
//				e.printStackTrace();
//			}	
//			operations++;
//			try{
//				location = "usa";
//				if(mw.addRooms(Id,location,max,10))
//					System.out.println("Room added");
//				else
//					System.out.println("Room could not be added");
//			}
//			catch(Exception e){
//				System.out.println("EXCEPTION:");
//				System.out.println(e.getMessage());
//				e.printStackTrace();
//			}
//			operations++;
//			try{
//				location = "usa";
//				numCars = 1;
//				price = 15;
//				if(mw.addCars(Id,location,numCars,price))
//					System.out.println("Cars added");
//				else
//					System.out.println("Cars could not be added");
//			}
//			catch(Exception e){
//				System.out.println("EXCEPTION:");
//				System.out.println(e.getMessage());
//				e.printStackTrace();
//			}
//			TotalTimeSpent+= (new Date().getTime()- start);
//			operations++;
//			try {
//				mw.commitMW(Id,0);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InvalidTransactionException e) {
//				e.printStackTrace();
//			} catch (TransactionAbortedException e) {
//				e.printStackTrace();
//			}
//			
//		}
//        	done2 = true;
//        }
//    }
//    
//}
//	
//	
//		