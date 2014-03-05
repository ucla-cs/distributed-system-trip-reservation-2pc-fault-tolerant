package TransactionManager;

import GlobalIdentifiers.ItemType;
import GlobalIdentifiers.TransactionState;

public class MWTransactionRecord 
{
	private final int transactionId;
	private boolean enlistedCarRM;
	private boolean enlistedFlightRM;
	private boolean enlistedHotelRM;
	private long lastActivityTime;
	private TransactionState transactionState;
	
	public MWTransactionRecord(int transactionId)
	{
		this.transactionId = transactionId;
		this.enlistedCarRM = false;
		this.enlistedFlightRM = false;
		this.enlistedHotelRM = false;
		lastActivityTime = System.currentTimeMillis();
		transactionState = TransactionState.PROCESSING;
	}
	
	public int getTransactionId()
	{
		return transactionId;
	}
	
	public void enlist(ItemType itemType) throws TransactionAbortedException, InvalidTransactionException
	{
		
		if(this.transactionState == TransactionState.COMMITED)
		{
			throw new InvalidTransactionException(transactionId, "Transaction has already been committed. No further operations are allowed within the context of the Transaction of id : " + transactionId);
		}
		else if(this.transactionState == TransactionState.ABORTED)
		{
			throw new TransactionAbortedException(transactionId, "Transaction has already been aborted. No further operations are allowed within the context of the Transaction of id : " + transactionId);
		}
		
		if(itemType == ItemType.Car)
		{
			enlistedCarRM = true;
		}
		else if (itemType == ItemType.Flight)
		{
			enlistedFlightRM = true;
		}
		else if (itemType == ItemType.Room)
		{
			enlistedHotelRM = true;
		}
		
		lastActivityTime = System.currentTimeMillis();
	}
	
	public boolean isEnlisted(ItemType itemType)
	{
		boolean enlisted = false;
		
		if(itemType == ItemType.Car)
		{
			enlisted = enlistedCarRM;
		}
		else if (itemType == ItemType.Flight)
		{
			enlisted = enlistedFlightRM;
		}
		else if (itemType == ItemType.Room)
		{
			enlisted = enlistedHotelRM;
		}
		
		return enlisted;
	}
	
	public long lastActivityTime()
	{
		return lastActivityTime;
	}
	
	public void setTransactionState(TransactionState transactionState) throws InvalidTransactionException, TransactionAbortedException
	{
		if(this.transactionState == TransactionState.PROCESSING)
		{
			this.transactionState = transactionState;
		}
		else if(this.transactionState == TransactionState.COMMITED && transactionState != TransactionState.COMMITED)
		{
			throw new InvalidTransactionException(transactionId, "Transaction has already been committed. No further operations are allowed within the context of the Transaction of id : " + transactionId);
		}
		else if(this.transactionState == TransactionState.ABORTED && transactionState != TransactionState.ABORTED)
		{
			throw new TransactionAbortedException(transactionId, "Transaction has already been aborted. No further operations are allowed within the context of the Transaction of id : " + transactionId);
		}
	}
	
	public TransactionState getTransactionState()
	{
		return transactionState;
	}
}
