package nl.dries.wicket.hibernate.dozer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Mock Spring bean to implement {@link SessionFinder}
 * 
 * @author dries
 */
public class MockSessionFinder implements SessionFinder
{
	/** */
	private final SessionFactory sessionFactory;

	/**
	 * Construct
	 * 
	 * @param sessionFactory
	 */
	public MockSessionFinder(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.SessionFinder#getSession()
	 */
	@Override
	public Session getSession()
	{
		return sessionFactory.getCurrentSession();
	}

}
