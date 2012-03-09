package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
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
	private final Set<Object> seen;

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
		walk(root);
		return root;
	}

	/**
	 * Recursive walker
	 * 
	 * @param object
	 *            current object
	 */
	private void walk(Object object)
	{
		Class<?> objectClass = HibernateProxyHelper.getClassWithoutInitializingProxy(object);

		SessionImplementor sessionImpl = (SessionImplementor) sessionFinder.getHibernateSession(objectClass);
		SessionFactoryImplementor factory = sessionImpl.getFactory();

		final VisitorStrategy strategy;
		if (factory.getClassMetadata(objectClass) != null)
		{
			strategy = new HibernateObjectVisitor(sessionImpl, callback, factory.getClassMetadata(objectClass));
		}
		else if (object instanceof Collection<?>)
		{
			strategy = new CollectionVisitor();
		}
		else if (object instanceof Map<?, ?>)
		{
			strategy = new MapVisitor();
		}
		else
		{
			strategy = new BasicObjectVisitor(sessionFinder, callback);
		}

		seen.add(object);

		Set<Object> toWalk = strategy.visit(object);
		Iterator<Object> iter = toWalk.iterator();
		while (iter.hasNext())
		{
			if (seen.contains(iter.next()))
			{
				iter.remove();
			}
		}

		for (Object next : toWalk)
		{
			walk(next);
		}
	}

}
