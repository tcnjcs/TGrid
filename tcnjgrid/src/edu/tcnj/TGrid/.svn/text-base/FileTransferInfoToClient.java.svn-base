package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.io.Serializable;

import edu.tcnj.TGrid.TransferFileRemoteDirectory;
/**
 * The FileTransferInfoToClient contains information that will be sent to the 
 * client in a file transfer
 * 
 * @author Stephen
 */
public class FileTransferInfoToClient implements Serializable
{
	/**
	 * The name to give the remote file
	 */
	private String remoteFilename;
	
	/**
	 * Number of bytes in the file
	 */
	private long filesize;
	
	/**
	 * Directory to place the file
	 */
	private TransferFileRemoteDirectory remoteDirectory = TransferFileRemoteDirectory.CURRENT_DIR;
	
	/**
	 * Last modified timestamp of the file
	 */
	private long lastModified;
	
	/**
	 * Indicates whether the file is an executable or not
	 */
	private boolean isExecutable = false;
	
	/**
	 * Creates a new FileTransferTask instance.
	 * 
	 * @param remoteFilename The name to give the remote file
	 * @param filesize Number of bytes in the file
	 * @param remoteDirectory Directory in which to place the file
	 * @param lastModified Last modified timestamp of the file
	 * @param isExecutable Is the file an executable	 	 	 
	 */
	public FileTransferInfoToClient(String remoteFilename, long filesize, TransferFileRemoteDirectory remoteDirectory, long lastModified, boolean isExecutable)
	{
		this.remoteFilename = remoteFilename;
		this.filesize = filesize;
		this.remoteDirectory = remoteDirectory;
		this.lastModified = lastModified;
		this.isExecutable = isExecutable;
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
	 * Returns the size of the file in bytes
	 * 
	 * @return Size of the file in bytes	 	 
	 */
	public long getFilesize()
	{
	  return filesize;
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
	 * Returns the last modified timestamp of the file
	 * 
	 * @return Last modified timestamp of the file in bytes	 	 
	 */
	public long getLastModified()
	{
	  return lastModified;
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
