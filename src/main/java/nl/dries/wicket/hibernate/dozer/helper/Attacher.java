package nl.dries.wicket.hibernate.dozer.helper;

import java.util.List;

import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

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
	/** The Hibernate session */
	private final SessionImplementor sessionImpl;

	/** The object to attach */
	private final T toAttach;

	/** Properties to attach */
	private final List<? extends AbstractPropertyDefinition> properties;

	/** Callback to the model */
	private final ModelCallback callback;

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
	 * @parma callback the {@link ModelCallback}
	 */
	public Attacher(T toAttach, Session session, List<? extends AbstractPropertyDefinition> detachedProperties,
		ModelCallback callback)
	{
		this.toAttach = toAttach;
		this.sessionImpl = (SessionImplementor) session;
		this.properties = detachedProperties;
		this.callback = callback;
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

		EntityKey key = new EntityKey(def.getHibernateProperty().getId(), persister, sessionImpl.getTenantIdentifier());

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
		ObjectHelper.setValue(toAttach, def.getProperty(), instance);
	}

	/**
	 * Attach a collection
	 * 
	 * @param def
	 *            the {@link CollectionPropertyDefinition}
	 */
	protected void attach(CollectionPropertyDefinition def)
	{
		Object currentValue = ObjectHelper.getValue(toAttach, def.getProperty());

		if (!isInitialized(currentValue))
		{
			CollectionPersister persister = getCollectionPersister(def);
			PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();

			PersistentCollection collection = def.getCollectionType().createCollection(sessionImpl);
			collection.setOwner(toAttach);
			collection.setSnapshot(def.getOwnerId(), def.getRole(), null); // Sort of 'fake' state...

			persistenceContext.addUninitializedCollection(persister, collection, def.getOwnerId());

			// Restore value
			ObjectHelper.setValue(toAttach, def.getProperty(), collection);
		}
		else
		{
			callback.removeProperty(toAttach, def);
		}
	}

	/**
	 * Check if the current object is initialized
	 * 
	 * @param currentValue
	 *            the current (collection) object
	 * @return <code>true</code> when initialized
	 */
	private boolean isInitialized(Object currentValue)
	{
		boolean initialized = currentValue != null;

		if (currentValue instanceof PersistentCollection)
		{
			PersistentCollection persistentCollection = (PersistentCollection) currentValue;
			initialized = persistentCollection.wasInitialized();
		}

		return initialized;
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
