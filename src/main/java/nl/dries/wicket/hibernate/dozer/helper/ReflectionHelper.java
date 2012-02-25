package nl.dries.wicket.hibernate.dozer.helper;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflection helper functions
 * 
 * @author dries
 */
public final class ReflectionHelper
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionHelper.class);

	/** Util -> private */
	private ReflectionHelper()
	{
	}

	/**
	 * Get a value using reflection
	 * 
	 * @param object
	 *            in this object
	 * @param property
	 *            the property to get
	 * @return its value
	 */
	public static Object getValue(Object object, String property)
	{
		Object value = null;
		try
		{
			value = PropertyUtils.getProperty(object, property);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			LOG.error(String.format("Cannot get value for property %s in object %s", property, object), e);
		}

		return value;
	}

	/**
	 * Set a value using reflection
	 * 
	 * @param object
	 *            target object
	 * @param property
	 *            target property to set
	 * @param value
	 *            the value to set
	 */
	public static void setValue(Object object, String property, Object value)
	{
		try
		{
			PropertyUtils.setProperty(object, property, value);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			LOG.error(String.format("Cannot set value %s for property %s in object %s", value, property, object), e);
		}
	}

	/**
	 * Deproxy a Hibernate enhanced object, only call when sure the object is initialized, otherwise (unwanted)
	 * intialization wil take place
	 * 
	 * @param object
	 *            the input object
	 * @return deproxied object
	 */
	@SuppressWarnings("unchecked")
	public static <U> U deproxy(U object)
	{
		if (object instanceof HibernateProxy)
		{
			HibernateProxy hibernateProxy = (HibernateProxy) object;
			LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();

			return (U) lazyInitializer.getImplementation();
		}
		return object;
	}
}
