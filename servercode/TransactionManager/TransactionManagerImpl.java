package TransactionManager;

import java.rmi.RemoteException;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import GlobalIdentifiers.ItemType;
import GlobalIdentifiers.TransactionState;
import MiddleWareImpl.MiddleWareImpl;
import MiddleWareInterface.MiddleWare;
import ResImpl.Trace;
import ResInterface.ResourceManager;

public class TransactionManagerImpl implements TransactionManager
{
	// A 1 minute time-to-live for a transaction. 
	// i.e. If a uncommitted transaction does not perform anything for a minute, it is aborted.
	private static final long TIME_TO_LIVE_MILLISECONDS =  1000 * 60;	
	private static final long TIME_TO_LIVE_VERIFICATION_INTERVAL_MILLISECONDS =  1000 * 10;	
	
	//private static final long RM_REBIND_INTERVAL_MILLISECONDS =  1000 * 20;	

	private static int nextTransactionId = 0;

	private MiddleWareImpl m_CustomerRM;
	private ResourceManager m_CarRM;
	private ResourceManager m_FlightRM;
	private ResourceManager m_RoomRM;

	private Hashtable<Integer, MWTransactionRecord> transactions;

	public TransactionManagerImpl(MiddleWare mw, ResourceManager m_CarRM, ResourceManager m_FlightRM, ResourceManager m_RoomRM )
	{
		this.m_CustomerRM = (MiddleWareImpl)mw;
		this.m_CarRM = m_CarRM;
		this.m_FlightRM = m_FlightRM;
		this.m_RoomRM = m_RoomRM;

		this.transactions = new Hashtable<Integer, MWTransactionRecord>();

		final TransactionManagerImpl TM = this;

		Runnable timedOutTransactionCollectorRunnable = new Runnable() 
		{
			@Override
			public void run() 
			{
				while(true)
				{
					try 
					{
						Thread.sleep(TIME_TO_LIVE_VERIFICATION_INTERVAL_MILLISECONDS);
					} 
					catch (InterruptedException e) 
					{
						//e.printStackTrace();
					}

					TM.abortTimedOutTransactions();
				}
			}
		};

		Thread timedOutTransactionCollectorThread = new Thread(timedOutTransactionCollectorRunnable);
		timedOutTransactionCollectorThread.start();

	}

	@Override
	public synchronized int startTransaction()
	{
		Trace.info("INFO: TM::startTransaction() called" );
		int newTransactionIdentifier = nextTransactionId++;
		MWTransactionRecord t = new MWTransactionRecord(newTransactionIdentifier);
		transactions.put(t.getTransactionId(), t);
		return t.getTransactionId();
	}

