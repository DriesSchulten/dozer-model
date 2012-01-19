package nl.dries.wicket.hibernate.dozer.helper;

import org.dozer.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create the target objects, used for every object that needs to be converted by Dozer.
 * 
 * @author dries
 */
public class ObjectBeanFactory implements BeanFactory
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(ObjectBeanFactory.class);

	/**
	 * @see org.dozer.BeanFactory#createBean(java.lang.Object, java.lang.Class, java.lang.String)
	 */
	@Override
	public Object createBean(Object source, Class<?> sourceClass, String targetBeanId)
	{
		Object instance = null;
		try
		{
			instance = sourceClass.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOG.error("Could not instantiate " + sourceClass, e);
		}

		return instance;
	}
}
