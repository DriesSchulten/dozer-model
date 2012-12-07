package nl.dries.wicket.hibernate.dozer.visitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;
import nl.dries.wicket.hibernate.dozer.proxy.Proxied;
import nl.dries.wicket.hibernate.dozer.proxy.ProxyBuilder;

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

		for (Field field : getAllFields(object.getClass()))
		{
			Class<?> type = field.getType();
			if (isValidType(type))
			{
				field.setAccessible(true);

				Object value = getValue(field, object);
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
							setValue(field, object, value);

							LOG.debug("Deproxying intialized value [{}.{}]", object.getClass().getName(),
								field.getName());

							toWalk.add(value);
						}
						else
						{
							LazyInitializer initializer = ((HibernateProxy) value).getHibernateLazyInitializer();
							HibernateProperty property = new HibernateProperty(initializer.getPersistentClass(),
								initializer.getIdentifier());
							AbstractPropertyDefinition prop = new SimplePropertyDefinition(object, field.getName(),
								callback, property);

							LOG.debug("Detaching proxy [{}.{}]", object.getClass().getName(), field.getName());

							setValue(field, object, ProxyBuilder.buildProxy(prop));
							callback.addProxiedProperty(prop);
						}
					}
					else
					{
						LOG.debug("Ignoring own proxied value [{}.{}]", object.getClass().getName(), field.getName());
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
	 * @param field
	 *            the field
	 * @param object
	 *            the object to invoke the getter on
	 * @return retrieved value
	 */
	private Object getValue(Field field, Object object)
	{
		try
		{
			return field.get(object);
		}
		catch (IllegalAccessException | IllegalArgumentException e)
		{
			LOG.error(String.format("Error while getting field %s on bean %s", field, object), e);
		}

		return null;
	}

	/**
	 * Sets a field
	 * 
	 * @param field
	 *            this field
	 * @param object
	 *            on this object
	 * @param newVal
	 *            the value to set
	 */
	private void setValue(Field field, Object object, Object newVal)
	{
		try
		{
			field.set(object, newVal);
		}
		catch (IllegalAccessException | IllegalArgumentException e)
		{
			LOG.error(String.format("Error while setting field %s on bean %s", field, object), e);
		}
	}

	/**
	 * Alle declared fields on a object hierarchy
	 * 
	 * @param clazz
	 *            starting {@link Class}
	 * @return all declared {@link Field}s
	 */
	private List<Field> getAllFields(Class<?> clazz)
	{
		List<Field> fields = new ArrayList<>();

		if (clazz != Object.class)
		{
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			fields.addAll(getAllFields(clazz.getSuperclass()));
		}

		return fields;
	}
}
