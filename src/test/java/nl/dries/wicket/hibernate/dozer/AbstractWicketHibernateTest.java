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
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hibernate/Wicket enabled base test class
 * 
 * @author dries
 */
public abstract class AbstractWicketHibernateTest
{
	/** Session factory */
	private static SessionFactory sessionFactory;

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
		if (sessionFactory == null)
		{
			sessionFactory = HibernateHelper.buildSessionFactory(getEntities());
		}

		if (!TransactionSynchronizationManager.hasResource(sessionFactory))
		{
			session = sessionFactory.openSession();
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		}

		if (wicketTester == null)
		{
			ApplicationContextMock context = new ApplicationContextMock();
			context.putBean(new MockSessionFinder(sessionFactory));

			MockApplication application = new MockApplication();
			new SpringComponentInjector(application, context);
			wicketTester = new WicketTester(application);

			// wicketTester.getRequestCycle().setMetaData(DozerRequestCycleListener.ENDING_REQUEST, true);
		}
	}

	/**
	 * Close
	 */
	@After
	public void closeSession()
	{
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
			.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
	}

	/**
	 * @return the session
	 */
	protected Session getSession()
	{
		return session;
	}

	/**
	 * @return the wicketTester
	 */
	protected WicketTester getWicketTester()
	{
		return wicketTester;
	}

	/**
	 * @return the entities to use
	 */
	protected abstract List<Class<? extends Serializable>> getEntities();
}
