/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.Exceptions;

/**
 * This exception is thrown when a ConnectionToRemoteHost object encounters an issue sending
 * a message.
 * 
 * @author Dan
 */
public class MessageNotSentException extends ConnectionToRemoteHostException {

    /**
     * Creates a new instance of <code>MessageNotSentException</code> without detail message.
     */
    public MessageNotSentException() {
    }


    /**
     * Constructs an instance of <code>MessageNotSentException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MessageNotSentException(String msg) {
        super(msg);
    }
}
