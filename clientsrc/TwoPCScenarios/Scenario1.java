package TwoPCScenarios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Vector;

import Client.client;
import MiddleWareInterface.MiddleWare;

public class Scenario1 {
	

private static final String MIDDLEWARE_SERVER = "g10-middleware";
	
	static String message = "blank";
	static MiddleWare mw = null;

	public static void main(String args[]) throws RemoteException
	{
		client obj = new client();
		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));			
		
		String command = "";
		Vector arguments  = new Vector();
		int Id, Cid;
		int flightNum;
		int flightPrice;
		int flightSeats;
		boolean Room;
		boolean Car;
		int price;
		int numRooms;
		int numCars;
		String location;


		String server = "willy.cs.mcgill.ca";
		if (args.length == 1) 
			server = args[0]; 
		else if (args.length != 0 &&  args.length != 1) 	//The second condition is useless
		{
			System.out.println ("Usage: java client [rmihost]"); 
			System.exit(1); 
		}

		
		try 
		{
			// get a reference to the rmiregistry
			Registry registry = LocateRegistry.getRegistry(server);
			// get the proxy and the remote reference by rmiregistry lookup
			mw = (MiddleWare) registry.lookup(MIDDLEWARE_SERVER);
			if(mw!=null)
			{
				System.out.println("Successfully Connected to RM");
			}
			else
			{
				System.out.println("Unsuccessful in Connecting to RM");
			}
			// make call on remote method
		} 
		catch (Exception e) 
		{	
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
		

		Id = mw.startTransaction();
		double start = new Date().getTime();
		try{
			flightNum = Id;
			flightSeats = 1;
			flightPrice = 1;
			if(mw.addFlight(Id,flightNum,flightSeats,flightPrice))
				System.out.println("Flight added");
			else
				System.out.println("Flight could not be added");
		}
		catch(Exception e){
			System.out.println("EXCEPTION:");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		
		

		boolean commit = true;
		try{
			commit=mw.commitMW(Id,1,"flight");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		if(commit==false){
			System.out.println("ABORT SUCCESS");
		}
		else{
			System.out.println("ABORT FAILURE");
		}
		
	}
	
}

