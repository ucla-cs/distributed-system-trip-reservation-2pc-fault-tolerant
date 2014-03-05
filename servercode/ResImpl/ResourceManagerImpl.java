// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import Persistance.RMPersistance;
import Persistance.MasterFile;
import ResInterface.*;
import TransactionManager.InvalidTransactionException;
import TransactionManager.TransactionAbortedException;
import TransactionManager.TransactionLog;
import TransactionManager.TransactionLogger;
import TransactionManager.TransactionLog.TransactionStatus;

import java.util.*;
import java.util.Map.Entry;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

//public class ResourceManagerImpl extends java.rmi.server.UnicastRemoteObject
public class ResourceManagerImpl implements ResourceManager, Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8132220435437777820L;
	
	private static final String SHADOW_0 = "shadow_0";
	private static final String SHADOW_1 = "shadow_1";
	
	private static final String TRANSACTION_LOG = "transaction-log.txt";
	
	private final static String RM_OBJECT_PREFIX = "g10-";
	private final static String RM_OBJECT_SUFFIX = "-rm";
	
	/**
	 * car|flight|room
	 */
	private final String rmType;
	
	/**
	 * Allows RMs to save to different shadowing locations and make the Master file aware of the latest location.
	 */
	private final String masterFileLocation;
	
	private MasterFile masterFile;
	
	private transient TransactionLogger transactionLogger;
	
	//Hashtable of the RMItems
	protected RMHashtable m_itemHT = new RMHashtable();
	
	//Hashtable of the transaction images for each current transaction.
	protected Hashtable<Integer, TransactionImage> m_TransactionImages = new Hashtable<Integer, TransactionImage>();
	
	//LL of all committed Transaction images
	protected Hashtable<Integer, TransactionImage> m_committedTransactions = new Hashtable<Integer, TransactionImage>();
	
	//LL of all committed Transaction images
	protected Hashtable<Integer, TransactionImage> m_abortedTransactions = new Hashtable<Integer, TransactionImage>();

	protected boolean isCrashed = false;
	
	public static void main(String args[]) 
	{
		// Figure out where server is running
		
		//String server = "localhost";

//		if (args.length == 2) {
//			server = server + ":" + args[1];
//		} else if (args.length != 1 &&  args.length != 2) {
//			System.err.println ("Wrong usage");
//			System.out.println("Usage: java ResImpl.ResourceManagerImpl <car|flight|room> [port]");
//			System.exit(1);
//		}
		
		if (args.length != 1) 
		{
			System.err.println("Wrong usage");
			System.out.println("Usage: java ResImpl.ResourceManagerImpl <car|flight|room>");
			System.exit(1);
		}
		
		String rmType = args[0].toLowerCase();
		
		System.out.println("rmType : " + rmType );
		
		if( !rmType.equalsIgnoreCase("car") && !rmType.equalsIgnoreCase("flight") && !rmType.equalsIgnoreCase("room") )
		{
			System.err.println("Wrong usage.");
			System.out.println("Usage: java ResImpl.ResourceManagerImpl <car|flight|room> [port] : A RM type must be specified as one of the three.");
			System.exit(1);
		}

		try 
		{

			// create a new Server object
			ResourceManagerImpl obj = loadRMFromDisk(rmType);
			
			if(obj == null)
			{
				//Then it is starting for the first time.
				obj = new ResourceManagerImpl(rmType);
			}
			else
			{
				/**
				Then it is recovering from a crash.
				Must create a new transaction logger for ourselves
				*/
				
				try 
				{
					//Create a new TransactionLogger. This is the only field that is transient.
					obj.transactionLogger = new TransactionLogger(RMPersistance.getRMDirectoryLocation(rmType) + TRANSACTION_LOG);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
			}
			
			
			//obj.setRmType(rmType); 		used before creating the ResourceManagerImpl
			
			// dynamically generate the stub (client proxy)
			ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(RM_OBJECT_PREFIX + rmType + RM_OBJECT_SUFFIX, rm);

			System.err.println("Server ready");
			
		} 
		catch (Exception e) 
		{
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		//        if (System.getSecurityManager() == null) {
		//          System.setSecurityManager(new RMISecurityManager());
		//        }
		//        try {
		//               ResourceManagerImpl obj = new ResourceManagerImpl();
		//               Naming.rebind("rmi://" + server + "/RM", obj);
		//               System.out.println("RM bound");
		//        } 
		//        catch (Exception e) {
		//               System.out.println("RM not bound:" + e);
		//        }
	}

	
	private ResourceManagerImpl(String rmType)
	{
		this.rmType = rmType;
		this.masterFileLocation = RMPersistance.getMasterFileLocation( rmType );
		
		String rm_Directory = RMPersistance.getRMDirectoryLocation(rmType);
		
		try 
		{
			this.transactionLogger = new TransactionLogger(rm_Directory + TRANSACTION_LOG);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		this.masterFile = new MasterFile( rm_Directory + SHADOW_0, rm_Directory + SHADOW_1 );
		
		String initialSaveLocation = masterFile.getNextLocation();
		
		RMPersistance.writeOut( initialSaveLocation, this );
		
		masterFile.pointToNextLocation();
		
		RMPersistance.writeOut( masterFileLocation, masterFile );
	}


	private synchronized static ResourceManagerImpl loadRMFromDisk(String rmType)
	{
		String rm_Directory = RMPersistance.getRMDirectoryLocation(rmType);
		
		File f = new File(rm_Directory);
		
		if( ! f.exists() )
		{
			f.mkdir();
		}
		
		String masterFileLocation = RMPersistance.getMasterFileLocation(rmType);
		File masterFile = new File(masterFileLocation);
		
		if(! masterFile.exists() )
		{
			return null;
		}
		
		MasterFile mf = (MasterFile)RMPersistance.readIn(RMPersistance.getMasterFileLocation(rmType));
		String locationToLoadFrom = mf.getLastLocation();
		Object object = RMPersistance.readIn(locationToLoadFrom);
		if(object == null)
		{
			return null;
		}
		else
		{
			ResourceManagerImpl rm = (ResourceManagerImpl) object;
			rm.masterFile.pointToNextLocation();
			return rm;
		}
	}
	
	private synchronized void saveRMToDisk()
	{
		String locationToSave = masterFile.getNextLocation();
		RMPersistance.writeOut(locationToSave, this);
	}
	
	private synchronized void saveMasterRecord()
	{
		masterFile.pointToNextLocation();
		RMPersistance.writeOut(masterFileLocation, masterFile);
	}
	
	
	// Reads a data item from the RMHashTable
	private RMItem readData( int id, String key )
	{
		synchronized(m_itemHT)
		{
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item to the RMHashTable
	private void writeData( int id, String key, RMItem value )
	{
		synchronized(m_itemHT)
		{
			saveTransactionImage(id,key);
			m_itemHT.put(key, value);
		}
	}
	
	// Remove the item from the RMHashTable
	private RMItem removeData(int id, String key)
	{
		synchronized(m_itemHT)
		{		
			RMItem value = (RMItem)m_itemHT.get(key);
			if(value != null)
			{
				saveTransactionImage(id,key);
			}
			return (RMItem)m_itemHT.remove(key);
		}
	}
	
	//The following four methods are used for indirect, generic accessors and modifiers of the RMHashtable

	// deletes the entire item if it exists and is not reserved by anyone.
	protected boolean deleteItem(int id, String key)
	{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		// Check if there is such an item in the storage
		if( curObj == null ) 
		{
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} 
		else 
		{
			synchronized(curObj)
			{
				if( curObj.getReserved() == 0 )
				{
					removeData(id, curObj.getKey());
					Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
					return true;
				}
				else
				{
					Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
					return false;
				}
			}
		}
	}


	// query the number of available seats/rooms/cars on/in plane/hotel/cardealer
	protected int queryNum(int id, String key) 
	{
		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
		int value = 0;  
		ReservableItem curObj = (ReservableItem) readData( id, key);
		if( curObj != null ) 
		{
			value = curObj.getCount();
		} 
		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}	

	// query the price of an item
	protected int queryPrice(int id, String key)
	{
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
		int value = 0; 
		ReservableItem curObj = (ReservableItem) readData( id, key);
		if( curObj != null ) 
		{
			value = curObj.getPrice();
		} 
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
	throws RemoteException
	{
		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		Car curObj = (Car) readData( id, Car.getKey(location) );
		if( curObj == null ) 
		{
			// car location doesn't exist...add it
			Car newObj = new Car( location, count, price );
			writeData( id, newObj.getKey(), newObj );
			Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
		} 
		else 
		{
			// add count to existing car location and update price...
			synchronized(curObj)
			{
				curObj.setCount( curObj.getCount() + count );
				if( price > 0 ) {
					curObj.setPrice( price );
				}
				//writeData( id, curObj.getKey(), curObj ); I don't think this will even do anything
				Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
			}
		}
		return(true);
	}
	
	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
	throws RemoteException
	{
		Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
		Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
		if( curObj == null ) 
		{
			// doesn't exist...add it
			Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
			writeData( id, newObj.getKey(), newObj );
			Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
					flightSeats + ", price=$" + flightPrice );
		} 
		else 
		{
			// add seats to existing flight and update the price...
			synchronized(curObj)
			{
				curObj.setCount( curObj.getCount() + flightSeats );
				if( flightPrice > 0 ) {
					curObj.setPrice( flightPrice );
				}
				//writeData( id, curObj.getKey(), curObj );	//I don't think this does anything
				Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
			}
		}
		return(true);
	}
	
	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price)
	throws RemoteException
	{
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
		if( curObj == null ) 
		{
			// doesn't exist...add it
			Hotel newObj = new Hotel( location, count, price );
			writeData( id, newObj.getKey(), newObj );
			Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
		} 
		else 
		{
			// add count to existing object and update price...
			synchronized(curObj)
			{
				curObj.setCount( curObj.getCount() + count );
				if( price > 0 ) {
					curObj.setPrice( price );
				}
				//writeData( id, curObj.getKey(), curObj ); I don't think this will even do anything
				Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
			}
		} 
		return(true);
	}
	
	// Delete cars from a location
	public boolean deleteCars(int id, String location)
	throws RemoteException
	{
		return deleteItem(id, Car.getKey(location));
	}
	
	public boolean deleteFlight(int id, int flightNum)
	throws RemoteException
	{
		return deleteItem(id, Flight.getKey(flightNum));
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location)
	throws RemoteException
	{
		return deleteItem(id, Hotel.getKey(location));

	}
	
	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
	throws RemoteException
	{
		return queryNum(id, Flight.getKey(flightNum));
	}

	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum )
	throws RemoteException
	{
		return queryPrice(id, Flight.getKey(flightNum));
	}

	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
	throws RemoteException
	{
		return queryNum(id, Hotel.getKey(location));
	}

	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
	throws RemoteException
	{
		return queryPrice(id, Hotel.getKey(location));
	}

	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
	throws RemoteException
	{
		return queryNum(id, Car.getKey(location));
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
	throws RemoteException
	{
		return queryPrice(id, Car.getKey(location));
	}
	
	// reserves an item. Returns the price if the reservation is made, -1 if the reservation doesn't work.
	public int reserveItem(int id, int customerID, String key, String location){
		
		// NOTE : The location parameter is only used in printing, it does not actually do anything.

		// check if the item is available
		int priceToReturn;
		ReservableItem item = (ReservableItem)readData(id, key);

		if(item==null)
		{
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
			return  -1;
		}

		synchronized(item)
		{
			if(item.getCount()==0)
			{
				Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
				priceToReturn = -1;
			}
			else
			{			
				// decrease the number of available items in the storage
				item.setCount( item.getCount() - 1 );
				item.setReserved( item.getReserved() + 1 );			

				Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
				priceToReturn =  item.getPrice();
			}	
		}
		
		return priceToReturn;
	}
	
	public boolean unReserveItem(int id, int customerID, String reservedItemKey, int reservedItemsToRemove)
	throws RemoteException 
	{
		ReservableItem item  = (ReservableItem) readData(id, reservedItemKey);
		
		if ( item == null )
		{
			Trace.info("RM::unReserveItem(" + id + ", " + customerID + ") failed--reservedItem doesn't exist in the ResourceManager.");
			return false;
		}
		
		synchronized(item)
		{			
			int reservedItems = item.getReserved();
			int availableItems =  item.getCount();
			
			if( reservedItems < reservedItemsToRemove )
			{
				Trace.error("RM::unReserveItem(" + id + ", " + customerID + ") has reserved " + reservedItemKey + "which is reserved" + reservedItems +  " times and is still available " + availableItems + " times. -- it has failed as it is attempting to cancel a number of reservations that exceeds the number of reservations in the system. Something has most likely gone wrong."  );
				return false;	//I assume the lock on item is released after this.
			}
			
			Trace.info("RM::unReserveItem(" + id + ", " + customerID + ") has reserved " + reservedItemKey + "which is reserved" +  reservedItems +  " times and is still available " + availableItems + " times"  );

			item.setReserved( reservedItems - reservedItemsToRemove );
			item.setCount( availableItems + reservedItemsToRemove );
		}
		return true;
	}
	
	public String getRmType() {
		return rmType;
	}


	@Override
	public boolean vote_Request(int transactionId,int crashOption) throws RemoteException//, TransactionAbortedException, InvalidTransactionException 
	{
		
		Trace.info("INFO: RM::vote_Request( "+ transactionId + ", " + crashOption + " ) called" );
		
		// TODO synchronize on the transactionImages anywhere abortedTransactions or committedTransactions is accessed.
		synchronized (m_TransactionImages) 
		{
			if(m_abortedTransactions.containsKey(transactionId))
			{
//				TransactionImage tImage = m_TransactionImages.get(transactionId);
//				tImage.beginCoordinatorTimeout();		//starts a new thread to timeout on the
				
				/**
				Don't need to abort again as it is already been aborted.
				The only time this will happen is if the MW timed out on a txn, told this RM to abort, and later receives a 
				commit from the client it believed to have timed out.
				*/
				
				//This actually doesn't need to be performed. Done for clarity.
				transactionLogger.log(new TransactionLog(transactionId, TransactionStatus.ABORT));
				
				if(crashOption==2)
				{
					Trace.error("Case 2, RM Crashed");
					crash();
				}
				
				Trace.info("INFO: RM::vote_Request( "+ transactionId + ", " + crashOption + " ) - returning NO" );
				return false;
				
			}
			else
			{	
				//If this RM hasn't aborted the txn yet, then it might as well agree to commit
				transactionLogger.log(new TransactionLog(transactionId, TransactionStatus.YES));	
				
				TransactionImage tImage = m_TransactionImages.get(transactionId);
				
				if(tImage != null)
				{
					tImage.beginCoordinatorTimeout();		//starts a new thread to timeout on the coordinator decision.

					if(crashOption==2)
					{
						Trace.error("Case 2, RM Crashed");
						crash();
					}
				}
				
				Trace.info("INFO: RM::vote_Request( "+ transactionId + ", " + crashOption + " ) - returning YES" );
				return true;
				
			}	
		}
	}


	public boolean commit_RM(int transactionId,int crashOption ) 
	throws RemoteException
	{

		
		Trace.info("INFO: RM::commit_RM( "+ transactionId + " ) called" );
		
		//We can get rid of the before image then, if there is one.
		synchronized(m_TransactionImages)
		{
			TransactionImage tImage = m_TransactionImages.get(transactionId);
			if(tImage != null)
			{
				
				Trace.info("INFO: RM:: commiting transaction "+ transactionId );
				
				tImage.awaitingCoordDecision = false;
				m_TransactionImages.remove(transactionId);
				m_committedTransactions.put(transactionId, tImage);
				
				//Save before logging the commit.
				saveRMToDisk();
				
				//Log the commit.
				transactionLogger.log(new TransactionLog(transactionId, TransactionStatus.COMMIT));
				
				//Write the master record.
				saveMasterRecord();
			}
		}
		
		if(crashOption==5)
		{
			crash();
		}
		
		Trace.info("INFO: RM::commit_RM( "+ transactionId + " ) method returning" );
		
		return true;
	}
	
	public boolean abort_RM(int transactionId) 
	throws RemoteException
	{
		
		Trace.info("INFO: RM::abort_RM( "+ transactionId + " ) called" );
		
		//We must restore the before image then, if there is one for this transaction.
		synchronized(m_TransactionImages)
		{
			TransactionImage tImage = m_TransactionImages.get(transactionId);
			if(tImage != null && ! tImage.restored)
			{
	
				Trace.info("INFO: RM:: aborting transaction "+ transactionId );
				
				tImage.awaitingCoordDecision = false;
				m_TransactionImages.remove(transactionId);
				tImage.restoreBeforeImage();
				m_abortedTransactions.put(transactionId, tImage);
				
				//Save before logging
				saveRMToDisk();
				
				//Log the commit.
				transactionLogger.log(new TransactionLog(transactionId, TransactionStatus.ABORT));
				
				//Write the master record.
				saveMasterRecord();
			}
			
		}
		
		return true;
	}
	
	public boolean testConnection() throws RemoteException
	{
		return true;
	}
	
	public synchronized boolean shutdown() 
	throws RemoteException
	{
		
		Trace.info("INFO: ResourceManager::shutdown() called" );
		
		boolean shutdown = true;
		
		// Verify that not other operations are modifying the hashtable
		synchronized(m_itemHT)
		{
			synchronized (m_TransactionImages) 
			{
				//Note: Supposed to assume that the RMs have no transactions running. Therefore, this check should not be made.
				
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
	
	@Override
	public boolean crash() throws RemoteException 
	{
		System.exit(0);
		return false;
	}


	public String toString()
	{
		return "ResourceManager::" + rmType;
	}


	// This must be called from a synchronized context on the m_itemHT.
	// This is why we do not need to synchronize on the access to m_items below
	private void saveTransactionImage(int id, String key)
	{
		synchronized(m_TransactionImages)
		{
			TransactionImage beforeImage = m_TransactionImages.get(id);
			if(beforeImage == null)
			{
				beforeImage = new TransactionImage(id);
				m_TransactionImages.put(id, beforeImage);
			}
			RMItem item = (RMItem) m_itemHT.get(key);	//Note that this may be null which is completely acceptable
			beforeImage.backup(key, item);
		}
	}
	

	private class TransactionImage implements Serializable
	{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7902743175748997813L;
		
		private static final long COORDINATOR_WAIT_INTERVAL = 10 * 1000;
		
		private int transactionID;
		private HashMap<String, ReservableItem> unmodifiedOriginaltems;
		
		private boolean restored;
		
		private boolean awaitingCoordDecision;
		
		private TransactionImage(int transactionID)
		{
			this.transactionID = transactionID;
			this.unmodifiedOriginaltems = new HashMap<String, ReservableItem>();
			restored = false;
			awaitingCoordDecision = false;
		}
		
		//This must be called from a synchronized context of the parent ResourceManagerImpl on the m_itemHT
		private synchronized boolean backup(String key, RMItem item)
		{
			boolean updated;
			
			if(restored)
			{	//If this before image has already been restored, then don't allow it to be updated anymore.
				return false;
			}
						
			ReservableItem resItem = (ReservableItem) item;
			
			if( ! unmodifiedOriginaltems.containsKey(key) )
			{
				//Then the before image of this item was not already recorded, and whatever is being updated now should be recorded.
				updated = true;
				ReservableItem backup = (resItem == null? null : resItem.clone());
				this.unmodifiedOriginaltems.put(key, backup);
			}
			else
			{
				// Then the before image of this item already recorded a previous instance of this item. This should not be recorded.
				updated = false;
			}
			
			return updated;
		}
		
		private synchronized boolean restoreBeforeImage()
		{
			if(restored)
			{	
				//If this before image has already been restored, then don't allow it to be updated anymore.
				return false;
			}
			
			restored = true;
			
			synchronized(m_itemHT)
			{
				String key;
				ReservableItem ri;
				for(Entry<String, ReservableItem> kvPair : this.unmodifiedOriginaltems.entrySet())
				{
					key = kvPair.getKey();
					ri = kvPair.getValue();
					if(ri == null)
					{
						m_itemHT.remove(key);
					}
					else
					{
						m_itemHT.put(key, ri);
					}
				}
			}
			
			return true;
		}
		
		/**
		 * Starts a thread that waits ~10 seconds before timing out waiting for Coord decision
		 */
		private void beginCoordinatorTimeout()
		{
			Runnable r = new Runnable()
			{
				
				@Override
				public void run() 
				{
					
					awaitingCoordDecision = true;
					
					long intitialTime = System.currentTimeMillis();
					long time = intitialTime;
					long finalTime = intitialTime + COORDINATOR_WAIT_INTERVAL;
					while(time < finalTime)
					{
						time = System.currentTimeMillis();
						try 
						{
							Thread.sleep(100);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
					}
					
					synchronized (this) 
					{
						if(awaitingCoordDecision)
						{
							//Then we timed out waiting for the coord decision.
							Trace.error("Timed out awaiting Coordinator decision of transactionID : " + transactionID);
							try 
							{
								abort_RM(transactionID);
								transactionLogger.log(new TransactionLog(transactionID, TransactionStatus.ABORT));
							} 
							catch (RemoteException e) 
							{
								e.printStackTrace();
							}
						}
					}
					
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			
		}
		
	}
	
//	public static void main(String[] args) throws RemoteException
//	{
//		
//		//A test main method.
//		String loc = "location0";
//		
//		ResourceManagerImpl rm;
//		
//		rm = loadRMFromDisk("car");
//		if(rm == null)
//		{
//			rm = new ResourceManagerImpl("car");
//		}
//		else
//		{
//			//Then it is recovering from a crash.
//			try 
//			{
//				//Create a new TransactionLogger. This is the only field that is transient.
//				rm.transactionLogger = new TransactionLogger(RMPersistance.getRMDirectoryLocation("car") + TRANSACTION_LOG);
//			} 
//			catch (IOException e) 
//			{
//				e.printStackTrace();
//			}
//
//			//recover by finding out what must be done with each transaction.
//
//		}
//		
//		rm.addCars(0, loc, 10, 150);
//		
//		rm.reserveItem(0, 10, Car.getKey(loc), loc);
//		
//		System.out.println(rm.queryCars(0, loc));
//		
//		rm.saveRMToDisk();
//		
//		rm.reserveItem(0, 10, Car.getKey(loc), loc);
//		rm.reserveItem(0, 10, Car.getKey(loc), loc);
//		rm.reserveItem(0, 10, Car.getKey(loc), loc);
//		
//		System.out.println(rm.queryCars(0, loc));
//		
//		rm.abortRM(0);
//		
//		rm.saveRMToDisk();
//		
//		System.out.println(rm.queryCars(1, loc));
//		
//	}

}
