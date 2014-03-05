package TransactionManager;

import java.rmi.RemoteException;

import GlobalIdentifiers.ItemType;

public interface TransactionManager 
{
	
    /**
     * Starts a new transaction.
     * @return The newly-created unique transaction identifier number.
     * @throws RemoteException
     */
    public int startTransaction();
    
    /**
     * 
     * Performs a 2PC.
     * 
     * @param transactionId The identifier for the transaction to commit.
     * @param machine 
     * @return true if successfully committed, false otherwise.
     * @throws InvalidTransactionException 
     * @throws TransactionAbortedException 
     * @throws RemoteException 
     */
    public boolean commit_TM(int transactionId,int crashOption, String machine) throws InvalidTransactionException, TransactionAbortedException, RemoteException;
    
    /**
     * 
     * @param transactionId The identifier for the transaction to commit.
     * @return true if successfully aborted, false otherwise.
     * @throws InvalidTransactionException 
     * @throws TransactionAbortedException 
     * @throws RemoteException
     */

    public boolean abort_TM(int transactionId) throws InvalidTransactionException, TransactionAbortedException, RemoteException;
    
    /**
     * 
     * @param transactionId The identifier of the transaction that will involve RM
     * @param itemType The itemType of the RM the transaction is enlisting.
     * @throws RemoteException
     * @throws InvalidTransactionException 
     * @throws TransactionAbortedException 
     */
    public void enlistRM(int transactionId, ItemType itemType) throws RemoteException, TransactionAbortedException, InvalidTransactionException;
    
}
