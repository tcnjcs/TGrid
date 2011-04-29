package edu.tcnj.TGrid;

import java.util.Calendar;
import java.io.Serializable;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.text.SimpleDateFormat;
import java.util.TreeMap;

/*******************************************************************************
* Holds information on potential clients.
* 
* @author Stephen
*******************************************************************************/
public class ClientInfo implements Serializable
{
	/**
	 * Serializable version UID
	 */
	public static final long serialVersionUID = -5211774703401247545L;
	
	/**
	 * Hostname or ip address for the client
	 */
	private String hostname = "localhost";
	
	/**
	 * Port number to use for ssh tunnel
	 */
	private int sshTunnelPort = 54321;
	
	/**
	 * Directory where the java class files are located
	 */
	private String javaClassDir = ".";
	
	/**
	 * Number of tasks assigned to the client
	 */
	private int numTasks = 0;
	
	/**
	 * Number of failed tasks for the client
	 */
	private int numFailedTasks = 0;
	
	/**
	 * Information on when the client has been idle in 1/2 hour increments.
	 * Index 0 represents 12:00am-12:30am.
	 */
	private int [] idleDuringInterval = new int[48];
	
	/**
	 * Information on when the client has not been idle in 1/2 hour increments.
	 * Index 0 represents 12:00am-12:30am.
	 */
	private int [] notIdleDuringInterval = new int[48];
	
	/**
	 * Contrains specifications about the client.
	 */
	private TreeMap<String, Object> specs = new TreeMap<String, Object>();
	
	/**
	 * Last error
	 */
	private String lastError = "";
	
	/**
	 * Default Constructor
	 */
	public ClientInfo()
	{
	}
	
	/**
	 * Constructor to set hostname and port number
	 * 
	 * @param hostname Hostname or IP address of the client
	 * @param portNum Port number to use for the SSH tunnel	 	 	 
	 */
	public ClientInfo(String hostname, int portNum)
	{
		this.hostname = hostname;
		this.sshTunnelPort = portNum;
	}
	
	/**
	 * Gets the last error
	 * 
	 * @return Last error message	 	 
	 */
	public String getLastError()
	{
		return lastError;
	}
	
	/**
	 * Gets the client specifications
	 * 
	 * @return Client specs map
	 */
	public TreeMap<String, Object> getSpecs()
	{
		// Make sure there are specs
		if (specs == null)
			specs = new TreeMap<String, Object>();
	
		// TODO: Remove this and add user interface for user to edit client specs.
		specs.put("OS", "linux");
		specs.put("RAM", 2048);
		
		return specs;
	}
	
	/**
	 * Sets the hostname or IP address of the client
	 * 
	 * @param hostname Hostname or IP address of the client	 	 
	 */
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	/**
	 * Gets the hostname or IP address of the client
	 * 
	 * @return Hostname or IP address of the client	 	 
	 */
	public String getHostname()
	{
		return hostname;
	}
	
	/**
	 * Sets the port number to use for the SSH tunnel
	 * 
	 * @param portNum Port number to use for the SSH tunnel
	 * 
	 * @return True if the port number was successfully set.  Error information
	 * 	can be obtained using getLastError()
	 */
	public boolean setSSHTunnelPort(int portNum)
	{
		// Check that port number is a private port number
		if (portNum < 49152 || portNum > 65535)
			lastError = "Port number must be between 49152 and 65535.";
		else
			sshTunnelPort = portNum;
			
		// Check if the port number was set
		return (sshTunnelPort == portNum);
	}
	
	/**
	 * Gets the port number to use for the SSH tunnel
	 * 
	 * @return Port number to use for the SSH tunnel
	 */
	public int getSSHTunnelPort()
	{
		return sshTunnelPort;
	}
	
	/**
	 * Sets the java class directory of the client
	 * 
	 * @param dir Java class directory of the client	 	 
	 */
	public void setClassFileDir(String dir)
	{
		javaClassDir = dir;
	}
	
	/**
	 * Gets the java class directory of the client
	 * 
	 * @return Java class directory of the client	 	 
	 */
	public String getClassFileDir()
	{
		return javaClassDir;
	}
	
