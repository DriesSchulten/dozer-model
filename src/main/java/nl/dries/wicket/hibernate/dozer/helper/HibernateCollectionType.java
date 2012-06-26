package nl.dries.wicket.hibernate.dozer.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
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
	LIST(PersistentBag.class, ArrayList.class, List.class),
	/** */
	SET(PersistentSet.class, HashSet.class, Set.class),
	/** */
	SORTED_SET(PersistentSortedSet.class, TreeSet.class, Set.class),
	/** */
	MAP(PersistentMap.class, HashMap.class, Map.class);

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(HibernateCollectionType.class);

	/** Specific Hibernate collection class */
	private Class<? extends PersistentCollection> hibernateCollectionClass;

	/** Plain 'Java collections' type */
	private Class<?> plainTypeClass;

	/** Plain interface collection type */
	private Class<?> plainInterface;

	/**
	 * Construct
	 * 
	 * @param hibernateCollectionClass
	 * @param plainTypeClass
	 */
	private HibernateCollectionType(Class<? extends PersistentCollection> hibernateCollectionClass,
		Class<?> plainTypeClass, Class<?> plainInterface)
	{
		this.hibernateCollectionClass = hibernateCollectionClass;
		this.plainTypeClass = plainTypeClass;
		this.plainInterface = plainInterface;
	}

	/**
	 * @return the plainInterface
	 */
	public Class<?> getPlainInterface()
	{
		return plainInterface;
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
	 * Creates a plain Java collection instance based on a {@link PersistentCollection} one
	 * 
	 * @param persistentCollection
	 *            the {@link PersistentCollection}
	 * @return plain collection
	 */
	public Object createPlainCollection(PersistentCollection persistentCollection)
	{
		Object collection = null;
		try
		{
			if (Map.class.isAssignableFrom(plainTypeClass))
			{
				collection = plainTypeClass.newInstance();
			}
			else
			{
				collection = plainTypeClass.newInstance();
			}
		}
		catch (SecurityException e)
		{
			LOG.error("Colection type {} has no empty constructor", plainTypeClass);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException e)
		{
			LOG.error("Cannot create collection instance of type " + plainTypeClass, e);
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

		if (sourceFieldValue.getValue() instanceof PersistentSortedSet)
		{
			type = HibernateCollectionType.SORTED_SET;
		}
		else if (sourceFieldValue.getValue() instanceof PersistentSet)
		{
			type = HibernateCollectionType.SET;
		}
		else if (sourceFieldValue.getValue() instanceof PersistentBag)
		{
			type = HibernateCollectionType.LIST;
		}
		else
		{
			type = HibernateCollectionType.MAP;
		}

		return type;
	}
}
