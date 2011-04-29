/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.JobEventListener;
import edu.tcnj.TGrid.States.JobState;

/**
 * The Job interface represents a the entire job to be completed on the grid, as
 * opposed to the individual "Tasks" that are sent to client computers.
 * 
 * @author Dan
 * @see edu.tcnj.TGrid.Task
 */
public interface Job {
    /**
     * Does what is necessary to make the Job ready to run, after which 
     * the Job is marked READY.
     * 
     * Generally, this will generate an initial group of Tasks, which can be 
     * retrieved with the getNextTask() method.
     */
    void makeReady();
    
    /**
     * Returns the next Task to be performed as part of this Job.  Note that
     * this method generally won't work if the Job isn't ready.
     * 
     * @return A Task object representing the next task needed to be run.
     */
    Task getNextReadyTask();
    
    /**
     * Returns whether or not there are any tasks remaining as part of this Job.
     * 
     * @return true if there is at least one more Task ready,
     *         false otherwise
     */
    boolean hasNextTask();
    
    /**
     * Stores the specified Task.
     * 
     * Intended to store either a) Tasks that have completed sucessfully,
     * b) those that have encountered errors and cannot run, or 
     * c) Tasks that failed to run, and must be delegated again
     * 
     */
    void storeTask(Task taskToStore);
    
    /**
     * Returns the initial number of Tasks featured as part of the Job.
     * 
     * @return the total number of Tasks that this Job started with.
     */
    int getInitialNumberOfTasks();
    
    /**
     * Returns the number of Tasks remaining in this Job.  (That is, those that
     * have not yet been retrieved with the getNextTask() method.)
     * 
     * @return the number of Tasks remaining in this Job.
     */
    int getRemainingNumberOfTasks();
    
    /**
     * Returns the number of Tasks in this Job that have completed sucessfully.
     * 
     * @return the number of Tasks remaining in this Job.
     */
    int getNumberOfTasksCompleted();
    
    /**
     * Returns the results of running the Job, as a human-readable String.
     * @return
     */
    String getResults();
    
    /**
     * Returns the current state of this Job
     * 
     * @return The current state of the Job instance
     */
    JobState getState();
    
    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of JobEvents.
     *
     * @param The listener to remove from the list of registered listeners.
     */
    void addJobEventListener(JobEventListener listenerToAdd);
    
    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notified of JobEvents.
     * 
     * @param The listener to remove from the list of registered listeners.
     */
    void removeJobEventListener(JobEventListener listenerToRemove);
}
