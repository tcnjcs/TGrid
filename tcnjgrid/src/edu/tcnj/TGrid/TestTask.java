/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.util.HashSet;
import java.util.Random;
import java.io.Serializable;

/**
 * The TestTask class represents an extremely basic Task, that does absolutely
 * nothing except report that it worked.
 * 
 * @author Dan
 */
public class TestTask extends Task
{
	/**
	 * Represents the results of running this thread, as a human-readable string.
	 */
	private String results;
	
	/**
	 * Represents the internal thread used to "run" the Task.
	 */
	private RunThread runThread; 
	
	/**
	 * Creates a new TestTask instance.
	 */
	public TestTask()
	{
	}
	
	/**
	 * "Runs" the TestTask, spawing a thread that waits a specific amount of time.
	 */
	public void runTask()
	{
	  setState(TaskState.RUNNING);
	  
	  runThread = new RunThread();
	  runThread.start();
	}
	
	/**
	 * If the Task is currently running, forces it to quit without finishing.
	 */
	public void forceQuit()
	{
	  if(currentState == currentState.RUNNING)
		{
	    try
			{
	      runThread.interrupt();
				runThread.join();
	
	      setState(TaskState.TERMINATED);
	    } catch (InterruptedException ex) {}
	  }
	}
	
	/**
	 * Changes the task's results to the correct value.
	 * 
	 * @param the results from running the task
	 */
	public void setResults(Serializable results)
	{
		this.results = (String)results;
	}
    
	/**
	 * Returns the results from running the task as a string.  Only useful if
	 * the task has completed execution successfully.
	 * 
	 * @return the results from running the task
	 */
	public Serializable getResults()
	{
		return results;
	}
    
	/**
	 * Returns a string representation of this TestTask
	 * 
	 * @return a string representation of this TestTask
	 */
	@Override
	public String toString()
	{
		return "TestTask #" + id;
	}
    
	protected class RunThread extends Thread
	{
		public void run()
		{
		
			// Randomly fail task
			if(Math.random() < 0.95)
			{
				setState(TaskState.TERMINATED);
			}
			else
			{
			  try
				{
			  	sleep(2000);
			  }
				catch (InterruptedException ex) {}
			  results = "Task " + id + " says: These are some pretty great results.";
			  setState(TaskState.COMPLETED);
			}
  	}
	}
	
	public int getTaskID()
	{
		return id;
	}
}
