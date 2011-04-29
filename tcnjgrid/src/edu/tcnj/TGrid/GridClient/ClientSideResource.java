/*
 * Resource.java
 * 
 * Created on Sep 29, 2007, 7:32:35 PM
 * 
 */
/*
TODO: Determine where to monitor CPU
// Return that the cpu is idle if the idle percent is greater than a predefined constant
connectionToServer.send(CpuMonitor.getIdleCpuPercent(CPU_MONITOR_INTERVAL) > CPU_IDLE_PERCENT);
 */
package edu.tcnj.TGrid.GridClient;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.States.ResourceState;
import edu.tcnj.TGrid.Events.ConnectionToRemoteHostEvent;
import edu.tcnj.TGrid.Events.ConnectionToRemoteHostEventListener;
import edu.tcnj.TGrid.Exceptions.ConnectionToRemoteHostException;
import edu.tcnj.TGrid.GridClient.Events.ResourceEvent;
import edu.tcnj.TGrid.GridClient.Events.ResourceEventListener;
import edu.tcnj.TGrid.Exceptions.ResourceException;

import edu.tcnj.TGrid.ConnectionToRemoteHost;
import edu.tcnj.TGrid.States.ConnectionToRemoteHostState;
import edu.tcnj.TGrid.CommandToServerFromClient;
import edu.tcnj.TGrid.CommandToClientFromServer;

import edu.tcnj.TGrid.TransferFileRemoteDirectory;
import edu.tcnj.TGrid.FileTransferInfoToClient;

import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.Task;
import java.net.InetAddress;

import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The client side Resource class is responsible for communicating with the
 * server, definining the behavior of the resource, and notifying registered 
 * listeners when noteworthy events occur.
 * 
 * @author Dan
 */
public class ClientSideResource implements ConnectionToRemoteHostEventListener, TaskEventListener {

    /**
     * The default time for actions to wait before they time out in milliseconds.
     */
    protected static final int DEFAULT_WAIT_TIME = 1000;
    /**
     * The default time between pings to make sure the server is still there, 
     * in milliseconds.
     */
    protected static final int DEFAULT_KEEPALIVE_INTERVAL = 5000;
    /**
     * The default number of times an action should be retried before giving up
     */
    protected static final int DEFAULT_NUMBER_OF_ATTEMPTS = 3;
    /**
     * Maximum value for CPU idle percent in which the CPU is considered not
     * idle.  Anything above this value is considered idle.
     */
    protected static final int CPU_IDLE_PERCENT = 95;
    /**
     * Amount of time, in seconds, to sample data for CPU usage
     */
    protected static final int CPU_MONITOR_INTERVAL = 10;
    /**
     * Amount of time, in minutes, between CPU monitorings
     */
    protected static final int CPU_MONITOR_SLEEP = 1;
    /**
     * The unique identification number of this Resource
     */
    protected int resourceID;
    
