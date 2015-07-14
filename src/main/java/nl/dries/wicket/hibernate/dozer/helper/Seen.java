package nl.dries.wicket.hibernate.dozer.helper;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Seen objects holder
 * 
 * @author dries
 */
public class Seen
{
	/** */
	Map<Object, Void> seen = new IdentityHashMap<>();

	/**
	 * @param obj
	 *            the object to mark as seen
	 */
	public void add(Object obj)
	{
		if (!seen.containsKey(obj))
		{
			seen.put(obj, null);
		}
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
		return seen.containsKey(obj);
	}
}
