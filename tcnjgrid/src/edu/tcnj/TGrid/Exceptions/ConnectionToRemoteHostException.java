/*
 * ConnectionToRemoteHostException.java
 */

package edu.tcnj.TGrid.Exceptions;

/**
 * This exception is thrown when the <code>ConnectionToRemoteHost</code> object encounters an issue.
 * 
 * @author Dan
 */
public class ConnectionToRemoteHostException extends Exception {

    /**
     * Creates a new instance of <code>ConnectionToRemoteHostException</code> without detail message.
     */
    public ConnectionToRemoteHostException() {
    }


    /**
     * Constructs an instance of <code>ConnectionToRemoteHostException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConnectionToRemoteHostException(String msg) {
        super(msg);
    }
}
