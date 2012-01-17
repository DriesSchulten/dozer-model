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
	 * @return {@link Hibernate} session
	 */
	Session getSession();
}
