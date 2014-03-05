package TransactionManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import TransactionManager.TransactionLog.TransactionStatus;

public class TransactionLogger
{
	
	private final String fileName;
	private final FileWriter fileWriter;
	private Hashtable<Integer, TransactionLog> transactionRecords;
	
	public TransactionLogger(String fileName) throws IOException
	{
		this.fileName = fileName;
		this.fileWriter = new FileWriter(fileName, true);
	}
	
	public synchronized void log(TransactionLog log)
	{
		try 
		{
			fileWriter.append(log.toString() + System.getProperty("line.separator"));
			fileWriter.flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public TransactionLog getTransactionRecord(int transactionID)
	{
		if(transactionRecords != null)
		{
			return transactionRecords.get(transactionID);
		}
		
		transactionRecords = new Hashtable<Integer, TransactionLog>();

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		try 
		{
			fileReader = new FileReader(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		bufferedReader = new BufferedReader(fileReader);
		
		String line;
		
		try 
		{
			while(bufferedReader.ready())
			{
				line = bufferedReader.readLine();
				TransactionLog tl = TransactionLog.parseTransactionLog(line);
				transactionRecords.put(tl.getTransactionID(), tl);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bufferedReader.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			try 
			{
				fileReader.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		return transactionRecords.get(transactionID);
	}
	
	public static void main(String[] args) throws IOException
	{
		TransactionLogger tLogger = new TransactionLogger("testTransactionLog.txt");
		
		TransactionLog tl0y = new TransactionLog(0, TransactionStatus.YES);
		TransactionLog tl0c = new TransactionLog(0, TransactionStatus.COMMIT);
		
		tLogger.log(tl0y);
		tLogger.log(tl0c);
		
		System.out.println(tLogger.getTransactionRecord(0).equals(tl0c));
		
	}
	
}
