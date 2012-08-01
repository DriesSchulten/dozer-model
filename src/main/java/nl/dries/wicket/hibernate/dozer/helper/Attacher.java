package nl.dries.wicket.hibernate.dozer.helper;

import java.io.Serializable;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Hibernate object (re-)attacher
 * 
 * @author dries
 */
public class Attacher
{
	/** The Hibernate session */
	private final SessionFinder sessionFinder;

	/** The property to attach */
	private final AbstractPropertyDefinition propertyDefinition;

	/**
	 * Construct
	 * 
	 * @param def
	 *            the {@link AbstractPropertyDefinition}
	 * @param proxy
	 *            the current, proxied, value
	 */
	public Attacher(AbstractPropertyDefinition def)
	{
		this.propertyDefinition = def;
		this.sessionFinder = def.getModelCallback().getSessionFinder();
	}

	/**
	 * Attach a property
	 * 
	 * @param def
	 *            the {@link SimplePropertyDefinition}
	 * @return the value of the property
	 */
	protected Object attach(SimplePropertyDefinition def)
	{
		SessionImplementor sessionImpl = (SessionImplementor) sessionFinder.getHibernateSession(def
			.getHibernateProperty().getEntityClass());

		EntityPersister persister = getPersister(def.getHibernateProperty(), sessionImpl);
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
		return instance;
	}

	/**
	 * Attach a collection
	 * 
	 * @param def
	 *            the {@link CollectionPropertyDefinition}
	 */
	protected Object attach(CollectionPropertyDefinition def)
	{
		SessionImplementor sessionImpl = (SessionImplementor) sessionFinder.getHibernateSession(def.getOwner()
			.getClass());

		CollectionPersister persister = getCollectionPersister(def, sessionImpl);
		PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();

		ClassMetadata metadata = sessionImpl.getFactory().getClassMetadata(def.getOwner().getClass());
		Serializable identifier = metadata.getIdentifier(def.getOwner(), sessionImpl);

		CollectionKey key = new CollectionKey(persister, identifier);
		PersistentCollection collection = persistenceContext.getCollection(key);
		if (collection == null)
		{
			collection = def.getCollectionType().createCollection(sessionImpl);
			collection.setSnapshot(identifier, def.getRole(), null); // Sort of 'fake' state...

			persistenceContext.addUninitializedDetachedCollection(persister, collection);
			persistenceContext.addUnownedCollection(key, collection);
		}

		// Return value
		return collection;
	}

	/**
	 * Returns a {@link EntityPersister} for the given entity class
	 * 
	 * @param val
	 *            the {@link HibernateProperty}
	 * @return {@link EntityPersister}
	 */
	protected EntityPersister getPersister(HibernateProperty val, SessionImplementor sessionImpl)
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
	protected CollectionPersister getCollectionPersister(CollectionPropertyDefinition def,
		SessionImplementor sessionImpl)
	{
		SessionFactoryImplementor factory = sessionImpl.getFactory();
		return factory.getCollectionPersister(def.getRole());
	}

	/**
	 * @param owner
	 *            the owner
	 * @return the {@link EntityPersister} for the object to attach
	 */
	protected EntityPersister getOwnPersister(Object owner, SessionImplementor sessionImpl)
	{
		SessionFactoryImplementor factory = sessionImpl.getFactory();
		return factory.getEntityPersister(owner.getClass().getName());
	}

	/**
	 * Attach driver
	 * 
	 * @return the attached object
	 */
	public Object attach()
	{
		if (propertyDefinition instanceof SimplePropertyDefinition)
		{
			return attach((SimplePropertyDefinition) propertyDefinition);
		}

		return attach((CollectionPropertyDefinition) propertyDefinition);
	}
}
