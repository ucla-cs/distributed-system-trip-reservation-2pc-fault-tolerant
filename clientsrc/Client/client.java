package Client;
import java.rmi.*;

//import MiddleWareImpl.MiddleWareImpl;
import MiddleWareInterface.MiddleWare;
//import ResImpl.ResourceManagerImpl;
//import ResInterface.ResourceManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.io.*;


public class client
{
	
	private static final String MIDDLEWARE_SERVER = "g10-middleware";
	
	static String message = "blank";
	static MiddleWare mw = null;

	static boolean lookupRegistry = true;
	
	public static void main(String args[])
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


		String server = "localhost";
		if (args.length == 1) 
			server = args[0]; 
		else if (args.length != 0 &&  args.length != 1) 	//The second condition is useless
		{
			System.out.println ("Usage: java client [rmihost]"); 
			System.exit(1); 
		}

		
		try 
		{
			if(lookupRegistry)
			{
				// get a reference to the rmiregistry
				Registry registry = LocateRegistry.getRegistry(server);
				// get the proxy and the remote reference by rmiregistry lookup
				mw = (MiddleWare) registry.lookup(MIDDLEWARE_SERVER);
				if(mw!=null)
				{
					System.out.println("Successfully Connected to MiddleWare");
				}
				else
				{
					System.out.println("Unsuccessful in Connecting to MiddleWare");
				}
				// make call on remote method
			}
			else
			{
				//REMOVE THIS. DONE ONLY FOR TESTING
//				ResourceManager car = new ResourceManagerImpl();
//				ResourceManager flight = new ResourceManagerImpl();
//				ResourceManager room = new ResourceManagerImpl();
//				mw = new MiddleWareImpl(car, flight, room);
			}
		} 
		catch (Exception e) 
		{	
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
		



		//	    System.setSecurityManager(new RMISecurityManager());
		//		try {
		//		rm = (ResourceManager) Naming.lookup("rmi://" + server+ "/RM");
		//		System.out.println("Connected to RM");
		//	    } 
		//	    catch (Exception e) {
		//		System.out.println("Client exception: " + e.getMessage());
		//		e.printStackTrace();
		//	    }

		System.out.println("\n\n\tClient Interface");
		System.out.println("Type \"help\" for list of supported commands");
		while(true)
		{
			System.out.print("\n>");
			try{
				//read the next command
				command =stdin.readLine();
				
				if(command == null)
				{
					stdin = new BufferedReader(new InputStreamReader(System.in));
					command =stdin.readLine();
				}
			}
			catch (IOException io)
			{
				System.out.println("Unable to read from standard in");
				System.exit(1);
			}
			//remove heading and trailing white space
			command=command.trim();
			arguments=obj.parse(command);

			//decide which of the commands this was
			
			if(arguments.size() == 0)
			{
				continue;
			}
			
			switch(obj.findChoice((String)arguments.elementAt(0))){
			case 1: //help section
				if(arguments.size()==1)   //command was "help"
					obj.listCommands();
				else if (arguments.size()==2)  //command was "help <commandname>"
					obj.listSpecific((String)arguments.elementAt(1));
				else  //wrong use of help command
					System.out.println("Improper use of help command. Type help or help, <commandname>");
				break;

			case 2:  //new flight
				if(arguments.size()!=5){
					obj.wrongNumber();
					break;
				}
				System.out.println("Adding a new Flight using id: "+arguments.elementAt(1));
				System.out.println("Flight number: "+arguments.elementAt(2));
				System.out.println("Add Flight Seats: "+arguments.elementAt(3));
				System.out.println("Set Flight Price: "+arguments.elementAt(4));

				try{
					Id = obj.getInt(arguments.elementAt(1));
					flightNum = obj.getInt(arguments.elementAt(2));
					flightSeats = obj.getInt(arguments.elementAt(3));
					flightPrice = obj.getInt(arguments.elementAt(4));
					if(mw.addFlight(Id,flightNum,flightSeats,flightPrice))
						System.out.println("Flight added");
					else
						System.out.println("Flight could not be added");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 3:  //new Car
				if(arguments.size()!=5){
					obj.wrongNumber();
					break;
				}
				System.out.println("Adding a new Car using id: "+arguments.elementAt(1));
				System.out.println("Car Location: "+arguments.elementAt(2));
				System.out.println("Add Number of Cars: "+arguments.elementAt(3));
				System.out.println("Set Price: "+arguments.elementAt(4));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					numCars = obj.getInt(arguments.elementAt(3));
					price = obj.getInt(arguments.elementAt(4));
					if(mw.addCars(Id,location,numCars,price))
						System.out.println("Cars added");
					else
						System.out.println("Cars could not be added");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 4:  //new Room
				if(arguments.size()!=5){
					obj.wrongNumber();
					break;
				}
				System.out.println("Adding a new Room using id: "+arguments.elementAt(1));
				System.out.println("Room Location: "+arguments.elementAt(2));
				System.out.println("Add Number of Rooms: "+arguments.elementAt(3));
				System.out.println("Set Price: "+arguments.elementAt(4));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					numRooms = obj.getInt(arguments.elementAt(3));
					price = obj.getInt(arguments.elementAt(4));
					if(mw.addRooms(Id,location,numRooms,price))
						System.out.println("Rooms added");
					else
						System.out.println("Rooms could not be added");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 5:  //new Customer
				if(arguments.size()!=2){
					obj.wrongNumber();
					break;
				}
				System.out.println("Adding a new Customer using id:"+arguments.elementAt(1));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer=mw.newCustomer(Id);
					System.out.println("new customer id:"+customer);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 6: //delete Flight
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Deleting a flight using id: "+arguments.elementAt(1));
				System.out.println("Flight Number: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					flightNum = obj.getInt(arguments.elementAt(2));
					if(mw.deleteFlight(Id,flightNum))
						System.out.println("Flight Deleted");
					else
						System.out.println("Flight could not be deleted");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 7: //delete Car
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Deleting the cars from a particular location  using id: "+arguments.elementAt(1));
				System.out.println("Car Location: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));

					if(mw.deleteCars(Id,location))
						System.out.println("Cars Deleted");
					else
						System.out.println("Cars could not be deleted");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 8: //delete Room
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Deleting all rooms from a particular location  using id: "+arguments.elementAt(1));
				System.out.println("Room Location: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					if(mw.deleteRooms(Id,location))
						System.out.println("Rooms Deleted");
					else
						System.out.println("Rooms could not be deleted");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
				break;

			case 9: //delete Customer
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Deleting a customer from the database using id: "+arguments.elementAt(1));
				System.out.println("Customer id: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer = obj.getInt(arguments.elementAt(2));
					if(mw.deleteCustomer(Id,customer))
						System.out.println("Customer Deleted");
					else
						System.out.println("Customer could not be deleted");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 10: //querying a flight
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying a flight using id: "+arguments.elementAt(1));
				System.out.println("Flight number: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					flightNum = obj.getInt(arguments.elementAt(2));
					int seats=mw.queryFlight(Id,flightNum);
					System.out.println("Number of seats available:"+seats);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 11: //querying a Car Location
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying a car location using id: "+arguments.elementAt(1));
				System.out.println("Car location: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					numCars=mw.queryCars(Id,location);
					System.out.println("number of Cars at this location:"+numCars);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 12: //querying a Room location
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying a room location using id: "+arguments.elementAt(1));
				System.out.println("Room location: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					numRooms=mw.queryRooms(Id,location);
					System.out.println("number of Rooms at this location:"+numRooms);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 13: //querying Customer Information
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying Customer information using id: "+arguments.elementAt(1));
				System.out.println("Customer id: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer = obj.getInt(arguments.elementAt(2));
					String bill=mw.queryCustomerInfo(Id,customer);
					System.out.println("Customer info : "+bill);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;		       

			case 14: //querying a flight Price
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying a flight Price using id: "+arguments.elementAt(1));
				System.out.println("Flight number: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					flightNum = obj.getInt(arguments.elementAt(2));
					price=mw.queryFlightPrice(Id,flightNum);
					System.out.println("Price of a seat:"+price);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 15: //querying a Car Price
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying a car price using id: "+arguments.elementAt(1));
				System.out.println("Car location: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					price=mw.queryCarsPrice(Id,location);
					System.out.println("Price of a car at this location:"+price);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}			    
				break;

			case 16: //querying a Room price
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Querying a room price using id: "+arguments.elementAt(1));
				System.out.println("Room Location: "+arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					location = obj.getString(arguments.elementAt(2));
					price=mw.queryRoomsPrice(Id,location);
					System.out.println("Price of Rooms at this location:"+price);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 17:  //reserve a flight
				if(arguments.size()!=4){
					obj.wrongNumber();
					break;
				}
				System.out.println("Reserving a seat on a flight using id: "+arguments.elementAt(1));
				System.out.println("Customer id: "+arguments.elementAt(2));
				System.out.println("Flight number: "+arguments.elementAt(3));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer = obj.getInt(arguments.elementAt(2));
					flightNum = obj.getInt(arguments.elementAt(3));
					if(mw.reserveFlight(Id,customer,flightNum))
						System.out.println("Flight Reserved");
					else
						System.out.println("Flight could not be reserved.");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 18:  //reserve a car
				if(arguments.size()!=4){
					obj.wrongNumber();
					break;
				}
				System.out.println("Reserving a car at a location using id: "+arguments.elementAt(1));
				System.out.println("Customer id: "+arguments.elementAt(2));
				System.out.println("Location: "+arguments.elementAt(3));

				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer = obj.getInt(arguments.elementAt(2));
					location = obj.getString(arguments.elementAt(3));

					if(mw.reserveCar(Id,customer,location))
						System.out.println("Car Reserved");
					else
						System.out.println("Car could not be reserved.");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 19:  //reserve a room
				if(arguments.size()!=4){
					obj.wrongNumber();
					break;
				}
				System.out.println("Reserving a room at a location using id: "+arguments.elementAt(1));
				System.out.println("Customer id: "+arguments.elementAt(2));
				System.out.println("Location: "+arguments.elementAt(3));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer = obj.getInt(arguments.elementAt(2));
					location = obj.getString(arguments.elementAt(3));

					if(mw.reserveRoom(Id,customer,location))
						System.out.println("Room Reserved");
					else
						System.out.println("Room could not be reserved.");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 20:  //reserve an Itinerary
				if(arguments.size()<7){
					obj.wrongNumber();
					break;
				}
				System.out.println("Reserving an Itinerary using id:"+arguments.elementAt(1));
				System.out.println("Customer id:"+arguments.elementAt(2));
				for(int i=0;i<arguments.size()-6;i++)
					System.out.println("Flight number"+arguments.elementAt(3+i));
				System.out.println("Location for Car/Room booking:"+arguments.elementAt(arguments.size()-3));
				System.out.println("Car to book?:"+arguments.elementAt(arguments.size()-2));
				System.out.println("Room to book?:"+arguments.elementAt(arguments.size()-1));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					int customer = obj.getInt(arguments.elementAt(2));
					Vector flightNumbers = new Vector();
					for(int i=0;i<arguments.size()-6;i++)
						flightNumbers.addElement(arguments.elementAt(3+i));
					location = obj.getString(arguments.elementAt(arguments.size()-3));
					Car = obj.getBoolean(arguments.elementAt(arguments.size()-2));
					Room = obj.getBoolean(arguments.elementAt(arguments.size()-1));

					if(mw.itinerary(Id,customer,flightNumbers,location,Car,Room))
						System.out.println("Itinerary Reserved");
					else
						System.out.println("Itinerary could not be reserved.");
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;

			case 21:  //quit the client
				if(arguments.size()!=1){
					obj.wrongNumber();
					break;
				}
				System.out.println("Quitting client.");
				System.exit(1);


			case 22:  //new Customer given id
				if(arguments.size()!=3){
					obj.wrongNumber();
					break;
				}
				System.out.println("Adding a new Customer using id:"+arguments.elementAt(1) + " and cid " +arguments.elementAt(2));
				try{
					Id = obj.getInt(arguments.elementAt(1));
					Cid = obj.getInt(arguments.elementAt(2));
					boolean customer=mw.newCustomer(Id,Cid);
					System.out.println("new customer id:"+Cid);
				}
				catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				break;
				
			case 23: //executefile given path
				if(arguments.size()!=2)
				{
					obj.wrongNumber();
					break;
				}
				System.out.println("Executing a file containing commands at location : " + arguments.elementAt(1) );
				
				try 
				{
					stdin = new BufferedReader(new FileReader((String)arguments.elementAt(1)));
				} 
				catch (FileNotFoundException e) 
				{
					System.err.println("Could not find file at location : " + arguments.elementAt(1));
					//reset the input just in case
					stdin = new BufferedReader(new InputStreamReader(System.in));
				}
				
				break;
				
			case 24: //start a transaction
				if(arguments.size()!=1)
				{
					obj.wrongNumber();
					break;
				}
				
				System.out.println("Starting a new transaction.");
				
				int newTransactionID = -1;
				
				try 
				{
					newTransactionID = mw.startTransaction();
				} 
				catch (RemoteException e) 
				{
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				
				System.out.println("Transaction started. Unique Transaction Identifier : " + newTransactionID);
				
				break;
				
			case 25: //commit a transaction
				if(arguments.size()<=2)
				{
					obj.wrongNumber();
					break;
				}

				try 
				{
					Id = obj.getInt(arguments.elementAt(1));
					int crashOption = 0;
					if(arguments.size() == 3)
					{
						crashOption = obj.getInt(arguments.elementAt(2));
						mw.commitMW(Id, crashOption,"");
						
					}
					if(arguments.size()==4){
						crashOption = obj.getInt(arguments.elementAt(2));
						String machine = (String) arguments.elementAt(3);
						mw.commitMW(Id, crashOption, machine);
					}
					
				} 
				catch (Exception e) 
				{
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}
				
				break;
				
			case 26: //abort a transaction
				if(arguments.size()!=2)
				{
					obj.wrongNumber();
					break;
				}

				try 
				{
					Id = obj.getInt(arguments.elementAt(1));
					mw.abortMW(Id);
				} 
				catch (Exception e) 
				{
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}

				break;
				
			case 27: //shutdown	
				if(arguments.size()!=1)
				{
					obj.wrongNumber();
					break;
				}

				try 
				{
					mw.shutdown();
				} 
				catch (Exception e) 
				{
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}

				break;
				
			case 28: //crash
				if(arguments.size()!=2)
				{
					obj.wrongNumber();
					break;
				}

				try 
				{
					String which = (String)arguments.elementAt(1);
					mw.crash(which);
				} 
				catch (Exception e) 
				{
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					//e.printStackTrace();
				}

				break;

			default:
				System.out.println("The interface does not support this command.");
				break;
			}//end of switch
		}//end of while(true)
	}

	public Vector parse(String command)
	{
		Vector arguments = new Vector();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		String argument ="";
		while (tokenizer.hasMoreTokens())
		{
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
	}
	
	public int findChoice(String argument)
	{
		if (argument.compareToIgnoreCase("help")==0)
			return 1;
		else if(argument.compareToIgnoreCase("newflight")==0)
			return 2;
		else if(argument.compareToIgnoreCase("newcar")==0)
			return 3;
		else if(argument.compareToIgnoreCase("newroom")==0)
			return 4;
		else if(argument.compareToIgnoreCase("newcustomer")==0)
			return 5;
		else if(argument.compareToIgnoreCase("deleteflight")==0)
			return 6;
		else if(argument.compareToIgnoreCase("deletecar")==0)
			return 7;
		else if(argument.compareToIgnoreCase("deleteroom")==0)
			return 8;
		else if(argument.compareToIgnoreCase("deletecustomer")==0)
			return 9;
		else if(argument.compareToIgnoreCase("queryflight")==0)
			return 10;
		else if(argument.compareToIgnoreCase("querycar")==0)
			return 11;
		else if(argument.compareToIgnoreCase("queryroom")==0)
			return 12;
		else if(argument.compareToIgnoreCase("querycustomer")==0)
			return 13;
		else if(argument.compareToIgnoreCase("queryflightprice")==0)
			return 14;
		else if(argument.compareToIgnoreCase("querycarprice")==0)
			return 15;
		else if(argument.compareToIgnoreCase("queryroomprice")==0)
			return 16;
		else if(argument.compareToIgnoreCase("reserveflight")==0)
			return 17;
		else if(argument.compareToIgnoreCase("reservecar")==0)
			return 18;
		else if(argument.compareToIgnoreCase("reserveroom")==0)
			return 19;
		else if(argument.compareToIgnoreCase("itinerary")==0)
			return 20;
		else if (argument.compareToIgnoreCase("quit")==0)
			return 21;
		else if (argument.compareToIgnoreCase("newcustomerid")==0)
			return 22;
		else if (argument.compareToIgnoreCase("executefile")==0)
			return 23;
		else if (argument.compareToIgnoreCase("starttransaction")==0)
			return 24;
		else if (argument.compareToIgnoreCase("commit")==0)
			return 25;
		else if (argument.compareToIgnoreCase("abort")==0)
			return 26;
		else if (argument.compareToIgnoreCase("shutdown")==0)
			return 27;
		else if (argument.compareToIgnoreCase("crash")==0)
			return 28;
		else
			return 666;

	}

	public void listCommands()
	{
		System.out.println("\nWelcome to the client interface provided to test your project.");
		System.out.println("Commands accepted by the interface are:");
		System.out.println("help");
		System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
		System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
		System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
		System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
		System.out.println("starttransaction\ncommit\nabort\ncrash");
		System.out.println("executefile");
		System.out.println("shutdown");
		System.out.println("quit");
		System.out.println("\ntype help, <commandname> for detailed info(NOTE the use of comma).");
	}


	public void listSpecific(String command)
	{
		System.out.print("Help on: ");
		switch(findChoice(command))
		{
		case 1:
			System.out.println("Help");
			System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
			System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
			break;

		case 2:  //new flight
			System.out.println("Adding a new Flight.");
			System.out.println("Purpose:");
			System.out.println("\tAdd information about a new flight.");
			System.out.println("\nUsage:");
			System.out.println("\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>");
			break;

		case 3:  //new Car
			System.out.println("Adding a new Car.");
			System.out.println("Purpose:");
			System.out.println("\tAdd information about a new car location.");
			System.out.println("\nUsage:");
			System.out.println("\tnewcar,<id>,<location>,<numberofcars>,<pricepercar>");
			break;

		case 4:  //new Room
			System.out.println("Adding a new Room.");
			System.out.println("Purpose:");
			System.out.println("\tAdd information about a new room location.");
			System.out.println("\nUsage:");
			System.out.println("\tnewroom,<id>,<location>,<numberofrooms>,<priceperroom>");
			break;

		case 5:  //new Customer
			System.out.println("Adding a new Customer.");
			System.out.println("Purpose:");
			System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
			System.out.println("\nUsage:");
			System.out.println("\tnewcustomer,<id>");
			break;

		case 6: //delete Flight
			System.out.println("Deleting a flight");
			System.out.println("Purpose:");
			System.out.println("\tDelete a flight's information.");
			System.out.println("\nUsage:");
			System.out.println("\tdeleteflight,<id>,<flightnumber>");
			break;

		case 7: //delete Car
			System.out.println("Deleting a Car");
			System.out.println("Purpose:");
			System.out.println("\tDelete all cars from a location.");
			System.out.println("\nUsage:");
			System.out.println("\tdeletecar,<id>,<location>,<numCars>");
			break;

		case 8: //delete Room
			System.out.println("Deleting a Room");
			System.out.println("\nPurpose:");
			System.out.println("\tDelete all rooms from a location.");
			System.out.println("Usage:");
			System.out.println("\tdeleteroom,<id>,<location>,<numRooms>");
			break;

		case 9: //delete Customer
			System.out.println("Deleting a Customer");
			System.out.println("Purpose:");
			System.out.println("\tRemove a customer from the database.");
			System.out.println("\nUsage:");
			System.out.println("\tdeletecustomer,<id>,<customerid>");
			break;

		case 10: //querying a flight
			System.out.println("Querying flight.");
			System.out.println("Purpose:");
			System.out.println("\tObtain Seat information about a certain flight.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryflight,<id>,<flightnumber>");
			break;

		case 11: //querying a Car Location
			System.out.println("Querying a Car location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain number of cars at a certain car location.");
			System.out.println("\nUsage:");
			System.out.println("\tquerycar,<id>,<location>");		
			break;

		case 12: //querying a Room location
			System.out.println("Querying a Room Location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain number of rooms at a certain room location.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryroom,<id>,<location>");		
			break;

		case 13: //querying Customer Information
			System.out.println("Querying Customer Information.");
			System.out.println("Purpose:");
			System.out.println("\tObtain information about a customer.");
			System.out.println("\nUsage:");
			System.out.println("\tquerycustomer,<id>,<customerid>");
			break;		       

		case 14: //querying a flight for price 
			System.out.println("Querying flight.");
			System.out.println("Purpose:");
			System.out.println("\tObtain price information about a certain flight.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryflightprice,<id>,<flightnumber>");
			break;

		case 15: //querying a Car Location for price
			System.out.println("Querying a Car location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain price information about a certain car location.");
			System.out.println("\nUsage:");
			System.out.println("\tquerycarprice,<id>,<location>");		
			break;

		case 16: //querying a Room location for price
			System.out.println("Querying a Room Location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain price information about a certain room location.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryroomprice,<id>,<location>");		
			break;

		case 17:  //reserve a flight
			System.out.println("Reserving a flight.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a flight for a customer.");
			System.out.println("\nUsage:");
			System.out.println("\treserveflight,<id>,<customerid>,<flightnumber>");
			break;

		case 18:  //reserve a car
			System.out.println("Reserving a Car.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a given number of cars for a customer at a particular location.");
			System.out.println("\nUsage:");
			System.out.println("\treservecar,<id>,<customerid>,<location>,<nummberofCars>");
			break;

		case 19:  //reserve a room
			System.out.println("Reserving a Room.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
			System.out.println("\nUsage:");
			System.out.println("\treserveroom,<id>,<customerid>,<location>,<nummberofRooms>");
			break;

		case 20:  //reserve an Itinerary
			System.out.println("Reserving an Itinerary.");
			System.out.println("Purpose:");
			System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
			System.out.println("\nUsage:");
			System.out.println("\titinerary,<id>,<customerid>,<flightnumber1>....<flightnumberN>,<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>");
			break;


		case 21:  //quit the client
			System.out.println("Quitting client.");
			System.out.println("Purpose:");
			System.out.println("\tExit the client application.");
			System.out.println("\nUsage:");
			System.out.println("\tquit");
			break;

		case 22:  //new customer with id
			System.out.println("Create new customer providing an id");
			System.out.println("Purpose:");
			System.out.println("\tCreates a new customer with the id provided");
			System.out.println("\nUsage:");
			System.out.println("\tnewcustomerid, <id>, <customerid>");
			break;
			
		case 23: //execute a file
			System.out.println("Execute a file privided a location.");
			System.out.println("Purpose:");
			System.out.println("\tNon-Interactively executes the instructions specified within the file.");
			System.out.println("\nUsage:");
			System.out.println("\texecutefile, <filepath>");
			break;
			
		case 24: //start a transaction
			System.out.println("Start a transaction.");
			System.out.println("Purpose:");
			System.out.println("\tNotify the system of a new transaction and receive a new transaction id.");
			System.out.println("\nUsage:");
			System.out.println("\tstartTransaction");
			break;
			
		case 25: //commit
			System.out.println("Commit a transaction.");
			System.out.println("Purpose:");
			System.out.println("\tNotify the system that a transaction has been completed.");
			System.out.println("\nUsage:");
			System.out.println("\tcommit, <transactionId>, <crashOption>");
			break;
			
		case 26: //abort
			System.out.println("Abort a transaction.");
			System.out.println("Purpose:");
			System.out.println("\tNotify the system that a transaction is to be aborted.");
			System.out.println("\nUsage:");
			System.out.println("\tabort, <transactionId>");
			break;
			
		case 27: //shutdown
			System.out.println("Shutdown.");
			System.out.println("Purpose:");
			System.out.println("\tNotify the system to shutdown all servers.");
			System.out.println("\nUsage:");
			System.out.println("\tshutdown");
			break;
			
		case 28: //crash
			System.out.println("crash.");
			System.out.println("Purpose:");
			System.out.println("\tSpecify a RM to crash immediately.");
			System.out.println("\nUsage:");
			System.out.println("\ncrash <car|flight|room|customer|middleware>");
			break;
			
		default:
			System.out.println(command);
			System.out.println("The interface does not support this command.");
			break;
		}
	}

	public void wrongNumber() {
		System.out.println("The number of arguments provided in this command are wrong.");
		System.out.println("Type help, <commandname> to check usage of this command.");
	}



	public int getInt(Object temp) throws Exception {
		try {
			return (new Integer((String)temp)).intValue();
		}
		catch(Exception e) {
			throw e;
		}
	}

	public boolean getBoolean(Object temp) throws Exception {
		try {
			return (new Boolean((String)temp)).booleanValue();
		}
		catch(Exception e) {
			throw e;
		}
	}

	public String getString(Object temp) throws Exception {
		try {	
			return (String)temp;
		}
		catch (Exception e) {
			throw e;
		}
	}
}