	/**
	 * Sets the number of tasks that were assigned to the client
	 * 
	 * @param numTasks Number of tasks that have been assigned to the client.
	 */
	public void setNumAssignedTasks(int numTasks)
	{
		this.numTasks = numTasks;
	}
	
	/**
	 * Increments the number of tasks that were assigned to the client.
	 */
	public void incrementNumAssignedTasks()
	{
		numTasks++;
	}
	
	/**
	 * Gets the number of tasks that were assigned to the client
	 * 
	 * @return Number of tasks that have been assigned to the client.
	 */
	public int getNumAssignedTasks()
	{
		return numTasks;
	}
	
	/**
	 * Sets the number of failed tasks for the client
	 * 
	 * @param numTasks Number of tasks that the client failed.
	 */
	public void setNumFailedTasks(int numTasks)
	{
		this.numFailedTasks = numTasks;
	}
	
	/**
	 * Increments the number of failed tasks for the client.
	 */
	public void incrementNumFailedTasks()
	{
		numFailedTasks++;
	}
	
	/**
	 * Gets the number of failed tasks for the client
	 * 
	 * @return Number of tasks that the client failed.
	 */
	public int getNumFailedTasks()
	{
		return numFailedTasks;
	}
	
	/**
	 * Gets the percentage of assigned tasks that failed.
	 * 
	 * @return Percentage of assigned tasks that failed.  Range: 0 - 100
	 */
	public int getFailPercentage()
	{
		return (numTasks == 0) ? 0 : (int)(numFailedTasks*100.0/numTasks);
	}
	
	/**
	 * Records whether the client was idle or not at a particular time.
	 * 
	 * @param time Time that the idle status was checked
	 * @param idle True if the client was idle.	 
	 */
	public void setIdleStatus(Calendar time, boolean idle)
	{
		// Determine the index based on the time
		int idx = time.get(Calendar.HOUR_OF_DAY) * 2;
		if (time.get(Calendar.MINUTE) >= 30)
			idx++;
		
		// Increment correct array
		if (idle)
			idleDuringInterval[idx]++;
		else
			notIdleDuringInterval[idx]++;
	}
	
	/**
	 * Gets the percentage of the time that the client is idle at a given time
	 * 
	 * @param time Time to check percentage for
	 * 
	 * @return Percentage of the time that the client was idle. Range: 0 - 100	 	 
	 */
	public int getIdlePercentage(Calendar time)
	{
		// Determine the index based on the time
		int idx = time.get(Calendar.HOUR_OF_DAY) * 2;
		if (time.get(Calendar.MINUTE) >= 30)
			idx++;
		
		// Increment correct array
		int total = idleDuringInterval[idx] + notIdleDuringInterval[idx];
		return (total == 0) ? 100 : (int)(idleDuringInterval[idx]*100.0/total);
	}
	
