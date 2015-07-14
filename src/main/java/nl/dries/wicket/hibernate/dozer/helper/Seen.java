package nl.dries.wicket.hibernate.dozer.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Seen objects holder
 * 
 * @author dries
 */
public class Seen
{
	/** */
	List<Object> seen = new ArrayList<>();

	/**
	 * @param obj
	 *            the object to mark as seen
	 */
	public void add(Object obj)
	{
		seen.add(obj);
	}

	/**
	 * @param obj
	 *            the object to remove
	 */
	public void remove(Object obj)
	{
		seen.remove(obj);
	}

	/**
	 * Checks if the seen list contains a given object
	 * 
	 * @param obj
	 *            the object
	 * @return <code>true</code>
	 */
	public boolean contains(Object obj)
	{
		for (Object seenObj : seen)
		{
			if (seenObj == obj)
			{
				return true;
			}
		}
		return false;
	}
}
