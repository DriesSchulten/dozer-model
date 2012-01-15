package nl.dries.wicket.hibernate.dozer;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;

/**
 * Hibernate enabled base test class
 * 
 * @author dries
 */
public abstract class AbstractHibernateTest
{
	/** Session factory */
	private final SessionFactory sessionFactory = HibernateHelper.buildSessionFactory(getEntities());

	/** Current session */
	private Session session;

	/**
	 * Open a session
	 */
	@Before
	public void openSession()
	{
		session = sessionFactory.openSession();
	}

	/**
	 * Close
	 */
	@After
	public void closeSession()
	{
		session.close();
	}

	/**
	 * @return the entities to use
	 */
	protected abstract List<Class<? extends Serializable>> getEntities();
}
