package edu.tcnj.TGrid;

import java.util.TreeMap;

/**
 * The MemoryChecker class checks that the client has a minimum amount of RAM.
 * 
 * @author Stephen Sigwart
 */
public class MemoryChecker implements RequirementsChecker
{
	/**
	 * Minimum amount of RAM required in MB
	 */
	private int minRam = 256;
	
	/**
	 * Specifies the minimum amount of RAM the client needs
	 * 
	 * @param minRam Minimum amount of RAM to allow
	 */
	public MemoryChecker(int minRam)
	{
		this.minRam = minRam;
	}
	
	/**
	 * Determines if the client meets the requirement based on it's specs.
	 * 
	 * @param specs Map containing client specs.
	 *	 	 
	 * @return Whether or not the client meets the requirement
	 */
	public boolean meetsRequirements(TreeMap<String, Object> specs)
	{
		boolean rtn = false;
		
		// Make sure this spec is set
		if (specs.containsKey("RAM"))
		{
			try
			{
				int ram = (Integer)specs.get("RAM");
				rtn = (ram >= minRam);
			}
			catch (Exception e)
			{
				// Don't do anything, just assume that the client doesn't meet requirement
			}
		}
		
		return rtn;
	}
}
