/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Serializable;

/**
 * The JacobsthalTask class represents a Task concerned with running the external 
 * program that calculates Jacobsthal numbers.
 * 
 * @author Dan
 */
public class JacobsthalTransferTask extends FileTransferTask
{    
    /**
     * Represents the current state of this Task
     */
    private TaskState currentState = TaskState.READY;
    
    /**
     * Represents the results of running this thread, as a human-readable string.
     */
    private String results;
    
    /**
     * Stores a list of every event listener that is registered to receive event
     * notifications from this class.
     */
    private HashSet<TaskEventListener> registeredListeners = new HashSet<TaskEventListener>();
    
    /**
     * Represents the internal thread used to "run" the Task.
     */
    private RunThread runThread; 
    
    /**
     * Represents the command to run on the command line, with additional arguments 
     * as Strings in the array
     */
    private String[] commandToRun;
    
    
    /**
     * Creates a new JacobsthalTransferTask instance.
     * 
     * @param 
     */
    public JacobsthalTransferTask(int[] commandLineArguments)
		{
				FileTransfer ft = new FileTransfer("../../primeFunc.run", "primeFunc.run");
        ft.setRemoteDirectory(TransferFileRemoteDirectory.EXECUTABLES_DIR);
        ft.setExecutable(true);
        addFile(ft);
        
        commandToRun = new String[1+commandLineArguments.length];
        
        for(int i=0; i<commandLineArguments.length; i++) {
            commandToRun[i+1] = Integer.toString(commandLineArguments[i]);
        }
    }
    
    /**
     * "Runs" the JacobsthalTransferTask, spawing a thread that waits a specific amount of time.
     */
    public void runTask()
		{
				// Set executable name
				commandToRun[0] = "./primeFunc.run";
				if (parentResource != null)
				{
					String dir = parentResource.getDirectoryPath(TransferFileRemoteDirectory.EXECUTABLES_DIR);
					if (!dir.equals(""))
						commandToRun[0] = dir + "primeFunc.run";
				}
				
        runThread = new RunThread();
        runThread.start();
    }
    
    /**
     * If the Task is currently running, forces it to quit without finishing.
     */
    public void forceQuit() {
        if(currentState == currentState.RUNNING) {
            try {
                runThread.interrupt();

                runThread.join();
            } catch (InterruptedException ex) {}
        }
    }
    
    /**
     * Changes the task's results to the correct value.
     * 
     * @param the results from running the task
     */
    public void setResults(Serializable results)
		{
        this.results = (String)results;
    }
    
    /**
     * Returns the results from running the task as a string.  Only useful if
     * the task has completed execution successfully.
     * 
     * @return the results from running the task
     */
    public Serializable getResults() {
        return results;
    }
    
    /**
     * Returns a string representation of this TestTask
     * 
     * @return a string representation of this TestTask
     */
    @Override
    public String toString() {
        String returnValue = new String();
        
        returnValue += "Jacobsthal Task with command ";
        
        for(String current : commandToRun) {
            returnValue += current + " ";
        }
        
        return returnValue;
    }
    
    protected class RunThread extends Thread {
        @Override
        public void run() {
            setState(TaskState.RUNNING);
            try {
                Process p = Runtime.getRuntime().exec(commandToRun);
                BufferedReader outputFromProcess = new BufferedReader(new InputStreamReader(p.getInputStream()));
                
                try {
                    //wait for the process to finish
                    p.waitFor();
                    
                    results = new String();
                
                    while(outputFromProcess.ready()) //read the process's output into the resulsts string, character by character
                        results += (char)outputFromProcess.read();

                    if(p.exitValue() == 0) //an exit value of 0 indicates that the process executed successfully
                        setState(TaskState.COMPLETED);
                    else
                        setState(TaskState.TROUBLED);
                } catch (InterruptedException ie) {  //if the thread is interrupted, kill the process and set the state to TERMINATED
                    p.destroy();
                    
                    setState(TaskState.TERMINATED);
                }
            } catch (IOException ex) {
                Logger.getLogger(JacobsthalTask.class.getName()).log(Level.SEVERE, "Unable to run task.", ex);
                setState(TaskState.TROUBLED);
            }
        }
    }
}
