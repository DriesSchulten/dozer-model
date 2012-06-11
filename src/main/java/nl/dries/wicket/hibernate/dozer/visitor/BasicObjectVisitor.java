package nl.dries.wicket.hibernate.dozer.visitor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;
import nl.dries.wicket.hibernate.dozer.proxy.ProxyBuilder;
import nl.dries.wicket.hibernate.dozer.proxy.ProxyBuilder.Proxied;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Hibernate;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visiting strategy for a plain object
 * 
 * @author dries
 */
public class BasicObjectVisitor implements VisitorStrategy
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(BasicObjectVisitor.class);

	/** */
	private static final String JAVA_PKG = "java";

	/** Session finder */
	private final SessionFinder sessionFinder;

	/** Callback */
	private final ModelCallback callback;

	/**
	 * Construct
	 * 
	 * @param sessionFinder
	 * @param callback
	 */
	public BasicObjectVisitor(SessionFinder sessionFinder, ModelCallback callback)
	{
		this.sessionFinder = sessionFinder;
		this.callback = callback;
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.visitor.VisitorStrategy#visit(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<Object> visit(Object object)
	{
		Set<Object> toWalk = new HashSet<>();

		for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(object.getClass()))
		{
			Class<?> type = descriptor.getPropertyType();

			if (isValidType(type))
			{
				Object value = getValue(descriptor.getReadMethod(), object);
				if (value != null)
				{
					SessionImpl sessionImpl = (SessionImpl) sessionFinder.getHibernateSession(type);
					ClassMetadata metadata = sessionImpl.getFactory().getClassMetadata(type);
					if (metadata == null)
					{
						toWalk.add(value);
					}
					else if (!(value instanceof Proxied))
					{
						if (Hibernate.isInitialized(value))
						{
							value = ObjectHelper.deproxy(value);
							setValue(descriptor.getWriteMethod(), object, value);

							LOG.debug("Deproxying intialized value [{}.{}]", object.getClass().getName(),
								descriptor.getName());

							toWalk.add(value);
						}
						else
						{
							LazyInitializer initializer = ((HibernateProxy) value).getHibernateLazyInitializer();
							HibernateProperty property = new HibernateProperty(initializer.getPersistentClass(),
								initializer.getIdentifier());
							AbstractPropertyDefinition prop = new SimplePropertyDefinition(
								object.getClass(), descriptor.getName(), callback, property);

							LOG.debug("Detaching proxy [{}.{}]", object.getClass().getName(), descriptor.getName());

							setValue(descriptor.getWriteMethod(), object, ProxyBuilder.buildProxy(prop));
						}
					}
					else
					{
						LOG.debug("Ignoring own proxied value [{}.{}]", object.getClass().getName(),
							descriptor.getName());
					}
				}
			}
		}

		return toWalk;
	}

	/**
	 * Checks if the given type is valid to visit
	 * 
	 * @param type
	 *            the type to check
	 * @return <code>true</code> if the type is valid
	 */
	private boolean isValidType(Class<?> type)
	{
		boolean valid = type != null && type.getPackage() != null && !type.isPrimitive();

		if (valid)
		{
			valid = !type.getPackage().getName().startsWith(JAVA_PKG);
		}

		return valid;
	}

	/**
	 * Get a value by invoking a getter
	 * 
	 * @param method
	 *            the get method
	 * @param object
	 *            the object to invoke the getter on
	 * @return retrieved value
	 */
	private Object getValue(Method method, Object object)
	{
		try
		{
			return method.invoke(object);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			LOG.error(String.format("Error while invoking getter %s on bean %s", method, object), e);
		}

		return null;
	}

	/**
	 * Sets a field
	 * 
	 * @param method
	 *            using this setter method
	 * @param object
	 *            on this object
	 * @param newVal
	 *            the value to set
	 */
	private void setValue(Method method, Object object, Object newVal)
	{
		try
		{
			method.invoke(object, new Object[] { newVal });
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			LOG.error(String.format("Error while invoking setter method %s on bean %s", method, object), e);
		}
	}
}
