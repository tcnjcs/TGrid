/*
 * ResourceDiscoveryAgent.java
 * 
 * Created on Sep 29, 2007, 5:44:44 PM
 * 
 */

package edu.tcnj.TGrid.GridServer;

import edu.tcnj.TGrid.ConnectionToRemoteHost;

import edu.tcnj.TGrid.Exceptions.ResourceException;
import edu.tcnj.TGrid.GridServer.Events.ResourceDiscoveryEvent;
import edu.tcnj.TGrid.GridServer.Events.ResourceDiscoveryEventListener;
import edu.tcnj.TGrid.GridServer.Exceptions.ResourceDiscoveryException;

import edu.tcnj.TGrid.Events.ClientKeyReceivedEvent;
import edu.tcnj.TGrid.Events.ClientKeyReceivedEventListener;
import edu.tcnj.TGrid.GridServer.Events.ClientCommandConnectionAcceptedEvent;
import edu.tcnj.TGrid.GridServer.Events.ClientCommandConnectionAcceptedEventListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.IOException;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ResourceDiscoveryAgent class is responsible for discovering new resources
 * and notifying registered listeners when they are discovered.
 * 
 * @author Dan
 */
public class ResourceDiscoveryAgent
{
    /**
     * The Thread that is used to accept connections.
     */
    protected Thread resourceDiscoveryThread;
    
    /**
     * The Thread that is used to accept client command connections.
     */
    protected Thread clientCommandConnectionThread;
    
    /**
     * The port on which the ResourceDiscoveryAgent should accept connections.
     */
    private int port;
    
    /**
     * Server socket to accept connections
     */
    private ServerSocket serverSocket;
    
    /**
     * Server socket to accept connections for client commands
     */
    private ServerSocket clientCommandsServerSocket;
    
    /**
     * The username that connecting resources must supply.  Passed on to the
     * created resource objects.
     */
    private String username;
    
    /**
     * The password that connecting resources must supply.  Passed on to the
     * created resource objects.
     */
    private String password;
    
    /** 
     * Stores whether or not the ResourceDiscoveryAgent should continue 
     * accepting connections.  This is used to control the execution of
     * the run() method.  (Note that  Thread.stop() and the like are depricated.)
     */
    protected volatile boolean shouldAcceptConnections;
    
    /**
     * Represents a reference to the resource object that will handle the next incoming connection
     */
    private ServerSideResource newResource;
    
    /**
     * Represents the id number that will be assigned to the next resource to connect
     */
    private int currentResourceId = 0;
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    private HashSet<ResourceDiscoveryEventListener> registeredListeners = new HashSet<ResourceDiscoveryEventListener>();
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications when a new client command connection is accepted
     */
    private HashSet<ClientCommandConnectionAcceptedEventListener> clientCmdListeners = new HashSet<ClientCommandConnectionAcceptedEventListener>();
    
    /**
     * Creates a new instance of ResourceDiscoveryAgent, with the specified port.
     * 
     * @param port The port on which the ResourceDiscoveryAgent should accept
     *             incoming connections on.
     */
    public ResourceDiscoveryAgent(int port, String username, String password) {
        this.port = port;
        this.username = username;
        this.password = password;
        
        shouldAcceptConnections = false;
    }
    
    /**
     * Begins listening for connections.
     * 
     * @throws ResourceDiscoveryException If the port is unavailable for any reason.
     */ 
    public void startListeningForConnections() throws ResourceDiscoveryException {
        shouldAcceptConnections = true;
        
        try
        {
	        // Start server socket
	        serverSocket = new ServerSocket(port);
	        clientCommandsServerSocket = new ServerSocket(port+1);
	      }
	      catch (IOException ex)
	      {
	      	System.out.println("Failed to open server socket.");
	      }
        
        //Start a thread that continually accepts incoming connections
        resourceDiscoveryThread = new ResourceDiscoveryThread();
        resourceDiscoveryThread.start();
        
				//Start a thread that continually accepts incoming client command connections
        clientCommandConnectionThread = new ClientCommandConnectionThread();
        clientCommandConnectionThread.start();
    }
    
