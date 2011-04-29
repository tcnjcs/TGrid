/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.GridServer;

import edu.tcnj.TGrid.Events.JobEvent;
import edu.tcnj.TGrid.Events.JobEventListener;
import edu.tcnj.TGrid.Exceptions.ResourceException;
import edu.tcnj.TGrid.GridServer.Events.ResourceDiscoveryEvent;
import edu.tcnj.TGrid.GridServer.Events.ResourceDiscoveryEventListener;
import edu.tcnj.TGrid.GridServer.Events.ClientCommandConnectionAcceptedEvent;
import edu.tcnj.TGrid.GridServer.Events.ClientCommandConnectionAcceptedEventListener;
import edu.tcnj.TGrid.GridServer.Events.ResourceEvent;
import edu.tcnj.TGrid.GridServer.Events.ResourceEventListener;
import edu.tcnj.TGrid.GridServer.Exceptions.ResourceDiscoveryException;
import edu.tcnj.TGrid.Job;
import edu.tcnj.TGrid.Task;
import edu.tcnj.TGrid.States.JobState;
import edu.tcnj.TGrid.ConnectionToRemoteHost;

import edu.tcnj.TGrid.States.ResourceState;
import edu.tcnj.TGrid.States.TaskState;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tcnj.TGrid.ClientInfo;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.EOFException;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * The Monitor is responsible for overseeing various facets of the program,
 * including resource discovery, task delegation, and more.
 * 
 * @author Dan
 */
public class Monitor implements ResourceDiscoveryEventListener, ClientCommandConnectionAcceptedEventListener, ResourceEventListener, JobEventListener
{	
	/**
	 * The resource discovery agent is responsible for resource discovery.
	 */
	private ResourceDiscoveryAgent resourceDiscoveryAgent;
	
	/**
	 * The resources map stores all resources, mapping them to their IDs.
	 */
	private volatile Map<Integer, ServerSideResource> resources = Collections.synchronizedMap(new HashMap<Integer, ServerSideResource>());
	
  	/**
	 * List of resources that are ready for a task
	 */
	private LinkedBlockingQueue<ServerSideResource> readyResources = new LinkedBlockingQueue<ServerSideResource>();
	
	/**
	 * Stores all assigned task IDs and maps them to the IDs of resources to which
	 * they've been assigned.  Tasks are reassigned when a task times out.  However,
	 * the task is not removed from the current resource until the task is completed
	 * by any one of the resources.     
	 */
	private Map<Integer, LinkedList<Integer>> assignedTasks = Collections.synchronizedMap(new HashMap<Integer, LinkedList<Integer>>());
	
	/**
	 * The tasksToReassign queue stores Tasks that should be reassigned. 
	 * (ie. Due to a timeout)
	 */
	private volatile LinkedBlockingQueue<Task> tasksToReassign = new LinkedBlockingQueue<Task>();
	
	/**
	 * The availableJobs queue stores all Jobs that have not yet run.
	 */
	private LinkedBlockingQueue<Job> availableJobs = new LinkedBlockingQueue<Job>();
	
	/**
	 * The finishedJobs queue stores all Jobs that have completed sucessfully.
	 */
	private LinkedBlockingQueue<Job> finishedJobs = new LinkedBlockingQueue<Job>();
	
	/**
	 * Stores list of clients that are not actively running
	 */
	private ArrayList<ClientInfo> unusedClients = new ArrayList<ClientInfo>();
	
	/**
	 * Stores list of all clients
	 */
	private ArrayList<ClientInfo> allClients = new ArrayList<ClientInfo>();
	
	/**
	 * Associates a random integer from a connecting client with the
	 * ServerSideResource object for that client.
	 */
	private Map<Integer, ServerSideResource> connectingServerSideResourceMap = Collections.synchronizedMap(new HashMap<Integer, ServerSideResource>());
	
	/**
	 * Associates a random integer from a connecting client with the ClientInfo
	 * object for that client.
	 */
	private Map<Integer, ClientInfo> connectingClientMap = Collections.synchronizedMap(new HashMap<Integer, ClientInfo>());
	
	/**
	 * Stores a random interger to be used in parallel with connectingClientMap.
	 * This acts like a password for the connecting client.	 
	 */
	private Map<Integer, Integer> connectingClientRandMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	
	/**
	 * Random number generator for connecting client map
	 */
	private Random randGen = new Random();
	
