package MiddleWareImpl;


import java.rmi.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import java.util.Map.Entry;
import java.util.concurrent.Executor;

import GlobalIdentifiers.ItemType;
import LockManager.DeadlockException;
import LockManager.LockManager;
import MiddleWareInterface.MiddleWare;
import ResImpl.Car;
import ResImpl.Flight;
import ResImpl.Hotel;
import ResImpl.RMHashtable;
import ResImpl.RMItem;
import ResImpl.Trace;
import ResInterface.ResourceManager;
import TransactionManager.InvalidTransactionException;
import TransactionManager.TransactionAbortedException;
import TransactionManager.TransactionManager;
import TransactionManager.TransactionManagerImpl;

public class MiddleWareImpl implements MiddleWare {

	protected int crashOption;

	protected Executor threadPool;

	//Hashtable to hold the customers.
	protected RMHashtable m_Customers = new RMHashtable();

	//Hashtable o2f the before images for a transaction.
	protected Hashtable<Integer, TransactionImage> m_BeforeImages = new Hashtable<Integer, TransactionImage>();

	protected ResourceManager m_CarRM;
	protected ResourceManager m_FlightRM;
	protected ResourceManager m_RoomRM;

	protected String carRMHost;
	protected String flightRMHost;
	protected String roomRMHost;
	
	protected TransactionManager transactionManager;
	protected LockManager lockManager;

	private static final String CAR_RM_OBJECT_NAME = "g10-car-rm";
	private static final String FLIGHT_RM_OBJECT_NAME = "g10-flight-rm";
	private static final String ROOM_RM_OBJECT_NAME = "g10-room-rm";

	private static final String MIDDLEWARE_RM_OBJECT_NAME = "g10-middleware";

	public MiddleWareImpl(ResourceManager car, ResourceManager flight, ResourceManager hotel )
	{
		super();
		m_CarRM = car;
		m_FlightRM = flight;
		m_RoomRM = hotel;

		//Instantiate a new TransactionManager (a part of the MiddleWare)
		transactionManager = new TransactionManagerImpl(this, m_CarRM, m_FlightRM, m_RoomRM);

		//Instantiate a new LockManager (a part of the MiddleWare)
		lockManager = new LockManager();
	}

	public MiddleWareImpl()
	{
		super();

	}

