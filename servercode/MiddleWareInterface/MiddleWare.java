package MiddleWareInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface MiddleWare extends Remote{
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
	throws RemoteException;//, TransactionAbortedException, InvalidTransactionException; 
   
    /* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addRooms(int id, String location, int numRooms, int price) 
	throws RemoteException;	    

			    
    /* new customer just returns a unique customer identifier */
    public int newCustomer(int id) 
	throws RemoteException;
    
    /* new customer with providing id */
    public boolean newCustomer(int id, int cid)
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
	throws RemoteException;; 

    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public boolean deleteRooms(int id, String location) 
	throws RemoteException;
    
    /* deleteCustomer removes the customer and associated reservations */
    public boolean deleteCustomer(int id, int customer) 
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

    /* return a bill */
    public String queryCustomerInfo(int id,int customer) 
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

    /* Reserve a seat on this flight*/
    public boolean reserveFlight(int id, int customer, int flightNumber) 
	throws RemoteException;

    /* reserve a car at this location */
    public boolean reserveCar(int id, int customer, String location) 
	throws RemoteException;

    /* reserve a room certain at this location */
    public boolean reserveRoom(int id, int customer, String location) 
	throws RemoteException;


    /* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location, boolean Car, boolean Room)
	throws RemoteException;
    
    /**
     * Starts a new transaction.
     * @return The newly-created unique transaction identifier number.
     * @throws RemoteException
     */
    public int startTransaction()
    throws RemoteException;
    
    /**
     * 
     * @param transactionId The identifier for the transaction to commit.
     * @param crashOption 
     * @return true if successfully committed, false otherwise.
     * @throws RemoteException
     */
    public boolean commitMW(int transactionId, int crashOption,String machine)
    throws RemoteException;
    
    /**
     * 
     * @param transactionId The identifier for the transaction to commit.
     * @return true if successfully aborted, false otherwise.
     * @throws RemoteException
     */
    public boolean abortMW(int transactionId)
    throws RemoteException;
    
    public boolean shutdown() 
    throws RemoteException;
    
    public boolean crash(String which) 
    throws RemoteException;
    
}
