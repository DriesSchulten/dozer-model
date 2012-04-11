package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;

import org.hibernate.proxy.HibernateProxy;

/**
 * Visits a collection
 * 
 * @author dries
 */
public class CollectionVisitor implements VisitorStrategy
{
	/**
	 * @see nl.dries.wicket.hibernate.dozer.visitor.VisitorStrategy#visit(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<Object> visit(Object object)
	{
		Set<Object> toWalk = null;

		if (object instanceof List)
		{
			toWalk = visitList((List<Object>) object);
		}
		else if (object instanceof Set)
		{
			toWalk = visitSet((Set<Object>) object);
		}

		return toWalk;
	}

	/**
	 * Visit a list, replacing any Hibernate proxies
	 * 
	 * @param list
	 *            the input list
	 * @return set containing objects to visit
	 */
	private Set<Object> visitList(List<Object> list)
	{
		Map<Integer, Object> toReplace = new HashMap<>();

		for (int i = 0; i < list.size(); i++)
		{
			Object obj = list.get(i);
			if (obj instanceof HibernateProxy)
			{
				toReplace.put(i, ObjectHelper.deproxy(obj));
			}
		}

		for (Entry<Integer, Object> entry : toReplace.entrySet())
		{
			list.set(entry.getKey(), entry.getValue());
		}

		return new HashSet<>(list);
	}

	/**
	 * Visit a set, replacing any Hibernate proxies
	 * 
	 * @param set
	 *            the input set
	 * @return the input
	 */
	private Set<Object> visitSet(Set<Object> set)
	{
		Map<Object, Object> toReplace = new HashMap<>();

		Iterator<Object> iter = set.iterator();
		while (iter.hasNext())
		{
			Object obj = iter.next();
			if (obj instanceof HibernateProxy)
			{
				toReplace.put(obj, ObjectHelper.deproxy(obj));
			}
		}

		set.removeAll(toReplace.keySet());
		set.addAll(toReplace.values());

		return set;
	}
}
