/*
 * Resource.java
 * 
 * Created on Sep 29, 2007, 7:32:35 PM
 * 
 */
package edu.tcnj.TGrid.GridServer;

import edu.tcnj.TGrid.States.ResourceState;
import edu.tcnj.TGrid.Events.ConnectionToRemoteHostEvent;
import edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException;
import edu.tcnj.TGrid.GridServer.Events.ResourceEvent;
import edu.tcnj.TGrid.GridServer.Events.ResourceEventListener;
import edu.tcnj.TGrid.Exceptions.ResourceException;

import edu.tcnj.TGrid.ConnectionToRemoteHost;
import edu.tcnj.TGrid.States.ConnectionToRemoteHostState;
import edu.tcnj.TGrid.Events.ConnectionToRemoteHostEventListener;
import edu.tcnj.TGrid.CommandToServerFromClient;
import edu.tcnj.TGrid.CommandToClientFromServer;

import edu.tcnj.TGrid.States.TaskState;
import edu.tcnj.TGrid.Task;
import java.net.InetAddress;
import java.net.ServerSocket;

import edu.tcnj.TGrid.ClientInfo;
import edu.tcnj.TGrid.FileTransfer;
import edu.tcnj.TGrid.FileTransferTask;
import edu.tcnj.TGrid.TransferFileRemoteDirectory;
import edu.tcnj.TGrid.FileTransferInfoToClient;

import java.util.Calendar;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The server side Resource class is responsible for maintaining a connection to a
 * resource, directing the resource, and notifying registered listeners when 
 * noteworthy events occur.
 * 
 * @author Dan
 */
public class ServerSideResource implements ConnectionToRemoteHostEventListener {

    /**
     * The default time for actions to wait before they time out in milliseconds.
     */
    protected static final int DEFAULT_WAIT_TIME = 2000;
    /**
     * The default number of times an action should be retried before giving up
     */
    protected static final int DEFAULT_NUMBER_OF_ATTEMPTS = 3;
    /**
     * The default time between checks to make sure the client is still there,
     * in milliseconds.
     */
    protected static final int DEFAULT_KEEPALIVE_INTERVAL = 5000;
    /**
     * The default time that the resource can go without sending a message before
     * the server reacts
     */
    protected static final int DEFAULT_KEEPALIVE_TIMEOUT = 15000;
    /**
     * The unique identification number of this Resource
     */
    protected int resourceID = 0;
    /**
     * The host name of this Resource.
     * Included primarily for ease of identification.
     */
    protected String hostName;
    /**
     * The IP address of this Resource, as a string.
     * Included primarily for ease of identification.
     */
    protected String hostAddress;
    /**
     * ClientInfo object to store information on the client such as requirements
     * it meets, running statistics, and connection information.
     */
    protected ClientInfo client = null;
    /**
     * The correct username required during authentication
     */
    private String correctUsername = null;
    /**
     * The correct password required during authentication
     */
    private String correctPassword = null;
    /**
     * The username provided during authentication
     */
    private volatile String providedUsername;
    /**
     * The password provided during authentication
     */
    private volatile String providedPassword;
    /**
     * Whether or not the client has provided a username
     */
    private volatile boolean hasProvidedUsername = false;
    /**
     * Whether or not the client has provided a password
     */
    private volatile boolean hasProvidedPassword = false;
    /**
     * Key for the client in the server's connecting client map
     */
    private volatile int clientKey = 0;
    /**
     * Password for the client in the server's client map
     */
    private volatile int clientPswd = 0;
    /**
     * Flags to indicate a variety of things
     */
    private volatile int flags = 0;
    /**
     * Flag to indicate that a file to be transferred was not modified
     */
    private static final int FILE_TRANSFER_NOT_MODIFIED = 0x00000001;
    /**
     * Flag to indicate that a file can be transferred now
     */
    private static final int FILE_TRANSFER_ACK = 0x00000002;
    /**
     * Indicates if the client has idle stats to report on.
     */
    private boolean clientHasIdleStats = false;
    /**
     * The Task currently assigned to this resource
     */
    private Task currentTask;
    /**
     * Used to determine how to react to received messages.
     *
     * If awaitingCommand is true, the message will be interpereted as a command.
     * Otherwise, the message will be interpereted as text.
     *
     * For example, after receiving the USERNAME command, awaitingCommand would be set to false,
     * as the next expected message is supposed to be a block of text, not a command.
     */
    private volatile boolean awaitingCommand = true;
    /**
     * Stores the current command being performed, if any.
     * Used to determine how to react to received text messages. (non-commands)
     *
     * If awaitingCommand is false, any message received will need to be associated with
     * whatever command preceeded it.
     */
    private CommandToServerFromClient currentCommand = CommandToServerFromClient.NONE;
    /**
     * The current state of this Resource
     */
    private volatile ResourceState currentState = ResourceState.NOT_CONNECTED;
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    private HashSet<ResourceEventListener> registeredListeners = new HashSet<ResourceEventListener>();
    /**
     * Represents the connection to the resource itself
     */
    private ConnectionToRemoteHost connectionToResource;
    /**
     * Represents client command connection.  This connection is reserved for
     * commands sent from the client to the server.
     */
    private ConnectionToRemoteHost commandConnectionToResource;
    /**
     * Indicates whether or not the keep alive thread should continue to run
     */
    private volatile boolean shouldPerformKeepAlive = false;
    /**
     * Represents the last time a message was received.  Used for determining if the
     * client is still alive.
     *
     * TODO: implement this
     */
    private volatile long timeLastMessageReceived = -1;
    /**
     * Represents a polymorphic handle to whichever one of the internal threads happens to be executing.
     *
     * Declared as a single reference due to the fact that only one of the internal thread types should
     * ever be running at a given time.
     */
    private Thread internalThread;
    /**
     * Thread to check if a task completes in the given timeout.
     */
    private TaskTimeoutThread TaskTimeoutThread;
    /**
     * List of task ids that the resource should try to avoid
     */
    private TreeSet<Integer> tasksToAvoid = new TreeSet<Integer>();

