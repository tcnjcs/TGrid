package edu.tcnj.TGrid.Events;

import edu.tcnj.TGrid.ConnectionToRemoteHost;
import edu.tcnj.TGrid.States.ConnectionToRemoteHostState;

/**
 * A ClientKeyReceivedEvent is triggered by the ConnectionToResource when the
 * connection receives the client key from the client. 
 * 
 * @author Stephen Sigwart
 * 
 */
public class ClientKeyReceivedEvent extends java.util.EventObject
{
	/**
	* Creates a new ClientKeyReceivedEvent instance.
	* 
	* @param source The connection on which the ClientKeyReceivedEvent initially occurred.
	*/
	public ClientKeyReceivedEvent(ConnectionToRemoteHost source)
	{
		super((Object)source);
	}
}
