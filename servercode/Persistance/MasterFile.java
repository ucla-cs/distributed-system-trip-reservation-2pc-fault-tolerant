package Persistance;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MasterFile implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3093715159578040777L;
	
	private final List<String> backupLocations;
	private int lastLocationIndex;
	
	public MasterFile( String... backupLocations )
	{
		//TODO verify backupLocations is not null or empty
		this.backupLocations = Arrays.asList( backupLocations );
		lastLocationIndex = 0;
	}
	
	public String getLastLocation()
	{
		return backupLocations.get( lastLocationIndex );
	}
	
	public String getNextLocation()
	{
		return backupLocations.get( ( lastLocationIndex + 1 ) % backupLocations.size() );
	}
	
	public void pointToNextLocation()
	{
		lastLocationIndex = ( lastLocationIndex + 1 ) % backupLocations.size();
	}
	
	public String toString()
	{
		return "MasterFile";
	}
	
//	public static void main(String[] args)
//	{
//		MasterFile mfone = new MasterFile("location0","location1");
//		
//		System.out.println(mfone.getLastLocation());
//		mfone.pointToNextLocation();
//		
//		String masterFileName = "testMasterFile";
//		
//		RMPersistance.writeOut(masterFileName, mfone);
//		
//		MasterFile mftwo = null;
//		
//		mftwo = (MasterFile)RMPersistance.readIn(masterFileName);
//		
//		System.out.println(mftwo.getLastLocation());
//	}
	
}