    protected int numFailed;
    /**
     * The Operating System
     * spring 2011
     */
    protected String nameOS = System.getProperty("os.name");
    /**
     * Total Ram that is available to the Java Virtual Machine
     * (not total available ram of system!)
     * spring 2011
     */
    protected String totalRAM = Long.toString(Runtime.getRuntime().totalMemory());
    /**
     * Total Free Ram currently able to be used by the Java virtual machine
     * spring 2011
     */
    protected String freeRAM = Long.toString(Runtime.getRuntime().freeMemory());
    /**
     * Number of processors on the resource
     * spring 2011
     */
    protected String freeDisk;
    protected String cpusAvailable = Integer.toString(Runtime.getRuntime().availableProcessors());
    /**
     * A reliability and performance score for this Resource, calculated 
     * based on several factors.  Used primarily for comparisons.
     * Based on 0 is not usable and everything up is better
     * Spring 2011
     */
    protected int resourceScore;
    /**
     * The username to be sent during authentication
     *
     */
    private String username;
    /**
     * The password to be sent during authentication
     *
     */
    private String password;
    /**
     * Key for the client in the server's connecting client map
     *
     */
    private int clientKey;
    /**
     * Password for the client in the server's client map
     */
    private int clientPswd;
    /**
     * Holds any argument that a commands may have 
     */
    private HashMap<String, Object> currentCommandArgs;
    /**
     * Used to determine how to react to received messages.
     * 
     * If awaitingCommand is true, the message will be interpereted as a command.
     * Otherwise, the message will be interpereted as text.
     */
    private volatile boolean awaitingCommand = true;
    /**
     * Stores the current command being performed, if any.
     * Used to determine how to react to received text messages. (non-commands)
     * 
     * If awaitingCommand is false, any message received will need to be associated with
     * whatever command preceeded it. 
     */
    private CommandToClientFromServer currentCommand;
    /**
     * The current state of this Resource
     */
    private volatile ResourceState currentState = ResourceState.NOT_CONNECTED;
    /**
     * Stores a list of times that the client is idle.  This is emptied once
     * they are reported to the server.     
     */
    private LinkedBlockingQueue<Calendar> idleTimes = new LinkedBlockingQueue<Calendar>();
    /**
     * Stores a list of times that the client is not idle.  This is emptied once
     * they are reported to the server.     
     */
    private LinkedBlockingQueue<Calendar> nonidleTimes = new LinkedBlockingQueue<Calendar>();
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    private HashSet<ResourceEventListener> registeredListeners = new HashSet<ResourceEventListener>();
    /**
     * Represents the connection to the server itself
     */
    private ConnectionToRemoteHost connectionToServer;
    /**
     * Represents client command connection to the server.  This connection is
     * reserved for commands sent from the client to the server.     
     */
    private ConnectionToRemoteHost commandConnectionToServer;
    /**
     * Indicates whether or not the keep alive thread should continue to run
     */
    private volatile boolean shouldPerformKeepAlive = false;
    /**
     * The Task currently assigned to this resource
     */
    private Task currentTask;
    /**
     * Represents a polymorphic handle to whichever one of the internal threads happens to be executing.
     * 
     * Declared as a single reference due to the fact that only one of the internal thread types should
     * ever be running at a given time.
     */
    private Thread internalThread;

    /**
     * Creates a new instance of Resource, with the specified server address.
     * 
     * @param port              The port to connect to on the server.
     * @param username          The username used to authenticate.
     * @param password          The password used to authenticate.
     * @param addressOfServer    The address of the server itself, represented by an
     *                             <code>InetAddress</code>
     * @param clientKey Key for the client in the server's connecting client map
     * @param clientPswd Password for the client in the server's client map     
     */
    public ClientSideResource(InetAddress addressOfServer, int port, String username, String password, int clientKey, int clientPswd) {
        this.connectionToServer = new ConnectionToRemoteHost(addressOfServer, port);
        this.commandConnectionToServer = new ConnectionToRemoteHost(addressOfServer, port + 1);
        this.username = username;
        this.password = password;
        this.clientKey = clientKey;
        this.clientPswd = clientPswd;

        //This is because there really is not a good way
        //to get disk space cross platform with java
        //so we have to do some guessing
        File[] roots = File.listRoots();
        long space = 0;
        //loop through and look for the most free space
        //windows can have many drives and act funny
        for (int i = 0; i < roots.length; i++) {
            if(roots[i].getUsableSpace() > space){
                //assume that the largest free space is the
                //drive we want
                space = roots[i].getUsableSpace();
            }
        }
        
        freeDisk = Long.toString(space);

        connectionToServer.addRemoteHostEventListener(this);
        //commandConnectionToServer.addRemoteHostEventListener(this);
    }

    /*
     * Returns the OS of the client, the total Ram to the jvm, the free Ram to jvm
     * and returns it as a string
     *
     * Spring 2011
     */
    public String getSpecs() {
        return "OS version " + nameOS + "\nTotal Ram: " + totalRAM + 
                "\nFree Ram: " + freeRAM + "\nFree Space: " + freeDisk +
                "CPUs Available: " + cpusAvailable;
    }

    /*
     * Determines the resource score for this client to be returned to
     * the server. This is just guesswork as to what is useful overall
     *
     * Spring 2011
     */
    public void setResourceScore() {
        //compile resource score before sending it and save it
        //Check to see if ram is above a certain level (hardcoded 128)
        resourceScore = 0;
        if (Integer.parseInt(totalRAM) > 128) {
            resourceScore += 10;
        }

        //if there is more cpus available than one add a score
        if (Integer.parseInt(cpusAvailable) > 1) {
            resourceScore += 10;
        }
    }