	/**
	 * Stores the Job that is currently being performed.
	 */
	private volatile Job currentJob;
	
	/**
	 * The local port to listen for incoming connections on.  Passed on to the 
	 * resource discovery agent.
	 */
	private int port;
	
	/**
	 * The username that connecting resources must supply.  Passed on to the 
	 * resource discovery agent.
	 */
	private String username;
	
	/**
	 * The password that connecting resources must supply.  Passed on to the 
	 * resource discovery agent.
	 */
	private String password;
	
	/**
	 * Determines if the server should exit after first job results
	 */
	private boolean exitOnJobResults = false;
	
	/**
	 * Represents an instance of the TaskDelegationThread.  A new one of these threads
	 * is spawned any time circumstances arise that would require task delegation
	 */
	private TaskDelegationThread taskDelegation;
	
	/**
	 * Date formatter
	 */
	private static SimpleDateFormat dateFmt = new SimpleDateFormat("kk:mm:ss");
	
	public Monitor(int port, String username, String password, boolean exitOnJobResults)
	{
		this.port = port;
		this.username = username;
		this.password = password;
		this.exitOnJobResults = exitOnJobResults;
		
		// Turn off logging
		Logger.getLogger(Monitor.class.getName()).setLevel(Level.OFF);
		
		// Load clients
		loadClients("clients.dat");
	}
	
	/*****************************************************************************
	* Loads client information from file.  Should only be called once before any
	* clients are in the client arrays.	
	* 
	* @param path Path to clients file
	* 
	* @return True if the clients were successfully loaded		
	*****************************************************************************/		
	private boolean loadClients(String path)
	{
		// Return value
		boolean rtn = false;
	
		// Check that the clients are not loaded yet
		if (allClients.isEmpty())
		{	
			try
			{
				// Open the file
				FileInputStream fIn = new FileInputStream(path);
				ObjectInputStream fObjIn = new ObjectInputStream(fIn);
				
				// Read clients
				rtn = true;
				ClientInfo client = null;
				try
				{
					// Loop until EOF exception
					while (true)
					{
						try
						{
							// Read and store client
							client = (ClientInfo)fObjIn.readObject();
							unusedClients.add(client);
							allClients.add(client);
						}
						catch (ClassNotFoundException e)
						{
							System.out.println("Failed loading client: " + e);
							rtn = false;
						}
					}
				}
				catch(EOFException e)
				{}
				
				// Close the file
				fObjIn.close();
			}
			catch (IOException e)
			{
				System.out.println("Failed to load clients: " + e);
			}
		}
		
		return rtn;
	}
	
	/*****************************************************************************
	* Saves client information to file
	* 
	* @return True if the file is successfully saved		
	*****************************************************************************/		
	public boolean saveClients()
	{
		return saveClients("clients.dat");
	}
	
	/*****************************************************************************
	* Saves client information to file
	* 
	* @param path Path to file in which to save clients
	* 
	* @return True if the file is successfully saved		
	*****************************************************************************/		
	private boolean saveClients(String path)
	{
		try
		{
			// Open file
			FileOutputStream fOut = new FileOutputStream(path);
			ObjectOutputStream fObjOut = new ObjectOutputStream(fOut);
		
			// Write each client
			for (ClientInfo client : allClients)
				fObjOut.writeObject(client);
			
			// Close file
			fObjOut.close();
			
			// Indicate success
			return true;
		}
		catch (IOException e)
		{
			System.out.println("Failed to save clients.");
		}
		
		return false;
	}
	
	/*****************************************************************************
	* Determines if more clients should be started and starts them if needed.
	*****************************************************************************/		
	private void startClients()
	{
		// Start client search thread
		ClientSearchThread cst = new ClientSearchThread();
		cst.start();
	}
	
	/*****************************************************************************
	* User interface for adding a client
	*****************************************************************************/		
	public void addClient()
	{
		// Create new client
		ClientInfo client = new ClientInfo();
		
		// Edit client information
		client.editClient();
		
		// Add to list of unused clients
		unusedClients.add(client);
		allClients.add(client);
		
		// Save to clients file
		saveClients("clients.dat");
	}
	
