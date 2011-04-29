package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.util.HashSet;
import java.util.Random;
import java.io.Serializable;

import java.math.BigInteger;

/**
 * The FactorialTask class represents an extremely basic Task, that does absolutely
 * nothing except report that it worked.
 * 
 * @author Stephen
 */
public class FactorialTask extends Task
{
	/**
	 * Represents the results of running this thread, as a human-readable string.
	 */
	private BigInteger results;
	
	/**
	 * Integer to start multiplication at
	 */
	private int lowNum;
	
	/**
	 * Integer to stop multiplication at
	 */
	private int highNum;
	
	/**
	 * Represents the internal thread used to run the Task.
	 */
	private RunThread runThread; 
	
	/**
	 * Creates a new FactorialTask instance.
	 * @param low Integer to start multiplication at
	 * @param high Integer to stop multiplication at	 	 
	 */
	public FactorialTask(int low, int high)
	{
		// Add a memory requirement
		taskRequirements.addRequirement(new MemoryChecker(512));
	
		if (low < high)
		{
			lowNum = low;
			highNum = high;
		}
		else
		{
			lowNum = high;
			highNum = low;
		}
	}
	
	/**
	 * Runs the FactorialTask, spawing a thread to run the task
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
		this.results = (BigInteger)results;
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
	 * Returns a string representation of this FactorialTask
	 * 
	 * @return a string representation of this FactorialTask
	 */
	@Override
	public String toString()
	{
		return "FactorialTask #" + id;
	}
    
	protected class RunThread extends Thread
	{
		public void run()
		{
		  BigInteger tmp = new BigInteger(""+lowNum);
		  BigInteger max = new BigInteger(""+highNum);
		  results = new BigInteger("1");
		  for (; !max.subtract(tmp).add(BigInteger.ONE).equals(BigInteger.ZERO); tmp = tmp.add(BigInteger.ONE))
		  	results = results.multiply(tmp);
		  
		  setState(TaskState.COMPLETED);
  	}
	}
	
	public int getTaskID()
	{
		return id;
	}
}
