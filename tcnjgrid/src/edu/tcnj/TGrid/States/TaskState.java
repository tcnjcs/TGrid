/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.States;

/**
 * Represents the possible states for a Task (any class implementing the Task interface)
 * 
 * @author Dan
 * @see edu.tcnj.TGrid.Task
 */
public enum TaskState {
    /**
     * Indicates that the Task is ready to be run.
     */
    READY,
    
    /**
     * Indicates that the Task is currently executing.
     */
    RUNNING,
    
    /**
     * Indicates that the Task has completed sucessfully.
     */
    COMPLETED,
    
    /**
     * Indicates that the task was terminated abnormally, usually by a call to
     * Task.forceQuit()
     */
    TERMINATED,
    
    /**
     * Indicates that the Task has encountered a problem.
     */
    TROUBLED,
    
    /**
     * Indicates that the system has determined that this task is too 
     * problematic to run sucessfully.
     */
    PROBLEMATIC
}
