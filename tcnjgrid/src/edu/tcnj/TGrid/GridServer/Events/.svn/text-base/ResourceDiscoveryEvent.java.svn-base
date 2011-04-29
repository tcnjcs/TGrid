/*
 * ResourceDiscoveryEvent.java
 * 
 * Created on Sep 29, 2007, 3:24:07 PM
 * 
 * ResourceDiscoveryEvents are Events triggered by--you guessed it--
 * the ResourceDiscoveryAgent.
 */

package edu.tcnj.TGrid.GridServer.Events;

import edu.tcnj.TGrid.GridServer.ServerSideResource;
import edu.tcnj.TGrid.GridServer.ResourceDiscoveryAgent;

/**
 * A ResourceDiscoveryEvent is triggered by the ResourceDiscoveryAgent
 * when a new Resource is discovered.
 * 
 * @author Dan
 * 
 */
public class ResourceDiscoveryEvent extends java.util.EventObject {
    protected ServerSideResource discoveredResource; 
    
    /**
     * Creates a new ResourceDiscoveryEvent instance.
     * 
     * @param source The object on which the ResourceDiscoveryEvent initially occurred.
     * @param discoveredResource The resource which was discovered.
     */
    public ResourceDiscoveryEvent(ResourceDiscoveryAgent source, ServerSideResource discoveredResource) {
        super((Object)source);
        
        this.discoveredResource = discoveredResource;
    }
    
    /**
     * Returns the address of the resource discovered
     * 
     * @return discoveredResourceAddress The resource which was discovered.
     */
    public ServerSideResource getDiscoveredResource() {
        return this.discoveredResource;
    }
}
