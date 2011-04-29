/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.JobEvent;
import edu.tcnj.TGrid.Events.JobEventListener;
import edu.tcnj.TGrid.States.JobState;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import java.math.BigInteger;
import edu.tcnj.TGrid.Util.ArithmeticOperation;

/**
 * The FactorialJob Class computes a large factorial
 * 
 * @author Stephen
 */
public class FactorialJob implements Job
{
	/**
	 * Stores a list of all internal Tasks that still need to be sent to clients.
	 */
	private LinkedBlockingQueue<Task> newTasks = new LinkedBlockingQueue<Task>();
	
	/**
	 * Stores a list of all internal Tasks have have completed successfully.
	 */
	private LinkedBlockingQueue<Task> finishedTasks = new LinkedBlockingQueue<Task>();
	
	/**
	 * Represents the original number of tasks in the file.
	 * 
	 * Used in determining whether or not all tasks have completed or not.
	 */
	private int initialNumberOfTasks = 0;
	
	/**
	 * Number of assigned tasks at the present moment.
	 */
	private int assignedTasks = 0;
	
	/**
	 * Number of completed tasks at the present moment.
	 */
	private int numFinishedTasks = 0;
	
	/**
	 * Minimum number of task results to combine.
	 */
	private int minCombineResults = 2;
	
	/**
	 * Represents the current state of this Job.
	 */
	private JobState currentState = JobState.NEW;
	
	/**
	 * Stores a list of every event listener that is registered to receive event
	 * notifications from this class.
	 */
	private HashSet<JobEventListener> registeredListeners = new HashSet<JobEventListener>();
	
	/**
	 * Creates the default factorial job
	 */
	public FactorialJob()
	{
		setupTasks(1000, 30);
	}
	
	/**
	 * Creates a factorial job for the given factorial using the default number of
	 * tasks.
	 * 
	 * @param num Number to determine factorial for
	 */
	public FactorialJob(int num)
	{
		setupTasks(num, 30);
	}
	
	/**
	 * Creates a factorial job for the given factorial breaking up the job into 
	 * the specified number of tasks.
	 * 
	 * @param num Number to determine factorial for
	 * @param numTasks Number of tasks to break it into
	 */
	public FactorialJob(int num, int numTasks)
	{
		setupTasks(num, numTasks);
	}
	
	/**
	 * Creates a factorial job for the given factorial breaking up the job into 
	 * the specified number of tasks.
	 * 
	 * @param num Number to determine factorial for
	 * @param numTasks Number of tasks to break it into
	 */
	private void setupTasks(int num, int numTasks)
	{
		// Make sure the values are valid
		if (num < 1)
			num = 1;
		if (numTasks > num)
			numTasks = num;
		
		// Setup minimum number of task results to combine
		minCombineResults = numTasks / (""+num).length();
		
		// Determine approximate gap size per task
		int i = 0, gap = 0;
		if (numTasks > 1)
		{
			gap = num / numTasks;
		  for (i = 0; i < numTasks - 1; i++)
		  {
		  	newTasks.add(new FactorialTask(i*gap+1, ((i+1)*gap)));
		  	//System.out.println("Gap: " + (i*gap+1) + " - " + ((i+1)*gap));
		  }
		}
	  // Add last task
	  newTasks.add(new FactorialTask(i*gap+1, num));
	  //System.out.println("Gap: " + (i*gap+1) + " - " + num);
	    
	  initialNumberOfTasks = newTasks.size();
	  assignedTasks = 0;
	}
	
	/**
	 * Does what is necessary to make the Job ready to run, after which 
	 * the Job is marked READY.
	 */
	public void makeReady()
	{
		setState(JobState.READY);
	}
	
	/**
	 * Returns the initial number of Tasks featured as part of the Job.
	 * 
	 * @return the total number of Tasks that this Job started with.
	 */
	public int getInitialNumberOfTasks()
	{
		return initialNumberOfTasks;
	}
    
	/**
	 * Returns the next Task to be performed as part of this Job.  Note that
	 * this method generally won't work if the Job isn't ready.
	 * 
	 * @return A Task object representing the next task needed to be run.
	 */
	public Task getNextReadyTask()
	{
		setState(JobState.RUNNING);
	  assignedTasks++;
	  return newTasks.poll();
	}
	
	/**
	 * Returns whether or not there are any tasks remaining as part of this Job.
	 * 
	 * @return true if there is at least one more Task ready,
	 *         false otherwise
	 */
	public boolean hasNextTask()
	{
	  return !newTasks.isEmpty();
	}
	
