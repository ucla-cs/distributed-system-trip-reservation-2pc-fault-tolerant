package ResInterface;


import java.rmi.Remote;
import java.rmi.RemoteException;


/** 
 * Simplified version from CSE 593 Univ. of Washington
 *
 * Distributed  System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean 
 * return values.  Exceptions are used for systemy things. Return
  * values are used for operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

public interface ResourceManager extends Remote 
{	
	/* Add seats to a flight.  In general this will be used to create a new
	 * flight, but it should be possible to add seats to an existing flight.
	 * Adding to an existing flight should overwrite the current price of the
	 * available seats.
	 *
	 * @return success.
	 */
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) 
	throws RemoteException; 

	/* Add cars to a location.  
	 * This should look a lot like addFlight, only keyed on a string location
	 * instead of a flight number.
	 */
	public boolean addCars(int id, String location, int numCars, int price) 
	throws RemoteException; 

	/* Add rooms to a location.  
	 * This should look a lot like addFlight, only keyed on a string location
	 * instead of a flight number.
	 */
	public boolean addRooms(int id, String location, int numRooms, int price) 
	throws RemoteException; 			    

	/**
	 *   Delete the entire flight.
	 *   deleteflight implies whole deletion of the flight.  
	 *   all seats, all reservations.  If there is a reservation on the flight, 
	 *   then the flight cannot be deleted
	 *
	 * @return success.
	 */   
	public boolean deleteFlight(int id, int flightNum) 
	throws RemoteException; 

	/* Delete all Cars from a location.
	 * It may not succeed if there are reservations for this location
	 *
	 * @return success
	 */		    
	public boolean deleteCars(int id, String location) 
	throws RemoteException; 

	/* Delete all Rooms from a location.
	 * It may not succeed if there are reservations for this location.
	 *
	 * @return success
	 */
	public boolean deleteRooms(int id, String location) 
	throws RemoteException; 

	/* queryFlight returns the number of empty seats. */
	public int queryFlight(int id, int flightNumber) 
	throws RemoteException; 

	/* return the number of cars available at a location */
	public int queryCars(int id, String location) 
	throws RemoteException; 

	/* return the number of rooms available at a location */
	public int queryRooms(int id, String location) 
	throws RemoteException; 

	/* queryFlightPrice returns the price of a seat on this flight. */
	public int queryFlightPrice(int id, int flightNumber) 
	throws RemoteException; 

	/* return the price of a car at a location */
	public int queryCarsPrice(int id, String location) 
	throws RemoteException; 

	/* return the price of a room at a location */
	public int queryRoomsPrice(int id, String location) 
	throws RemoteException; 
	
	/* reserves and returns the price of a room at a loaction */
	public int reserveItem (int id, int customerID, String key, String location)
	throws RemoteException;
	
	public boolean unReserveItem(int id, int customerID, String reservedItemKey, int reservedItemCount)
	throws RemoteException;
	
	public boolean commit_RM(int transactionId, int crashOption)
	throws RemoteException;
	
	public boolean abort_RM(int transactionId)
	throws RemoteException;
	
	public boolean shutdown()
	throws RemoteException;
	
	public boolean crash()
	throws RemoteException;
	
	public boolean vote_Request(int transactionId,int crashOption)
	throws RemoteException;

	public boolean testConnection()
	throws RemoteException;
	

}
