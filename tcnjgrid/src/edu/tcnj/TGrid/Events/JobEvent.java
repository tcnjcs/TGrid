/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.Events;

import edu.tcnj.TGrid.Job;

/**
 *
 * @author Dan
 */
public class JobEvent extends java.util.EventObject {
    /**
     * Creates a new JobEvent instance.
     * 
     * @param source The object on which the JobEvent initially occurred.
     * @param state The state of the calling JobEvent
     */
    public JobEvent(Job source) {
        super((Object)source);
    }
}
