package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.proxy.HibernateProxyHelper;

/**
 * Walker to traverse an object graph, and remove Hibernate state
 * 
 * @author schulten
 */
public class ObjectVisitor<T>
{
	/** Root */
	private final T root;

	/** */
	private final SessionFinder sessionFinder;

	/*** */
	private final ModelCallback callback;

	/** Seen objects, to prevent never ending recursion etc */
	private final Set<List<Object>> seen;

	/**
	 * @param root
	 * @param sessionFinder
	 * @param callback
	 */
	public ObjectVisitor(T root, SessionFinder sessionFinder, ModelCallback callback)
	{
		this.root = root;
		this.sessionFinder = sessionFinder;
		this.callback = callback;
		this.seen = new HashSet<>();
	}

	/**
	 * Walk the object tree, handeling registering un-initialized proxies
	 * 
	 * @return the root object
	 */
	public T walk()
	{
		List<Object> chain = new ArrayList<>();
		chain.add(root);

		walk(root, null, chain);
		return root;
	}

	/**
	 * Recursive walker
	 * 
	 * @param current
	 *            current object
	 * @param previousHibernateObject
	 *            the previous Hibernate object (for detecting parent <-> child mappings)
	 * @param chain
	 *            the current visited object chain, includes current object
	 */
	private void walk(Object current, Object previousHibernateObject, List<Object> chain)
	{
		Class<?> objectClass = HibernateProxyHelper.getClassWithoutInitializingProxy(current);

		SessionImplementor sessionImpl = (SessionImplementor) sessionFinder.getHibernateSession(objectClass);
		SessionFactoryImplementor factory = sessionImpl.getFactory();

		final VisitorStrategy strategy;
		if (factory.getClassMetadata(objectClass) != null)
		{
			strategy = new HibernateObjectVisitor(sessionImpl, callback, factory.getClassMetadata(objectClass),
				previousHibernateObject);
			previousHibernateObject = current;
		}
		else if (current instanceof Collection<?>)
		{
			strategy = new CollectionVisitor();
		}
		else if (current instanceof Map<?, ?>)
		{
			strategy = new MapVisitor();
		}
		else
		{
			strategy = new BasicObjectVisitor(sessionFinder, callback);
		}

		seen.add(chain);

		Set<Object> toWalk = strategy.visit(current);

		Iterator<Object> iter = toWalk.iterator();
		while (iter.hasNext())
		{
			Object next = iter.next();

			List<Object> newChain = new ArrayList<>(chain);
			newChain.add(next);

			// Check if we have already seen the exact object tree before vistiting it, preventing never ending
			// recursion
			if (!seen.contains(newChain))
			{
				walk(next, previousHibernateObject, newChain);
			}
		}
	}

}
