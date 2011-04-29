package edu.tcnj.TGrid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.TreeMap;

/*******************************************************************************
* Stores the client requirements that a task has
* 
* @author Stephen Sigwart
*******************************************************************************/
public class TaskRequirements implements Serializable
{
	/**
	 * List of <code>RequirementChecker</code>s
	 */
	private LinkedList<RequirementsChecker> requirements = new LinkedList<RequirementsChecker>();
	
	/**
	 * Default Constructor
	 */
	public TaskRequirements()
	{
	}
	
	/**
	 * Adds a task requirement
	 * 
	 * @param checker <code>RequirementsChecker</code> that will check if the 
	 * 	client meets the requirements.
	 */
	public void addRequirement(RequirementsChecker checker)
	{
		requirements.add(checker);
	}
	
	/**
	 * Determines if the client meets the requirements based on it's specs.
	 * 
	 * @param client Client object containing specs.
	 *	 	 
	 * @return Whether or not the client meets the requirements.
	 */
	public boolean meetsRequirements(ClientInfo client)
	{
		TreeMap<String, Object> specs = client.getSpecs();
		
		// Check specs againt each requirement
		for (RequirementsChecker checker : requirements)
			if (!checker.meetsRequirements(specs))
				return false;
		
		// Client meets if execution reaches here
		return true;
	}
}
