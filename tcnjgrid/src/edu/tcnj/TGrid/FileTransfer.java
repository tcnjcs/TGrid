package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.io.Serializable;

import edu.tcnj.TGrid.TransferFileRemoteDirectory;

/**
 * The FileTransfer contains information needed for a file transfer
 * 
 * @author Stephen
 */
public class FileTransfer implements Serializable
{		
	/**
	 * The name of the file to send to the resource
	 */
	private String localFilename;
	
	/**
	 * The name to give the remote file
	 */
	private String remoteFilename;
	
	/**
	 * Directory to place the file
	 */
	private TransferFileRemoteDirectory remoteDirectory = TransferFileRemoteDirectory.CURRENT_DIR;
	
	/**
	 * Indicates whether the file is an executable or not
	 */
	private boolean isExecutable = false;
	
	/**
	 * Creates a new FileTransferTask instance.
	 * 
	 * @param localFilename The name of the file to send to the resource
	 * @param remoteFilename The name to give the remote file
	 */
	public FileTransfer(String localFilename, String remoteFilename)
	{
		this.localFilename = localFilename;
		this.remoteFilename = remoteFilename;
	}
	
	/**
	 * Returns the local filename
	 * 
	 * @return Local filename to transfer
	 */
	public String getLocalFilename()
	{
	  return localFilename;
	}
	
	/**
	 * Returns the remote filename
	 * 
	 * @return Name to give remote file
	 */
	public String getRemoteFilename()
	{
	  return remoteFilename;
	}
	
	/**
	 * Sets the remote directory in which to place the file
	 * 
	 * @param remoteDirectory Remote directory in which to place the file
	 */
	public void setRemoteDirectory(TransferFileRemoteDirectory remoteDirectory)
	{
	  this.remoteDirectory = remoteDirectory;
	}
	
	/**
	 * Returns the remote directory in which to place the file
	 * 
	 * @return Remote directory in which to place the file
	 */
	public TransferFileRemoteDirectory getRemoteDirectory()
	{
	  return remoteDirectory;
	}
	
	/**
	 * Sets whether the file is an executable or not
	 * 
	 * @param isExecutable Is the file an executable
	 */
	public void setExecutable(boolean isExecutable)
	{
	  this.isExecutable = isExecutable;
	}
	
	/**
	 * Returns whether the file is an executable or not
	 * 
	 * @return True if the file is an executable
	 */
	public boolean isExecutable()
	{
	  return isExecutable;
	}
}
