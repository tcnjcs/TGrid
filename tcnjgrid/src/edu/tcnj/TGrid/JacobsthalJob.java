/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.JobEvent;
import edu.tcnj.TGrid.Events.JobEventListener;
import edu.tcnj.TGrid.States.JobState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The JacobsthalJob Class represents an Job based on calculating Jacobsthal numbers.
 * 
 * @author Dan
 */
public class JacobsthalJob implements Job {
    /**
     * Stores a list of all internal Tasks that still need to be sent to clients.
     */
    private LinkedBlockingQueue<JacobsthalTask> newTasks = new LinkedBlockingQueue<JacobsthalTask>();
    
    /**
     * Stores a list of all internal Tasks that have failed.
     */
    private LinkedBlockingQueue<JacobsthalTask> problematicTasks = new LinkedBlockingQueue<JacobsthalTask>();
    
    /**
     * Stores a list of all internal Tasks have have completed successfully.
     */
    private LinkedBlockingQueue<JacobsthalTask> finishedTasks = new LinkedBlockingQueue<JacobsthalTask>();
   
    /**
     * Represents the original number of tasks in the file.
     * 
     * Used in determining whether or not all tasks have completed or not.
     */
    private int initialNumberOfTasks = 0;
    
    /**
     * Represents the current state of this Job.
     */
    private JobState currentState = JobState.NEW;
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    private HashSet<JobEventListener> registeredListeners = new HashSet<JobEventListener>();
    
    private ArrayList<Integer> primes;
    
    private int min;
    private int max;
    private int increment;
    private int n = 25;
    private int w = 744;
    private int winitial = 744;
    private int lengthOfSearch;
    
    public JacobsthalJob() {
        //TODO: first figure out what the numbers actually do, then have them set via constructor parameters
    }
    
    /**
     * Does what is necessary to make the Job ready to run, after which 
     * the Job is marked READY.
     */
    public void makeReady() {
        primes = getPrimeNumbers(1000);
        int starts = -800;
        lengthOfSearch = determineLengthOfSearch(w);
        int numPrimes = numberOfModularPrimes(w);
        int ols = Math.min(3000000, lengthOfSearch/30);
        
        if (w == winitial) {
            starts = -800;
        }
	
        while(starts <= lengthOfSearch) {
            int[] commandLineArguments = {n, w, starts, ols, numPrimes};
            newTasks.add(new JacobsthalTask(commandLineArguments));
            initialNumberOfTasks++;
            starts = starts + ols;
        }
		
        setState(JobState.READY);
    }
    
    /**
     * Returns the next Task to be performed as part of this Job.  Note that
     * this method generally won't work if the Job isn't ready.
     * 
     * @return A Task object representing the next task needed to be run.
     */
    public Task getNextReadyTask() {
        setState(JobState.RUNNING);
        return newTasks.poll();
    }
    
    /**
     * Returns whether or not there are any tasks remaining as part of this Job.
     * 
     * @return true if there is at least one more Task ready,
     *         false otherwise
     */
    public boolean hasNextTask() {
        return !newTasks.isEmpty();
    }
    
    /**
     * Stores the specified Task.
     * 
     * Intended to store either a) Tasks that have completed sucessfully,
     * b) those that have encountered errors and cannot run, or 
     * c) Tasks that failed to run, and must be delegated again
     */
    public void storeTask(Task taskToStore) {
        switch(taskToStore.getState()) {
            case COMPLETED: //if the task is completed, add it to the completed tasks list
                finishedTasks.add((JacobsthalTask)taskToStore);
                break;
            case TROUBLED:  //if the task had a problem, put it in a queue where it will not have to run again
            case PROBLEMATIC:
                problematicTasks.add((JacobsthalTask)taskToStore);
                break;
            case READY:
            case TERMINATED: //if the task was forcefully terminated, add it back into the queue of tasks waiting to run
                newTasks.add((JacobsthalTask)taskToStore);
        }
        
        if(finishedTasks.size() + problematicTasks.size() == initialNumberOfTasks) {
            setState(JobState.COMPLETED);
        }
    }
    
    /**
     * Returns the initial number of Tasks featured as part of the Job.
     * 
     * @return the total number of Tasks that this Job started with.
     */
    public int getInitialNumberOfTasks() {
        return initialNumberOfTasks;
    }
    
