package nl.dries.wicket.hibernate.dozer.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedSet;
import org.hibernate.engine.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate collection types
 * 
 * @author dries
 */
public enum HibernateCollectionType
{
	/** */
	LIST(PersistentBag.class),
	/** */
	SET(PersistentSet.class),
	/** */
	SORTED_SET(PersistentSortedSet.class);

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(HibernateCollectionType.class);

	/** Specific Hibernate collection class */
	private Class<? extends PersistentCollection> hibernateCollectionClass;

	/**
	 * Construct
	 * 
	 * @param hibernateCollectionClass
	 */
	private HibernateCollectionType(Class<? extends PersistentCollection> hibernateCollectionClass)
	{
		this.hibernateCollectionClass = hibernateCollectionClass;
	}

	/**
	 * Creates a instance of the {@link PersistentCollection} defined by this type
	 * 
	 * @param sessionImpl
	 *            {@link SessionImplementor}
	 * @return {@link PersistentCollection} instance
	 */
	public PersistentCollection createCollection(SessionImplementor sessionImpl)
	{
		PersistentCollection collection = null;

		try
		{
			Constructor<? extends PersistentCollection> constructor = hibernateCollectionClass
				.getConstructor(SessionImplementor.class);
			collection = constructor.newInstance(sessionImpl);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			LOG.error("Persistent collection type {} has no SessionImplementor constructor", hibernateCollectionClass);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException e)
		{
			LOG.error("Cannot create collection instance of type " + hibernateCollectionClass, e);
		}

		return collection;
	}

	/**
	 * Determine collection type based on input value
	 * 
	 * @param sourceFieldValue
	 *            the input {@link PersistentCollection}
	 * @return {@link HibernateCollectionType}
	 */
	public static HibernateCollectionType determineType(PersistentCollection sourceFieldValue)
	{
		final HibernateCollectionType type;

		if (sourceFieldValue instanceof PersistentSortedSet)
		{
			type = HibernateCollectionType.SORTED_SET;
		}
		else if (sourceFieldValue instanceof PersistentSet)
		{
			type = HibernateCollectionType.SET;
		}
		else
		{
			type = HibernateCollectionType.LIST;
		}

		return type;
	}
}
