/*
 * ConnectionToRemoteHostEventListener.java
 * 
 * Created on Sep 29, 2007, 7:24:05 PM
 */

package edu.tcnj.TGrid.Events;

/**
 * The ConnectionToRemoteHostEventListener interface should be implemented 
 * by any class interested in receiving notification of 
 * ConnectionToRemoteHostEvent.
 * 
 * @author Dan
 * @see ConnectionToRemoteHostEvent
 */
public interface ConnectionToRemoteHostEventListener extends java.util.EventListener {
    /*
     * Triggered when the remote host changes state.
     */
    public void connectionToRemoteHostStateChanged(ConnectionToRemoteHostEvent e);
    
    /*
     * Triggered when the remote host recives a serialized object
     */
    public void messageReceivedFromRemoteHost(ConnectionToRemoteHostEvent e);
}
