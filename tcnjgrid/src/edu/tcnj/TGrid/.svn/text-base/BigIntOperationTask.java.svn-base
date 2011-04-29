package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;

import java.io.Serializable;

import java.util.LinkedList;
import java.math.BigInteger;
import edu.tcnj.TGrid.Util.ArithmeticOperation;

/**
 * The BigIntOperationTask class performs an arithmetic operation on a set of
 * BigIntegers.  Only one operation can be performed at a single time. 
 * 
 * @author Stephen Sigwart
 */
public class BigIntOperationTask extends Task
{
	/**
	 * Represents the results of running this thread, as a human-readable string.
	 */
	private BigInteger results;
	
	/**
	 * List of numbers to apply the operation to
	 */
	private LinkedList<BigInteger> operands = new LinkedList<BigInteger>();
	
	/**
	 * Operation to apply to operands
	 */
	private ArithmeticOperation operator;
	
	/**
	 * Represents the internal thread used to run the Task.
	 */
	private RunThread runThread; 
	
	/**
	 * Creates a new BigIntOperationTask instance.
	 * @param op Operation to perform	 	 
	 */
	public BigIntOperationTask(ArithmeticOperation op)
	{
		operator = op;
	}
	
	/**
	 * Adds a BigInteger operand
	 * @param operand Operand to perform operation on	 	 
	 */
	public void addOperand(BigInteger operand)
	{
		operands.add(operand);
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
		return "BigIntOperationTask #" + id;
	}
    
	protected class RunThread extends Thread
	{
		public void run()
		{
			// Check which operation to perform
			BigInteger head;
			switch (operator)
			{
				case ADD:
				  results = new BigInteger("0");
				  for (BigInteger tmp : operands)
				  	results = results.add(tmp);
			  	break;
			  case SUBTRACT:
			  	if (operands.isEmpty())
			  		results = new BigInteger("0");
			  	else
			  	{
					  head = operands.poll();
					  results = new BigInteger(head.toString());
					  for (BigInteger tmp : operands)
					  	results = results.subtract(tmp);
					  operands.add(0, head);
					}
			  	break;
			  case MULTIPLY:
				  results = new BigInteger("1");
				  for (BigInteger tmp : operands)
				  	results = results.multiply(tmp);
			  	break;
			  case DIVIDE:
			  	if (operands.isEmpty())
			  		results = new BigInteger("0");
			  	else
			  	{
					  head = operands.poll();
						results = new BigInteger(head.toString());
					  for (BigInteger tmp : operands)
					  	results = results.divide(tmp);
					  operands.add(0, head);
					}
			  	break;
			}
		  
		  setState(TaskState.COMPLETED);
  	}
	}
	
	public int getTaskID()
	{
		return id;
	}
}
