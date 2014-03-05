package TransactionManager;

public class TransactionAbortedException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6651591614181838037L;
	
	protected int xid = 0;
	
	public TransactionAbortedException (int xid, String msg)
	{
		super(msg);
		this.xid = xid;
	}
	
	public int getXId() 
	{
		return this.xid;
	}
}
