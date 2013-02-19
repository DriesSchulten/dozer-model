package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.Seen;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walker to traverse an object graph, and remove Hibernate state
 * 
 * @author schulten
 */
public class ObjectVisitor<T>
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(ObjectVisitor.class);

	/** Root */
	private final T root;

	/** */
	private final SessionFinder sessionFinder;

	/*** */
	private final ModelCallback callback;

	/** Seen objects, to prevent never ending recursion etc */
	private final Seen seen;

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
		this.seen = new Seen();
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
	 * @param current
	 *            current object
	 */
	private void walk(Object current)
	{
		Class<?> objectClass = HibernateProxyHelper.getClassWithoutInitializingProxy(current);

		SessionImplementor sessionImpl = (SessionImplementor) sessionFinder.getHibernateSession(objectClass);

		if (sessionImpl == null)
		{
			LOG.debug("No session, stop detaching");
			return;
		}

		SessionFactoryImplementor factory = sessionImpl.getFactory();

		final VisitorStrategy strategy;
		if (factory.getClassMetadata(objectClass) != null)
		{
			strategy = new HibernateObjectVisitor(sessionImpl, callback, factory.getClassMetadata(objectClass));
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

		seen.add(current);

		Set<Object> toWalk = strategy.visit(current);

		Iterator<Object> iter = toWalk.iterator();
		while (iter.hasNext())
		{
			Object next = iter.next();

			// Check if we have already seen the exact object tree before vistiting it, preventing never ending
			// recursion
			if (!seen.contains(next))
			{
				walk(next);
			}
		}
	}

}