	/**
	 * Edits the client information	 	 
	 */
	public void editClient()
	{
		// Setup scanner
		Scanner scan = new Scanner(System.in);
		
		// Selected option
		int opt;
		
		do
		{
			// Output menu
			System.out.println("Main Menu");
			System.out.println("1. Hostname");
			System.out.println("2. SSH tunnel port number");
			System.out.println("3. Java class directory");
			System.out.println("4. Task statistics");
			System.out.println("5. Idle statistics");
			System.out.println("6. Finish");
			
			// Read in option
			opt = 0;
			while (opt < 1 || opt > 6)
			{
				System.out.print("Select option to change: ");
				try
				{
					opt = scan.nextInt();
				}
				catch (InputMismatchException e)
				{
					scan.nextLine();
					System.out.println("Invalid input.  Please enter an integer.");
				}
			}
			
			// Variables used int switch cases
			boolean valid;
			String tmpStr;
			int tmpInt;
			
			// Process option
			switch (opt)
			{
				// Hostname
				case 1:
					// Ignore to end of line
					scan.nextLine();
					
					// Get input
					System.out.print("Enter hostname (" + hostname + "): ");
					tmpStr = scan.nextLine();
					
					// Only change if not empty
					if (tmpStr.length() > 0)
						hostname = tmpStr;
					
					break;
				// SSH tunnel port number
				case 2:
					valid = false;
					while (!valid)
					{
						System.out.print("Enter port number (" + sshTunnelPort + "): ");
						try
						{
							tmpInt = scan.nextInt();
							if (!(valid = setSSHTunnelPort(tmpInt)))
								System.out.println(getLastError());
						}
						catch (InputMismatchException e)
						{
							scan.nextLine();
							System.out.println("Invalid input.  Please enter an integer.");
						}
					}
					
					break;
				// Java class directory
				case 3:
					// Ignore to end of line
					scan.nextLine();
					
					// Get input
					System.out.print("Enter java class directory (" + javaClassDir + "): ");
					tmpStr = scan.nextLine();
					
					// Only change if not empty
					if (tmpStr.length() > 0)
						javaClassDir = tmpStr;
					
					break;
				// Task statistics
				case 4:
					// Get total tasks
					System.out.print("Enter number of tasks assigned (" + numTasks + "): ");
					valid = false;
					while (!valid)
					{
						try
						{
							numTasks = scan.nextInt();
							valid = true;
						}
						catch (InputMismatchException e)
						{
							scan.nextLine();
							System.out.println("Invalid input.  Please enter an integer.");
						}
					}
					
					// Get failed tasks
					valid = false;
					while (!valid)
					{
						System.out.print("Enter number of failed tasks (" + numFailedTasks + "): ");
						try
						{							
							tmpInt = scan.nextInt();
							
							// Check that this is less than the total
							if (tmpInt > numTasks)
								System.out.println("Number of failed tasks can be at most " + numTasks + ".");
							else
							{
								numFailedTasks = tmpInt;
								valid = true;
							}
						}
						catch (InputMismatchException e)
						{
							scan.nextLine();
							System.out.println("Invalid input.  Please enter an integer.");
						}
					}
					
					break;
				// Idle statistics
				case 5:
					// Select the time frame
					System.out.println("Time Frames");
					Calendar cal = Calendar.getInstance();
					cal.set(2000, 1, 1, 0, 0, 0);
					SimpleDateFormat dtFmt = new SimpleDateFormat("hh:mma");
					for (int i = 0; i < 24; i++)
					{
						System.out.print(i*2+1 + ".\t" + dtFmt.format(cal.getTime()));
						cal.add(Calendar.MINUTE, 29);
						System.out.print(" - " + dtFmt.format(cal.getTime()));
						cal.add(Calendar.MINUTE, 1);
						
						System.out.print("\t\t" + ((i+1)*2) + ".\t" + dtFmt.format(cal.getTime()));
						cal.add(Calendar.MINUTE, 29);
						System.out.println(" - " + dtFmt.format(cal.getTime()));
						cal.add(Calendar.MINUTE, 1);
					}
					int timeframe = -1;
					valid = false;
					while (!valid)
					{
						System.out.print("Select a time frame: ");
						try
						{
							tmpInt = scan.nextInt();
							if (tmpInt < 1 || tmpInt > 48)
								System.out.println("Invalid input.");
							else
							{
								timeframe = tmpInt - 1;
								valid = true;
							}
						}
						catch (InputMismatchException e)
						{
							scan.nextLine();
							System.out.println("Invalid input.  Please enter an integer.");
						}
					}
					
					// Get number of times idle
					System.out.print("Enter number of times that the client was idle (" + idleDuringInterval[timeframe] + "): ");
					valid = false;
					while (!valid)
					{
						try
						{
							idleDuringInterval[timeframe] = scan.nextInt();
							valid = true;
						}
						catch (InputMismatchException e)
						{
							scan.nextLine();
							System.out.println("Invalid input.  Please enter an integer.");
						}
					}
					
					// Get number of times not idle
					System.out.print("Enter number of times that the client was not idle (" + notIdleDuringInterval[timeframe] + "): ");
					valid = false;
					while (!valid)
					{
						try
						{
							notIdleDuringInterval[timeframe] = scan.nextInt();
							valid = true;
						}
						catch (InputMismatchException e)
						{
							scan.nextLine();
							System.out.println("Invalid input.  Please enter an integer.");
						}
					}
					break;
			}
		} while(opt != 6);
	}
}
