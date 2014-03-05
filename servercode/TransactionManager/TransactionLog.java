package TransactionManager;

import java.io.Serializable;

public class TransactionLog implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5545455276489176607L;

	public enum TransactionStatus { YES, NO, COMMIT, ABORT };
	
	private TransactionStatus status;
	private int transactionID;
	
	public TransactionLog(int transactionID, TransactionStatus status)
	{
		this.transactionID = transactionID;
		this.status = status;
	}
	
//	public TransactionStatus getTransactionStatus()
//	{
//		return status;
//	}
//	
	
	public int getTransactionID()
	{
		return transactionID;
	}
	
	public static TransactionLog parseTransactionLog(String string)
	{
		int transactionID = Integer.parseInt(string.replaceAll("[^0-9]*", ""));
		TransactionStatus status = TransactionStatus.valueOf(string.replaceAll("[0-9]*", ""));
		return new TransactionLog(transactionID, status);
	}
	
	public String toString()
	{
		return transactionID + status.toString();
	}
	
	public boolean equals(Object object)
	{
		
		if( ! (object instanceof TransactionLog) )
		{
			return false;
		}
		else
		{
			TransactionLog tlObject = (TransactionLog)object;
			return tlObject.transactionID == this.transactionID && tlObject.status == this.status;
		}
	
	}
	
}