    /**
     * Creates a new instance of Resource, with the specified ID.
     *
     * @param resourceID           A (preferrably unique) identification number
     *                             for this Resource.
     * @param username             The correct username, used during authentication
     *                             with remote resource.
     * @param password             The correct password, used during authentication
     *                             with remote resource.
     */
    public ServerSideResource(int resourceID, String username, String password) {
        this.resourceID = resourceID;
        this.correctUsername = username;
        this.correctPassword = password;

        connectionToResource = new ConnectionToRemoteHost();

        connectionToResource.addRemoteHostEventListener(this);
    }

    /**
     * Creates a new instance of Resource, with the specified address and ID.
     *
     * @param resourceID           A (preferrably unique) identification number
     *                             for this Resource.
     * @param addressOfResource    The address of the resource itself, represented by an
     *                             <code>InetAddress</code>
     * @param port                 The port number on the resource to connect to.
     * @param username             The correct username, used during authentication
     *                             with remote resource.
     * @param password             The correct password, used during authentication
     *                             with remote resource.
     */
    public ServerSideResource(int resourceID, InetAddress addressOfResource, int port, String username, String password) {
        this.resourceID = resourceID;
        this.correctUsername = username;
        this.correctPassword = password;

        connectionToResource = new ConnectionToRemoteHost(addressOfResource, port);
        commandConnectionToResource = new ConnectionToRemoteHost(addressOfResource, 0);

        connectionToResource.addRemoteHostEventListener(this);
    }

    /**
     * Waits for an incoming connection on the specified port.
     *
     * @param serverSocket Server socket that is waiting for a connection
     * @throws edu.tcnj.TGrid.Exceptions.ResourceException
     */
    public void waitForIncomingConnection(ServerSocket serverSocket) throws ResourceException {
        if (currentState == ResourceState.NOT_CONNECTED) {
            setState(ResourceState.WAITING_FOR_CONNECTION);

            try {
                connectionToResource.waitForConnectionFromRemoteHost(serverSocket);
            } catch (ConnectionToRemoteHostException ex) {
                setState(ResourceState.ERROR_CONNECTING);
                throw new ResourceException(ex.getMessage());
            }

            hostName = connectionToResource.getHostName();
            hostAddress = connectionToResource.getHostAddress();
        } else {
            throw new ResourceException("Cannot wait for connection from current state (" + currentState.toString() + ")");
        }
    }

    /**
     * If the resource is currently blocked waiting for an incoming connection,
     * this method cancels the operation.
     * @throws edu.tcnj.TGrid.Exceptions.ResourceException
     */
    public void stopWaitingForIncomingConnection() throws ResourceException {
        if (currentState == ResourceState.WAITING_FOR_CONNECTION) {
            setState(ResourceState.DISCONNECTING);

            try {
                connectionToResource.stopWaitingForConnectionFromRemoteHost();
            } catch (ConnectionToRemoteHostException ex) {
                setState(ResourceState.ERROR_DISCONNECTING);
                throw new ResourceException(ex.getMessage());
            }

            setState(ResourceState.NOT_CONNECTED);
        }
    }

