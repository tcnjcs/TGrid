/*
 * ConnectionToResource.java
 * 
 * Created on Oct 27, 2007, 5:22:10 PM
 * 
 * Represents a connection to a remote host (a computer listening for connections
 * on a specified port.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.States.ConnectionToRemoteHostState;

import edu.tcnj.TGrid.Events.ConnectionToRemoteHostEvent;
import edu.tcnj.TGrid.Events.ConnectionToRemoteHostEventListener;

import edu.tcnj.TGrid.Events.ClientKeyReceivedEvent;
import edu.tcnj.TGrid.Events.ClientKeyReceivedEventListener;

import edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException;
import edu.tcnj.TGrid.Exceptions.MessageNotSentException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConnectionToRemoteHost class is responsible for handling all communcation
 * to a remote host.  Messages can be sent via simple method calls, and received 
 * messages are distributed by triggering events.
 * 
 * @author Dan
 */
public class ConnectionToRemoteHost {
    
    /**
     * The default amount of time to wait before timing out during connections
     * in milliseconds.  (The default value is 10 seconds.)
     */
    protected static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    
    /**
     * The current state of this ConnectionToResource
     */
    private ConnectionToRemoteHostState currentState = ConnectionToRemoteHostState.NOT_CONNECTED;
    
    /**
     * The thread responsible for sending text to the remote host.
     */
    private ReceiveThread receiveThread = null;
    
    /**
     * The address of the remote host.
     */
    private InetAddress addressOfRemoteHost;
    
    /**
     * The port number of the remote host.
     */
    private int port;
    
    /**
     * The connection to the remote host as a Socket.
     */
    private Socket connection = new Socket();
    
    /**
     * Responsible for sending serialized objects to the remote host by 
     * writing to the connection's input stream.
     */
    private ObjectOutputStream toRemoteHost = null;
    
    /**
     * Responsible for receiving messages in the form of serialized objects
     * from the remote host by reading from the connection's input stream.
     */
    private ObjectInputStream fromRemoteHost = null;
    
    /**
     * Determines whether or not the sending and receiving threads should 
     * continue to run.  This is necessary so they know when to quit.
     */
    private volatile boolean receiveEnabled = false;
    
    /**
     * Indicates whether the client key is going to be passed in from the 
     * client.  This is used when the client command connection is established.
     * By default, it is assumed that the event listeners will handle all 
     * received messages.  Call <code>getClientKey()</code> method to change
     * this value.     
     */
    private boolean getClientKey = false;
    
    /**
     * Stores the client key passed in from the client.  This is used when the
     * client command connection is established.  The first object received will
     * be the client key.
     */
    private int clientKey = -1;
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    private HashSet<ConnectionToRemoteHostEventListener> registeredListeners = new HashSet<ConnectionToRemoteHostEventListener>();
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications when the client key is received.
     */
    private HashSet<ClientKeyReceivedEventListener> clientKeyListeners = new HashSet<ClientKeyReceivedEventListener>();
    
    /**
     * Creates a new instance of ConnectionToRemoteHost with no associated address.
     * Not that without an address, a wait for connection method much be called first.
     * 
     */
    public ConnectionToRemoteHost() {
    }
    
    /**
     * Creates a new instance of ConnectionToResource.
     * 
     * @param addressOfRemoteHost  The physical address of the remote host.
     */
    public ConnectionToRemoteHost(InetAddress addressOfRemoteHost, int port) {
        this.addressOfRemoteHost = addressOfRemoteHost;
        this.port = port;
    }
    
    /**
     * Returns the client key.
     * 
     * @return Client key send by client.
     */
    public int getClientKey()
		{
    	return clientKey;
    }
    
    /**
     * Indicate whether the client key will be sent as the first data item.
     * 
     * @param getClientKey Set to true if the first data item is to be 
     * interpreted as the client key. (Event listeners will not be notified).
     */
    public void getClientKey(boolean getClientKey)
		{
        this.getClientKey = getClientKey;
    }
    
    /**
     * Sets port number
     * 
     * @param int Port number
     */
    public void setPort(int port)
		{
        this.port = port;
    }
    