	/*****************************************************************************
	* User interface for editing a client
	*****************************************************************************/		
	public void editClient()
	{
		// Check that there are clients to edit
		if (allClients.isEmpty())
			System.out.println("No clients loaded.");
		else
		{
			// Setup scanner
			Scanner scan = new Scanner(System.in);
		
			// Determine which client to edit
			ClientInfo client;
			for (int i = 0; i < allClients.size(); i++)
			{
				client = allClients.get(i);
				System.out.println((i+1) + ". " + client.getHostname() + ":" + client.getSSHTunnelPort());
			}
			System.out.println((allClients.size()+1) + ". Cancel");
			
			// Read in option
			int clientIdx = -1;
			while (clientIdx < 0 || clientIdx > allClients.size())
			{
				System.out.print("Select client: ");
				try
				{
					clientIdx = scan.nextInt() - 1;
				}
				catch (InputMismatchException e)
				{
					scan.nextLine();
					System.out.println("Invalid input.  Please enter an integer.");
				}
			}
			
			// Check if the user wants to cancel
			if (clientIdx < allClients.size())
			{
				// Get the client
				client  = allClients.get(clientIdx);
				
				// Edit client information
				client.editClient();
				
				// Save to clients file
				saveClients("clients.dat");
			}
		}
	}
	
	/*****************************************************************************
	* User interface for deleting a client
	*****************************************************************************/		
	public void deleteClient()
	{
		// Check that there are clients to delete
		if (allClients.isEmpty())
			System.out.println("No clients loaded.");
		else
		{
			// Setup scanner
			Scanner scan = new Scanner(System.in);
		
			// Determine which client to delete
			ClientInfo client;
			for (int i = 0; i < allClients.size(); i++)
			{
				client = allClients.get(i);
				System.out.println((i+1) + ". " + client.getHostname() + ":" + client.getSSHTunnelPort());
			}
			System.out.println((allClients.size()+1) + ". Cancel");
			
			// Read in option
			int clientIdx = -1;
			while (clientIdx < 0 || clientIdx > allClients.size())
			{
				System.out.print("Select client: ");
				try
				{
					clientIdx = scan.nextInt() - 1;
				}
				catch (InputMismatchException e)
				{
					scan.nextLine();
					System.out.println("Invalid input.  Please enter an integer.");
				}
			}
			
			// Check if the user wants to cancel
			if (clientIdx < allClients.size())
			{
				// Get the client
				client  = allClients.get(clientIdx);
				
				// Delete from arrays
				allClients.remove(client);
				unusedClients.remove(client);
				
				// Save to clients file
				saveClients("clients.dat");
			}
		}
	}
	
