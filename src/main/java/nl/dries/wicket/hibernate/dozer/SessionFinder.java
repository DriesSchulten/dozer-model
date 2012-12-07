package nl.dries.wicket.hibernate.dozer;

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
	 * @return Hibernate {@link Session}
	 */
	Session getHibernateSession(Class<?> clazz);
}
