/*
 * CpuMonitor.java
 * 
 * Created on Nov 11, 2009
 * 
 */

package edu.tcnj.TGrid.GridClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Contains static methods to monitor CPU.
 * 
 * @author Stephen Sigwart
 */
public class CpuMonitor
{
	/**
	 * Returns the idle percentage for the CPU.
	 * 	
	 * @return Percent of CPU that is idle between 0 and 100.	 
	 */
	public static int getIdleCpuPercent()
	{
		return getIdleCpuPercent(10);
	}
	
	/**
	 * Returns the idle percentage for the CPU.
	 * 
	 * @param sampleTime Amount of time, in seconds, to wait for the sample data.	 
	 * 	
	 * @return Percent of CPU that is idle between 0 and 100.	 
	 */
	public static int getIdleCpuPercent(int sampleTime)
	{
		int idlePercent = 0;
		Process p = null;
		try
		{
			// Construct and run command to run vmstat
			String [] command = {"vmstat", "-n", ""+sampleTime, "2"};
			p = Runtime.getRuntime().exec(command);
			
			// Capture output
			Scanner scan = new Scanner(p.getInputStream());
			
			// Wait for the process to end
			p.waitFor();
			
			// Check that the exit value is correct (ie. zero)
			if (p.exitValue() == 0)
			{
				// Check that there is a header line
				if (scan.hasNextLine())
				{
					// Read in header line
					String line = scan.nextLine();
					
					// Find the area of the output that deals with the cpu
					int idx = line.indexOf("cpu"), sIdx, eIdx;
					if (idx != -1)
					{
						// Find start and end indices for the section
						sIdx = idx;
						eIdx = idx + 2;
						while (sIdx > 0 && line.charAt(sIdx - 1) == '-')
							sIdx--;
						while (eIdx + 1 < line.length() && line.charAt(eIdx + 1) == '-')
							eIdx++;
						
						// Get next header line
						if (scan.hasNextLine())
						{
							// Read in line
							line = scan.nextLine();
							
							// Shorten to the CPU section
							line = line.substring(sIdx, eIdx);
							
							// Create scanner for the string
							Scanner lineScan = new Scanner(line);
							
							// Determine which column is the idle percent
							int colIdx = -1;
							for (int i = 0; colIdx == -1 && lineScan.hasNext(); i++)
								if (lineScan.next().equals("id"))
									colIdx = i;
							
							// Check that the column was found
							if (colIdx != -1)
							{
								// Ignore a line and read in second data line
								if (scan.hasNextLine())
								{
									// Ignore line
									scan.nextLine();
									if (scan.hasNextLine())
									{
										// Get data line and shorted to CPU section
										line = scan.nextLine().substring(sIdx, eIdx);
										
										// Create scanner for the string
										lineScan = new Scanner(line);
										
										// Read in until we get to the idle column
										for (; colIdx > 0 && lineScan.hasNext(); colIdx--)
											lineScan.next();
										
										// Get idle percent
										if (lineScan.hasNextInt())
											idlePercent = lineScan.nextInt();
									}
								}
							}
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			System.err.println("Failed to get CPU usage: " + e);
		}
		catch (InterruptedException e)
		{
			// Kill the process
			if (p != null)
				p.destroy();
		
			System.err.println("Failed to get CPU usage: " + e);
		}
		
		return idlePercent;
	}
}
