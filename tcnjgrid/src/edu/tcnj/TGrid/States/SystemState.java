/*
 * SystemState.java
 * 
 * Created on Sep 30, 2007, 1:35:35 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.States;

/**
 * Enumerates the possible states of the system as a whole.
 * 
 * @author Dan
 */
public enum SystemState {

    /** Indicates that the system is not ready to do anything. */ 
    OFFLINE,

    /** Indicates that the system is preparing to become ready */ 
    INITIALIZING,

    /** Indicates that the system is actively accpting connections
     * from clients, and performing its other duties */ 
    READY,

    /** Indicates that the system has encountered a problem from
     *  which it may still recover.  */ 
    TROUBLED,

    /** Indicates that the system has encountered a problem from
     *  which it cannot recover.  */ 
    UNABLE_TO_CONTINUE
}