package edu.tcnj.TGrid.Events;

/**
 * The ClientKeyReceivedEventListener interface should be implemented 
 * by any class interested in receiving notification of ClientKeyReceivedEvent.
 * 
 * @author Stephen Sigwart
 * @see ClientKeyReceivedEvent
 */
public interface ClientKeyReceivedEventListener extends java.util.EventListener
{
	/**
	 * Triggered when the connection receives the client key.
	 */
	public void clientKeyReceivedFromRemoteHost(ClientKeyReceivedEvent e);
}
