/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.GridClient.ClientSideResource;
import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.util.HashSet;
import java.io.Serializable;

/**
 * The Task interface represents a portions of a Job which are sent to clients.
 * 
 * @author Dan
 * @see edu.tcnj.TGrid.Job
 */
public abstract class Task implements Serializable
{
		/**
     * A unique ID number for this Task
     */
    private static int nextId = 1;
    
    /**
     * A unique ID number for this Task
     */
    protected final int id = Task.nextId++;
    
    /**
     * Timeout in milliseconds for the task
     */
    protected int timeout = 5000;
    
    /**
     * Represents the current state of this Task
     */
    protected TaskState currentState = TaskState.READY;
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    protected HashSet<TaskEventListener> registeredListeners = new HashSet<TaskEventListener>();
		   
    /**
     * Reference to the client side resource that is running it
     */
    protected ClientSideResource parentResource = null;
    
    /**
     * List of task requirements
     */
    protected TaskRequirements taskRequirements = new TaskRequirements();
    
    /**
		 * Determines if the client meets the task requirements.
		 * 
		 * @param client Client object containing specs.
		 *	 	 
		 * @return Whether or not the client meets the task requirements.
		 */
		public boolean meetsRequirements(ClientInfo client)
		{
			return taskRequirements.meetsRequirements(client);
		}
    
    /**
     * Sets the client side resource that is running this task
     * 
     * @param res Client side resource that is running this task
     */
    public void setParentResource(ClientSideResource res)
    {
    	parentResource = res;
    }
    
    /**
     * Gets the client side resource that is running this task
     * 
     * @return Client side resource that is running this task
     */
    public ClientSideResource getParentResource()
    {
    	return parentResource;
    }
    
    /**
     * Returns a unique identification number for this task.
     * 
     * @return this task's task id
     */
    public int getTaskID()
    {
        return id;
    }
    
    /**
     * Runs the current Task.  This operation should be non-blocking.
     */
    public abstract void runTask();
    
    /**
     * If the Task is currently running, forces it to quit without finishing.
     */
    public abstract void forceQuit();
    
    /**
     * Changes the task's results to the correct value.
     * 
     * @param the results from running the task, as a String
     */
    public abstract void setResults(Serializable results);
    
    /**
     * Returns the results from running the task as a string.  Only useful if
     * the task has completed execution successfully.
     * 
     * @return the results from running the task
     */
    public abstract Serializable getResults();
    
    /**
     * Returns the task timeout in milliseconds
     * 
     * @return Timeout for task in milliseconds
     */
    public int getTimeout()
    {
    	return timeout;
    }
    
    /**
     * Increases the task timeout.  Override this function to change method for
     * increasing timeout.
     *
		 * @return New timeout for task in milliseconds		      
     */
    public int increaseTimeout()
    {
    	// Double timeout
    	timeout = timeout * 2;
    	return timeout;
    }
    
    /**
     * Manually sets the Task's state, triggering event notifications if appropriate.
     */
    public synchronized void setState(TaskState newState) {
        if(currentState != newState) {
            currentState = newState;
            fireStateChanged();
        }
    }

    /**
     * Returns the Task's current state
     * 
     * @return the Task's current state
     */
    public TaskState getState() {
        return currentState;
    }
    
    /**
     * Notify all registered listeners of state changes.
     */
    protected void fireStateChanged() {
        synchronized(registeredListeners) {
            for (TaskEventListener listener : registeredListeners) {
                listener.TaskStateChanged(new TaskEvent(this, currentState));
            }
        }
    }
    
    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of TaskEvents.
     *
     * @param listenerToAdd The listener to remove from the list of registered listeners.
     */
    public void addTaskEventListener(TaskEventListener listenerToAdd) {
        synchronized(registeredListeners) {
            registeredListeners.add(listenerToAdd);
        }
    }
    
    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notified of TaskEvents.
     * 
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeTaskEventListener(TaskEventListener listenerToRemove) {
        synchronized(registeredListeners) {
            registeredListeners.remove(listenerToRemove);
        }
    }
    
    /**
     * Checks if the Task is equal to another.  This is determined based on the 
     * task's taskID
     * 
     * @param The Task object with which to compare this one.
     */
    //@Override
    //public abstract boolean equals(Object obj);
}
