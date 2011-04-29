/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Events.TaskEvent;
import edu.tcnj.TGrid.Events.TaskEventListener;
import edu.tcnj.TGrid.States.TaskState;
import java.util.HashSet;
import java.io.Serializable;

/**
 * The TestFileTask class represents an extremely basic Task, that does absolutely
 * nothing except report that it worked.
 * 
 * @author Stephen
 */
public class TestFileTask extends FileTransferTask
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
     * Creates a new TestFileTask instance.
     */
    public TestFileTask()
		{
        FileTransfer ft = new FileTransfer("../../primeFunc.c", "primeFunc.c");
        ft.setRemoteDirectory(TransferFileRemoteDirectory.EXECUTABLES_DIR);
        addFile(ft);
        
				ft = new FileTransfer("edu/tcnj/TGrid/Main.class", "Main.class");
        ft.setRemoteDirectory(TransferFileRemoteDirectory.JAVA_CLASS_DIR);
        addFile(ft);
        
        ft = new FileTransfer("../../build.xml", "build.xml");
        ft.setRemoteDirectory(TransferFileRemoteDirectory.MISC_DIR);
        addFile(ft);
        
        ft = new FileTransfer("../../nbproject/project.properties", "somefile.properties");
        ft.setRemoteDirectory(TransferFileRemoteDirectory.DATA_DIR);
        addFile(ft);
    }
    
    /**
     * "Runs" the TestFileTask, spawing a thread that waits a specific amount of time.
     */
    public void runTask() {
        setState(TaskState.RUNNING);
        
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

                setState(TaskState.TERMINATED);
            } catch (InterruptedException ex) {}
        }
    }
    
    /**
     * Changes the task's results to the correct value.
     * 
     * 
     * @param the results from running the task
     */
    public void setResults(Serializable results) {
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
     * Returns a string representation of this TestFileTask
     * 
     * @return a string representation of this TestFileTask
     */
    @Override
    public String toString() {
        return "TestFileTask #" + id;
    }
    
    protected class RunThread extends Thread {
        public void run() {
            try {
                sleep(2000);
            } catch (InterruptedException ex) {}
            results = "Task " + id + " says: File transferred.";
            setState(TaskState.COMPLETED);
        }
    }
}