    /**
     * Attempts to initiate a connection to the resource.
     * @throws edu.tcnj.TGrid.Exceptions.ResourceException
     */
    public void requestConnection() throws ResourceException {
        if (!connectionToResource.hasRemoteAddress()) {
            throw new ResourceException("Cannot request a connection to an unspecified address");
        }

        if (currentState == ResourceState.NOT_CONNECTED) {
            setState(ResourceState.CONNECTING);

            try {
                connectionToResource.connectToRemoteHost();
            } catch (ConnectionToRemoteHostException ex) {
                setState(ResourceState.ERROR_CONNECTING);
                throw new ResourceException(ex.getMessage());
            }

            hostName = connectionToResource.getHostName();
            hostAddress = connectionToResource.getHostAddress();
        } else {
            throw new ResourceException("Cannot start connection from current state (" + currentState.toString() + ")");
        }
    }

    /**
     * Attempts to sever the connection to the resource.
     * @throws edu.tcnj.TGrid.Exceptions.ResourceException
     */
    public void requestDisconnection() throws ResourceException {
        if (currentState != ResourceState.NOT_CONNECTED) {
            setState(ResourceState.DISCONNECTING);

            //TODO: add logic here to either a) tell the client to shut down
            //                            or b) tell the client to wait for incoming connections

            //if any, try to shut down an internal thread
            if (internalThread != null && internalThread.isAlive()) {
                try {
                    internalThread.interrupt();
                    internalThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerSideResource.class.getName()).log(Level.FINER, "Interrupted while waiting for internal thread to exit.", ex);
                }
            }

            try {
                connectionToResource.disconnect();
            } catch (ConnectionToRemoteHostException ex) {
                setState(ResourceState.ERROR_DISCONNECTING);
                throw new ResourceException(ex.getMessage());
            }

            setState(ResourceState.NOT_CONNECTED);
        } else {
            throw new ResourceException("Cannot disconnect connection from current state (" + currentState.toString() + ")");
        }
    }

    /**
     * Begins the authentication procedure.
     *
     * This entails checking the username and password, and the like.
     */
    public void beginAuthorization() {
        //begin authorization procedure
        internalThread = new ConnectThread();
        internalThread.start();
    }

    /**
     * Assigns a task for this resource to run.
     *
     * @param taskToAssign The task to assign to this resource
     * @return true if the task is successfully assigned, false otherwise
     */
    public boolean assignTask(Task taskToAssign) {
        if (currentState == ResourceState.READY) {
            /* note that task assignment is done in its own thread.  this is because
            assigning a task can involve sending a sizeable amount of data, and
            this operation shouldn't block for the entire duration
             */
            /*
            //first, stop the internal thread (at this point, it will always be the keepalive thread)
            shouldPerformKeepAlive = false;
            internalThread.interrupt();
            internalThread.join();
             */
            currentTask = taskToAssign;

            setState(ResourceState.TASK_ASSIGNED);

            internalThread = new TaskSendThread();
            internalThread.start();

            // Start timeout thread
            TaskTimeoutThread = new TaskTimeoutThread();
            TaskTimeoutThread.start();

            return true;
        } else {
            return false;
        }
    }

    /**
     * Communicates with the client to get the idle statistics.
     */
    public void getIdleStats() {
        synchronized (currentState) {
            // Check that the resource is ready and that the client has stats
            if (currentState == ResourceState.READY && clientHasIdleStats) {
                try {
                    // Set new state
                    setState(ResourceState.SENDING_IDLE_STATS);

                    // Inform listeners that this resource is no longer ready
                    for (ResourceEventListener listener : registeredListeners) {
                        listener.resourceNoLongerReady(new ResourceEvent(this));
                    }

                    // Send command to send back stats
                    connectionToResource.send(CommandToClientFromServer.SEND_IDLE_STATS);

                    // Indicate that there are no more idle stats
                    clientHasIdleStats = false;
                } catch (ConnectionToRemoteHostException connectionToRemoteHostException) {
                    Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, connectionToRemoteHostException);
                    setState(ResourceState.TROUBLED);
                }
            }
        }
    }

    /**
     * If a task is running, forces it to terminate.
     */
    public void cancelTask() {
        //this operation only has an effect on RUNNING tasks
        if (currentTask != null && currentTask.getState() == TaskState.RUNNING) {
            // Interrupt TaskSend thread
            if (internalThread != null && internalThread.isAlive()) {
                try {
                    internalThread.interrupt();
                    internalThread.join();
                } catch (InterruptedException ie) {
                    Logger.getLogger(ServerSideResource.class.getName()).log(Level.FINER, "Interrupted while waiting for internal thread to exit.", ie);
                }
            }

            // Send the termination request to the client
            try {
                connectionToResource.send(CommandToClientFromServer.CANCEL_TASK);
            } catch (ConnectionToRemoteHostException ex) {
                Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, ex);
                setState(ResourceState.TROUBLED);
            }
        }
    }

    /**
     * Returns the currently assigned task and disassocates it from this Resource.
     *
     * If the task is in the process of running, or no task is assigned,
     * this operation returns an error.
     */
    public Task takeCurrentTask() throws ResourceException {
        if (currentTask != null) {
            if (currentTask.getState() != TaskState.RUNNING) {
                Task returnValue = currentTask;
                currentTask = null;

                // Wait for timeout thread
                if (TaskTimeoutThread.isAlive()) {
                    try {
                        TaskTimeoutThread.interrupt();
                        TaskTimeoutThread.join();
                        TaskTimeoutThread = null;
                    } catch (InterruptedException e) {
                    }
                }

                return returnValue;
            } else {
                throw new ResourceException("Tasks cannot be removed while running.");
            }
        } else {
            throw new ResourceException("Cannot remove nonexistant task");
        }
    }

    /**
     * Add the specified event listener to the list of registered listeners, thus
     * allowing it to be notified of ResourceEvents.
     *
     * @param listenerToAdd The listener to remove from the list of registered listeners.
     */
    public void addResourceEventListener(ResourceEventListener listenerToAdd) {
        registeredListeners.add(listenerToAdd);
    }

    /**
     * Remove the specified event listener from the list of registered listeners, thus
     * no longer allowing it to be notified of ResourceEvents.
     *
     * @param listenerToRemove The listener to remove from the list of registered listeners.
     */
    public void removeResourceEventListener(ResourceEventListener listenerToRemove) {
        registeredListeners.remove(listenerToRemove);
    }

    /**
     * Returns the current state of the Resource
     *
     * @return the current state of the resource
     */
    public ResourceState getState() {
        return currentState;
    }

    /**
     * Returns the id number of this Resource
     * @return the resource's id number
     */
    public int getResourceID() {
        return resourceID;
    }

    /**
     * Returns the host name of this Resource
     * @return
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the client key
     * @return Key for the client in the connecting client map
     */
    public int getClientKey() {
        return clientKey;
    }

    /**
     * Returns the client map password
     * @return Password for the client in the connecting client map
     */
    public int getClientPassword() {
        return clientPswd;
    }

    /**
     * Sets the ClientInfo object associated with this object
     * @param client Information about this client
     */
    protected void setClientInfo(ClientInfo client) {
        this.client = client;
    }

    /**
     * Sets the connection for client commands
     * @param con Client command connection
     */
    protected void setClientCommandConnection(ConnectionToRemoteHost con) {
        // Set connection
        commandConnectionToResource = con;

        // Add event listener
        commandConnectionToResource.addRemoteHostEventListener(this);

        // Tell the client that the command connection was established.
        try {
            connectionToResource.send(CommandToClientFromServer.CLIENT_CONNECTION_ESTABLISHED);
        } catch (ConnectionToRemoteHostException ex) {
            Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, ex);
            setState(ResourceState.TROUBLED);
        }
    }

    /**
     * Returns the ClientInfo object associated with this object
     * @return Information about this client
     */
    protected ClientInfo getClientInfo() {
        return client;
    }

    /**
     * Returns the ip address, as a string, of this Resource
     * @return the ip address of the resource as a string
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Returns the current task assigned to this resource
     *
     * @return the current task assigned to this resource
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * Returns whether the resource has idle statistics to report
     *
     * @return True if the client has idle stats.
     */
    public boolean doesClientHaveIdleStats() {
        return clientHasIdleStats;
    }

    /**
     * Examines the provided username and password variables, checking if they
     * are correct.
     *
     * @param username the username to check
     * @param password the password to check
     * @return true if the values are correct
     *          false if the values are incorrect
     */
    protected boolean checkAuthentication(String username, String password) {
        boolean usernameIsCorrect = false;
        boolean passwordIsCorrect = false;

        if (correctUsername == null || correctPassword == null) {
            if (correctUsername == null && username == null) {
            }

            if (correctPassword == null && password == null) {
                passwordIsCorrect = true;
            }
        } else {
            usernameIsCorrect = correctUsername.equals(username);
            passwordIsCorrect = correctPassword.equals(password);
        }

        return usernameIsCorrect && passwordIsCorrect;
    }

    /**
     * Sets a task id that the resource should avoid if possible
     * @param taskId Id of task that should be avoided if possible
     */
    protected void setTaskToAvoid(int taskId) {
        // Add to tree
        tasksToAvoid.add(taskId);
    }

    /**
     * Checks if the resource should avoid a task id
     * @param taskId Id of task that should be checked
     */
    public boolean shouldAvoidTask(int taskId) {
        return tasksToAvoid.contains(taskId);
    }

    /**
     * Notify all registered listeners of state changes.
     * @param newState the new state to set this resource to
     */
    protected void setState(ResourceState newState) {
        if (currentState != newState) {
            currentState = newState;
            fireStateChanged();
            /*
            //if we're changing to the READY state, start the keepalive thread.
            //TODO: make this a little less hack-ish
            if(currentState == ResourceState.READY) {
            startKeepAlive();
            }
             */
        }
    }

    /**
     * Notify all registered listeners of state changes.
     */
    protected void fireStateChanged() {
        for (ResourceEventListener listener : registeredListeners) {
            listener.resourceStateChanged(new ResourceEvent(this, currentState));
        }
    }

    /**
     * React to connection state changes properly.  Uses a switch case construct to
     * achieve this.
     *
     * @param e The event raised.
     */
    public void connectionToRemoteHostStateChanged(ConnectionToRemoteHostEvent e) {
        ConnectionToRemoteHostState newState = e.getState();

        switch (newState) {
            case NOT_CONNECTED:
                setState(ResourceState.NOT_CONNECTED);
                break;
            case ERROR_CONNECTING:
                setState(ResourceState.TROUBLED);
                break;
            case COMMUNICATION_ERROR:
                setState(ResourceState.TROUBLED);
                break;
            case DISCONNECTING:
                setState(ResourceState.DISCONNECTING);
                break;
        }

    }

    /**
     * Reacts to received messages properly.
     *
     * @param e The event raised.
     */
    public void messageReceivedFromRemoteHost(ConnectionToRemoteHostEvent e) {
        // Get message
        Object message = e.getObject();
        timeLastMessageReceived = System.currentTimeMillis();

        //System.out.println("Received message \"" + message + "\" from client.");

        // Determine if the message is on the primary connection or the client command connection
        if (e.getSource() == connectionToResource) {
            try {
                //Check if the message should be interpreted as a command or as a text message
                if (awaitingCommand) {
                    try {
                        CommandToServerFromClient command = (CommandToServerFromClient) message;

                        switch (command) {
                            case PING:
                                connectionToResource.send(CommandToClientFromServer.PING_ACKNOWLEDGED);
                                break;
                            case PING_ACKNOWLEDGED:
                                if (shouldPerformKeepAlive == true) {
                                    internalThread.interrupt();
                                }
                                break;
                            case STATE_CHANGED:
                                //wait for the new state message, coming next
                                awaitingCommand = false;
                                currentCommand = CommandToServerFromClient.STATE_CHANGED;
                                break;
                            case INVALID_COMMAND:
                                awaitingCommand = true;
                                currentCommand = null;
                                Logger.getLogger(ServerSideResource.class.getName()).log(Level.INFO, "Invalid command error.");
                                break;
                            default:
                                //Different states expect different responses, so react accordingly
                                switch (currentState) {
                                    case AUTHORIZING:
                                        //there are the two possible commands we wait for when authorizing
                                        switch (command) {
                                            case CLIENT_KEY:
                                            case USERNAME:
                                            case PASSWORD:
                                                currentCommand = command;
                                                awaitingCommand = false;
                                                break;
                                            default:
                                                connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                        }

                                        break;
                                    case READY:
                                        //nothing here yet
                                        break;
                                    case TASK_ASSIGNED:
                                        switch (command) {
                                            case TRANSFER_FILE_NOT_MODIFIED:
                                                flags |= FILE_TRANSFER_NOT_MODIFIED;

                                                // Interrupt internal thread to stop waiting
                                                internalThread.interrupt();
                                                break;
                                            case TRANSFER_FILE_ACK:
                                                flags |= FILE_TRANSFER_ACK;

                                                // Interrupt internal thread to stop waiting
                                                internalThread.interrupt();
                                                client.setLastTime(System.currentTimeMillis());
                                                break;
                                            default:
                                                connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                                break;
                                        }
                                        break;
                                    case FINISHING_TASK:
                                        if (command == CommandToServerFromClient.SENDING_TASK_RESULTS) {
                                            //wait for the task's state
                                            awaitingCommand = false;
                                            currentCommand = command;
                                            client.setLastTime(System.currentTimeMillis()-client.getLastTime());
                                        } else {
                                            connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                        }
                                        break;
                                    case SENDING_IDLE_STATS:
                                        if (command == CommandToServerFromClient.SENDING_IDLE_TIME || command == CommandToServerFromClient.SENDING_NONIDLE_TIME) {
                                            // Wait for the time to be sent
                                            awaitingCommand = false;
                                            currentCommand = command;
                                        } else {
                                            connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                        }
                                        break;
                                }
                        }
                    } catch (ClassCastException ex) {
                        connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                    }
                } else {
                    //again, first process state-independent commands
                    if (currentCommand == CommandToServerFromClient.STATE_CHANGED) {
                        try {
                            ResourceState newState = (ResourceState) message;

                            // Handle the two interesting task-related states
                            switch (newState) {
                                case TASK_TERMINATED:
                                    currentTask.setState(TaskState.TERMINATED);

                                    // Try to avoid being assigned this task again.
                                    // Note: It doesn't matter if it was cancelled because another
                                    // resource completed it.  That means it will not be assigned
                                    // anyway.
                                    setTaskToAvoid(currentTask.getTaskID());
                                    break;
                                case TASK_ENDED_ABNORMALLY:
                                    currentTask.setState(TaskState.TROUBLED);

                                    // Try to avoid being assigned this task again.
                                    // Note: It doesn't matter if it was cancelled because another
                                    // resource completed it.  That means it will not be assigned
                                    // anyway.
                                    setTaskToAvoid(currentTask.getTaskID());
                                    break;
                            }

                            // Interrupt TaskSend thread
                            if ((newState == ResourceState.TASK_TERMINATED || newState == ResourceState.TASK_ENDED_ABNORMALLY)
                                    && internalThread != null && internalThread.isAlive()) {
                                try {
                                    internalThread.interrupt();
                                    internalThread.join();
                                } catch (InterruptedException ie) {
                                    Logger.getLogger(ServerSideResource.class.getName()).log(Level.FINER, "Interrupted while waiting for internal thread to exit.", ie);
                                }
                            }

                            //synchronize the states
                            setState(newState);

                        } catch (ClassCastException ex) {
                            connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                        }

                        awaitingCommand = true;
                        currentCommand = CommandToServerFromClient.NONE;
                    } else {
                        switch (currentState) {
                            case AUTHORIZING:
                                switch (currentCommand) {
                                    case CLIENT_KEY:
                                        // Check if we have the key already
                                        if (clientKey == 0) {
                                            clientKey = (Integer) message;
                                        } // If so, get password
                                        else {
                                            clientPswd = (Integer) message;

                                            // Indicate that we are waiting for another command
                                            awaitingCommand = true;
                                            currentCommand = CommandToServerFromClient.NONE;
                                        }
                                        break;
                                    case USERNAME:
                                        providedUsername = (String) message;
                                        hasProvidedUsername = true;
                                        awaitingCommand = true;
                                        currentCommand = CommandToServerFromClient.NONE;
                                        break;
                                    case PASSWORD:
                                        providedPassword = (String) message;
                                        hasProvidedPassword = true;
                                        awaitingCommand = true;
                                        currentCommand = CommandToServerFromClient.NONE;
                                        break;
                                }

                                //check if both the usename and password are set.
                                //if so, interrupt the running thread so it can react accordingly
                                if (providedUsername != null && providedPassword != null) {
                                    internalThread.interrupt();
                                }

                                break;
                            case FINISHING_TASK:
                                switch (currentCommand) {
                                    case SENDING_TASK_RESULTS:
                                        try {
                                            currentTask.setResults((Serializable) message);
                                            currentTask.setState(TaskState.COMPLETED);

                                            setState(ResourceState.TASK_COMPLETED);
                                        } catch (ClassCastException ex) {
                                            connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                        }
                                        break;
                                    default:
                                        connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                }
                                awaitingCommand = true;
                                currentCommand = CommandToServerFromClient.NONE;
                                break;
                            case SENDING_IDLE_STATS:
                                // Get time
                                try {
                                    if (currentCommand == CommandToServerFromClient.SENDING_IDLE_TIME || currentCommand == CommandToServerFromClient.SENDING_NONIDLE_TIME) {
                                        client.setIdleStatus((Calendar) message, currentCommand == CommandToServerFromClient.SENDING_IDLE_TIME);
                                    } else {
                                        connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                    }
                                } catch (ClassCastException ex) {
                                    connectionToResource.send(CommandToClientFromServer.INVALID_COMMAND);
                                }

                                // Wait for next command
                                awaitingCommand = true;
                                currentCommand = CommandToServerFromClient.NONE;
                                break;
                            default:
                                awaitingCommand = true;
                                currentCommand = CommandToServerFromClient.NONE;
                                break;
                        }
                    }
                }

            } catch (ConnectionToRemoteHostException connectionToRemoteHostException) {
                Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, connectionToRemoteHostException);
                setState(ResourceState.TROUBLED);
            }
        } // Command was received on client command connection
        else {
            try {
                // Cast message to a command
                CommandToServerFromClient command = (CommandToServerFromClient) message;

                // Determine command
                switch (command) {
                    // Client has idle stats to report to server
                    case IDLE_STATS_AVAILABLE:
                        clientHasIdleStats = true;

                        // If the resource is ready, get stats now
                        if (currentState == ResourceState.READY) {
                            getIdleStats();
                        }

                        break;
                }
            } catch (ClassCastException ex) {
            }
        }
    }

    /* keepalive disabled until it can be made to work properly
    private void startKeepAlive() {
    if(!internalThread.getClass().getName().equals("KeepAliveThread")) {
    if(internalThread != null) {
    internalThread.interrupt();

    //make sure the internal thread is gone before replacing it
    try {
    internalThread.join();
    } catch (InterruptedException ex) {}
    }

    shouldPerformKeepAlive = true;
    internalThread = new KeepAliveThread();
    internalThread.setDaemon(true);
    internalThread.start();
    }
    }
     */
    /**
     * Thread responsible for handling connections to the remote host.  Primarily indended to handle authorization
     * tasks
     */
    protected class ConnectThread extends Thread {

        int numberOfAttempts;

        public ConnectThread() {
            super();

            this.setName("Resource #" + resourceID + " connection thread");
        }

        /**
         * Thread responsible for handling and negotiation with the resource itself
         */
        @Override
        public void run() {
            //if a connection is initiated, proceed.  otherwise, the thread has nothing left to do.
            if (connectionToResource.getState() == ConnectionToRemoteHostState.CONNECTED) {
                try {
                    setState(ResourceState.AUTHORIZING);

                    int numAttempts = 0;

                    //ask the client to send credentials
                    connectionToResource.send(CommandToClientFromServer.AUTHORIZATION_REQUEST);
                    awaitingCommand = true;

                    /* Continue sleeping/being interrupted until either numAttempts gets too high
                     * or the password and username are both set, and we're not disconnecting.
                     * a do while loop is used here because there's no point in checking before anything is done */
                    do {
                        //now put this thread to sleep; we will trigger its interrupt if anything noteworthy occurs
                        try {
                            sleep(DEFAULT_WAIT_TIME);
                        } catch (InterruptedException ex) {
                        }
                        //TODO: not sure if it's kosher to use interrupts like this

                        ++numAttempts;
                    } while ((!hasProvidedUsername || !hasProvidedPassword) && numAttempts < DEFAULT_NUMBER_OF_ATTEMPTS && currentState != ResourceState.DISCONNECTING);

                    //don't even bother to do the authentication check if we're disconnecting
                    if (currentState != ResourceState.DISCONNECTING && checkAuthentication(providedUsername, providedPassword)) {
                        //send the client its ID number
                        connectionToResource.send(CommandToClientFromServer.SENDING_RESOURCE_ID);
                        connectionToResource.send(resourceID);

                        // Tell the client that it authorized successfully
                        connectionToResource.send(CommandToClientFromServer.AUTHORIZATION_SUCCEEDED);
                        /*
                        // Create client command connection
                        try
                        {
                        // Open server socket (on random port) for client command connection
                        ServerSocket csSocket = new ServerSocket(0);

                        // Get and send socket's port number
                        connectionToResource.send(csSocket.getLocalPort());

                        commandConnectionToResource.waitForConnectionFromRemoteHost(csSocket);
                        csSocket.close();
                        }
                        catch (IOException e)
                        {
                        System.out.println("Failed: " + e);
                        }
                         */

                    } else {
                        connectionToResource.send(CommandToClientFromServer.AUTHORIZATION_FAILED);
                    }


                } catch (ConnectionToRemoteHostException ex) {
                    Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending authorization request", ex);
                    setState(ResourceState.ERROR_CONNECTING);
                }
            } else {
                setState(ResourceState.ERROR_CONNECTING);
            }
        }
    }

    /**
     * Thread responsible for checking that the client's still there.
     *
     * Does nothing more than occaisionally check that it has received a message from
     * the client recently.  If it hasn't, it sends a PING to ensure the client's
     * still alive.
     *
     * DISABLED UNTIL IT CAN BE MADE TO FUNCTION SATISFACTORILY
     */
