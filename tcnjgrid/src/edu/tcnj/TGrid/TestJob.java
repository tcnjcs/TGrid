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

/**
 * The TestJob Class represents an extremely basic Job that sends out dummy 
 * tasks.
 * 
 * @author Dan
 */
public class TestJob implements Job
{
	/**
	 * Stores a list of all internal Tasks that still need to be sent to clients.
	 */
	private LinkedBlockingQueue<TestTask> newTasks = new LinkedBlockingQueue<TestTask>();
	
	/**
	 * Stores a list of all internal Tasks that have failed.
	 */
	private LinkedBlockingQueue<TestTask> problematicTasks = new LinkedBlockingQueue<TestTask>();
	
	/**
	 * Stores a list of all internal Tasks have have completed successfully.
	 */
	private LinkedBlockingQueue<TestTask> finishedTasks = new LinkedBlockingQueue<TestTask>();
	
	/**
	 * Represents the original number of tasks in the file.
	 * 
	 * Used in determining whether or not all tasks have completed or not.
	 */
	private int initialNumberOfTasks;
	
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
	 * Stores the id number of the last task created, so that new ones can be 
	 * assigned unique values.
	 */
	private int currentTaskNumber;
	
	/**
	 * Does what is necessary to make the Job ready to run, after which 
	 * the Job is marked READY.
	 */
	public void makeReady()
	{
	  for(currentTaskNumber=0; currentTaskNumber<10; currentTaskNumber++)
	  	newTasks.add(new TestTask());
	    
	  initialNumberOfTasks = newTasks.size();
	  
	  setState(JobState.READY);
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
	      finishedTasks.add((TestTask)taskToStore);
	      
	      //randomly, let's cancel all tasks and add a new one
	      //if(Math.random() > 0.5) {
	          //cancelAllTasks();
	     // }
	      break;
	    case TROUBLED:  //if the task had a problem, put it in a queue where it will not have to run again
	    case PROBLEMATIC:
	      problematicTasks.add((TestTask)taskToStore);
	      break;
	    case READY:
	    case TERMINATED: //if the task was forcefully terminated, add it back into the queue of tasks waiting to run
	      newTasks.add((TestTask)taskToStore);
	      break;
	  }
	  
	  if(finishedTasks.size() + problematicTasks.size() == initialNumberOfTasks)
	  	setState(JobState.COMPLETED);
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
		return finishedTasks.size();
	}
	
	/**
	 * Returns the results of running the Job, as a human-readable String.
	 * @return
	 */
	public String getResults()
	{
		String retVal = "";
		
		if(!finishedTasks.isEmpty())
		{
		  retVal += "Tasks that finished were:\n";
		  for(TestTask task : finishedTasks)
		  	retVal += task.getResults() + "\n";
		}
		
		if(!newTasks.isEmpty())
		{
		  retVal += "\nTasks that did not run were:\n";
		  for(TestTask task : newTasks)
		  	retVal += task + "\n";
		}
		
		if(!problematicTasks.isEmpty())
		{
		  retVal += "\nTasks that encountered problems were:\n";
		  for(TestTask task : problematicTasks)
		  	retVal += task + "\n";
		}
		
		return retVal;
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
