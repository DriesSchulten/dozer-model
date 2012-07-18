package nl.dries.wicket.hibernate.dozer;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializer for the Dozer request cycle listener
 * 
 * @author dries
 */
public class DozerInitializer implements IInitializer
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(DozerInitializer.class);

	/**
	 * @see org.apache.wicket.IInitializer#init(org.apache.wicket.Application)
	 */
	@Override
	public void init(Application application)
	{
		application.getRequestCycleListeners().add(new DozerRequestCycleListener());
		LOG.info("Dozer request cycle listener registered");
	}

	/**
	 * @see org.apache.wicket.IInitializer#destroy(org.apache.wicket.Application)
	 */
	@Override
	public void destroy(Application application)
	{
	}
}
