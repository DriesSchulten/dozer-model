package nl.dries.wicket.hibernate.dozer.visitor;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

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

			if (type != null && !type.isPrimitive())
			{
				Object value = getValue(descriptor.getReadMethod(), object);
				if (value != null && !(value instanceof Class<?>))
				{
					SessionImpl sessionImpl = (SessionImpl) sessionFinder.getHibernateSession(type);
					ClassMetadata metadata = sessionImpl.getFactory().getClassMetadata(type);
					if (metadata == null)
					{
						toWalk.add(value);
					}
					else
					{
						if (Hibernate.isInitialized(value))
						{
							value = ObjectHelper.deproxy(value);
							setValue(descriptor.getWriteMethod(), object, value);
							toWalk.add(value);
						}
						else
						{
							LazyInitializer initializer = ((HibernateProxy) value).getHibernateLazyInitializer();
							HibernateProperty property = new HibernateProperty(initializer.getPersistentClass(),
								initializer.getIdentifier());
							callback.addDetachedProperty(object,
								new SimplePropertyDefinition((Class<? extends Serializable>) object.getClass(), null,
									descriptor.getName(), property));
							setValue(descriptor.getWriteMethod(), object, null);
						}
					}
				}
			}
		}

		return toWalk;
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
			LOG.error(String.format("Error while invoking reset method %s on bean %s", method, object), e);
		}
	}
}