	public void begin()
	{
		try {
			resourceDiscoveryAgent = new ResourceDiscoveryAgent(port, username, password);
			resourceDiscoveryAgent.addResourceDiscoveryEventListener(this);
			resourceDiscoveryAgent.addClientCommandConnectionAcceptedEventListener(this);
	
			resourceDiscoveryAgent.startListeningForConnections();
			
			// Start clients
			startClients();
		} catch (ResourceDiscoveryException ex) {
			Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, "Could not start resource discovery.", ex);
		}
	}
	
	public void addJob(Job job)
	{
		//First, check if a job is currently running
		if(currentJob == null)
		{
			//if there is indeed no current job, make this new job the current one
			currentJob = job;
			currentJob.addJobEventListener(this);
			
			//make the new current job ready; once the new current job is ready, it will trigger an event
			currentJob.makeReady();
		}
		else
		{
			//if a job IS running, just add the new one to the Queue
			availableJobs.offer(job);
		}
	}
	
	public void resourceDiscovered(ResourceDiscoveryEvent e)
	{
		ServerSideResource newResource = e.getDiscoveredResource();
		
		Logger.getLogger(Monitor.class.getName()).log(Level.INFO, "A new resource, \"" + newResource.getHostName() + ",\" connected from " + newResource.getHostAddress() + ".");
		
		newResource.addResourceEventListener(this);
		newResource.beginAuthorization();
		
		// Add to list of resources
		synchronized(resources)
		{
			resources.put(newResource.getResourceID(), newResource);
		}
	}
	
	/**
	 * Triggered when a new client command connection is accepted
	 */
	public void clientCommandConnectionAccepted(ClientCommandConnectionAcceptedEvent e)
	{
		ConnectionToRemoteHost con = e.getConnectionToRemoteHost();
		synchronized (connectingServerSideResourceMap)
		{
  		int clientKey = con.getClientKey();
			if (connectingServerSideResourceMap.containsKey(clientKey))
			{
				// Set the connection and remove from map
				connectingServerSideResourceMap.remove(clientKey).setClientCommandConnection(con);
				
				// Remove client and password maps
				connectingClientMap.remove(clientKey);
				connectingClientRandMap.remove(clientKey);
			}
  	}
	}

	public void resourceStateChanged(ResourceEvent e)
	{
		ServerSideResource source = (ServerSideResource)e.getSource();
		
		switch(e.getState())
		{
			case READY:
				// Check if the resource has idle stats to report
				if (source.doesClientHaveIdleStats())
				{
					// Get stats from client
					source.getIdleStats();
				}
				// Add to list of ready resources
				else
				{
					// Add the resource to ready resources
					readyResources.add(source);
					
					Logger.getLogger(Monitor.class.getName()).log(Level.INFO, source.getHostName() + "(" + source.getHostAddress() + ") is now ready.");
			
					synchronized(taskDelegation)
					{
			    	if(taskDelegation == null || !taskDelegation.isAlive())
			    	{
			    		taskDelegation = new TaskDelegationThread();
			    		taskDelegation.start();
						}
					}
				}
				break;
			case AUTHORIZATION_SUCCESS:
				// Associate the client with its ClientInfo object
				synchronized (connectingClientMap)
				{
		  		int clientKey = source.getClientKey();
					int clientPswd = source.getClientPassword();
					if (connectingClientMap.containsKey(clientKey))
					{
						// Check that the password is correct (If not, the client just continues without the associated ClientInfo object)
						if (clientPswd == connectingClientRandMap.get(clientKey))
						{
							// Get the client information for the resource and remove from map
							source.setClientInfo(connectingClientMap.get(clientKey));
							
							// Remove from password (rand) map
							connectingClientRandMap.get(clientKey);
							
							// Add ServerSideResource to map
							connectingServerSideResourceMap.put(clientKey, source);
						}
					}
		  	}
				break;
			case AUTHORIZATION_FAILURE:
				Logger.getLogger(Monitor.class.getName()).log(Level.INFO, source.getHostName() + "(" + source.getHostAddress() + ") failed authorization.");
				break;
			case RUNNING_TASK:
				Logger.getLogger(Monitor.class.getName()).log(Level.INFO, source.getHostName() + "(" + source.getHostAddress() + ") is now running a task.");
				break;
			case TASK_COMPLETED:
				try
				{
					Task completedTask = source.takeCurrentTask();
			
					// Remove the resource ids that are assigned to this task
					LinkedList<Integer> resourceIds = assignedTasks.remove(completedTask.getTaskID());
					// If there are no resource ids, it means that another 
					// resource already returned a result for the resource.
					if (resourceIds != null)
					{
						// Cancel the task for all of the other resources
						for(int resourceId : resourceIds)
						{
							// Get resource
							ServerSideResource resource = resources.get(resourceId);
							// Check that the resource is not the source and not null
							if (resource != source && resource != null)
								resource.cancelTask();
						}
			
						// Add the (now complete) task back to the Job
						currentJob.storeTask(completedTask);
						
						// Record stats on client
						ClientInfo client = source.getClientInfo();
						if (client != null)
							client.incrementNumAssignedTasks();
					}
					//else
						//System.out.println(completedTask + " already completed.  Ignoring result.");
				}
				catch(ResourceException ex)
				{
					Logger.getLogger(Monitor.class.getName()).log(Level.WARNING, "Error removing task from resource #" + source.getResourceID(), ex);
				}
				
				break;
			case TASK_ENDED_ABNORMALLY:
				// Add the (incomplete) task back to the Job
				try
				{
					// Check if this task is still assigned.  It might have been 
					// cancelled if another resource already finished it
					Task oldTask = source.takeCurrentTask();
					if (assignedTasks.get(oldTask.getTaskID()) != null)
						currentJob.storeTask(oldTask);
					
					// Record stats on client
					ClientInfo client = source.getClientInfo();
					if (client != null)
					{
						client.incrementNumAssignedTasks();
						client.incrementNumFailedTasks();
					}
				}
				catch(ResourceException ex)
				{
					Logger.getLogger(Monitor.class.getName()).log(Level.WARNING, "Error removing task from resource #" + source.getResourceID(), ex);
				}
				break;
			case TASK_TERMINATED:
			case TROUBLED:
				try
				{
					// Add the troubled resource's task back into the job
					Task oldTask = source.takeCurrentTask();
					oldTask.setState(TaskState.READY);
			
					// Check if this task is still assigned.  It might have been 
					// cancelled if another resource already finished it
					if (assignedTasks.get(oldTask.getTaskID()) != null)
			    	currentJob.storeTask(oldTask);
			    
			    // Record stats on client if troubled
			    if (e.getState() == ResourceState.TROUBLED)
			    {
						ClientInfo client = source.getClientInfo();
						if (client != null)
						{
							client.incrementNumAssignedTasks();
							client.incrementNumFailedTasks();
						}
					}
				}
				catch(ResourceException ex)
				{
					//it's possible the resource doesn't have a task attached.
				}
			
			default:
				//System.out.println(source.getHostName() + "(" + source.getHostAddress() + ") is now " + e.getState().toString());
				break;
		}
	}
	
	/**
	 * Triggered when the Resource failed to complete a task in the given
	 * timeout.
	 */
	public void resourceTaskTimedOut(ResourceEvent e)
	{
		// Get resource
		ServerSideResource source = (ServerSideResource)e.getSource();
		
		// Get task and make sure it is not null.  I may have finished between the
		// timeout and now
		Task task = source.getCurrentTask();
		if (task != null)
		{
			// Increase the task timeout
			int timeout = task.increaseTimeout();
			//System.out.println("Task timeout increased to " + timeout/1000.0 + " seconds.");
			
			// Assign task to another resource
			tasksToReassign.add(task);
			
			// Start task delegation if it is not running
			if(taskDelegation == null || !taskDelegation.isAlive())
			{
				taskDelegation = new TaskDelegationThread();
				taskDelegation.start();
			}
		}
	}
	
	/**
   * Triggered when the Resource wants to be removed from the list of ready
   * resources		  
   */
  public void resourceNoLongerReady(ResourceEvent e)
  {
  	// Get resource
		ServerSideResource source = (ServerSideResource)e.getSource();
		
		// Remove the resource to ready resources
		readyResources.remove(source);
  }
	
	public void JobStateChanged(JobEvent e)
	{
		Job source = (Job) e.getSource();
		
		switch(source.getState())
		{
			case READY: //if a Job reports that it is ready, begin initial task delegation
				System.out.println("Job ready at " + dateFmt.format(Calendar.getInstance().getTime()) + ".");
				if(taskDelegation == null || !taskDelegation.isAlive())
				{
					taskDelegation = new TaskDelegationThread();
					taskDelegation.start();
				}
				
				// Start clients
				startClients();
				
				break;
			case COMPLETED:
				/*
				System.out.println("Job results:");
				System.out.println(source.getResults());
				*/
				
				// Save results to a file
				try
				{
					// Open file
					PrintWriter fOut = new PrintWriter("results.dat");
				
					// Write results
					fOut.println(source.getResults());
					
					// Close file
					fOut.close();
					
					// Inform user
					System.out.println("Job results saved to \"results.dat\" at " + dateFmt.format(Calendar.getInstance().getTime()) + ".");
					
					// Exit if desired
					if (exitOnJobResults)
						System.exit(0);
				}
				catch (IOException ex)
				{
					System.out.println("Failed to save job results to \"results.dat\" at " + dateFmt.format(Calendar.getInstance().getTime()) + ".");
				}
				break;
		}
	}
	
	/**
	 * The ClientSearchThread thread searches and starts the best clients based
	 * on previous statistics.	 
	 */
	protected class ClientSearchThread extends Thread
	{
		@Override
	  public void run()
	  {
	  	// Check that there is a current job
	  	if (currentJob != null)
	  	{
		  	synchronized (currentJob)
		  	{
		  		synchronized (unusedClients)
		  		{
						// Decide how many clients are needed
						int startNum = (int)(1.25 * (currentJob.getRemainingNumberOfTasks()+tasksToReassign.size())) - readyResources.size();
						
						for (; !unusedClients.isEmpty() && startNum > 0; startNum--)
						{
							// TODO: It might be best to do a quick sort instead if we need to start
							// a significant number of clients.
						
							// Decide on best client
							int bestIdx = 0;
							Calendar now = Calendar.getInstance();	// Get current time
							for (int i = 1; i < unusedClients.size(); i++)
							{
								// Compare failure percentage
								if (unusedClients.get(i).getFailPercentage() < unusedClients.get(bestIdx).getFailPercentage())
									bestIdx = i;
								else if (unusedClients.get(i).getFailPercentage() == unusedClients.get(bestIdx).getFailPercentage())
								{	
									// Compare idle stats
									if (unusedClients.get(i).getIdlePercentage(now) > unusedClients.get(bestIdx).getIdlePercentage(now)) 
										bestIdx = i;
								}
							}
				
							// Get client
							ClientInfo client = unusedClients.remove(bestIdx);
							//System.out.println("Starting client '" + client.getHostname() + "'");
							
							// Start client
							ClientStartThread cst = new ClientStartThread();
							cst.setClientInfo(client);
							cst.start();
						}
					}
				}
			}
		}
	}
	
	/**
	 * The ClientStartThread thread starts a client.
	 */
	protected class ClientStartThread extends Thread
	{
		/**
		 * Information for client that is to be started.
		 */
		 ClientInfo client = null;
		 
		/**
		 * Sets information for client to be started.
		 * 
		 * @param client Information on the client to be started		 		 
		 */
		public void setClientInfo(ClientInfo client)
		{
			this.client = client;
		}
		 
		@Override
	  public void run()
	  {
			// Check that the client is set
			if (client != null)
			{
				/* Store information to map the connecting client back to the ClientInfo
				object. */
				// Create unique key
				int key = randGen.nextInt();
				while (key == 0 || connectingClientMap.containsKey(key))
					key = randGen.nextInt();
				// Generate 'password'
				int pswd = randGen.nextInt();
				// Store client
				connectingClientMap.put(key, client);
				connectingClientRandMap.put(key, pswd);
			
				// Construct command to run
				String [] command = new String[7];
				command[0] = "./startRemoteLinuxClient.sh";	// Command to run
				command[1] = client.getHostname();					// Hostname of client
				command[2] = ""+port;												// Port number of server
				command[3] = ""+client.getSSHTunnelPort();	// Get port number to use for SSH tunnel
				command[4] = client.getClassFileDir();			// Directory of java class files
				command[5] = ""+key;												// Client map key
				command[6] = ""+pswd;												// Client map password
			
				try
				{
					// Run the command
					Process p = Runtime.getRuntime().exec(command);
					
					// Create scanner for process output
					Scanner pScan = new Scanner(p.getInputStream());
					
					// Wait for the process to finish
					p.waitFor();
					
					// Check return code
					if (p.exitValue() == 0 && pScan.hasNextInt())
					{
						// Get SSH pid
						int sshPid = pScan.nextInt();
						//System.out.println("SSH pid: " + sshPid);
					}
					else
						System.out.println("Failed to connect client. (Exit Code: " + p.exitValue() + ")");
				}
				catch (InterruptedException ex)
				{
					System.out.println("Failed to connect client: " + ex);
				}
				catch (IOException ex)
				{
					System.out.println("Failed to connect client: " + ex);
				}
			}
		}
	}

	/**
	 * The TaskDelegationThread internal thread is intended to send 
	 * any available Tasks to any available resources.  When this is 
	 * finished, the thread terminates itself.
	 */
	protected class TaskDelegationThread extends Thread
	{
	  private boolean shouldContinue;
	  
	  @Override
	  public void run()
		{
	  	shouldContinue = true;
											
		  //make sure the current job both exists and is ready to be run
		  if (currentJob != null && (currentJob.getState() == JobState.READY || currentJob.getState() == JobState.RUNNING))
			{
				Task currentTask;
				synchronized(readyResources)
				{
					// Check that there is a task to assign and a resource to assign it to
					while (
						// Should we continue?
						shouldContinue &&
						// Is there a task?
						(tasksToReassign.size() > 0 || currentJob.hasNextTask()) &&
						// Is there a ready resource?
						readyResources.size() > 0
					)
					{
						// Get a resource
						ServerSideResource currentResource = readyResources.poll();
						synchronized(currentResource)
						{
							//  Keep track of resource ids for the task
							LinkedList<Integer> resourceIds = null;
		
							// Get the next task
							currentTask = null;
							if (tasksToReassign.size() > 0)
							{
								// If there are no resource ids, it means that
								// this task was already completed so should
								// not be reassigned anymore
								while (resourceIds == null && tasksToReassign.size() > 0)
								{
								  currentTask = tasksToReassign.poll();
								  resourceIds = assignedTasks.get(currentTask.getTaskID());
							  }
							}
							if (currentTask == null && currentJob.hasNextTask())
							{
								currentTask = currentJob.getNextReadyTask();
								resourceIds = new LinkedList<Integer>();
							}
							
							// Break if there are no tasks
							if (currentTask == null)
							{
								// Add resource back to ready resources list
								readyResources.add(currentResource);
								break;
							}
							
							// Check that the resource meets the task requrements
							if (!currentTask.meetsRequirements(currentResource.getClientInfo()))
							{
								System.out.println(currentResource.getHostName() + " does not meet task requirements.");
								
								// Add this resource back to ready resources
								readyResources.add(currentResource);
							}
							else
							{	
								// Check if we should avoid giving this task to this resource
								for (int i = readyResources.size(); i > 0 && currentResource.shouldAvoidTask(currentTask.getTaskID()); i--)
								{
									//System.out.println("Resource #" + currentResource.getResourceID() + " has a bad history with task #" + currentTask.getTaskID() + ".  Try another resource.");
									
									// Add this resource back to ready resources
									readyResources.add(currentResource);
									
									// Get next resource
									currentResource = readyResources.poll();
								}
								
								// If this resource has had previous trouble with this task, also assign it
								// to another resource.
								if (
									currentResource.shouldAvoidTask(currentTask.getTaskID()) &&
									// Only reassign if this is not already reassigned
									resourceIds == null
								)
									tasksToReassign.add(currentTask);
								  
								// Assign that task to a resource
								currentResource.assignTask(currentTask);
								
								// Make sure resource ids list is not null
								if (resourceIds == null)
									resourceIds = new LinkedList<Integer>();
									
								// Make a note of it in our task -> resource map
								resourceIds.add((Integer)currentResource.getResourceID());
								assignedTasks.put((Integer)currentTask.getTaskID(), resourceIds);
							}
						}
					}
				}
		  }
		}
		
		/**
		 * Stops the task delegation process midway, after which the thread should
		 * terminate.
		 */
		public void stopTaskDelegation()
		{
			shouldContinue = false;
			System.out.println("Task delegation cancelled.");
		}
	}
	
  public void TasksCancelled(JobEvent e, int[] taskIDs)
	{
	  //if the task delegation thread is running, stop it
	  if(taskDelegation != null && taskDelegation.isAlive())
		{
		  try
			{
		  	taskDelegation.stopTaskDelegation();
		  	taskDelegation.join();
		  }
			catch (InterruptedException ex)
			{
		  	Logger.getLogger(Monitor.class.getName()).log(Level.WARNING, "Failed to stop task delegation before task cancellation.", ex);
		  }
	  }

	  // Traverse through the assigned tasks
	  for(int taskID : taskIDs)
		{
		  if(resources.containsKey(taskID))
		  	resources.get(taskID).cancelTask();
	  }
  }

  public void AllTasksCancelled(JobEvent e)
	{
		//if the task delegation thread is running, stop it
		if(taskDelegation != null && taskDelegation.isAlive())
		{
			try
			{
				taskDelegation.stopTaskDelegation();
				taskDelegation.join();
			}
			catch (InterruptedException ex)
			{
	  	  Logger.getLogger(Monitor.class.getName()).log(Level.WARNING, "Failed to stop task delegation before task cancellation.", ex);
			}
		}
		
		// Traverse through the assigned tasks
		for(int taskId : assignedTasks.keySet())
		{
			// Get the resource ids assigned for this task
			LinkedList<Integer> resourceIds = assignedTasks.get(taskId);
			for(int resourceId : resourceIds)
			{
				// Get resource
				ServerSideResource resource = resources.get(resourceId);
				if (resource != null)
					resource.cancelTask();
			}
		}
  }
}
