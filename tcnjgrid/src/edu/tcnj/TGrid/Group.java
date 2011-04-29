 
 /**
 *
 * @author Shane
 * Group class for access control
 */

package edu.tcnj.TGrid;

import java.util.ArrayList;

public class Group
{
	private String groupName;
	private int level;
	private ArrayList<User> users = new ArrayList<User>();

	public Group()
	{
	groupName = "default";
	level = 0;
	}
	
	public Group(String name, int l)
	{
	groupName = name;
	level = l;
	}

	public void addUser(User u)
	{
	users.add(u);
	}

	public int getLevel()
	{
	return level;
	}

	public String getName()
	{
	return groupName;
	}

	public void displayUsers()
	{
	System.out.println("Users in the group "+ getName());
	for(int n = 0; n < users.size(); n++) {
	System.out.println(users.get(n).getName()+"\n");
	}
	}

}