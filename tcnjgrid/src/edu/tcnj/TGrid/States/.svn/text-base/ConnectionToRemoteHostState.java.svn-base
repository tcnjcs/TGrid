/*
 * ConnectionToRemoteHostState.java
 * 
 * Created on Oct 27, 2007, 5:42:17 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.States;

/**
 * Enumerates the possible states of a ConnectionToRemoteHost object.
 * 
 * @author Dan
 */
public enum ConnectionToRemoteHostState {
    /** Indicates that the ConnectionToRemoteHost is not connected to the remote host. */ 
    NOT_CONNECTED,

    /** Indicates that the ConnectionToRemoteHost in the process of connecting to 
     *  the remote host. */ 
    CONNECTING,
    
    /** Indicates that the ConnectionToRemoteHost is waiting for a connection from  
     *  the remote host. */ 
    WAITING_FOR_CONNECTION,

    /** Indicates that the ConnectionToRemoteHost is connected to a physical client,
     *  and awaiting instructions. */ 
    CONNECTED,

    /** Indicates that the ConnectionToRemoteHost has encountered a problem
     *  connecting.  */ 
    ERROR_CONNECTING,
    
    /** Indicates that the ConnectionToResource has encountered a problem
     *  communicating with the remote host.  (either with sending or receiving)  */ 
    COMMUNICATION_ERROR,
    
    /** Indicates that the ConnectionToResource is in the process of disconnecting. */ 
    DISCONNECTING,
    
    /** Indicates that the ConnectionToRemoteHost has encountered a problem
     *  while disconnecting.  */ 
    ERROR_DISCONNECTING,
}
