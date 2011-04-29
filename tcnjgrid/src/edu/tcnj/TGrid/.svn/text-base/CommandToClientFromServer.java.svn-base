/*
 * CommandToClientFromServer.java
 * 
 * Created on Oct 27, 2007, 11:01:19 PM
 * 
 */

package edu.tcnj.TGrid;

/**
 * Enumerates the possible commands that the server can send to a client.
 * 
 * @author Dan
 */
public enum CommandToClientFromServer {
    
    /**
     * Indicates that the server wishes for the client to send authorization information.
     */
    AUTHORIZATION_REQUEST,
    
    /**
     * Indicates that the client sent incorrect authorization information.
     */
    AUTHORIZATION_FAILED,
    
    /**
     * Indicates that the client sent correct authorization information.
     */
    AUTHORIZATION_SUCCEEDED,
    
    /**
     * Indicates that the server is about to send the port number for the client
     * command connection.  Not used because of firewall issues.		  
     */
    CLIENT_COMMAND_PORT,
    
    /**
     * Indicates that the server has setup the client connection.		  
     */
    CLIENT_CONNECTION_ESTABLISHED,
    
    /**
     * Indicates that the server is about to send the client its ID number.
     */
    SENDING_RESOURCE_ID,
    
    /**
     * Indicates that the server is about to send a new task.
     */
    SENDING_NEW_TASK,
    
    /**
     * Indicates that the server wants the client to monitor the CPU and report
     * back whether it thinks it is idle or not.     
     */
    MONITOR_CPU,
    
    /**
     * Indicates that the server completed sending a new task.
     */
    TASK_SENT,
    
    /**
     * Indicates that the client should immediatly cease execution of its current task.
     */
    CANCEL_TASK,
    
    /**
     * Indicates that the server had received the client's new state.
     */
    STATE_ACKNOWLEDGED,
    
    /**
     * Indicates that the server wishes for the client to shut down.
     * The behavior of the shut down action is dependent on the client's
     * individual configuration.
     */
    SHUTDOWN_REQUEST,
    
    /**
     * Indicates that the server wishes for the client to close its connection
     * to the server, and begin awaiting incoming connections.
     * 
     * Not implemented for the time being, as TCNJ lab computers cannot await
     * connections due to security restrictions in effect.
     */
    DISCONNECTION_REQUEST,
    
    /**
     * Indicates that the last command the client sent to the server was invalid,
     * and thus ignored.
     */
    INVALID_COMMAND,
    
    /**
     *  Indicates that the server wishes to recieve a response from the client,
     *  to indicate its presence
     */
    PING,
    
    /**
     * Indicates that the server received the client's request for acknowledgement
     */
    PING_ACKNOWLEDGED,
    
    /**
     * Indicates that the server has encountered a general error preventing it
     * from continuing.
     */
    GENERAL_SERVER_ERROR,
    
    /**
     * Indicates that the server is going to transfer a file
     */
    TRANSFER,
    
    /**
     * Indicates that the client should send back it's idle statistics
     */
    SEND_IDLE_STATS
}
