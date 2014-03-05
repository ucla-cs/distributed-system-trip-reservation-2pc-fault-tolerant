package Persistance;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ResImpl.Trace;

public class RMPersistance 
{
	
	private static final String RM_MASTER_FILE_SUFFIX = "-master";
	private static final String RM_DIRECTOY_SUFFIX = "-directory";
	
	public static String getMasterFileLocation(String rmType)
	{
		return getRMDirectoryLocation(rmType) + rmType + RM_MASTER_FILE_SUFFIX;
	}
	
	public static String getRMDirectoryLocation(String rmType)
	{		
		String slash = getOSDependentSlash();
		return rmType + RM_DIRECTOY_SUFFIX + slash;
	}
	
	public static void writeOut(String location, Object object)
	{
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try
		{
			fos = new FileOutputStream(location);
			oos = new ObjectOutputStream(fos);

			Trace.info("Saving " + object + " to location " + location);
			
			oos.writeObject(object);

		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try 
			{
				//System.out.println("Closing all output streams for the saved " + object + " to location " + location);
				oos.close();
				fos.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static Object readIn(String location)
	{
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		Object o = null;
		
		try
		{
			fis = new FileInputStream(location);
			ois = new ObjectInputStream(fis);

			Trace.info("Loading in object from location " + location);
			
			try 
			{
				o = ois.readObject();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}

		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try 
			{
				//System.out.println("Closing all input streams for the file at location " + location);
				if(ois != null)
				{
					ois.close();
				}
				if(fis != null)
				{
					fis.close();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		return o;
	}	
	
	private static String getOSDependentSlash()
	{
		String osName = System.getProperty("os.name");
		if(osName.contains("Windows"))
		{
			return "\\";
		}
		else
		{
			//Assuming its linux for now.
			return "/";
		}
	}
	
}
