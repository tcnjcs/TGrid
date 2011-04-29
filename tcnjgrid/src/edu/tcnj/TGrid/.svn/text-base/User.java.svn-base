/**
 *
 * @author Shane
 * user class for login purposes
 */
package edu.tcnj.TGrid;

import java.util.ArrayList;
/**
     * User object handles name, password,
 * and level for each user
     */
public class User {

    private String name; //users name
    private String password; //users password
    private int level; //users permision level
    //private ArrayList<Job> jobs;

    /**
     * Constructor for User takesname, password, and level
     * and makes a User object
     * @param String name, String password, int level
     * @return void
     */
    public User(String n, String p, int m) {
        name = n;
        password = p;
        level = m;
    }

    /**
     * Constructor takes name and password and creates
     * a User ojbect with level 0
     * @param String name, String password
     * @return void
     */
    public User(String n, String p) {
        name = n;
        password = p;
        level = 0;
    }

    /**
     * Constructor makes a null User ojbect with
     * null name and password and no level
     * @param void
     * @return void
     */
    public User() {
        name = null;
        password = null;
    }

    //Method for adding a job to the list of jobs the user has access to,implemented in the future
	/*public void addJob(Job j)
    {
    jobs.add(j);
    }*/
    //Method for checking if a user has access to complete a job, implemented in the future
	/*public boolean containsJob(Job j)
    {
    boolean out = false;
    for(int i = 0; i < jobs.size(); i++)
    {
    if(j.equals(jobs.get(i))){
    out = true;
    }
    }
    return out;
    }*/

    /**
     * Checks if the supplied User is the current User
     * @param User u
     * @return boolean
     */
    public boolean equals(User u) {
        return (this.getName().equals(u.getName()) && this.getPassword().equals(u.getPassword()));
    }

    /**
     * Getter for User's name
     * @param void
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for User's password
     * @param void
     * @return String password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for User's password
     * @param String password
     * @return void
     */
    public void setPassword(String p) {
        password = p;
    }

    /**
     * Getter for User's level
     * @param void
     * @return int level
     */
    public int getLevel() {
        return level;
    }

    /**
     * toString returns the name, password and level
     * seperated by spaces
     * @param void
     * @return String "{name} {password} {level}"
     */
    public String toString() {
        String out;
        out = name + " " + password + " " + level;
        return out;
    }
}