    /**
     * Connects to the remote host, opening the socket, and starting up the send and receive threads.
     * 
     * @throws edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException 
     */
    public void connectToRemoteHost() throws ConnectionToRemoteHostException {
        // Naturally, a connection can only be opened if this is currently disconnected.
        if(currentState == ConnectionToRemoteHostState.NOT_CONNECTED && addressOfRemoteHost != null) {
            // Make 3 attempts to connect
            for (int i = 0; currentState != ConnectionToRemoteHostState.CONNECTED && i < 3; i++)
            {
            	// Sleep a while if this is not the first attempt
            	if (i != 0)
            	{
	              try
	              {
	              	Thread.sleep(2500);
	              }
	              catch (InterruptedException e)
	              {
	              }
	              
	              // Create new socket
	            	connection = new Socket();
	            }
            
            	//System.out.println("Connection attempt #" + (i+1));
	            setState(ConnectionToRemoteHostState.CONNECTING);
	
	            try {//attempt to connect to the remote host, using the address specified in the constructor
	                connection.connect(new InetSocketAddress(addressOfRemoteHost, port), DEFAULT_CONNECTION_TIMEOUT);
	                
	                //initialize toClient and fromClient as inputs and outputs for this socket.
	                toRemoteHost = new ObjectOutputStream(this.connection.getOutputStream());
	                fromRemoteHost = new ObjectInputStream(this.connection.getInputStream());
	
	                //initialize and start the thread responsible for receiving data
	                receiveThread = new ReceiveThread();
	                
	                receiveEnabled = true;
	                
	                receiveThread.start();
	
	                setState(ConnectionToRemoteHostState.CONNECTED);
	            } catch (IOException ex) {
	                Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, "Error initiating connection with remote host.", ex);
	                System.out.println(ex);
	            }
            }
            
            // Check if the connection was made
            if (currentState != ConnectionToRemoteHostState.CONNECTED)
            {
            	setState(ConnectionToRemoteHostState.ERROR_CONNECTING);
            	throw new ConnectionToRemoteHostException("Error initiating connection with remote host.");
            }
        } else {
            throw new ConnectionToRemoteHostException("Cannot connect if already connected");
        }
    }
    
    /**
     * Waits for the remote host to connect.  When it does, the socket is opened,
     * and the send and receive threads are started.
     * 
     * Note that if this instance has an associated address, only connections from that
     * address are permitted.  However, if no address is associated, whoever connects first 
     * is associated.
     * 
     * @param serverSocket Server socket that is waiting for a connection
     * @throws edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException 
     * 
     */
    public void waitForConnectionFromRemoteHost(ServerSocket serverSocket) throws ConnectionToRemoteHostException {
        // Naturally, a connection can only be opened if this is currently disconnected.
        if(currentState == ConnectionToRemoteHostState.NOT_CONNECTED) {
            setState(ConnectionToRemoteHostState.WAITING_FOR_CONNECTION);

            try {
                //sockets don't default to a "closed" state, so this is necessary to ensure that the 
                //loop below works as advertised.
                if (connection != null)
                	connection.close();
                
                /* Continues waiting until a legitimate connection is made */
                while(connection.isClosed())
								{
										/* Blocks until a connection comes in. */
                    connection = serverSocket.accept();
                    
//                    if(!connection.getInetAddress().equals(addressOfRemoteHost.getAddress())) {
//                        Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.INFO, "An incorrect host at " + connection.getRemoteSocketAddress().toString() + " tried to establish a connection.");
//                        connection.close();
//                    }
                }
                                
                //initialize toClient and fromClient as inputs and outputs for this socket.
                toRemoteHost = new ObjectOutputStream(this.connection.getOutputStream());
                fromRemoteHost = new ObjectInputStream(this.connection.getInputStream());

                //initialize and start the thread responsible for receiving data
                receiveThread = new ReceiveThread();
                
                receiveEnabled = true;
                
                receiveThread.start();
                
                if(addressOfRemoteHost == null)
								{
                    addressOfRemoteHost = connection.getInetAddress();
                    port = connection.getPort();
                }
                
                setState(ConnectionToRemoteHostState.CONNECTED);
            } catch (IOException ex) {
                //generally, if an IOexception is thown while disconnecting, it means the cancel... method was called
                if(currentState != ConnectionToRemoteHostState.DISCONNECTING) {
                    Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, "Error initiating connection with remote host.", ex);

                    setState(ConnectionToRemoteHostState.ERROR_CONNECTING);
                    throw new ConnectionToRemoteHostException("Error initiating connection with remote host.");
                } else {
                    Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.FINER, "Wait for remote connection cancelled", ex);

                    setState(ConnectionToRemoteHostState.NOT_CONNECTED);
                    throw new ConnectionToRemoteHostException("Wait for connection operation cancelled; no connection was made");
                }
            }
        } else {
            throw new ConnectionToRemoteHostException("Cannot wait for connection if already connected");
        }
    }
    
    /**
     * If this instance is currently waiting for an incoming connection, attmpts to cancel the operation
     * 
     * @throws edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException
     */
    public void stopWaitingForConnectionFromRemoteHost() throws ConnectionToRemoteHostException
		{
			if(currentState == ConnectionToRemoteHostState.WAITING_FOR_CONNECTION)
				setState(ConnectionToRemoteHostState.DISCONNECTING);
    /*
        if(currentState == ConnectionToRemoteHostState.WAITING_FOR_CONNECTION) {
            if(serverSocket != null) {
                setState(ConnectionToRemoteHostState.DISCONNECTING);
                
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    setState(ConnectionToRemoteHostState.ERROR_DISCONNECTING);
                    throw new ConnectionToRemoteHostException("Error closing server socket");
                }
                
                setState(ConnectionToRemoteHostState.NOT_CONNECTED);
            }
        }
    */
    }
    
    /**
     * Disconnects from the remote host, closing the socket, and shutting down the send 
     * and receive threads.
     * 
     * Synchronized to ensure that disconnect can't be called while other things
     * are transpiring.
     * 
     * @throws edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException
     */
    public synchronized void disconnect() throws ConnectionToRemoteHostException {
        if(currentState != ConnectionToRemoteHostState.NOT_CONNECTED) {
            try {
                setState(ConnectionToRemoteHostState.DISCONNECTING);

                receiveEnabled = false;

                /* First, stop the receive thread. */

                if(receiveThread != null) {
                    //first, interrupt the thread, in case it's sleeping
                    receiveThread.interrupt();

                    //wait for the receive thread to finish up.
                    receiveThread.join(DEFAULT_CONNECTION_TIMEOUT);
                }

                /* Close the toClient and fromClient streams. */
                if(fromRemoteHost != null) {
                    fromRemoteHost.close();
                }

                if(toRemoteHost != null) {
                    toRemoteHost.close();
                }

                connection.close();

                setState(ConnectionToRemoteHostState.NOT_CONNECTED);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, "Error closing connection to remote host", ex);

                setState(ConnectionToRemoteHostState.ERROR_CONNECTING);
                throw new ConnectionToRemoteHostException("Error closing connection to remote host");
            } catch (InterruptedException ex) {
                Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, "Disconnection interrupted.");

                setState(ConnectionToRemoteHostState.ERROR_CONNECTING);
                throw new ConnectionToRemoteHostException("Disconnection interrupted");
            }
        } else {
            throw new ConnectionToRemoteHostException("Cannot disconnect if already disconnected");
        }
    }
    
    /**
     * Sends an object in serialized form to the remote host.
     * 
     * Synchronized to ensure that multiple threads cannot send messages
     * simultaneously.
     *
     * @param messageToSend The message to send to the connected client.
     * 
     * @throws edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException if sending
     *         the message fails
     */
    public synchronized void send(Serializable objectToSend) throws ConnectionToRemoteHostException {
        if(currentState == ConnectionToRemoteHostState.CONNECTED) {
            try {
                //System.out.println("Sending " + objectToSend);
                toRemoteHost.writeObject(objectToSend);
                toRemoteHost.flush();
            } catch(IOException ex) {
                Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, "Error writing to socket.", ex);

                setState(ConnectionToRemoteHostState.COMMUNICATION_ERROR);

                throw new MessageNotSentException("Error writing to remote host");
            }
        } else {
            throw new MessageNotSentException("Cannot send message while disconnected");
        }
    }
    
    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of ResourceEvents.
     *
     * @param listenerToAdd The listener to remove from the list of registered listeners.
     */
    public void addRemoteHostEventListener(ConnectionToRemoteHostEventListener listenerToAdd) {
        synchronized(registeredListeners) {
            registeredListeners.add(listenerToAdd);
        }
    }
    
    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notified of ResourceEvents.
     * 
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeRemoteHostEventListener(ConnectionToRemoteHostEventListener listenerToRemove) {
        synchronized(registeredListeners) {
            registeredListeners.remove(listenerToRemove);
        }
    }
    
    /**
     * Add the specified event listener to the list of client key listeners.
     *
     * @param listenerToAdd The listener to remove from the list of listeners.
     */
    public void addClientKeyReceivedEventListener(ClientKeyReceivedEventListener listenerToAdd)
		{
      synchronized(clientKeyListeners)
      {
      	clientKeyListeners.add(listenerToAdd);
      }
    }
    
    /**
     * Remove the specified event listener from the list of client key listeners.
     * 
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeClientKeyReceivedEventListener(ClientKeyReceivedEventListener listenerToRemove)
    {
    	synchronized(clientKeyListeners)
      {
				clientKeyListeners.remove(listenerToRemove);
			}
    }
    
    /**
     * Returns the current state of the ConnectionToRemoteHost
     * @return currentState the current state of the resource
     */
    public ConnectionToRemoteHostState getState() {
        return currentState;
    }
    
    /**
     * Returns whether or not this instance is connected to a remote host.
     * @return true if a connection has been established, false otherwise
     */
    public boolean isConnected() {
        return currentState == ConnectionToRemoteHostState.CONNECTED;
    }
    
    /**
     * Returns the host name of the remote host
     * 
     * @return hostName the host name of the remote host
     */
    public String getHostName() {
        if(addressOfRemoteHost != null) {
            return addressOfRemoteHost.getHostName();
        } else {
            return null;
        }
    }
    
    /**
     * Returns a string representation of the remote host's IP address
     * 
     * @return hostAddress a string representation of the remote host's IP address
     */
    public String getHostAddress() {
        if(addressOfRemoteHost != null) {
            return addressOfRemoteHost.getHostAddress();
        } else {
            return null;
        }
    }
    
    /**
     * Returns whether or not this ConnectionToRemoteHost has an address associated with it
     * @return currentState the current state of the resource
     */
    public boolean hasRemoteAddress() {
        return (addressOfRemoteHost != null);
    }
    
    /**
     * Changes this object's state, and notifies all registered listeners of the change.
     * 
     * @param newState the new state to set the ConnectionToRemoteHost's state
     */
    protected void setState(ConnectionToRemoteHostState newState) {
        if(currentState != newState) {
            currentState = newState;
            fireStateChanged();
        }
    }
    
    /**
     * Notify all registered listeners of state changes.
     */
    protected void fireStateChanged() {
        synchronized(registeredListeners) {
            for (ConnectionToRemoteHostEventListener listener : registeredListeners) {
                listener.connectionToRemoteHostStateChanged(new ConnectionToRemoteHostEvent(this, currentState));
            }
        }
    }
    
    /**
     * Notify all registered listeners of a received message.
     * 
     * @param message the message that was received
     */
    protected void fireMessageReceived(Object message) {
        synchronized(registeredListeners) {
            for (ConnectionToRemoteHostEventListener listener : registeredListeners) {
                listener.messageReceivedFromRemoteHost(new ConnectionToRemoteHostEvent(this, currentState, message));
            }
        }
    }
    
    /**
     * Notify all registered listeners that the client key was received.
     */
    protected void fireClientKeyReceived()
		{
      synchronized(clientKeyListeners)
			{
        for (ClientKeyReceivedEventListener listener : clientKeyListeners)
          listener.clientKeyReceivedFromRemoteHost(new ClientKeyReceivedEvent(this));
      }
    }
    
    /**
     * Thread responsible for receiving data from the remote host.
     */ 
    protected class ReceiveThread extends Thread {
        Object objectReceived;
        
        public ReceiveThread() {
            super();
            
            this.setName("Remote host receive thread " + this.getId());
        }
        
        /**
         * While the sendReceiveEnabled is true, continue checking for new data received
         */ 
        @Override
        public void run() {
            while(receiveEnabled && currentState == ConnectionToRemoteHostState.CONNECTED) {
                try {
                    objectReceived = fromRemoteHost.readObject();
                    
                    // Check if it is the client key
                    if (getClientKey && clientKey == -1)
                    {
                    	clientKey = (Integer)objectReceived;
                    	
                    	// Notify listeners that the client key was received.
                    	fireClientKeyReceived();
                    }
										// Otherwise, notify event listeners 
                    else
                    	fireMessageReceived(objectReceived);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionToRemoteHost.class.getName()).log(Level.WARNING, "Error reading from socket.", ex);
                    
                    setState(ConnectionToRemoteHostState.COMMUNICATION_ERROR);
                }
            }
        }
    } 
}