    /*
     * Returns the resource score
     */
    public int getResourceScore() {

        return resourceScore;
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
                connectionToServer.stopWaitingForConnectionFromRemoteHost();
                commandConnectionToServer.stopWaitingForConnectionFromRemoteHost();
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
        if (!connectionToServer.hasRemoteAddress()) {
            throw new ResourceException("Cannot request a connection to an unspecified address");
        }

        if (currentState == ResourceState.NOT_CONNECTED) {
            setState(ResourceState.CONNECTING);

            awaitingCommand = true;

            try {
                connectionToServer.connectToRemoteHost();
            } catch (ConnectionToRemoteHostException ex) {
                setState(ResourceState.ERROR_CONNECTING);
                awaitingCommand = false;

                throw new ResourceException(ex.getMessage());
            }

            //begin authorization procedure
            internalThread = new ConnectThread();
            internalThread.start();

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

            //if any, try to shut down an internal thread
            if (internalThread != null && internalThread.isAlive()) {
                try {
                    internalThread.interrupt();
                    internalThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientSideResource.class.getName()).log(Level.FINER, "Interrupted while waiting for internal thread to exit.", ex);
                }
            }

            try {
                connectionToServer.disconnect();
                commandConnectionToServer.disconnect();
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
     * @return the current state of the Resource
     */
    public ResourceState getState() {
        return currentState;
    }

    /**
     * Changes the resource's state, and notifies appropriate parties of the change.
     * 
     * @param newState the state to set the resource to
     */
    protected synchronized void setState(ResourceState newState) {
        if (currentState != newState) {
            try {
                currentState = newState;

                if (connectionToServer.isConnected()) {
                    //if possible, notify the server of the change
                    if (newState != ResourceState.AUTHORIZING) {
                        //there are problems that arise when the program's state message
                        //conflicts with the authorization reqests
                        //TODO: make this less hackly
                        connectionToServer.send(CommandToServerFromClient.STATE_CHANGED);
                        connectionToServer.send(newState);
                    }
                }

                //notify any registered listeners of the change
                fireStateChanged();
            } catch (ConnectionToRemoteHostException ex) {
                Logger.getLogger(ClientSideResource.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        Object message = e.getObject();
        //System.out.println("Received message \"" + message + "\" from server.");

        try {
            //Check if the message should be interpereted as a command or as a text message
            if (awaitingCommand) {
                System.out.println("Received command \"" + message + "\" from server.");
                //if we're awaiting a command, first convert the message into a CommandToClientFromServer
                try {
                    CommandToClientFromServer command = (CommandToClientFromServer) message;

                    //first process commands that can occur from any state
                    switch (command) {
                        case PING: //in the event of a PING, simply send the response
                            connectionToServer.send(CommandToServerFromClient.PING_ACKNOWLEDGED);
                            break;
                        case PING_ACKNOWLEDGED: //in the event of a PING acknowledgement, interrupt the thread, in case it's waiting
                            if (shouldPerformKeepAlive == true) {
                                internalThread.interrupt();
                            }
                            break;
                        case INVALID_COMMAND:
                            awaitingCommand = true;
                            currentCommand = null;
                            Logger.getLogger(ClientSideResource.class.getName()).log(Level.INFO, "Invalid command error.");
                            break;
                        default: //if it's not a "universal" message, react according to the current state
                            //Different states expect different responses, so react accordingly
                            switch (currentState) {
                                case AUTHORIZING:
                                    //there are the two possible commands we wait for when authorizing
                                    switch (command) {
                                        case AUTHORIZATION_REQUEST:
                                            // Send client info (Must be before username and password since authentication state ends after username and password)
                                            connectionToServer.send(CommandToServerFromClient.CLIENT_KEY);
                                            connectionToServer.send(clientKey);
                                            connectionToServer.send(clientPswd);

                                            //send the authorization info
                                            connectionToServer.send(CommandToServerFromClient.USERNAME);
                                            connectionToServer.send(username);

                                            connectionToServer.send(CommandToServerFromClient.PASSWORD);
                                            connectionToServer.send(password);

                                            awaitingCommand = true;
                                            break;
                                        case SENDING_RESOURCE_ID:
                                            currentCommand = CommandToClientFromServer.SENDING_RESOURCE_ID;
                                            awaitingCommand = false;

                                            break;
                                        case AUTHORIZATION_SUCCEEDED:
                                            setState(ResourceState.AUTHORIZATION_SUCCESS);
                                            setState(ResourceState.AWAITING_CLIENT_COMMAND_CONNECTION);

                                            try {
                                                commandConnectionToServer.connectToRemoteHost();

                                                // Send client key
                                                commandConnectionToServer.send(clientKey);
                                            } catch (ConnectionToRemoteHostException ex) {
                                                setState(ResourceState.TROUBLED);
                                                awaitingCommand = false;
                                            }

                                            awaitingCommand = false;

                                            break;
                                        case AUTHORIZATION_FAILED:
                                            setState(ResourceState.AUTHORIZATION_FAILURE);

                                            awaitingCommand = true;
                                            internalThread.interrupt();

                                            break;
                                        default:
                                            connectionToServer.send(CommandToServerFromClient.INVALID_COMMAND);
                                    }

                                    break;
                                case READY:
                                    switch (command) {
                                        case SENDING_NEW_TASK:
                                            if (currentTask == null) {
                                                awaitingCommand = false; //the Task itself is expected next
                                                currentCommand = command;
                                            } else {
                                                //if there's already a task here...

                                                //TODO: replace this with a "Task queue" of sorts
                                                connectionToServer.send(CommandToServerFromClient.INVALID_COMMAND);
                                            }

                                            break;
                                        case TRANSFER:
                                            currentCommandArgs = new HashMap<String, Object>();
                                            awaitingCommand = false; // File meta data and contents expected next
                                            currentCommand = command;

                                            break;
                                        case SEND_IDLE_STATS:

                                            // Change state
                                            setState(ResourceState.SENDING_IDLE_STATS);

                                            // Synchronize
                                            synchronized (idleTimes) {
                                                synchronized (nonidleTimes) {
                                                    // Send idle times
                                                    Calendar time = null;
                                                    while ((time = idleTimes.poll()) != null) {
                                                        connectionToServer.send(CommandToServerFromClient.SENDING_IDLE_TIME);
                                                        connectionToServer.send(time);
                                                    }

                                                    // Send nonidle times
                                                    while ((time = nonidleTimes.poll()) != null) {
                                                        connectionToServer.send(CommandToServerFromClient.SENDING_NONIDLE_TIME);
                                                        connectionToServer.send(time);
                                                    }
                                                }
                                            }

                                            // Indicate that resource is ready now
                                            setState(ResourceState.READY);
                                            awaitingCommand = true;
                                            currentCommand = null;

                                            break;
                                        default:
                                            connectionToServer.send(CommandToServerFromClient.INVALID_COMMAND);
                                    }
                                    break;
                                case RUNNING_TASK:
                                    switch (command) {
                                        case CANCEL_TASK:
                                            if (currentTask != null) {
                                                currentTask.forceQuit();
                                            }
                                    }
                                    break;

                            }
                    }
                } catch (ClassCastException ex) {
                    connectionToServer.send(CommandToServerFromClient.INVALID_COMMAND);
                }
            } else {
                switch (currentState) {
                    case AUTHORIZING:
                        switch (currentCommand) {
                            case SENDING_RESOURCE_ID:
                                resourceID = (Integer) message;

                                break;
                        }
                        awaitingCommand = true;
                        currentCommand = null;
                        break;
                    case AWAITING_CLIENT_COMMAND_CONNECTION:
                        /*
                        // Get port number
                        try
                        {
                        int tmpPort = (Integer)message;

                        // Create SSH tunnel
                        String [] command = new String[4];
                        command[0] = "./createSshTunnel.sh";						// Command to run
                        command[1] = connectionToServer.getHostName();	// Hostname of server
                        command[2] = ""+tmpPort;// Port number on server
                        command[3] = "56789";	// Get port number to use for SSH tunnel

                        // Run the command
                        Process p = Runtime.getRuntime().exec(command);

                        // Open socket
                        commandConnectionToServer.setPort(56789);
                        commandConnectionToServer.connectToRemoteHost();
                        setState(ResourceState.READY);
                        }
                        catch (Exception ex)
                        {
                        setState(ResourceState.TROUBLED);
                        }
                         */

                        // Determine if the connection was established
                        if (message == CommandToClientFromServer.CLIENT_CONNECTION_ESTABLISHED) {
                            // Start CPU monitor thread
                            CpuMonitorThread cpuThread = new CpuMonitorThread();
                            cpuThread.setDaemon(true);	// Indicate that the thread should die when all other threads die.
                            cpuThread.start();

                            // Indicate that client is ready and waiting for a command
                            awaitingCommand = true;
                            currentCommand = null;
                            setState(ResourceState.READY);
                        } else {
                            setState(ResourceState.TROUBLED);
                        }
                        break;
                    case READY:
                        switch (currentCommand) {
                            case SENDING_NEW_TASK:
                                try {
                                    currentTask = (Task) message;

                                    // Set this as the client resource for the task
                                    currentTask.setParentResource(this);

                                    setState(ResourceState.TASK_ASSIGNED);

                                    currentTask.addTaskEventListener(this);
                                    currentTask.runTask();
                                } catch (ClassCastException ex) {
                                    connectionToServer.send(CommandToServerFromClient.INVALID_COMMAND);
                                }
                                awaitingCommand = true;
                                currentCommand = null;
                                break;
                            case TRANSFER:
                                if (!currentCommandArgs.containsKey("file")) {
                                    // Cast message
                                    FileTransferInfoToClient transferInfo = (FileTransferInfoToClient) message;

                                    // Store file transfer info
                                    currentCommandArgs.put("transferInfo", transferInfo);

                                    // Open file
                                    try {
                                        // Check that the filename does not contain the directory separator
                                        String filename = transferInfo.getRemoteFilename();
                                        File file = null;
                                        if (filename.indexOf(File.separatorChar) == -1) {
                                            // Add directory to filename
                                            filename = getDirectoryPath(transferInfo.getRemoteDirectory()) + filename;

                                            // Open the file and look at last modified date
                                            file = new File(filename);
                                            // Create parent dirs if needed
                                            if (file.getParentFile() != null) {
                                                file.getParentFile().mkdirs();
                                            }
                                            file.createNewFile();
                                            long lastModified = file.lastModified();

                                            // Set new file permissions if the file should be executable
                                            if (transferInfo.isExecutable()) {
                                                // TODO: Try to get this to work on non-Linux platforms
                                                //new FilePermission(file.getPath(), "read,write,execute");
                                                String[] command = {"chmod", "755", file.getPath()};
                                                Process p = Runtime.getRuntime().exec(command);
                                            }

                                            // Check if the file is not empty
                                            if (transferInfo.getFilesize() == 0) {
                                                Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error saving file to resource #" + resourceID + ": File empty.");
                                                setState(ResourceState.TASK_TERMINATED);

                                                // For now, we'll just set the state back to READY, and pretend nothing happened
                                                currentTask = null;
                                                setState(ResourceState.READY);

                                                // Wait for new command
                                                awaitingCommand = true;
                                                currentCommand = null;
                                            } // Check existing modified date and set file last modified date
                                            else if (lastModified == transferInfo.getLastModified()) {
                                                // Indicate that we are waiting for a new command
                                                awaitingCommand = true;
                                                currentCommand = null;
                                                currentState = ResourceState.READY;

                                                // Tell server not to send file
                                                connectionToServer.send(CommandToServerFromClient.TRANSFER_FILE_NOT_MODIFIED);
                                            } else {
                                                try {
                                                    // Create FileOutputStream
                                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                    currentCommandArgs.put("fileOutputStream", fileOutputStream);
                                                } catch (IOException ioe) {
                                                    Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error saving file to resource #" + resourceID, ioe);
                                                    setState(ResourceState.TASK_TERMINATED);

                                                    // For now, we'll just set the state back to READY, and pretend nothing happened
                                                    currentTask = null;
                                                    setState(ResourceState.READY);

                                                    // Wait for new command
                                                    awaitingCommand = true;
                                                    currentCommand = null;
                                                }

                                                // Store last modified date
                                                currentCommandArgs.put("lastModified", transferInfo.getLastModified());

                                                // Store filesize
                                                currentCommandArgs.put("untransferredBytes", transferInfo.getFilesize());

                                                try {
                                                    // Store checksum
                                                    currentCommandArgs.put("md5", MessageDigest.getInstance("md5"));
                                                } catch (NoSuchAlgorithmException checksumE) {
                                                }

                                                // Send back acknowledgement to start transfer
                                                connectionToServer.send(CommandToServerFromClient.TRANSFER_FILE_ACK);
                                            }
                                        } else {
                                            Logger.getLogger(ClientSideResource.class.getName()).log(Level.INFO, "Error saving file to resource #" + resourceID);
                                            setState(ResourceState.TASK_TERMINATED);

                                            // For now, we'll just set the state back to READY, and pretend nothing happened
                                            currentTask = null;
                                            setState(ResourceState.READY);

                                            // Wait for new command
                                            awaitingCommand = true;
                                            currentCommand = null;
                                        }

                                        currentCommandArgs.put("file", file);
                                    } catch (IOException ioe) {
                                        Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error saving file to resource #" + resourceID, ioe);
                                        setState(ResourceState.TROUBLED);
                                    }
                                } else {
                                    // Get contents
                                    byte[] contents = (byte[]) message;

                                    // Get number of untransferred bytes
                                    long untransferredFileBytes = (long) ((Long) currentCommandArgs.get("untransferredBytes"));

                                    // Get message digest
                                    MessageDigest md5 = (MessageDigest) currentCommandArgs.get("md5");

                                    // Check if this is the checksum (Sent after last bytes of file)
                                    if (untransferredFileBytes == 0) {
                                        // Check that the checksum matchs
                                        byte[] digest = md5.digest();
                                        boolean valid = true;
                                        for (int i = 0; valid && i < digest.length; i++) {
                                            valid = (i < contents.length && digest[i] == contents[i]);
                                        }

                                        // Check if it is valid
                                        if (!valid) {
                                            Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "File checksum invaild for resource #" + resourceID);
                                            setState(ResourceState.TASK_TERMINATED);

                                            // For now, we'll just set the state back to READY, and pretend nothing happened
                                            currentTask = null;
                                            setState(ResourceState.READY);
                                        }

                                        // Wait for new command
                                        awaitingCommand = true;
                                        currentCommand = null;
                                    } else {
                                        // Subtract bytes transferred
                                        int contentLength = (untransferredFileBytes < contents.length) ? (int) untransferredFileBytes : contents.length;
                                        untransferredFileBytes -= contentLength;
                                        currentCommandArgs.put("untransferredBytes", untransferredFileBytes);

                                        System.out.println("# Bytes Left: " + untransferredFileBytes);

                                        // Get file writer
                                        FileOutputStream fileOutputStream = (FileOutputStream) currentCommandArgs.get("fileOutputStream");

                                        try {
                                            // Write to file
                                            fileOutputStream.write(contents, 0, contentLength);

                                            // Update message digest
                                            md5.update(contents, 0, contentLength);
                                        } catch (IOException ioe) {
                                            Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error saving file to resource #" + resourceID, ioe);
                                            setState(ResourceState.TROUBLED);
                                        }

                                        // If there are no more bytes left, wait for next command
                                        if (untransferredFileBytes <= 0) {
                                            try {
                                                // Close file
                                                fileOutputStream.close();

                                                // Get file object
                                                File file = (File) currentCommandArgs.get("file");

                                                // Get file transfer information
                                                FileTransferInfoToClient transferInfo = (FileTransferInfoToClient) currentCommandArgs.get("transferInfo");

                                                // Set new modified date
                                                file.setLastModified((long) ((Long) currentCommandArgs.get("lastModified")));
                                            } catch (IOException ioe) {
                                                Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error saving file to resource #" + resourceID, ioe);
                                                setState(ResourceState.TROUBLED);
                                            }
                                        }
                                    }
                                }

                                break;
                            default:
                                awaitingCommand = true;
                                currentCommand = null;
                                break;
                        }
                        break;
                    default:
                        awaitingCommand = true;
                        currentCommand = null;
                        break;
                }
            }

        } catch (ConnectionToRemoteHostException connectionToRemoteHostException) {
            Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, connectionToRemoteHostException);
            setState(ResourceState.TROUBLED);
        }

    }

    /**
     * Returns the paths for predefined directories
     *
     * @param dir Directory constant that caller needs the path for
     *		      
     * @return Path of the given directory
     */
    public String getDirectoryPath(TransferFileRemoteDirectory dir) {
        // Determine path
        String path = new String("");
        switch (dir) {
            case CURRENT_DIR:
                break;
            case DATA_DIR:
                path = "data" + File.separatorChar;
                break;
            case JAVA_CLASS_DIR:
                path = "javaClasses" + File.separatorChar;
                break;
            case EXECUTABLES_DIR:
                path = "executables" + File.separatorChar;
                break;
            case MISC_DIR:
                path = "misc" + File.separatorChar;
                break;
        }
        return path;
    }

    public void TaskStateChanged(TaskEvent e) {
        Task source = (Task) e.getSource();

        try {
            switch (e.getState()) {
                case RUNNING:
                    setState(ResourceState.RUNNING_TASK);
                    break;
                case COMPLETED:
                    synchronized (connectionToServer) {
                        setState(ResourceState.FINISHING_TASK);

                        //first, unregister as a listener
                        //this is important so that this ClientSideResource doesn't try to get serialized along with the task
                        source.removeTaskEventListener(this);

                        connectionToServer.send(CommandToServerFromClient.SENDING_TASK_RESULTS);
                        connectionToServer.send(currentTask.getResults());

                        //TODO: perhaps make this a bit more graceful
                        currentTask = null;

                        //TODO: TASK_COMPLETED should probably be a message, not a state.
                        setState(ResourceState.TASK_COMPLETED);
                        setState(ResourceState.READY);
                    }
                    break;
                case TERMINATED:
                    synchronized (connectionToServer) {
                        setState(ResourceState.TASK_TERMINATED);

                        //TODO: in this state, we should be able to accept two new commands, 
                        //      RESTART_TASK or CLEAR_TASK


                        //TODO: perhaps make this a bit more graceful
                        //currentTask = null;

                        //for now, we'll just set the state back to READY, and pretend nothing happened
                        currentTask = null;
                        setState(ResourceState.READY);
                    }
                    break;
                case TROUBLED:
                    synchronized (connectionToServer) {
                        setState(ResourceState.TASK_ENDED_ABNORMALLY);
                        //TODO: in this state, we should be able to accept two new commands, 
                        //      RESTART_TASK or CLEAR_TASK


                        //TODO: perhaps make this a bit more graceful
                        //currentTask = null;

                        //for now, we'll just set the state back to READY, and pretend nothing happened
                        currentTask = null;
                        setState(ResourceState.READY);
                    }
                    break;
            }

        } catch (ConnectionToRemoteHostException ex) {
            setState(ResourceState.TROUBLED);
            Logger.getLogger(ClientSideResource.class.getName()).log(Level.WARNING, "Error sending message to resource #" + resourceID, ex);
        }
    }

    /**
     * Thread responsible for handling connections to the remote host.
     */
    protected class ConnectThread extends Thread {

        int numberOfAttempts;

        public ConnectThread() {
            super();

            this.setName("Resource #" + resourceID + " connection thread");
        }

        /**
         * Thread responsible for handling initial connection to, and negotiation with, the resource itself
         */
        @Override
        public void run() {
            //if a connection is initiated, proceed.  otherwise, the thread has nothing left to do.
            if (connectionToServer.getState() == ConnectionToRemoteHostState.CONNECTED) {
                setState(ResourceState.AUTHORIZING);

                /* continue putting the thread to sleep until we're no longer "authorizing" */
                do {
                    //now put this thread to sleep; we will trigger its interrupt if anything noteworthy occurs
                    try {
                        sleep(DEFAULT_WAIT_TIME);
                    } catch (InterruptedException ex) {
                    }

                } while (currentState == ResourceState.AUTHORIZING);

            } else {
                setState(ResourceState.ERROR_CONNECTING);
            }
        }
    }

    /**
     * Thread to periodically check idle status of host.
     */
    protected class CpuMonitorThread extends Thread {

        /**
         * Thread responsible periodically monitoring CPU
         */
        @Override
        public void run() {
            try {
                while (true) {
                    // Check that the client is in the ready state
                    if (ClientSideResource.this.getState() == ResourceState.READY) {
                        System.out.println("CPU stats about to be collected.");
                        // Synchronize
                        synchronized (idleTimes) {
                            synchronized (nonidleTimes) {
                                // Check and store idle stats
                                if (CpuMonitor.getIdleCpuPercent(CPU_MONITOR_INTERVAL) > CPU_IDLE_PERCENT) {
                                    idleTimes.add(Calendar.getInstance());
                                } else {
                                    nonidleTimes.add(Calendar.getInstance());
                                }

                                System.out.println("CPU Stats Collected.");

                                // Tell the server we have stats if we haven't already
                                if (idleTimes.size() + nonidleTimes.size() == 1) {
                                    try {
                                        commandConnectionToServer.send(CommandToServerFromClient.IDLE_STATS_AVAILABLE);
                                    } catch (ConnectionToRemoteHostException ex) {
                                        /* Non-fatal Error */
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("CPU stats not collected.");
                    }

                    // Sleep a while
                    sleep(CPU_MONITOR_SLEEP * 60000); // Convert minutes to milliseconds
                }
            } catch (InterruptedException e) {
                System.out.println("CPU stat collection interrupted.");
            }
        }
    }
}
