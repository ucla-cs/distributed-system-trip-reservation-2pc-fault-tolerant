//package MiddleWareInterface;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Test;
//import org.junit.Before;
//
//import MiddleWareImpl.MiddleWareImpl;
//import MiddleWareImpl.ReservedItem;
//import ResourceManager.Car;
//import ResourceManager.Hotel;
//import ResourceManager.RMHashtable;
//import ResourceManager.Trace;
//
//import java.rmi.RemoteException;
//
//
///* Basic Test class to test distribution logic prior to TCP implementation. */
//public class MiddleWareTester {
//
//	private MiddleWareImpl mw;
//	private int id;
//	private int custID;
//	private String loc1;
//	private String loc2;
//	private String loc3;
//	private int num;
//	
//	@Before
//	public void initializeTestVariables()
//	{
//		mw = new MiddleWareImpl();
//	}
//	
//	@Test
//	public void testBasicInitialization()
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "ALocation";
//		num = 123;
//		
//		try {
//			assertEquals( 0, mw.queryCars(id, loc1) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( 0, mw.queryCarsPrice(id, loc1) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( 0, mw.queryFlight(id, num) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( 0, mw.queryFlightPrice(id, num) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( 0, mw.queryRooms(id, loc1) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( 0, mw.queryRoomsPrice(id, loc1) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertFalse( mw.reserveCar(id, custID, loc1) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertFalse( mw.reserveFlight( id, custID, num) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertFalse( mw.reserveRoom(id, custID, loc1) );
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//	}
//	
//	@Test
//	public void testBasicCustomerDeletion() 
//	{	
//		id = 0;
//		loc1 = "TheStandard";
//		
//		final int INITIAL_CARS = 5;
//		final int INITIAL_PRICE = 220;
//		
//		try {
//			mw.addCars(id, loc1, INITIAL_CARS, INITIAL_PRICE);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( INITIAL_CARS, mw.queryCars(id, loc1));
//		} catch (RemoteException e1) {
//			e1.printStackTrace();
//		}
//		
//		int c1 = -1;
//		try {
//			c1 = mw.newCustomer(1);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			boolean b = mw.reserveCar(id, c1, loc1);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( INITIAL_CARS - 1, mw.queryCars(id, loc1));
//		} catch (RemoteException e1) {
//			e1.printStackTrace();
//		}
//		
//		try {
//			assertEquals(INITIAL_PRICE, mw.queryCarsPrice(id, loc1));
//		} catch (RemoteException e2) {
//			e2.printStackTrace();
//		}
//		
//		try {
//			mw.deleteCustomer(id, c1);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			assertEquals( INITIAL_CARS, mw.queryCars(id, loc1));
//		} catch (RemoteException e1) {
//			e1.printStackTrace();
//		}
//		
//		try {
//			mw.addCars(id, loc1, 2, INITIAL_PRICE + 20);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} 
//		
//		try {
//			assertEquals( INITIAL_CARS + 2, mw.queryCars(id, loc1));
//		} catch (RemoteException e1) {
//			e1.printStackTrace();
//		}
//		
//		try {
//			assertEquals(INITIAL_PRICE + 20, mw.queryCarsPrice(id, loc1));
//		} catch (RemoteException e2) {
//			e2.printStackTrace();
//		}		
//		
//	}
//	
//	@Test
//	public void complexReserveTest() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "ALocation";
//		loc2 = "BLocation";
//		loc3 = "CLocation";
//		num = 123;
//		
//		mw.addCars(id, loc1, 1, 210);
//		mw.addCars(id, loc2, 2, 220);
//		mw.addCars(id, loc3, 3, 230);
//		
//		mw.addFlight(id, 6, 60, 260);
//		mw.addFlight(id, 7, 70, 270);
//		mw.addFlight(id, 8, 80, 280);
//		
//		mw.addRooms(id, loc1, 101, 100);
//		mw.addRooms(id, loc2, 102, 200);
//		mw.addRooms(id, loc3, 103, 300);
//		
//		assertEquals(1, mw.queryCars(id, loc1) );
//		assertEquals(2, mw.queryCars(id, loc2) );
//		assertEquals(3, mw.queryCars(id, loc3) );
//		
//		assertEquals(210, mw.queryCarsPrice(id, loc1) );
//		assertEquals(220, mw.queryCarsPrice(id, loc2) );
//		assertEquals(230, mw.queryCarsPrice(id, loc3) );
//		
//		assertFalse( mw.reserveCar(id, custID, loc1) );
//		
//		mw.newCustomer(id, custID);
//		
//		assertTrue( mw.reserveCar(id, custID, loc1) );
//		
//		assertEquals( 0 , mw.queryCars(id, loc1) );
//		
//		assertFalse( mw.reserveCar(id, custID, loc1) );
//		
//		mw.reserveCar(id, custID, loc2);
//		
//		mw.reserveCar(id, custID, loc2);
//		
//		assertEquals(0, mw.queryCars(id, loc2));
//		
//		assertTrue(true); //used as a  breakpoint to test final variables in debug mode.
//		
//	}
//	
//	@Test
//	public void itineraryTestSuccess() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "FinalLocation";
//		String flight1 = "1";
//		String flight2 = "2";
//		String flight3 = "3";
//		java.util.Vector flights = new java.util.Vector();
//		flights.add(flight1);
//		flights.add(flight2);
//		flights.add(flight3);
//		
//		mw.newCustomer(id, custID);
//		
//		mw.addCars(id, loc1, 2, 200);
//		mw.addRooms(id, loc1, 3, 300);
//		mw.addFlight(id, 1, 11, 110);
//		mw.addFlight(id, 2, 12, 120);
//		mw.addFlight(id, 3, 13, 130);
//		
//		mw.itinerary(id, custID, flights, loc1, true, true);
//		
//		assertEquals( 1 , mw.queryCars(id, loc1) );
//		assertEquals( 2 , mw.queryRooms(id, loc1) );
//		assertEquals( 10 , mw.queryFlight(id, 1) );
//		assertEquals( 11 , mw.queryFlight(id, 2) );
//		assertEquals( 12 , mw.queryFlight(id, 3) );
//		
//		//assertTrue(true);
//		
//	}
//	
//	@Test
//	public void itineraryTestFailCar() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "FinalLocation";
//		String flight1 = "1";
//		String flight2 = "2";
//		String flight3 = "3";
//		java.util.Vector flights = new java.util.Vector();
//		flights.add(flight1);
//		flights.add(flight2);
//		flights.add(flight3);
//		
//		mw.newCustomer(id, custID);
//		
//		//mw.addCars(id, loc1, 2, 200);
//		mw.addRooms(id, loc1, 3, 300);
//		mw.addFlight(id, 1, 11, 110);
//		mw.addFlight(id, 2, 12, 120);
//		mw.addFlight(id, 3, 13, 130);
//		
//		assertFalse( mw.itinerary(id, custID, flights, loc1, true, true) ) ;
//		
//		assertEquals( 0 , mw.queryCars(id, loc1) );
//		assertEquals( 3 , mw.queryRooms(id, loc1) );
//		assertEquals( 11 , mw.queryFlight(id, 1) );
//		assertEquals( 12 , mw.queryFlight(id, 2) );
//		assertEquals( 13 , mw.queryFlight(id, 3) );
//		
//		assertTrue( true );
//		
//	}
//	
//	@Test
//	public void itineraryTestFailRoom() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "FinalLocation";
//		String flight1 = "1";
//		String flight2 = "2";
//		String flight3 = "3";
//		java.util.Vector flights = new java.util.Vector();
//		flights.add(flight1);
//		flights.add(flight2);
//		flights.add(flight3);
//		
//		mw.newCustomer(id, custID);
//		
//		mw.addCars(id, loc1, 2, 200);
//		//mw.addRooms(id, loc1, 3, 300);
//		mw.addFlight(id, 1, 11, 110);
//		mw.addFlight(id, 2, 12, 120);
//		mw.addFlight(id, 3, 13, 130);
//		
//		assertFalse( mw.itinerary(id, custID, flights, loc1, true, true) ) ;
//		
//		assertEquals( 2 , mw.queryCars(id, loc1) );
//		assertEquals( 0 , mw.queryRooms(id, loc1) );
//		assertEquals( 11 , mw.queryFlight(id, 1) );
//		assertEquals( 12 , mw.queryFlight(id, 2) );
//		assertEquals( 13 , mw.queryFlight(id, 3) );
//		
//		assertTrue( true );
//		
//	}
//	
//	@Test
//	public void itineraryTestFailFlight() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "FinalLocation";
//		String flight1 = "1";
//		String flight2 = "2";
//		String flight3 = "3";
//		java.util.Vector flights = new java.util.Vector();
//		flights.add(flight1);
//		flights.add(flight2);
//		flights.add(flight3);
//		
//		mw.newCustomer(id, custID);
//		
//		mw.addCars(id, loc1, 2, 200);
//		mw.addRooms(id, loc1, 3, 300);
//		mw.addFlight(id, 1, 11, 110);
//		mw.addFlight(id, 2, 12, 120);
//		//mw.addFlight(id, 3, 13, 130);
//		
//		assertFalse( mw.itinerary(id, custID, flights, loc1, true, true) ) ;
//		
//		assertEquals( 2 , mw.queryCars(id, loc1) );
//		assertEquals( 3 , mw.queryRooms(id, loc1) );
//		assertEquals( 11 , mw.queryFlight(id, 1) );
//		assertEquals( 12 , mw.queryFlight(id, 2) );
//		assertEquals( 0 , mw.queryFlight(id, 3) );
//		
//		assertTrue( true );
//		
//	}
//	
//	@Test
//	public void itineraryTestFailFlightWithPreviousReservations() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "FinalLocation";
//		String flight1 = "1";
//		String flight2 = "2";
//		String flight3 = "3";
//		java.util.Vector flights = new java.util.Vector();
//		flights.add(flight1);
//		flights.add(flight2);
//		flights.add(flight3);
//		
//		mw.newCustomer(id, custID);
//		
//		mw.addCars(id, loc1, 2, 200);
//		mw.addRooms(id, loc1, 3, 300);
//		mw.addFlight(id, 1, 11, 110);
//		mw.addFlight(id, 2, 12, 120);
//		//mw.addFlight(id, 3, 13, 130);
//		
//		//create other reservations 
//		mw.reserveCar(id, custID, loc1);
//		mw.reserveRoom(id, custID, loc1);
//		mw.reserveRoom(id, custID, loc1);
//		
//		assertFalse( mw.itinerary(id, custID, flights, loc1, true, true) ) ;
//		
//		assertEquals( 1 , mw.queryCars(id, loc1) );
//		assertEquals( 1 , mw.queryRooms(id, loc1) );
//		assertEquals( 11 , mw.queryFlight(id, 1) );
//		assertEquals( 12 , mw.queryFlight(id, 2) );
//		assertEquals( 0 , mw.queryFlight(id, 3) );
//		
//		RMHashtable ht = mw.getCustomerReservations(id, custID);
//		
//		assertEquals( 1, ( (ReservedItem) ht.get( Car.getKey(loc1) ) ).getCount() );
//		assertEquals( 2, ( (ReservedItem) ht.get( Hotel.getKey(loc1) ) ).getCount() );
//		
//		assertTrue( true );
//		
//	}
//
//	@Test
//	public void addDuplicateItemTest() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "ALocation";
//		num = 123;
//		
//		mw.newCustomer(id, custID);
//		
//		mw.addFlight(id, num, 20, 200);
//		
//		assertEquals(20, mw.queryFlight(id, num));
//		assertEquals(200, mw.queryFlightPrice(id, num));
//		
//		mw.addFlight(id, num, 10, 230);
//		
//		assertEquals(30, mw.queryFlight(id, num));
//		assertEquals(230, mw.queryFlightPrice(id, num));
//		
//	}
//	
//	@Test //There are no assertions here yet. I am only testing for the general interleaving chances of 2 threads.
//	public void basicMultithreadedTest() throws RemoteException
//	{
//		id = 0;
//		custID = 321;
//		loc1 = "ALocation";
//		num = 123;
//		
//		mw.newCustomer(id, custID);
//		mw.addCars(id, loc1, 1, 110);
//		
//		final boolean t2Done;
//		
//		
//		Runnable r1 = new Runnable(){
//			public void run() {
//				try 
//				{
//					int cars = mw.queryCars(id, loc1);
//					if( cars > 0 )
//					{
//						System.out.println("r1 : There is at least one car left");
//						//Thread.sleep(1000);
//						System.out.println("r1 : Going to try and reserve that car.");
//						boolean reserved = mw.reserveCar(id, custID, loc1);
//						if(reserved)
//						{
//							Trace.error("r1 : Reserved the car");
//						}
//						else
//						{
//							Trace.error("r1 : Couldn't reserve the car");
//						}
//					}
//				} 
//				catch (RemoteException e) 
//				{
//					e.printStackTrace();
//				} 
////				catch (InterruptedException e) {
////					e.printStackTrace();
////				}
//			}
//		};
//		
//		Runnable r2 = new Runnable(){
//			public void run() 
//			{
//				try 
//				{
//					Trace.info("r2 : about to execute reserveCar");
//					mw.reserveCar(id, custID, loc1);
//				} 
//				catch (RemoteException e) 
//				{
//					e.printStackTrace();
//				}
//			}
//			
//		};
//		
//		Thread t1 = new Thread(r1);
//		Thread t2 = new Thread(r2);
//		
//		t1.start();
//		t2.start();
//		
//	}
//	
//	
//	
//}
