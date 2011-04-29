/*
 * CommandToClientFromclient.java
 * 
 * Created on Oct 27, 2007, 11:01:19 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

/**
 * Enumerates the possible commands that the client can send to a client.
 * 
 * @author Dan
 */
public enum CommandToServerFromClient {
    
    /**
     * Indicates that the client is about to send a username.
     */
    USERNAME,
    
    /**
     * Indicates that the client is about to send a password.
     */
    PASSWORD,
    
    /**
     * Indicates that the client is about to send the client key and password
     */
    CLIENT_KEY,
    
    /**
     * Indicates that the client's state changed, and that it is about to send its current state.
     */
    STATE_CHANGED,
    
    /**
     * Indicates that the client wishes to recieve a response from the server,
     * to indicate its presence
     */
    PING,
    
    /**
     * Indicates that the client received the server's request for acknowledgement
     */
    PING_ACKNOWLEDGED,
    
    /**
     * Indicates that a task has completed, and the results are about to be sent.
     */
    SENDING_TASK_RESULTS,
    
    /**
     * Indicates that the last command sent was invalid.
     */
    INVALID_COMMAND,
    
    /**
     * Indicates that the client encountered a miscellaneous error.
     */
    GENERAL_ERROR,
    
    /**
     * Indicates that the client wishes to disconnect from the server.
     */
    GOING_OFFLINE,
    
    /**
     * Indicates that the server has no command in particular to handle.
     */
    NONE,
    
    /**
     * Indicates that the client has a file already and does not want it transferred
     */
    TRANSFER_FILE_NOT_MODIFIED,
    
    /**
     * Indicates that the client want to actually start the file transfer
     */
    TRANSFER_FILE_ACK,
    
    /**
     * Indicates that the client has some idle statistics to remort back to the
     * server.     
     */
    IDLE_STATS_AVAILABLE,
    
    /**
     * Indicates that an idle time is about to be sent from the client.
     */
    SENDING_IDLE_TIME,
    
    /**
     * Indicates that a nonidle time is about to be sent from the client.
     */
    SENDING_NONIDLE_TIME
}
