package edu.tcnj.TGrid.GridServer.Events;

import edu.tcnj.TGrid.ConnectionToRemoteHost;
import edu.tcnj.TGrid.GridServer.ResourceDiscoveryAgent;

/**
 * A ResourceDiscoveryEvent is triggered by the ResourceDiscoveryAgent
 * when a new Resource is discovered.
 * 
 * @author Stephen
 * 
 */
public class ClientCommandConnectionAcceptedEvent extends java.util.EventObject
{
    protected ConnectionToRemoteHost con; 
    
    /**
     * Creates a new ClientCommandConnectionAcceptedEvent instance.
     * 
     * @param source The object on which the ResourceDiscoveryEvent initially occurred.
     * @param con Connection to remote host
     */
    public ClientCommandConnectionAcceptedEvent(ResourceDiscoveryAgent source, ConnectionToRemoteHost con) {
        super((Object)source);
        
        this.con = con;
    }
    
    /**
     * Returns the address of the resource discovered
     * 
     * @return discoveredResourceAddress The resource which was discovered.
     */
    public ConnectionToRemoteHost getConnectionToRemoteHost() {
        return this.con;
    }
}