    /**
     * Stops listening for connections.
     * 
     * @throws ResourceDiscoveryException If the port is unavailable for any reason.
     */ 
    public void stopListeningForConnections() throws ResourceDiscoveryException {
        shouldAcceptConnections = false;
        
        try {
            newResource.stopWaitingForIncomingConnection();
            serverSocket.close();
            clientCommandsServerSocket.close();
        } catch (ResourceException ex) {
            throw new ResourceDiscoveryException("Could not stop connection");
        } catch (NullPointerException ex) {
            //this is only caught because newResource might not have been initialized yet
        }
        catch (IOException ex) {
            //this is only caught because newResource might not have been initialized yet
        }
        
        try {
            resourceDiscoveryThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger("global").log(Level.FINER, "Resource discovery thread interrupted while waiting to close");
        } catch (NullPointerException ex) {
            //again, resourceDiscoveryThread might not have been initialized
        }
        
       
    }
    
    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of ResourceDiscoveryEvents.
     * 
     * @param listenerToAdd The listener to remove from the list of registered listeners.
     */
    public void addResourceDiscoveryEventListener(ResourceDiscoveryEventListener listenerToAdd) {
        registeredListeners.add(listenerToAdd);
    }
    
    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notified of ResourceDiscoveryEvents.
     * 
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeResourceDiscoveryEventListener(ResourceDiscoveryEventListener listenerToRemove) {
        registeredListeners.remove(listenerToRemove);
    }
    
    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of client command connections.
     * 
     * @param listenerToAdd The listener to remove from the list of registered listeners.
     */
    public void addClientCommandConnectionAcceptedEventListener(ClientCommandConnectionAcceptedEventListener listenerToAdd) {
        clientCmdListeners.add(listenerToAdd);
    }
    
    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notified of client command connections.
     * 
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeClientCommandConnectionAcceptedEventListener(ClientCommandConnectionAcceptedEventListener listenerToRemove) {
        clientCmdListeners.remove(listenerToRemove);
    }
    
    /**
     * Thread responsible for accepting connections on the specified port.
     */ 
    protected class ResourceDiscoveryThread extends Thread
		{    
        /**
         * Accepts connections on the specified port until shouldAcceptConnections
         * becomes false.
         */ 
        @Override
        public void run() {
            while(shouldAcceptConnections) {
                try {
                    newResource = new ServerSideResource(currentResourceId++, username, password);
                    
                    newResource.waitForIncomingConnection(serverSocket);
                    
                    fireResourceDiscovered(newResource);
                } catch (ResourceException ex) {
                    Logger.getLogger(ResourceDiscoveryAgent.class.getName()).log(Level.INFO, null, ex);
                    //TODO: make this error handling a little more graceful
                    shouldAcceptConnections = false;
                }
            }
        }
    }

    /**
     * Notify all registered listeners of resource discoveries.
     * 
     * @param discoveredResourceAddress The address of the newly discovered Resource object to be
     *                                  attached to the event.
     */
    protected void fireResourceDiscovered(ServerSideResource discoveredResource) {
        for (ResourceDiscoveryEventListener listener : registeredListeners) {
            listener.resourceDiscovered(new ResourceDiscoveryEvent(this, discoveredResource));
        }
    }
    
    /**
     * Thread responsible for accepting connections for the client command socket
     */ 
    protected class ClientCommandConnectionThread extends Thread implements ClientKeyReceivedEventListener
		{
			/**
			* Accepts connections on the specified port until shouldAcceptConnections
			* becomes false.
			*/ 
			@Override
			public void run()
			{
				while(shouldAcceptConnections)
				{
					try
					{
						ConnectionToRemoteHost commandCon = new ConnectionToRemoteHost();
						commandCon.getClientKey(true);
						commandCon.addClientKeyReceivedEventListener(this);
						commandCon.waitForConnectionFromRemoteHost(clientCommandsServerSocket);
					}
					catch (Exception ex)
					{
						Logger.getLogger(ResourceDiscoveryAgent.class.getName()).log(Level.INFO, null, ex);
					}
				}
			}
			
			/**
			 * Triggered when the command connection receives the client key.
			 */
			public void clientKeyReceivedFromRemoteHost(ClientKeyReceivedEvent e)
			{
				// Get connection
				ConnectionToRemoteHost con = (ConnectionToRemoteHost)e.getSource();
				
				// Remove event listener
				con.removeClientKeyReceivedEventListener(this);
				
				// Fire event listeners
				fireClientCommandConnectionAccepted(con);
			}
    }

    /**
     * Notify all registered listeners that a connection was made
     * 
     * @param con Client command connection that was accepted
     */
    protected void fireClientCommandConnectionAccepted(ConnectionToRemoteHost con)
		{
			for (ClientCommandConnectionAcceptedEventListener listener : clientCmdListeners)
				listener.clientCommandConnectionAccepted(new ClientCommandConnectionAcceptedEvent(this, con));
    }
}
