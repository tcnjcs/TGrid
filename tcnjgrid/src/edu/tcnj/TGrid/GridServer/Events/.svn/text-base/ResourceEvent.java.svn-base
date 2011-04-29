/*
 * ResourceEvent.java
 * 
 * Created on Sep 29, 2007, 3:24:07 PM
 * 
 * ResourceEvents are Events triggered by--you guessed it--
 * Resources.
 */

package edu.tcnj.TGrid.GridServer.Events;

import edu.tcnj.TGrid.GridServer.ServerSideResource;
import edu.tcnj.TGrid.States.ResourceState;
import edu.tcnj.TGrid.Task;

/**
 * A ResourceEvent is triggered by the Resource
 * when a events occur.
 * 
 * @author Dan
 * 
 */
public class ResourceEvent extends java.util.EventObject {
    private String message = null;
    private ResourceState state = null;
    
    /**
     * Creates a new ResourceEvent instance.
     * 
     * @param source The object on which the ResourceEvent initially occurred.
     */
    public ResourceEvent(ServerSideResource source) {
        super((Object)source);
    }
    
    /**
     * Creates a new ResourceEvent instance.
     * 
     * @param source The object on which the ResourceEvent initially occurred.
     * @param state The state of the calling Resource
     */
    public ResourceEvent(ServerSideResource source, ResourceState state) {
        super((Object)source);
        
        this.state = state;
    }
    
    /**
     * Creates a new ResourceEvent instance.
     * 
     * @param source The object on which the ResourceEvent initially occurred.
     * @param state The state of the calling Resource
     * @param message An explanatory message, containing additional details.
     */
    public ResourceEvent(ServerSideResource source, ResourceState state, String message) {
        super((Object)source);
        
        this.state = state;
        this.message = message;
    }
    
    /**
     * Returns the Resource's state specified in the constructor
     */
    public ResourceState getState() {
        return this.state;
    }
}
