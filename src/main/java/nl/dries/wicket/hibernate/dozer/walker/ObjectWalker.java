package nl.dries.wicket.hibernate.dozer.walker;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.helper.HibernateCollectionType;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walker to traverse an object graph, and remove Hibernate state
 * 
 * @author schulten
 */
public class ObjectWalker<T>
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(ObjectWalker.class);

	/** Root */
	private final T root;

	/** */
	private final SessionImplementor sessionImpl;

	/** */
	private final SessionFactoryImplementor factory;

	/*** */
	private final ModelCallback callback;

	/** Seen objects, to prevent never ending recursion etc */
	private final Set<Object> seen;

	/**
	 * @param root
	 * @param sessionFinder
	 * @param callback
	 */
	public ObjectWalker(T root, SessionFinder sessionFinder, ModelCallback callback)
	{
		this.root = root;
		this.sessionImpl = (SessionImplementor) sessionFinder.getHibernateSession();
		this.factory = sessionImpl.getFactory();
		this.callback = callback;
		this.seen = new HashSet<>();
	}

	/**
	 * Walk the object tree, handeling registering un-initialized proxies
	 * 
	 * @return the root object
	 */
	public T walk()
	{
		walk(deproxy(root));
		return root;
	}

	/**
	 * Recursive walker
	 * 
	 * @param object
	 *            current object
	 */
	private void walk(Object object)
	{
		Set<Object> toWalk = new HashSet<>();
		ClassMetadata metadata = factory.getClassMetadata(object.getClass());

		if (metadata != null)
		{
			Serializable identifier = metadata.getIdentifier(object, sessionImpl);

			for (String propertyName : metadata.getPropertyNames())
			{
				Type type = metadata.getPropertyType(propertyName);
				if (type instanceof AssociationType)
				{
					Object value = getValue(object, propertyName);

					if (value != null)
					{
						if (!Hibernate.isInitialized(value))
						{
							handleProxy(object, identifier, propertyName, value);
						}
						else if (value instanceof PersistentCollection)
						{
							handleInitializedCollection(object, toWalk,
								convertToPlainCollection(object, propertyName, value));
						}
						else if (value instanceof Collection<?>)
						{
							handlePlainCollection(toWalk, (Collection<?>) value);
						}
						else
						{
							value = deproxy(value);
							if (!seen.contains(value))
							{
								toWalk.add(value);
							}
						}
					}
				}
			}
		}

		seen.add(object);

		for (Iterator<Object> iter = toWalk.iterator(); iter.hasNext();)
		{
			walk(iter.next());
		}
	}

	/**
	 * Add a plain collection to the visitable objects
	 * 
	 * @param toWalk
	 *            set with walkable objects
	 * @param collection
	 *            the collection to add
	 */
	public void handlePlainCollection(Set<Object> toWalk, Collection<?> collection)
	{
		Iterator<?> iter = collection.iterator();
		while (iter.hasNext())
		{
			Object next = iter.next();
			if (!seen.contains(next))
			{
				toWalk.add(next);
			}
		}
	}

	/**
	 * Handles a initialized Hibernate collection value
	 * 
	 * @param object
	 *            the containing object
	 * @param toWalk
	 *            set with objects to visit
	 * @param value
	 *            the collection value
	 */
	public void handleInitializedCollection(Object object, Set<Object> toWalk, Collection<?> value)
	{
		Iterator<?> iter = value.iterator();
		while (iter.hasNext())
		{
			Object next = deproxy(iter.next());
			if (!seen.contains(next))
			{
				toWalk.add(next);
			}
		}
	}

	/**
	 * Convert Hibernate collection to a plain collection type
	 * 
	 * @param object
	 *            the object that owns the property
	 * @param propertyName
	 *            the property
	 * @param value
	 *            input collection
	 * @return plain collection type
	 */
	private Collection<?> convertToPlainCollection(Object object, String propertyName, Object value)
	{
		PersistentCollection collection = (PersistentCollection) value;
		Collection<?> plainCollection = HibernateCollectionType.determineType(collection).createPlainCollection(
			collection);
		setValue(object, propertyName, plainCollection);
		return plainCollection;
	}

	/**
	 * Creates a mapping for a Hibernate proxy
	 * 
	 * @param object
	 *            the owning object
	 * @param identifier
	 *            it's identifier
	 * @param propertyName
	 *            the name of the property
	 * @param value
	 *            its current value
	 */
	@SuppressWarnings("unchecked")
	public void handleProxy(Object object, Serializable identifier, String propertyName, Object value)
	{
		final AbstractPropertyDefinition def;

		Class<? extends Serializable> objectClass = HibernateProxyHelper.getClassWithoutInitializingProxy(object);

		// Collection
		if (value instanceof PersistentCollection)
		{
			def = new CollectionPropertyDefinition(objectClass, identifier, propertyName,
				HibernateCollectionType.determineType((PersistentCollection) value));
		}
		// Other
		else
		{
			LazyInitializer initializer = ((HibernateProxy) value).getHibernateLazyInitializer();
			HibernateProperty property = new HibernateProperty(initializer.getPersistentClass(),
				initializer.getIdentifier());
			def = new SimplePropertyDefinition(objectClass, identifier, propertyName, property);
		}

		callback.addDetachedProperty(object, def);
		setValue(object, propertyName, null); // Reset to null
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
	protected Object getValue(Object object, String property)
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
	protected void setValue(Object object, String property, Object value)
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
	protected <U> U deproxy(U object)
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