	public static void main(String[] args)
	{
		MiddleWareImpl mwImpl = new MiddleWareImpl();

		mwImpl.carRMHost = "localhost";
		mwImpl.flightRMHost = "localhost";
		mwImpl.roomRMHost = "localhost";

		if (args.length == 3) 
		{
			mwImpl.carRMHost = args[0]; 
			mwImpl.flightRMHost = args[1];
			mwImpl.roomRMHost = args[2];
		}
		else if (args.length != 0 &&  args.length != 3) 
		{
			System.out.println ("Usage: java client CarHost FlightHost RoomHost"); 
			System.exit(1); 
		}

		try 
		{
			// get a reference to the rmiregistry
			Registry registry = LocateRegistry.getRegistry(mwImpl.carRMHost);
			// get the proxy and the remote reference by rmiregistry lookup
			mwImpl.m_CarRM = (ResourceManager) registry.lookup(CAR_RM_OBJECT_NAME);	

			if(mwImpl.m_CarRM != null) { System.out.println("Successfully Connected to CarRM"); }
			else
			{
				System.err.println("Unsuccessfully Connected to CarRM");
				System.exit(1);
			}

			// get a reference to the rmiregistry
			registry = LocateRegistry.getRegistry(mwImpl.flightRMHost);
			// get the proxy and the remote reference by rmiregistry lookup
			mwImpl.m_FlightRM = (ResourceManager) registry.lookup(FLIGHT_RM_OBJECT_NAME);

			if(mwImpl.m_FlightRM != null) { System.out.println("Successfully Connected to FlightRM"); }
			else
			{
				System.err.println("Unsuccessfully Connected to FlightRM");
				System.exit(1);
			}

			// get a reference to the rmiregistry
			registry = LocateRegistry.getRegistry(mwImpl.roomRMHost);
			// get the proxy and the remote reference by rmiregistry lookup
			mwImpl.m_RoomRM = (ResourceManager) registry.lookup(ROOM_RM_OBJECT_NAME);	

			if(mwImpl.m_RoomRM != null) { System.out.println("Successfully Connected to RoomRM"); }
			else
			{
				System.err.println("Unsuccessfully Connected to RoomRM");
				System.exit(1);
			}
		} 
		catch (RemoteException e1)  { e1.printStackTrace(); } 
		catch (NotBoundException e) { e.printStackTrace(); }

		try 
		{
			// dynamically generate the stub (client proxy)
			MiddleWare mwInter = (MiddleWare) UnicastRemoteObject.exportObject(mwImpl, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind( MIDDLEWARE_RM_OBJECT_NAME, mwInter );

			System.err.println("MiddleWare ready.");
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}

		//Instantiate a new TransactionManager (a part of the MiddleWare)
		mwImpl.transactionManager = new TransactionManagerImpl(mwImpl, mwImpl.m_CarRM, mwImpl.m_FlightRM, mwImpl.m_RoomRM);

		//Instantiate a new LockManager (a part of the MiddleWare)
		mwImpl.lockManager = new LockManager();
	}
	
	public ResourceManager lookupFlightRM()
	{
		// get a reference to the rmiregistry
		Registry registry = null;
		
		try 
		{
			registry = LocateRegistry.getRegistry(flightRMHost);
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}
		
		// get the proxy and the remote reference by rmiregistry lookup
		try 
		{
			Trace.warn("Verifying connection to the Flight RM...");
			m_FlightRM = (ResourceManager) registry.lookup(FLIGHT_RM_OBJECT_NAME);
			m_FlightRM.testConnection();
		} 
		catch (Exception e) 
		{
			Trace.error("Connection to Flight RM INVALID.");
			//m_FlightRM = null;
			return null;
		}
	
		if(m_FlightRM != null) { Trace.info("Successfully Connected to FlightRM"); }

		return m_FlightRM;
	}
	
	public ResourceManager lookupCarRM()
	{
		// get a reference to the rmiregistry
		Registry registry = null;
		try 
		{
			registry = LocateRegistry.getRegistry(carRMHost);
		} 
		catch (RemoteException e) {
			e.printStackTrace();
		}
		// get the proxy and the remote reference by rmiregistry lookup
		try 
		{
			Trace.warn("Verifying connection to the Car RM...");
			m_CarRM = (ResourceManager) registry.lookup(CAR_RM_OBJECT_NAME);
			m_CarRM.testConnection();
		} 
		catch (Exception e) {
			Trace.error("Connection to Car RM INVALID.");
			//m_CarRM = null;
			return null;
		}
	
		if(m_CarRM != null) { Trace.info("Successfully Connected to CarRM"); }

		return m_CarRM;
	}
	
	public ResourceManager lookupRoomRM()
	{
		// get a reference to the rmiregistry
		Registry registry = null;
		try 
		{
			registry = LocateRegistry.getRegistry(roomRMHost);
		} 
		catch (RemoteException e) {
			e.printStackTrace();
		}
		// get the proxy and the remote reference by rmiregistry lookup
		try {
			Trace.warn("Verifying connection to the Room RM...");
			m_RoomRM = (ResourceManager) registry.lookup(ROOM_RM_OBJECT_NAME);
			m_RoomRM.testConnection();
		} catch (Exception e) {
			Trace.error("Connection to Room RM INVALID.");
			//m_RoomRM = null;
			return null;
		}
	
		if(m_RoomRM != null) { Trace.info("Successfully Connected to RoomRM"); }

		return m_RoomRM;
	}
	
	private ResourceManager validateCarRM()
	{
		try 
		{
			m_CarRM.testConnection();
		} 
		catch (RemoteException e) 
		{
			Runnable r = new Runnable() {
				@Override
				public void run() 
				{
					while(lookupCarRM() == null)
					{
						try 
						{
							Thread.sleep(250);
						} 
						catch (InterruptedException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
		}
		return m_CarRM;
	}
	
	private ResourceManager validateFlightRM()
	{
		try 
		{
			m_FlightRM.testConnection();
		} 
		catch (RemoteException e) 
		{
			Runnable r = new Runnable() {
				@Override
				public void run() 
				{
					while(lookupFlightRM() == null)
					{
						try 
						{
							Thread.sleep(250);
						} 
						catch (InterruptedException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
		}
		return m_FlightRM;
	}
	
	private ResourceManager validateRoomRM()
	{
		try 
		{
			m_RoomRM.testConnection();
		}
		catch (RemoteException e) 
		{
			Runnable r = new Runnable() {
				@Override
				public void run() 
				{
					while(lookupRoomRM() == null)
					{
						try 
						{
							Thread.sleep(250);
						} 
						catch (InterruptedException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
		}
		return m_RoomRM;
	}
	
	private ResourceManager validateRMOfType(ItemType itemType)
	{
		if(itemType.Car.equals(itemType))
		{
			return validateCarRM();
		}
		else if(itemType.Flight.equals(itemType))
		{
			return validateFlightRM();
		}
		else if(itemType.Room.equals(itemType))
		{
			return validateRoomRM();
		}
		else
		{
			return null;
		}
	}
	
	
	//The following three methods are used to directly modify the local HashTable of items.

	// Reads a data item from the RMHashTable
	private RMItem readData( int id, String key )
	{
		synchronized(m_Customers)
		{
			try 
			{
				lockManager.Lock(id, key, LockManager.READ);
				return (RMItem) m_Customers.get(key);				
			} 
			catch (DeadlockException e) 
			{
				//e.printStackTrace();
				Trace.error(e.getMessage());
				return null;
			}
		}
	}

	// Writes a data item to the RMHashTable
	@SuppressWarnings("unchecked")
	private void writeData( int id, String key, RMItem value )
	{
		synchronized(m_Customers)
		{
			saveTransactionImage(id, key);
			m_Customers.put(key, value);
		}
	}

	// Remove the item from the RMHashTable
	private RMItem removeData(int id, String key)
	{
		synchronized(m_Customers)
		{
			Customer value = (Customer)m_Customers.get(key);
			if(value != null)
			{
				saveTransactionImage(id,key);
			}
			return (RMItem)m_Customers.remove(key);
		}
	}

	// Used to resolve the appropriate resource manager object via itemType
	private ResourceManager getResourceManager(ItemType itemType)
	{
		if(itemType == ItemType.Car)
		{
			return m_CarRM;
		}
		else if (itemType == ItemType.Flight)
		{
			return m_FlightRM;
		}
		else if (itemType == ItemType.Room)
		{
			return m_RoomRM;
		}
		else
		{
			return null;
		}
	}

	// reserve an item
	protected boolean reserveItem( int id, int customerID, String hashKey, String location, ItemType itemType )
	{
		Trace.info("MW::reserveItem( " + id + ", customer=" + customerID + ", " +hashKey+ ", "+location+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) 
		{
			Trace.warn("MW::reserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+location+")  failed--customer doesn't exist" );
			return false;
		} 

		//Get the appropriate RM reference
		ResourceManager rm = getResourceManager( itemType );

		// check if the item is available. This is done in the appropriate RM
		int reservationPrice;
		try 
		{
			validateRMOfType(itemType);
			
			reservationPrice = rm.reserveItem( id, customerID, hashKey, location );
		} 
		catch ( RemoteException e ) {
			Trace.warn("MW::reserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+location+")  failed--cannot retrieve item data" );
			return false;
		}

		if( reservationPrice > 0 )
		{
			cust.reserve( hashKey, location, reservationPrice, itemType );
			return true;
		}
		else
		{
			Trace.warn("MW::reserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+location+")  failed--Please try again." );
			return false;
		}

	}


	/* I MAY NOT NEED THESE UNRESERVE METHODS ANYMORE AS THEY ARE ONLY USED FOR THE ITINERARY METHOD.
	 * IF I CAN USE ITINERARY THROUGH TRANACTIONS THEN I CAN GET RID OF THESE METHODS.  */

	// remove a reservation for an item
	protected boolean unReserveItem( int id, int customerID, String hashKey, int itemCount, ItemType itemType )
	{
		Trace.info("MW::unReserveItem( " + id + ", customer=" + customerID + ", " +hashKey+ ", "+itemCount+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) 
		{
			Trace.warn("MW::unReserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+itemCount+")  failed--customer doesn't exist" );
			return false;
		} 

		//Get the appropriate RM
		ResourceManager rm = getResourceManager( itemType );

		boolean unReservationSuccess;
		try 
		{
			validateRMOfType(itemType);
			
			unReservationSuccess = rm.unReserveItem( id, customerID, hashKey, itemCount );
		} 
		catch ( RemoteException e ) {
			Trace.warn("MW::reserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+itemCount+")  failed--cannot retrieve item data" );
			return false;
		}

		if( unReservationSuccess )
		{
			RMHashtable reservations = cust.getReservations();
			ReservedItem item = (ReservedItem) reservations.get(hashKey);

			if( item == null )
			{
				Trace.warn("MW::reserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+itemCount+")  failed--reserved item doesn't exist" );
				return false;
			}

			synchronized(item)
			{
				if( item.getCount() > itemCount )
				{
					item.setCount( item.getCount() - itemCount );
				}
				else
				{
					reservations.remove( hashKey );
				}
			}
			return true;
		}
		else
		{
			Trace.warn("MW::unReserveItem( " + id + ", " + customerID + ", " + hashKey + ", "+itemCount+")  failed--could not unreserve item data" );
			return false;
		}

	}

	protected boolean unReserveCar(int id, int customerID, String location, int itemCount)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException 
	{
		transactionManager.enlistRM(id, ItemType.Car);
		return this.unReserveItem(id, customerID, Car.getKey(location), itemCount, ItemType.Car);
	}

	protected boolean unReserveFlight(int id, int customerID, int flightNumber, int itemCount)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException 
	{
		transactionManager.enlistRM(id, ItemType.Flight);
		return this.unReserveItem(id, customerID, Flight.getKey(flightNumber), itemCount, ItemType.Flight);
	}

	protected boolean unReserveRoom(int id, int customerID, String location, int itemCount)
	throws RemoteException, TransactionAbortedException, InvalidTransactionException 
	{
		transactionManager.enlistRM(id, ItemType.Room);
		return this.unReserveItem(id, customerID, Hotel.getKey(location), itemCount, ItemType.Room);
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
	throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Car);		
			requestLock(id, Car.getKey(location), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateCarRM();
		
		return m_CarRM.addCars(id, location, numCars, price);
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) 
	throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Flight);
			requestLock(id, Flight.getKey(flightNum), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateFlightRM();
		
		return m_FlightRM.addFlight(id, flightNum, flightSeats, flightPrice);
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
	throws RemoteException 
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Room);
			requestLock(id, Hotel.getKey(location), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateRoomRM();
		
		return m_RoomRM.addRooms(id, location, numRooms, price);
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		try
		{
			transactionManager.enlistRM(id, ItemType.Car);
			requestLock(id, Car.getKey(location), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateCarRM();
		
		return m_CarRM.deleteCars(id, location);

	}

	@Override
	public boolean deleteCustomer(int id, int customerID) throws RemoteException
	{
		Trace.info("MW::deleteCustomer(" + id + ", " + customerID + ") called" );
		try
		{
			transactionManager.enlistRM(id, ItemType.Customer);
			requestLock(id,Customer.getKey(customerID),LockManager.READ);

			Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
			if( cust == null ) 
			{
				Trace.warn("MW::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
				return false;
			} 
			else 
			{			
				// Decrease the reserved numbers of all reservable items which the customer reserved. 
				RMHashtable reservationHT = cust.getReservations();
				boolean successfullyUnreserved = true;	
				ItemType itemType;
				for( Enumeration e = reservationHT.keys() ; e.hasMoreElements() ; )
				{		
					String reservedkey = (String) ( e.nextElement() );
					ReservedItem reserveditem = cust.getReservedItem( reservedkey );
					Trace.info("MW::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );

					//We must find out what kind of reservable item it is. After this, we can then request the needed information from the appropriate server.	
					itemType = reserveditem.getItemType();
					ResourceManager rm = getResourceManager( itemType );

					transactionManager.enlistRM( id, itemType );	
					requestLock(id,reserveditem.getKey(),LockManager.WRITE);
					
					validateRMOfType(itemType);

					successfullyUnreserved = rm.unReserveItem( id, customerID, reserveditem.getKey(), reserveditem.getCount() );			

					if( ! successfullyUnreserved )
					{
						Trace.info("MW::deleteCustomer(" + id + ", " + customerID + ")failed--could not remove reservation.");
						break;
					}
				}

				// remove the customer from the storage			
				if( successfullyUnreserved )
				{
					requestLock(id,Customer.getKey(customerID),LockManager.WRITE);

					removeData( id, cust.getKey() );
					Trace.info("MW::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
					return true;
				}
				else
				{
					Trace.info("MW::deleteCustomer(" + id + ", " + customerID + ") failed" );
					return false;
				}
			} 
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Flight);
			requestLock(id, Flight.getKey(flightNum), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateFlightRM();
		
		return m_FlightRM.deleteFlight(id, flightNum);
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Room);
			requestLock(id, Hotel.getKey(location), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateRoomRM();
		
		return m_RoomRM.deleteRooms(id, location);
	}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	//FIXME This is a test facilitation method!! MUST BE REMOVED/COMMENTED_OUT BEFORE SUBMISSION
	public RMHashtable getCustomerReservations(int id, int customerID)
	throws RemoteException
	{
		try
		{
			Trace.info("MW::getCustomerReservations(" + id + ", " + customerID + ") called" );
			String key = Customer.getKey(customerID);
			transactionManager.enlistRM(id, ItemType.Customer);
			requestLock(id, key, LockManager.READ);
			Customer cust = (Customer) readData( id, key );
			if( cust == null ) 
			{
				Trace.warn("MW::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
				return null;
			} 
			else 
			{
				return cust.getReservations();
			}
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
	}


	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers,
			String location, boolean reserveCar, boolean reserveRoom) throws RemoteException
			{		
		//If the entire itinerary works, then there's nothing to be done.
		//If a part of the itinerary fails, then it is undone in the same fasion as last delievable
		//If it deadlocks, then the entire transaction will abort, as expected.
		try
		{
			boolean successfullyReservedCar = false;
			boolean successfullyReservedRoom = false;
			boolean succussfullyReservedFlight = false;		

			if( reserveCar )
			{	
				successfullyReservedCar = this.reserveCar( id, customer, location );
				if( ! successfullyReservedCar )
				{
					return false;
				}
			}

			if( reserveRoom )
			{
				successfullyReservedRoom = this.reserveRoom( id, customer, location );
				if( ! successfullyReservedRoom )
				{
					if( successfullyReservedCar )
					{
						this.unReserveCar( id, customer, location, 1 );
					}
					return false;
				}
			}

			int size = flightNumbers.size();
			for ( int i = 0 ; i < size ; i++ )
			{
				String numOfFlightToReserve = flightNumbers.get(i).toString();
				int flightNum = Integer.parseInt( numOfFlightToReserve );
				succussfullyReservedFlight = this.reserveFlight( id, customer, flightNum );
				if( ! succussfullyReservedFlight )
				{
					for( int j = 0 ; j < i ; j++ )
					{
						String numOfReservedFlightToCancel = flightNumbers.get( j ).toString();
						this.unReserveFlight( id, customer, Integer.parseInt( numOfReservedFlightToCancel ), 1 );
					}

					if( successfullyReservedCar )
					{
						this.unReserveCar(id, customer, location, 1);
					}

					if( successfullyReservedRoom )
					{
						this.unReserveRoom(id, customer, location, 1);
					}

					return false;
				}
			}
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return true;
	}

	@Override
	public int newCustomer(int id) throws RemoteException
	{

		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt( String.valueOf(id) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		try
		{
			Customer cust = new Customer( cid );
			transactionManager.enlistRM(id, ItemType.Customer);
			requestLock(id, cust.getKey(), LockManager.WRITE);
			writeData( id, cust.getKey(), cust );
			Trace.info("MW::newCustomer(" + cid + ") returns ID=" + cid );
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return cid;
	}

	@Override
	public boolean newCustomer(int id, int customerID) throws RemoteException
	{
		try
		{
			Trace.info("INFO: MW::newCustomer(" + id + ", " + customerID + ") called" );
			String key = Customer.getKey( customerID );
			//requestLock(id, key, LockManager.READ);
			Customer cust = (Customer) readData( id, key );
			if( cust == null ) 
			{
				cust = new Customer( customerID );
				transactionManager.enlistRM(id, ItemType.Customer);
				requestLock(id, key, LockManager.WRITE);
				writeData( id, cust.getKey(), cust );
				Trace.info("INFO: MW::newCustomer(" + id + ", " + customerID + ") created a new customer" );
				return true;
			} 
			else 
			{
				Trace.info("INFO: MW::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
				return false;
			}
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		try
		{
			transactionManager.enlistRM(id, ItemType.Car);
			requestLock(id, Car.getKey(location), LockManager.READ);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateCarRM();
		
		return m_CarRM.queryCars(id, location);
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		try
		{
			transactionManager.enlistRM(id, ItemType.Car);
			requestLock(id, Car.getKey(location), LockManager.READ);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateCarRM();
		
		return m_CarRM.queryCarsPrice(id, location);
	}

	@Override
	/* returns a bill */
	public String queryCustomerInfo(int id, int customerID)
	throws RemoteException
	{
		try
		{
			Trace.info("MW::queryCustomerInfo(" + id + ", " + customerID + ") called" );
			String key = Customer.getKey(customerID);
			transactionManager.enlistRM(id, ItemType.Customer);
			requestLock(id, key, LockManager.READ);
			Customer cust = (Customer) readData( id, key );
			if( cust == null ) 
			{
				Trace.warn("MW::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
				return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
			} 
			else 
			{
				String bill = cust.getBill();
				Trace.info("MW::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
				//System.out.println( bill );
				return bill;
			} 
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Flight);
			requestLock(id, Flight.getKey(flightNumber), LockManager.READ);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateFlightRM();
		
		return m_FlightRM.queryFlight(id, flightNumber);
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
	throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Flight);
			requestLock(id, Flight.getKey(flightNumber), LockManager.READ);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateFlightRM();
		
		return m_FlightRM.queryFlightPrice(id, flightNumber);
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Room);
			requestLock(id, Hotel.getKey(location), LockManager.READ);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
		validateRoomRM();
		
		return m_RoomRM.queryRooms(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Room);
			requestLock(id, Hotel.getKey(location), LockManager.READ);
		}
		catch(Exception e)
		{
			throw new RemoteException();
		}
		
		validateRoomRM();
		
		return m_RoomRM.queryRoomsPrice(id, location);
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
	throws RemoteException
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Car);
			requestLock(id, Car.getKey(location), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return reserveItem(id, customer, Car.getKey(location), location, ItemType.Car);
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber)
	throws RemoteException 
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Flight);
			requestLock(id, Flight.getKey(flightNumber), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return reserveItem(id, customer, Flight.getKey(flightNumber), String.valueOf(flightNumber), ItemType.Flight);
	}

	@Override
	public boolean reserveRoom(int id, int customer, String location)
	throws RemoteException 
	{
		try
		{
			transactionManager.enlistRM(id, ItemType.Room);
			requestLock(id, Hotel.getKey(location), LockManager.WRITE);
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return reserveItem(id, customer, Hotel.getKey(location), location, ItemType.Room);
	}

	@Override
	public boolean crash(String which) throws RemoteException {
	
		if(which.equals("car"))
		{
			m_CarRM.crash();
		}
		else if(which.equals("flight"))
		{
			m_FlightRM.crash();
		}
		else if(which.equals("room"))
		{
			m_RoomRM.crash();
		}
		else if(which.equals("customer")){
			System.exit(0);
		}
		else if(which.equals("middleware")){
			System.exit(0);
		}
	
		return true;
	}

	@Override
	public int startTransaction() throws RemoteException
	{
		return transactionManager.startTransaction();
	}

	@Override
	public boolean commitMW(int transactionId, int crashOption,String machine) throws RemoteException
	{
		try
		{
			boolean committed = commitTransaction(transactionId, crashOption,machine);
			if(committed)
			{
				boolean unlocked = lockManager.UnlockAll(transactionId);
			}
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return true;
	}

	private boolean commitTransaction(int transactionId, int crashOption, String machine) throws RemoteException
	{
		try{
			boolean committed =  transactionManager.commit_TM(transactionId,crashOption,machine);
			//We can get rid of the before image then, if there is one.
			if(committed)
			{
				synchronized(m_BeforeImages)
				{
					TransactionImage beforeImage = m_BeforeImages.get(transactionId);
					if(beforeImage != null)
					{
						m_BeforeImages.remove(transactionId);
					}
				}
				//Trace.info("Transaction of id " + transactionId + " has been committed");
			}
		}catch(Exception e){

			throw new RemoteException(e.getMessage());
		}
		return true;
	}

	@Override
	public boolean abortMW(int transactionId) throws RemoteException
	{
		boolean aborted = abortTransaction(transactionId);
		if(aborted)
		{
			boolean unlocked = lockManager.UnlockAll(transactionId);
		}
		return aborted;
	}

	private boolean abortTransaction(int transactionId) throws RemoteException
	{
		boolean aborted = false;
		try
		{
			aborted =  transactionManager.abort_TM(transactionId);

			if(aborted)
			{
				synchronized(m_BeforeImages)
				{
					TransactionImage beforeImage = m_BeforeImages.get(transactionId);
					if(beforeImage != null)
					{
						m_BeforeImages.remove(transactionId);
						beforeImage.restoreBeforeImage();
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return aborted;	
	}

	@Override
	public boolean shutdown() throws RemoteException
	{
		Trace.info("INFO: MiddleWare::shutdown() called" );

		//Assumes that there are no transactions running.
		m_CarRM.shutdown();
		m_FlightRM.shutdown();
		m_RoomRM.shutdown();

		boolean shutdown = true;

		// Verify that not other operations are modifying the hashtable
		synchronized(m_Customers)
		{
			synchronized (m_BeforeImages) 
			{
				//Note: Supposed to assume that the RMs (which the MiddleWare is) have no transactions running. Therefore, this check should not be made.
				//				if(m_BeforeImages.size() > 0)
				//				{
				//					shutdown = false;
				//				}
				//				else if( m_BeforeImages.size() == 0 )
				//				{
				//					shutdown = true;
				//				}
				//				else
				//				{
				//					Trace.warn("Tried to shutdown an RM with a negative amount of transaction before images.\nThis should never happen.");
				//				}

				if(shutdown)
				{
					Runnable r = new Runnable() 
					{
						@Override
						public void run() 
						{
							try 
							{
								Thread.sleep(1000);
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							System.exit(0);
						}
					};

					Thread shutdownThread = new Thread(r);
					shutdownThread.start();
				}
			}
		}

		return shutdown;
	}	


	/**
	 * Creates a lock request. If granted then it simply returns.
	 * If a deadlock exception is thrown then it is caught, aborted, and then throw a TransactionAbortedException.
	 * Throws a RemoteException, TransactionAbortedException, InvalidTransactionException.
	 */
	private boolean requestLock(int transactionId, String key, int lockType) throws RemoteException
	{
		try
		{
			try 
			{
				lockManager.Lock(transactionId, key, lockType);
			} 
			catch (DeadlockException deadlockException) 
			{
				//Abort the transaction
				int deadlockedTransactionId = deadlockException.GetXId();

				if( deadlockedTransactionId != transactionId )
				{
					Trace.error("The transaction id that was found to have timed out is not the transaction that was released.");
				}
				
				Trace.error("Aborting Transaction of ID : "  + transactionId + " due to deadlock.");
				
				abortMW(deadlockedTransactionId);

				//Going to throw an InvalidTransactionException
				throw new TransactionAbortedException(transactionId, "Transaction had to be aborted due to a deadlock.");
			}
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		return true;
	}

	// This must be called from a synchronized context on the m_itemHT.
	// This is why we do not need to synchronize on the access to m_items below
	private void saveTransactionImage(int id, String key)
	{
		synchronized(m_BeforeImages)
		{
			TransactionImage beforeImage = m_BeforeImages.get(id);
			if(beforeImage == null)
			{
				beforeImage = new TransactionImage(id);
				m_BeforeImages.put(id, beforeImage);
			}
			Customer cust = (Customer) m_Customers.get(key);	//Note that this may be null which is completely acceptable
			beforeImage.backup(key, cust);
		}
	}

	private class TransactionImage
	{

		private int transactionID;
		private HashMap<String, Customer> modifiedItems;

		private boolean restored;

		public TransactionImage(int transactionID)
		{
			this.transactionID = transactionID;
			this.modifiedItems = new HashMap<String, Customer>();
			restored = false;
		}

		//This must be called from a synchronized context of the parent ResourceManagerImpl on the m_itemHT
		public synchronized boolean backup(String key, Customer customer)
		{
			boolean updated;

			if(restored)
			{	//If this before image has already been restored, then don't allow it to be updated anymore.
				return false;
			}

			//			Customer cust = customer;

			if( ! modifiedItems.containsKey(key) )
			{
				//Then the before image of this item was not already recorded, and whatever is being updated now should be recorded.
				updated = true;
				Customer backup = (customer == null? null : customer.clone());
				this.modifiedItems.put(key, backup);
			}
			else
			{
				// Then the before image of this item already recorded a previous instance of this item. This should not be recorded.
				updated = false;
			}

			return updated;
		}

		public synchronized boolean restoreBeforeImage()
		{
			if(restored)
			{	//If this before image has already been restored, then don't allow it to be updated anymore.
				return false;
			}

			restored = true;

			synchronized(m_Customers)
			{
				//this wont work if any of the backup items are null
				//				for(ReservableItem rmItem : this.modifiedItems.values())
				//				{
				//					m_itemHT.put(rmItem.getKey(), rmItem);
				//				}
				String key;
				Customer value;
				for(Entry<String, Customer> kvPair : this.modifiedItems.entrySet())
				{
					key = kvPair.getKey();
					value = kvPair.getValue();
					if(value == null)
					{
						m_Customers.remove(key);
					}
					else
					{
						m_Customers.put(key, value);
					}
				}
			}

			return true;
		}
	}

}
