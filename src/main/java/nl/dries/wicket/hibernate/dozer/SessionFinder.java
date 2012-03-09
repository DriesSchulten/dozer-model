package nl.dries.wicket.hibernate.dozer;

import org.hibernate.Hibernate;
import org.hibernate.Session;

/**
 * Helper to retrieve a Hibernate session
 * 
 * @author dries
 */
public interface SessionFinder
{
	/**
	 * @param clazz
	 *            for this Hibernate object class
	 * @return {@link Hibernate} session
	 */
	Session getHibernateSession(Class<?> clazz);
}
