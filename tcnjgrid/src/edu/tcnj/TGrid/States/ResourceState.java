/*
 * ResourceState.java
 * 
 * Created on Sep 30, 2007, 1:32:48 AM
 * 
 */

package edu.tcnj.TGrid.States;

/**
 * Enumerates the possible states of a resource.
 * 
 * @author Dan
 */
public enum ResourceState {

    /** Indicates that the Resource is not connected to a physical client. */ 
    NOT_CONNECTED,

    /** Indicates that the Resource in the process of  connecting to a 
     *  physical client. */ 
    CONNECTING,

    /** Indicates that the Resource failed to connect to the physical client. */ 
    ERROR_CONNECTING,
    
    /** Indicates that the Resource waiting for an incoming connection from a 
     * physical client
     */
    WAITING_FOR_CONNECTION,
    
    /** Indicates that the Resource in the process of performing the authorization procedure with the
     *  physical client. */ 
    AUTHORIZING,
    
    /** Indicates that the Resource failed to provide proper authorization information */ 
    AUTHORIZATION_FAILURE,
    
    /** Indicates that the Resource succeeded to provide proper authorization information */
    AUTHORIZATION_SUCCESS,
    
    /** Indicates that the Resource is waiting for the server's port number for
     * the client command socket
     */
    AWAITING_CLIENT_COMMAND_CONNECTION,
    
    /** Indicates that the Resource is connected to a physical client,
     *  and awaiting instructions. */ 
    READY,
    
    /**
     * Indicates that a Task has been assigned to the Resource, but it is not
     * yet running.
     */
    TASK_ASSIGNED,
    
    /**
     * Indicates that a Task is currently executing on the resource.
     */
    RUNNING_TASK,
    
    /**
     * Indicates that the task ended abnormally
     */
    TASK_ENDED_ABNORMALLY,
    
    /**
     * Indicates that the task has completed successfully, but is not yet quite
     * finished.
     * 
     * (Generally, that means that the results have not yet been transferred.)
     */
    FINISHING_TASK,
    
     /**
     * Indicates that the task has completed successfully.
     */
    TASK_COMPLETED,
    
    /**
     * Indicates that the task was successfully terminated at the server's request
     */
    TASK_TERMINATED,
    
    /** Indicates that the Resource has encountered a problem from
     *  which it may still recover.  */ 
    TROUBLED,
    
    /** Indicates that the Resource is in the process of disconnecting. */ 
    DISCONNECTING,
    
    /** Indicates that the Resource encountered an error in the process of disconnecting. */ 
    ERROR_DISCONNECTING,

    /** Indicates that the Resource has encountered a problem from
     *  which it cannot recover.  */ 
    UNABLE_TO_CONTINUE,
    
    /** Indicates that the Resource is sending idle stats back to server.  */ 
    SENDING_IDLE_STATS

}
