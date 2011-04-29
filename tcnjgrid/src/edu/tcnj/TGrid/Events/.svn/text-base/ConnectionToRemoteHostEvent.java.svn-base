/*
 * ConnectionToResourceEvent.java
 * 
 * Created on Sep 29, 2007, 3:24:07 PM
 * 
 * ConnectionToResourceEvents are Events triggered by--you guessed it--
 * ConnectionToResource objects.
 */

package edu.tcnj.TGrid.Events;

import edu.tcnj.TGrid.ConnectionToRemoteHost;
import edu.tcnj.TGrid.States.ConnectionToRemoteHostState;

/**
 * A ConnectionToResourceEvent is triggered by the ConnectionToResource
 * on which the event occurs.
 * 
 * @author Dan
 * 
 */
public class ConnectionToRemoteHostEvent extends java.util.EventObject {
    private Object message;
    private ConnectionToRemoteHostState state;
    
    /**
     * Creates a new ConnectionToResourceEvent instance.
     * 
     * @param source The object on which the ConnectionToResourceEvent initially occurred.
     */
    public ConnectionToRemoteHostEvent(ConnectionToRemoteHost source) {
        super((Object)source);
    }
    
    /**
     * Creates a new ResourceEvent instance.
     * 
     * @param source The object on which the ConnectionToResourceEvent initially occurred.
     * @param state The state of the calling ConnectionToResource
     */
    public ConnectionToRemoteHostEvent(ConnectionToRemoteHost source, ConnectionToRemoteHostState state) {
        super((Object)source);
        
        this.state = state;
        this.message = null;
    }
    
    /**
     * Creates a new ResourceEvent instance.
     * 
     * @param source The object on which the ConnectionToResourceEvent initially occurred.
     * @param state The state of the calling ConnectionToResource
     * @param message An explanatory message, containing additional details.
     */
    public ConnectionToRemoteHostEvent(ConnectionToRemoteHost source, ConnectionToRemoteHostState state, Object message) {
        super((Object)source);
        
        this.state = state;
        this.message = message;
    }
    
    /**
     * Returns the ConnectionToResource's state specified in the constructor
     */
    public ConnectionToRemoteHostState getState() {
        return this.state;
    }
    
    /**
     * Returns the object specified in the constructor
     */
    public Object getObject() {
        return this.message;
    }
}
