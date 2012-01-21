package nl.dries.wicket.hibernate.dozer.helper;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate object (re-)attacher
 * 
 * @author dries
 * 
 * @param <T>
 *            type of the object to re-attach
 */
public class Attacher<T>
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(Attacher.class);

	/** The Hibernate session */
	private final SessionImplementor sessionImpl;

	/** The object to attach */
	private final T toAttach;

	/** Properties to attach */
	private final List<? extends AbstractPropertyDefinition> properties;

	/**
	 * Construct
	 * 
	 * @param toAttach
	 *            object to attach to
	 * @param session
	 *            Hibernate {@link Session}
	 * @param detachedProperties
	 *            detached properties
	 * @param detachedCollections
	 *            detached collections
	 */
	public Attacher(T toAttach, Session session, List<? extends AbstractPropertyDefinition> detachedProperties)
	{
		this.toAttach = toAttach;
		this.sessionImpl = (SessionImplementor) session;
		this.properties = detachedProperties;
	}

	/**
	 * Attach
	 */
	public void doAttach()
	{
		if (properties != null)
		{
			for (AbstractPropertyDefinition def : properties)
			{
				if (def instanceof SimplePropertyDefinition)
				{
					attach((SimplePropertyDefinition) def);
				}
				else
				{
					attach((CollectionPropertyDefinition) def);
				}
			}
		}
	}

	/**
	 * Attach a property
	 * 
	 * @param def
	 *            the {@link SimplePropertyDefinition}
	 */
	protected void attach(SimplePropertyDefinition def)
	{
		EntityPersister persister = getPersister(def.getHibernateProperty());
		PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();

		EntityKey key = new EntityKey(def.getHibernateProperty().getId(), persister, EntityMode.POJO);

		// Check existing instance
		Object instance = persistenceContext.getEntity(key);

		// No instance found
		if (instance == null)
		{
			// Also no proxy found, generate new one
			Object existing = persistenceContext.getProxy(key);
			if (existing == null)
			{
				instance = persister.createProxy(def.getHibernateProperty().getId(), sessionImpl);
				persistenceContext.getBatchFetchQueue().addBatchLoadableEntityKey(key);
				persistenceContext.addProxy(key, instance);
			}
			else
			{
				instance = existing;
			}
		}

		// Set the property to te real value, or a proxy
		setProperty(def.getProperty(), instance);
	}

	/**
	 * Attach a collection
	 * 
	 * @param def
	 *            the {@link CollectionPropertyDefinition}
	 */
	protected void attach(CollectionPropertyDefinition def)
	{
		CollectionPersister persister = getCollectionPersister(def);
		PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();

		PersistentCollection collection = def.getCollectionType().createCollection(sessionImpl);
		collection.setOwner(toAttach);
		collection.setSnapshot(def.getOwnerId(), def.getRole(), null); // Sort of 'fake' state...

		persistenceContext.addUninitializedCollection(persister, collection, def.getOwnerId());

		// Restore value
		setProperty(def.getProperty(), collection);
	}

	/**
	 * Property set
	 * 
	 * @param property
	 *            the instance variabele name
	 * @param value
	 *            the designated value
	 */
	private void setProperty(String property, Object value)
	{
		try
		{
			PropertyUtils.setProperty(toAttach, property, value);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			LOG.error("Errer restoring property", e);
		}
	}

	/**
	 * Returns a {@link EntityPersister} for the given entity class
	 * 
	 * @param val
	 *            the {@link HibernateProperty}
	 * @return {@link EntityPersister}
	 */
	protected EntityPersister getPersister(HibernateProperty val)
	{
		SessionFactoryImplementor factory = sessionImpl.getFactory();
		return factory.getEntityPersister(val.getEntityClass().getName());
	}

	/**
	 * Returns a {@link CollectionPersister} for the given entity class
	 * 
	 * @param def
	 *            the {@link AbstractPropertyDefinition}
	 * @return a {@link CollectionPersister}
	 */
	protected CollectionPersister getCollectionPersister(CollectionPropertyDefinition def)
	{
		SessionFactoryImplementor factory = sessionImpl.getFactory();
		return factory.getCollectionPersister(def.getRole());
	}
}