    /**
     * Returns the number of Tasks remaining in this Job.  (That is, those that
     * have not yet been retrieved with the getNextTask() method.)
     * 
     * @return the number of Tasks remaining in this Job.
     */
    public int getRemainingNumberOfTasks() {
        return newTasks.size();
    }
    
    /**
     * Returns the number of Tasks in this Job that have completed sucessfully.
     * 
     * @return the number of Tasks remaining in this Job.
     */
    public int getNumberOfTasksCompleted() {
        return finishedTasks.size();
    }
    
    /**
     * Returns the results of running the Job, as a human-readable String.
     * @return
     */
    public String getResults() {
        String retVal = "";
        
        if(!finishedTasks.isEmpty()) {
            retVal += "Tasks that finished were:\n";
            for(JacobsthalTask task : finishedTasks) {
                retVal += task.toString() + ": " + task.getResults() + "\n";
            }
        }
        
        if(!newTasks.isEmpty()) {
            retVal += "\nTasks that did not run were:\n";
            for(JacobsthalTask task : newTasks) {
                retVal += task.toString() + "\n";
            }
        }
        
        if(!problematicTasks.isEmpty()) {
            retVal += "\nTasks that encountered problems were:\n";
            for(JacobsthalTask task : problematicTasks) {
                retVal += task.toString() + "\n";
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns the current state of this Job
     * 
     * @return The current state of the Job instance
     */
    public JobState getState() {
        return currentState;
    }
    
    /**
     * Change this Job's state, sending event notifications if necessary.
     *
     * @param The new state to set the job to.
     */
    protected void setState(JobState newState) {
        if(currentState != newState) {
            currentState = newState;
            fireStateChanged();
        }
    }
    
    /**
     * Notify all registered listeners of state changes.
     */
    protected void fireStateChanged() {
        synchronized(registeredListeners) {
            for (JobEventListener listener : registeredListeners) {
                listener.JobStateChanged(new JobEvent(this));
            }
        }
    }
    
    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of JobEvents.
     *
     * @param listenerToAdd The listener to remove from the list of registered listeners.
     */
    public void addJobEventListener(JobEventListener listenerToAdd) {
        synchronized(registeredListeners) {
            registeredListeners.add(listenerToAdd);
        }
    }
    
    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notifiedJobEvents.
     * 
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeJobEventListener(JobEventListener listenerToRemove) {
        synchronized(registeredListeners) {
            registeredListeners.remove(listenerToRemove);
        }
    }
    
    /**
     * Finds prime numbers between 0 and upperRange using a sieve algorithm.
     * 
     * @param upperRange The highest number at which the function will search for
     *                   primes before returning.
     * @return an Arraylist of prime numbers represented as Integer objects
     */
    protected ArrayList<Integer> getPrimeNumbers(int upperRange) {
        ArrayList<Integer> primeNumbers = new ArrayList<Integer>();
        
        //first, 2 to the list, as this algorithm doesn't identify 2 as prime
        primeNumbers.add(2);
        //NOTE THAT 0 WAS REMOVED BECAUSE IT CAUSED A DIVIDE BY ZERO ERROR
        
        //next, add every odd number from 3 to upperLimit into the list
        for (int i = 3; i < upperRange; i += 2) {
            primeNumbers.add(i);
        }
        
        //remove non-primes from the list
        for (int ptr = 0; ptr < primeNumbers.size(); ptr++) {
            int val = primeNumbers.get(ptr);

            for (int i = ptr+1; i < primeNumbers.size(); i++) {
                if (i%val == 0) {
                    primeNumbers.remove(i);
                }
            }
        }
        
        return primeNumbers;
    }
    
    /**
     * Returns the number of modular primes used for a given value.
     * 
     * @param m
     * @return the number of modular primes
     */
    protected int numberOfModularPrimes(int m) {
        int i = 0;
        
        while(20*primes.get(i+2) <= m-4 && i < n-1) {
            i++;
        }
        
        return i;
    }
    
    /**
     * Determines the length of search for a given value.
     * 
     * @param w
     * @return the length of the search
     */
    protected int determineLengthOfSearch(int w) {
	int length = 1;
                
        for(int i=1; i<numberOfModularPrimes(w); i++) {
            length = length * primes.get(i+1);
        }
		
	return length;
    }
}
