package nl.dries.wicket.hibernate.dozer.helper;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
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
	private final Map<PropertyDefinition, HibernateProperty> properties;

	/**
	 * Construct
	 * 
	 * @param toAttach
	 */
	public Attacher(T toAttach, Session session, Map<PropertyDefinition, HibernateProperty> properties)
	{
		this.toAttach = toAttach;
		this.sessionImpl = (SessionImplementor) session;
		this.properties = properties;
	}

	/**
	 * Attach
	 */
	public void doAttach()
	{
		if (properties != null)
		{
			for (Entry<PropertyDefinition, HibernateProperty> entry : properties.entrySet())
			{
				attachProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Attach a property
	 * 
	 * @param def
	 *            the {@link PropertyDefinition}
	 * @param val
	 *            the {@link HibernateProperty}
	 */
	protected void attachProperty(PropertyDefinition def, HibernateProperty val)
	{
		EntityPersister persister = getPersister(val);
		PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();

		EntityKey key = new EntityKey(val.getId(), persister, EntityMode.POJO);

		// Check existing instance
		Object instance = persistenceContext.getEntity(key);

		// No instance found
		if (instance == null)
		{
			// Also no proxy found, generate new one
			Object existing = persistenceContext.getProxy(key);
			if (existing == null)
			{
				instance = persister.createProxy(val.getId(), sessionImpl);
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
}
