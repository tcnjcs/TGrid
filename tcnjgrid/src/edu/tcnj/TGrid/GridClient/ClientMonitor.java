/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid.GridClient;

import edu.tcnj.TGrid.Exceptions.ResourceException;
import edu.tcnj.TGrid.GridClient.Events.ResourceEvent;
import edu.tcnj.TGrid.GridClient.Events.ResourceEventListener;
import edu.tcnj.TGrid.States.ResourceState;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ClientMonitor class is designed to simply manage a single Resource object.
 * 
 * @author Dan
 */
public class ClientMonitor implements ResourceEventListener {
    /**
     * The representation of the computer on which the client program is running.
     */
    private ClientSideResource resource;
    
    /**
     * The address of the server to connect to
     */
    private InetAddress addressOfServer;
    
    /**
     * The port of the server to connect on
     */
    private int portOfServer;
    
    /**
     * The usename to be provided to the server
     */
    private String username;
    
    /**
     * The password to be provided to the server
     */
    private String password;
    
    /**
     * Creates a new instance of ClientMonitor
     * 
     * @param addressOfServer The address of the server to connect to
     * @param portOfServer The port of the server to connect on
     * @param username The usename to be provided to the server
     * @param password The password to be provided to the server
     * @param clientKey Key for the client in the server's connecting client map
     * @param clientPswd Password for the client in the server's client map
     */
    public ClientMonitor(InetAddress addressOfServer, int portOfServer, String username, String password, int clientKey, int clientPswd) {
        this.addressOfServer = addressOfServer;
        this.portOfServer = portOfServer;
        this.username = username;
        this.password = password;
        
        resource = new ClientSideResource(addressOfServer, portOfServer, username, password, clientKey, clientPswd);
        resource.addResourceEventListener(this);
    }
    
    public void connectToServer() throws ResourceException {
        resource.requestConnection();
    }
    
    public void resourceStateChanged(ResourceEvent e) {
        ResourceState newState = e.getState();
        
        switch(newState) {
            case CONNECTING:
                Logger.getLogger(ClientMonitor.class.getName()).log(Level.INFO, "Attempting to connect to the server at " + addressOfServer.getHostAddress() + "...");
                break;
            case AUTHORIZING:
                Logger.getLogger(ClientMonitor.class.getName()).log(Level.INFO, "Sucessfully connected to server, authorizing...");
                break;
            case READY:
                Logger.getLogger(ClientMonitor.class.getName()).log(Level.INFO, "Resource now ready.");
                break;
            case AUTHORIZATION_FAILURE:
                Logger.getLogger(ClientMonitor.class.getName()).log(Level.WARNING, "Authorization failed.");
                break;
            case TROUBLED:
                Logger.getLogger(ClientMonitor.class.getName()).log(Level.WARNING, "Communication problem with server.");
                break;
        }
    }
}
