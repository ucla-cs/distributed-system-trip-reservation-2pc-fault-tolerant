package ResImpl;

import java.rmi.RemoteException;

import ResInterface.ResourceManager;
import TransactionManager.InvalidTransactionException;
import TransactionManager.TransactionAbortedException;

public class ResourceManagerImplTest 
{
	public static void main(String[] args) throws RemoteException, InvalidTransactionException, TransactionAbortedException
	{
//		ResourceManagerImpl rm = new ResourceManagerImpl("car");
//		
//		int t0 = 0;
//		String location0 = "A";
//		int numCars = 100;
//		int price = 125;
//		
//		int t1 = 1;
//		
////		rm.addCars(t0, location0, numCars, price);
////		rm.queryCars(t1, location0);		
////		rm.abort(t0);	
////		rm.queryCars(t1, location0);
////		
////		Trace.info("\n\n");
////		
////		rm.addCars(t1, location0, numCars, price);
////		rm.queryCars(t1, location0);		
////		rm.commit(t1);	
////		rm.queryCars(t1, location0);
////		
////		Trace.info("\n\n");
//		
//		int t2 = 2;
//		String location1 = "B";
//		
//		rm.addRooms(t2, location1, numCars, price);
//		rm.queryRooms(t2, location1);		
//		rm.addRooms(t2, location1, numCars, price);
//		rm.queryRooms(t2, location1);
//		
//		rm.addCars(t2, location0, numCars, price);
//		rm.queryCars(t2, location0);	
//		rm.deleteCars(t2, location0);
//		//rm.reserveItems(t2, customer1, location0);
//		rm.queryCars(t2, location0);
//		
//		rm.abortRM(t2);	
//		rm.queryCars(t2, location0);
//		rm.queryRooms(t2, location1);
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		Trace.warn("About to shutdown RM");
//		Trace.info(Boolean.toString(rm.shutdown()));
//		Trace.warn("If the RM shutdown, then this should not all of the numbers 1-1000000 should be printed as the JVM should terminate.");
//		
//		for(int i = 0 ; i < 1000000 ; i++)
//		{
//			System.out.println(i);
//		}
//		
	}
}