	/**
	 * Stores the specified Task.
	 * 
	 * Intended to store either a) Tasks that have completed sucessfully,
	 * b) those that have encountered errors and cannot run, or 
	 * c) Tasks that failed to run, and must be delegated again
	 */
	public void storeTask(Task taskToStore)
	{
	  switch(taskToStore.getState())
		{
	    case COMPLETED: //if the task is completed, add it to the completed tasks list
	      finishedTasks.add(taskToStore);
	      assignedTasks--;
	      numFinishedTasks++;
	      break;
	    case TROUBLED:
	    case PROBLEMATIC:
	    case TERMINATED:
	    	assignedTasks--;
	    case READY:
	      newTasks.add(taskToStore);
	      break;
	  }
	  
	  // Combine finished tasks into new task
	  synchronized(finishedTasks)
	  {
	  	// Check that there are tasks
		  if (finishedTasks.size() > 1)
		  {
				// Combine if there are no more tasks or there are the minimum number of tasks to combine
				if (
					(assignedTasks == 0 && newTasks.size() == 0) ||
					finishedTasks.size() >= minCombineResults
				)
				{
			  	BigIntOperationTask combineTask = new BigIntOperationTask(ArithmeticOperation.MULTIPLY);
			  	while (!finishedTasks.isEmpty())
			  		combineTask.addOperand((BigInteger)(finishedTasks.poll().getResults()));
			  	newTasks.add(combineTask);
			  }
		  }
		}
	  
	  if(assignedTasks == 0 && newTasks.size() == 0)
	  	setState(JobState.COMPLETED);
	}
	
	/**
	 * Returns the number of Tasks remaining in this Job.  (That is, those that
	 * have not yet been retrieved with the getNextTask() method.)
	 * 
	 * @return the number of Tasks remaining in this Job.
	 */
	public int getRemainingNumberOfTasks()
	{
		return newTasks.size();
	}
	
	/**
	 * Returns the number of Tasks in this Job that have completed sucessfully.
	 * 
	 * @return the number of Tasks remaining in this Job.
	 */
	public int getNumberOfTasksCompleted()
	{
		return numFinishedTasks;
	}
	
	/**
	 * Returns the results of running the Job, as a human-readable String.
	 * @return
	 */
	public String getResults()
	{
		BigInteger rtn = new BigInteger("1");
		if(!finishedTasks.isEmpty())
		{
		/*
		  for(FactorialTask task : finishedTasks)
		  	rtn = rtn.multiply((BigInteger)task.getResults());
		  */
		  rtn = (BigInteger)finishedTasks.peek().getResults();
		}
		
		return rtn.toString();
	}
	
	/**
	 * Returns the current state of this Job
	 * 
	 * @return The current state of the Job instance
	 */
	public JobState getState()
	{
		return currentState;
	}
	
	/**
	 * Change this Job's state, sending event notifications if necessary.
	 *
	 * @param The new state to set the job to.
	 */
	protected void setState(JobState newState)
	{
		if(currentState != newState)
		{
		  currentState = newState;
		  fireStateChanged();
		}
	}
	
	/**
	 * Notify all registered listeners of state changes.
	 */
	protected void fireStateChanged()
	{
		synchronized(registeredListeners)
		{
			for (JobEventListener listener : registeredListeners)
				listener.JobStateChanged(new JobEvent(this));
		}
	}
	
	/**
	 * Notify all registered listeners that the given tasks should be cancelled.
	 * 
	 * @param taskIDs An array of all task IDs that should be cancelled.
	 */
	protected void cancelSpecifiedTasks(int[] taskIDs)
	{
		synchronized(registeredListeners)
		{
			for (JobEventListener listener : registeredListeners)
				listener.TasksCancelled(new JobEvent(this), taskIDs);
		}
	}
	
	/**
	 * Notify all registered listeners that all tasks should be cancelled.
	 */
	protected void cancelAllTasks()
	{
		synchronized(registeredListeners)
		{
			for (JobEventListener listener : registeredListeners)
				listener.AllTasksCancelled(new JobEvent(this));
		}
	}
	
	/**
	 * Add the specified event listener to the list of registered listeners, thus
	 * allowing it to be notified of JobEvents.
	 *
	 * @param listenerToAdd The listener to remove from the list of registered listeners.
	 */
	public void addJobEventListener(JobEventListener listenerToAdd)
	{
	  synchronized(registeredListeners)
		{
	  	registeredListeners.add(listenerToAdd);
	  }
	}
	
	/**
	 * Remove the specified event listener from the list of registered listeners, thus
	 * no longer allowing it to be notifiedJobEvents.
	 * 
	 * @param listenerToRemove The listener to remove from the list of registered listeners.
	 */
	public void removeJobEventListener(JobEventListener listenerToRemove)
	{
	  synchronized(registeredListeners)
		{
	  	registeredListeners.remove(listenerToRemove);
	  }
	}
}
