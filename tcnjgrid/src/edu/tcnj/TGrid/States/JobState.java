/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.States;

/**
 * Represents the possible states for a Job (any class implementing the Job interface)
 * 
 * @author Dan
 * @see edu.tcnj.TGrid.Job
 */
public enum JobState {
    /**
     * Indicates that the Job is not yet ready to be run.  (Generally meaning it hasn't been split into tasks)
     */
    NEW, 
    
    /**
     * Indicates that the Job is ready to run.  (Meaning it has been split into tasks)
     */
    READY,
    
    /**
     * Indicates that the Job is being performed currently.
     */
    RUNNING,
    
    /**
     * Indicates that the Job had been completed.
     */
    COMPLETED
}
