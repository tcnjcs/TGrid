/*
 * ResourceEventListener.java
 * 
 * Created on Sep 29, 2007, 7:24:05 PM
 */

package edu.tcnj.TGrid.GridServer.Events;

/**
 * The ResourceEventListener interface should be implemented 
 * by any class interested in receiving notification of 
 * ResourceEvents
 * 
 * @author Dan
 * @see GridServer.Events.ResourceEvent
 */
public interface ResourceEventListener extends java.util.EventListener {
    /**
     * Triggered when the Resource changes state.
     */
    public void resourceStateChanged(ResourceEvent e);
    
    /**
     * Triggered when the Resource failed to complete a task in the given
     * timeout.
     */
    public void resourceTaskTimedOut(ResourceEvent e);
    
    /**
     * Triggered when the Resource wants to be removed from the list of ready
     * resources		  
     */
    public void resourceNoLongerReady(ResourceEvent e);
}