	@Override
	public synchronized boolean commit_TM(final int transactionId, int crashOption, final String machine) throws InvalidTransactionException, TransactionAbortedException, RemoteException
	{
		
		Trace.info("INFO: TM::commit_TM( "+ transactionId + ", " + crashOption + ", " + machine +" ) called" );
		
		MWTransactionRecord t = transactions.get(transactionId);
		
		if(t == null)
		{
			throw new InvalidTransactionException(transactionId, "Transaction does not exist of id : " + transactionId);
		}
		
		if(crashOption == 1)
		{
			Trace.error("Case 1, "+machine+" is going to be Crashed");
			//Crash RM before is can receive a vote request
			//m_CustomerRM.crash(machine);
			Runnable run = new Runnable() 
			{			
				@Override
				public void run() 
				{
					try {
						Trace.error("Case 1, "+machine+" should be Crashed from inside this thread.");
						m_CustomerRM.crash(machine);
						Trace.error("Case 1, "+machine+" should have been Crashed from inside this thread.");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			};
			Thread th = new Thread(run);
			th.start();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(crashOption == 6)
		{
			Trace.error("Case 6, MiddleWare Crashed");
			//Crash coord before sending vote requests
			m_CustomerRM.crash("customer");
		}

		//Send Vote-Requests
		VoteRequestContext vrc = new VoteRequestContext(t, crashOption, machine);		
		boolean voteDecision = vrc.getVoteRequests();
	
		if(crashOption==10){
			Trace.error("Case 10, MiddleWare Crashed");
			m_CustomerRM.crash("customer");
		}

		if( voteDecision == false )
		{
			Trace.warn("The overall decision of the VOTE_REQUEST is false. Going to send ABORT to all RMs involved in the transaction " + transactionId);
			m_CustomerRM.abortMW(transactionId);
			return false;
		}
		
		Trace.warn("The overall decision of the VOTE_REQUEST is true. Going to send COMMIT to all RMs involved in the transaction " + transactionId);
		
		
		//The vote decision is commit. Must tell all RMs to commit

		
		if(t.isEnlisted(ItemType.Car))
		{
			
			int localCrashOption = crashOption;
			
			if(!"car".equals(machine)) localCrashOption = 0;
			
			try
			{
				m_CarRM.commit_RM(transactionId,localCrashOption);
			}
			catch(RemoteException re)
			{
				Runnable run = new Runnable() 
				{
					@Override
					public void run()
					{
						m_CarRM = null;

						while(m_CarRM == null)
						{
							m_CarRM = m_CustomerRM.lookupCarRM();		
						}
						
						try {
							m_CarRM.commit_RM(transactionId, 0);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				};
				
				Thread thread = new Thread(run);
				thread.start();
			}
		}

		if(t.isEnlisted(ItemType.Flight))
		{
			
			int localCrashOption = crashOption;
			
			if(!"flight".equals(machine)) localCrashOption = 0;
			
			try{
				m_FlightRM.commit_RM(transactionId,localCrashOption);
			}
			catch(RemoteException re)
			{
				Runnable run = new Runnable() 
				{
					@Override
					public void run()
					{
						m_FlightRM = null;

						while(m_FlightRM == null)
						{
							m_FlightRM = m_CustomerRM.lookupFlightRM();		
						}

						try {
							m_FlightRM.commit_RM(transactionId, 0);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				};

				Thread thread = new Thread(run);
				thread.start();
			}

		}

		if(crashOption == 11)
		{
			Trace.error("Case 11, MiddleWare Crashed");
			m_CustomerRM.crash("customer");
		}

		if(t.isEnlisted(ItemType.Room))
		{
			
			int localCrashOption = crashOption;
			
			if(!"flight".equals(machine)) localCrashOption = 0;
			
			try{
				m_RoomRM.commit_RM(transactionId,localCrashOption);
			}
			catch(RemoteException re)
			{
				Runnable run = new Runnable() 
				{
					@Override
					public void run()
					{
						m_RoomRM = null;

						while(m_RoomRM == null)
						{
							m_RoomRM = m_CustomerRM.lookupRoomRM();		
						}

						try {
							m_RoomRM.commit_RM(transactionId, 0);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				};

				Thread thread = new Thread(run);
				thread.start();
			}
		}
		
		transactions.remove(t);
		
		// *** THROWS a TransactionAbortedException and an InvalidTransactionException
		t.setTransactionState(TransactionState.COMMITED);

		
		if(crashOption == 12)
		{
			Trace.error("Case 12, MiddleWare Crashed");
			m_CustomerRM.crash("customer");
		}

		return true;
	}

	@Override
	public synchronized boolean abort_TM(final int transactionId) throws InvalidTransactionException, TransactionAbortedException, RemoteException
	{
		
		Trace.info("INFO: TM::abort_TM( "+ transactionId + " ) called" );
		
		MWTransactionRecord t = transactions.get(transactionId);

		if(t == null)
		{
			//throw new InvalidTransactionException(transactionId, "Transaction does not exist of id : " + transactionId);
			Trace.info("Tried to abort a transaction is no longer active.");
			return true;
		}

		//Might want to catch the TransactionAbortedException here and remove the txn regardless.
		//transactions.remove(t.getTransactionId());	
		//TODO, all aborted txns are left in the hashtable. It would grow infinitely, i could remove
		//aborted txns, but then another client could begin processing using that aborted txn and the system would see it as if it was new.
		//must think about this some more.

		
		if(t.isEnlisted(ItemType.Car))
		{
			try
			{
				m_CarRM.abort_RM(transactionId);
			}
			catch(RemoteException re)
			{
				Runnable run = new Runnable() 
				{
					@Override
					public void run()
					{
						m_CarRM = null;

						while(m_CarRM == null)
						{
							m_CarRM = m_CustomerRM.lookupCarRM();		
						}
						
						try {
							m_CarRM.abort_RM(transactionId);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				};
				
				Thread thread = new Thread(run);
				thread.start();
				
			}
		}

		if(t.isEnlisted(ItemType.Flight))
		{
			try
			{
				m_FlightRM.abort_RM(transactionId);
			}
			catch(RemoteException re)
			{
				Runnable run = new Runnable() 
				{
					@Override
					public void run()
					{
						m_FlightRM = null;

						while(m_FlightRM == null)
						{
							m_FlightRM = m_CustomerRM.lookupFlightRM();		
						}
						
						try 
						{
							m_FlightRM.abort_RM(transactionId);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
					}
				};
				
				Thread thread = new Thread(run);
				thread.start();
				
			}
		}

		if(t.isEnlisted(ItemType.Room))
		{
			try
			{
				m_RoomRM.abort_RM(transactionId);
			}
			catch(RemoteException re)
			{
				Runnable run = new Runnable() 
				{
					@Override
					public void run()
					{
						
						m_RoomRM = null;

						while(m_RoomRM == null)
						{
							m_RoomRM = m_CustomerRM.lookupRoomRM();		
						}
						
						try 
						{
							m_RoomRM.abort_RM(transactionId);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
					}
				};
				
				Thread thread = new Thread(run);
				thread.start();
				
			}
		}

		// *** THROWS a TransactionAbortedException and an InvalidTransactionException
		t.setTransactionState(TransactionState.ABORTED);

		return true;
	}

	@Override
	public void enlistRM(int transactionId, ItemType itemType) throws TransactionAbortedException, InvalidTransactionException
	{
		MWTransactionRecord t = transactions.get(transactionId);

		if(t != null)
		{
			t.enlist(itemType);
		}

	}

	private void abortTimedOutTransactions() 
	{

		LinkedList<MWTransactionRecord> transactionsToAbort = new LinkedList<MWTransactionRecord>();

		synchronized(this)
		{
			long time = System.currentTimeMillis();

			for(MWTransactionRecord t : transactions.values())
			{
				if( t.getTransactionState() == TransactionState.PROCESSING &&  time - t.lastActivityTime() > TIME_TO_LIVE_MILLISECONDS )
				{	
					transactionsToAbort.add(t);
				}
			}
		}

		for(MWTransactionRecord t : transactionsToAbort)
		{
			try 
			{
				Trace.error("Transaction's time-to-live has expired. Aborting transaction " + t.getTransactionId());
				m_CustomerRM.abortMW(t.getTransactionId());
			} 
			catch (RemoteException e) {
				Trace.error("A remote exception was caught within the abortTimedOutTransactions method when aborting a transaction.");
				e.printStackTrace();
			}
		}

	}

	private class VoteRequestContext
	{
		private boolean voteDecisionCar = true;
		private boolean voteDecisionFlight = true;
		private boolean voteDecisionHotel = true;

		private MWTransactionRecord t;
		private int transactionId;
		private int crashOption;
		private String machine;

		private CyclicBarrier startBarrier;
		private CyclicBarrier stopBarrier;

		public VoteRequestContext(MWTransactionRecord t, int crashOption, String machine)
		{
			this.t = t;
			this.transactionId = t.getTransactionId();
			this.crashOption = crashOption;
			this.machine = machine;
		}

		//This should only be called once per VoteRequestContext.
		public boolean getVoteRequests()
		{

			LinkedList<Thread> voteRequestThreads = new LinkedList<Thread>();

			if(t.isEnlisted(ItemType.Car))
			{
				voteRequestThreads.add( getVoteRequest(m_CarRM, ItemType.Car) );
			}

			if(t.isEnlisted(ItemType.Flight))
			{
				voteRequestThreads.add( getVoteRequest(m_FlightRM, ItemType.Flight) );
			}

			if(t.isEnlisted(ItemType.Room))
			{
				voteRequestThreads.add( getVoteRequest(m_RoomRM, ItemType.Room) );
			}

			//Simulate a multicast
			startBarrier = new CyclicBarrier( voteRequestThreads.size() + 1 );
			stopBarrier = new CyclicBarrier( voteRequestThreads.size() + 1 );
			
			for(Thread t : voteRequestThreads)
			{
				t.start();
			}

			//Send VoteRequest
			try 
			{
				Trace.warn("About to wait on start barrier (main T)");
				startBarrier.await();
				Trace.warn("Done waiting on start barrier (main T)");
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			} 
			catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
			
			if(crashOption == 7)
			{
				Trace.error("Case 7, MiddleWare Crashed");
				try 
				{
					m_CustomerRM.crash("customer");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			

			//Await VoteRequest answers
			try 
			{
				Trace.warn("About to wait on stop barrier (main T");
				stopBarrier.await();
				Trace.warn("Done waiting on stop barrier (main T)");
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			} 
			catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
			
			
			if(crashOption==9)
			{
				System.out.println("Case 9, MiddleWare Crashed");
				try {
					m_CustomerRM.crash("customer");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			return voteDecisionCar && voteDecisionFlight && voteDecisionHotel;
		}

		private Thread getVoteRequest(final ResourceManager rm, final ItemType itemType)
		{
			return 
			new Thread(
					new Runnable() 
					{	
						@Override
						public void run() 
						{

							try 
							{
								Trace.warn("About to wait on start barrier");
								startBarrier.await();
								Trace.warn("Done waiting on start barrier");
							} 
							catch (InterruptedException e) {
								e.printStackTrace();
							} 
							catch (BrokenBarrierException e) {
								e.printStackTrace();
							}

							//If anything goes wrong, this is initially false. This would indicate that the RM returned a NO.
							boolean b = false;

							try 
							{
								b = rm.vote_Request(transactionId, crashOption);
							} 
							catch (RemoteException e) 
							{										
								b = false;	
							} 

							if(itemType == ItemType.Car)
							{
								voteDecisionCar = b;
							}
							else if(itemType == ItemType.Flight)
							{
								voteDecisionFlight = b;
							}
							else if(itemType == ItemType.Room)
							{
								voteDecisionHotel = b;
							}
							
							if( crashOption==3 && itemType.toString().toLowerCase().contains(machine) )
							{
								Trace.error("Case 3, "+machine+" Crashed");
								try {
									m_CustomerRM.crash(machine);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
							

							try 
							{
								
								if(crashOption == 8 && stopBarrier.getNumberWaiting() >= 1)
								{
									try 
									{
										Trace.error("Case 8, MiddleWare Crashed");
										m_CustomerRM.crash("customer");
									} 
									catch (RemoteException e) 
									{
										e.printStackTrace();
									}
								}
								
								Trace.warn("About to wait on stop barrier");
								stopBarrier.await();
								Trace.warn("Done waiting on stop barrier");
							} 
							catch (InterruptedException e) {
								e.printStackTrace();
							} 
							catch (BrokenBarrierException e) {
								e.printStackTrace();
							}

						}
					}
			);
		}
	}
}
