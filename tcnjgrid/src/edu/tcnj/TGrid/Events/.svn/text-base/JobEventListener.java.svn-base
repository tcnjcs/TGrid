/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.Events;

/**
 *
 * @author Dan
 */
public interface JobEventListener {
    /**
     * Triggered when the Job changes state.
     */
    public void JobStateChanged(JobEvent e);
    
    /**
     * Triggered when the Job needs to cancel one or more tasks.
     */
    public void TasksCancelled(JobEvent e, int[] taskIDs);
    
    /**
     * Triggered when the Job needs to cancel all tasks.
     */
    public void AllTasksCancelled(JobEvent e);
}
