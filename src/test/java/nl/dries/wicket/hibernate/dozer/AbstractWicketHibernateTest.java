package nl.dries.wicket.hibernate.dozer;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;

/**
 * Hibernate/Wicket enabled base test class
 * 
 * @author dries
 */
public abstract class AbstractWicketHibernateTest
{
	/** Session factory */
	private final SessionFactory sessionFactory = HibernateHelper.buildSessionFactory(getEntities());

	/** Wicket tester instance */
	private WicketTester wicketTester;

	/** Current session */
	private Session session;

	/**
	 * Open a session
	 */
	@Before
	public void openSession()
	{
		if (wicketTester == null)
		{
			ApplicationContextMock context = new ApplicationContextMock();
			context.putBean(new MockSessionFinder(sessionFactory));

			MockApplication application = new MockApplication();
			new SpringComponentInjector(application, context);
			wicketTester = new WicketTester(application);
		}

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
	 * Close and open a new session
	 */
	protected void closeAndOpenSession()
	{
		session.close();
		session = sessionFactory.openSession();
	}

	/**
	 * @return the session
	 */
	protected Session getSession()
	{
		return session;
	}

	/**
	 * @return the entities to use
	 */
	protected abstract List<Class<? extends Serializable>> getEntities();
}
