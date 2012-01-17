package nl.dries.wicket.hibernate.dozer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		return holder.getSession();
	}
}
