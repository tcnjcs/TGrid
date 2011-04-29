/*
 * ResourceEventListener.java
 * 
 * Created on Sep 29, 2007, 7:24:05 PM
 */

package edu.tcnj.TGrid.GridClient.Events;

import edu.tcnj.TGrid.GridServer.Events.*;

/**
 * The ResourceEventListener interface should be implemented 
 * by any class interested in receiving notification of 
 * ResourceEvents
 * 
 * @author Dan
 * @see GridServer.Events.ResourceEvent
 */
public interface ResourceEventListener extends java.util.EventListener {
    /*
     * Triggered when the Resource changes state.
     */
    public void resourceStateChanged(ResourceEvent e);
}
