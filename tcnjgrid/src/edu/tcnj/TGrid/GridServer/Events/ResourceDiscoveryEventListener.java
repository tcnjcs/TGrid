/*
 * ResourceDiscoveryEventListener.java
 * 
 * Created on Sep 29, 2007, 7:24:05 PM
 */

package edu.tcnj.TGrid.GridServer.Events;

/**
 * The ResourceDiscoveryEventListener interface should be implemented 
 * by any class interested in receiving notification of 
 * ResourceDiscoveryEvents
 * 
 * @author Dan
 * @see GridServer.Events.ResourceDiscoveryEvent
 */
public interface ResourceDiscoveryEventListener extends java.util.EventListener {
    /*
     * Triggered when a new resource becomes available.
     */
    public void resourceDiscovered(ResourceDiscoveryEvent e);
}