//    protected class KeepAliveThread extends Thread {
//        int numberOfAttempts;
//        
//        public KeepAliveThread() {
//            super();
//            
//            this.setName("Resource #" + resourceID + " connection keep alive thread");
//        }
//        
//        @Override
//        public void run() {        
//            int numAttempts = 0;
//            
//            while(shouldPerformKeepAlive) {
//                //first, wait for the designated time delay
//                try {
//                    sleep(DEFAULT_KEEPALIVE_INTERVAL);
//                    
//                    /* if the time of the last message was too long ago, send a message to the 
//                     * client to ensure that it's still alive */
//                    if(timeLastMessageReceived > System.currentTimeMillis() - DEFAULT_KEEPALIVE_TIMEOUT || timeLastMessageReceived == -1) {
//                        numAttempts = 0;
//                    } else {
//                        /*send the ping; if the client's there, it will send a response, and on the next
//                          loop the response will be reflected in the timeLastMessageReceived value */
//                        connectionToResource.send(CommandToClientFromServer.PING);
//                        
//                        numAttempts++;
//                    }
//                    
//                    if(numAttempts > DEFAULT_NUMBER_OF_ATTEMPTS) {
//                        setState(ResourceState.TROUBLED);
//                        shouldPerformKeepAlive = false;
//                    }
//                    
//                } catch (InterruptedException ex) {
//                    shouldPerformKeepAlive = false;
//                } catch (ConnectionToRemoteHostException ex) {
//                    setState(ResourceState.TROUBLED); //TODO: add a more descriptive state
//                    shouldPerformKeepAlive = false;
//                }
//            }
//        }
//    }
    /**
     * This thread is responsible for sending a new task to the client, at which
     * point it terminates.
     *
     * This thread is necessary so that, if the task contains a large amount of
     * data (eg. a file) the send operation won't block execution of whoever called
     * the assignTask() method.
     */
    protected class TaskSendThread extends Thread {

        @Override
        public void run() {
            try {
                synchronized (connectionToResource) {
                    if (currentTask instanceof FileTransferTask) {
                        FileTransferTask fileTransferTask = (FileTransferTask) currentTask;
                        // Start file transfer
                        FileTransfer fileInfo;
                        while (fileTransferTask.getNumFilesLeft() > 0 && (fileInfo = fileTransferTask.getNextFile()) != null) {
                            // Clear flags that start of cancel transfer
                            flags &= ~(FILE_TRANSFER_NOT_MODIFIED | FILE_TRANSFER_ACK);

                            // Start file transfer commands
                            connectionToResource.send(CommandToClientFromServer.TRANSFER);
                            //connectionToResource.send(fileInfo.getRemoteFilename());

                            try {
                                // Send file size
                                File localFile = new File(fileInfo.getLocalFilename());
                                long localFileSize = localFile.length();

                                // Send file information
                                FileTransferInfoToClient transferInfo = new FileTransferInfoToClient(
                                        fileInfo.getRemoteFilename(), // Remote filename
                                        localFileSize, // Filesize
                                        fileInfo.getRemoteDirectory(), // Remote directory in which to place file
                                        localFile.lastModified(), // Last modified timestamp of file
                                        fileInfo.isExecutable() // Is the file an executable
                                        );
                                connectionToResource.send(transferInfo);

                                // Wait
                                while (currentState == ResourceState.TASK_ASSIGNED && (flags & FILE_TRANSFER_NOT_MODIFIED) == 0 && (flags & FILE_TRANSFER_ACK) == 0) {
                                    try {
                                        sleep(DEFAULT_WAIT_TIME);
                                    } catch (InterruptedException ie) {
                                        // End thread if the task is no longer assigned
                                        if (currentTask.getState() != TaskState.READY) {
                                            //System.out.println("Thread exiting prematurely.");
                                            return;
                                        }
                                    }
                                }

                                // Send file contents
                                if ((flags & FILE_TRANSFER_ACK) != 0) {
                                    // Get checksum
                                    MessageDigest md5 = null;
                                    try {
                                        md5 = MessageDigest.getInstance("md5");

                                        // Read bytes
                                        byte[] fileContents = new byte[4096];
                                        int bytesRead, fileOffset = 0;
                                        FileInputStream localFileInputStream = new FileInputStream(localFile);
                                        while (fileOffset < localFileSize && (bytesRead = localFileInputStream.read(fileContents)) != -1) {
                                            // Update message digest
                                            md5.update(fileContents, 0, bytesRead);

                                            // Send contents
                                            connectionToResource.send(fileContents);
                                            fileContents = new byte[4096];

                                            // Add to offset
                                            fileOffset += bytesRead;
                                        }
                                        localFileInputStream.close();

                                        // Send digest
                                        connectionToResource.send(md5.digest());
                                    } catch (NoSuchAlgorithmException checksumE) {
                                        cancelTask();
                                    }
                                }
                            } catch (IOException e) {
                                Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending file to resource #" + resourceID, e);
                                setState(ResourceState.TROUBLED);
                            }
                        }
                    }

                    connectionToResource.send(CommandToClientFromServer.SENDING_NEW_TASK);
                    connectionToResource.send(currentTask);
                }
            } catch (ConnectionToRemoteHostException ex) {
                Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, ex);
                setState(ResourceState.TROUBLED);
            }
        }
    }

    /**
     * This thread is responsible for checking that the task completes the task in
     * the specified timeout
     */
    protected class TaskTimeoutThread extends Thread {

        @Override
        public void run() {
            try {
                // Check that there is a current task
                if (currentTask != null) {
                    // Get task timeout
                    int timeout = currentTask.getTimeout();
                    sleep(timeout);

                    // This thread should be interrupted and never read here if the task
                    // completed
                    //System.out.println("Task exceeded timeout of " + timeout/1000.0 + " seconds.");

                    // Inform listeners
                    for (ResourceEventListener listener : registeredListeners) {
                        listener.resourceTaskTimedOut(new ResourceEvent(ServerSideResource.this));
                    }
                }
            } catch (InterruptedException ie) {
            }
            /*catch (ConnectionToRemoteHostException ex)
            {
            Logger.getLogger(ServerSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, ex);
            setState(ResourceState.TROUBLED);
            }*/
        }
    }
}
