/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.util.LinkedList;

/**
 * The FileTransferTask class allows a file to be transfered between the server
 * and the client 
 * 
 * @author Stephen
 */
public abstract class FileTransferTask extends Task
{		
	/**
	 * Array of files to transfer
	 */
	private LinkedList<FileTransfer> files = new LinkedList<FileTransfer>();
	
	/**
	 * Index of next file to get
	 */
	private int nextFileIdx = 0;
	
	/**
	 * Stores files to transfer to the client
	 * 
	 * @param file File to transfer to client
	 */
	public void FileTransferTask()
	{
	}
	
	/**
	 * Adds a file to transfer to the client
	 * 
	 * @param file File to transfer to client
	 */
	public void addFile(FileTransfer file)
	{
		files.add(file);
	}
	
	/**
	 * Resets next file index to first file
	 */
	public void resetFileIndex()
	{
		nextFileIdx = 0;
	}
	
	/**
	 * Returns the next file to transfer to the client and removes from list
	 * 
	 * @return File information to transfer to client
	 */
	public FileTransfer getNextFile()
	{
		if (nextFileIdx < files.size())
			return files.get(nextFileIdx++);
		return null;
	}
	
	/**
	 * Returns the number of files left to transfer
	 * 
	 * @return Number of files left to transfer
	 */
	public int getNumFilesLeft()
	{
		return files.size() - nextFileIdx;
	}
}
