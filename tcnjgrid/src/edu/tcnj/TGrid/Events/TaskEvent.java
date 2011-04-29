/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.Events;

import edu.tcnj.TGrid.Task;
import edu.tcnj.TGrid.States.TaskState;

/**
 *
 * @author Dan
 */
public class TaskEvent extends java.util.EventObject {
    private TaskState state;
    
    /**
     * Creates a new TaskEvent instance.
     * 
     * @param source The object on which the TaskEvent initially occurred.
     * @param state The state of the calling TaskEvent
     */
    public TaskEvent(Task source, TaskState state) {
        super((Object)source);
        
        this.state = state;
    }
    
    /**
     * Returns the ConnectionToResource's state specified in the constructor
     */
    public TaskState getState() {
        return state;
    }
}
