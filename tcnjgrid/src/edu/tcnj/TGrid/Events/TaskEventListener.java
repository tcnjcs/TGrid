/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.Events;

/**
 *
 * @author Dan
 */
public interface TaskEventListener {
    /*
     * Triggered when the Task changes state.
     */
    public void TaskStateChanged(TaskEvent e);
}
