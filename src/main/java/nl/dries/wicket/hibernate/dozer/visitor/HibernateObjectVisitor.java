package nl.dries.wicket.hibernate.dozer.visitor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.helper.HibernateCollectionType;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
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
 * Visitor strategy for Hibernate objects
 * 
 * @author schulten
 */
public class HibernateObjectVisitor implements VisitorStrategy
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(HibernateObjectVisitor.class);

	/** */
	private final SessionImplementor sessionImpl;

	/** */
	private final ModelCallback callback;

	/** */
	private final ClassMetadata metadata;

	/** */
	private final Object previous;

	/**
	 * Construct
	 * 
	 * @param sessionImpl
	 * @param metadata
	 * @param previous
	 */
	public HibernateObjectVisitor(SessionImplementor sessionImpl, ModelCallback callback, ClassMetadata metadata,
		Object previous)
	{
		this.sessionImpl = sessionImpl;
		this.callback = callback;
		this.metadata = metadata;
		this.previous = previous;
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.visitor.VisitorStrategy#visit(java.lang.Object)
	 */
	@Override
	public Set<Object> visit(Object object)
	{
		Serializable identifier = metadata.getIdentifier(object, sessionImpl);

		Set<Object> toWalk = new HashSet<>();

		for (String propertyName : metadata.getPropertyNames())
		{
			Type type = metadata.getPropertyType(propertyName);
			if (type instanceof AssociationType)
			{
				Object value = ObjectHelper.getValue(object, propertyName);

				if (value != null && !value.equals(previous))
				{
					Object[] logVals = new Object[] { identifier, metadata.getMappedClass(EntityMode.POJO).getName(),
						propertyName };

					if (!Hibernate.isInitialized(value))
					{
						handleProxy(object, identifier, propertyName, value);

						LOG.debug("Detaching proxy [#{} {}.{}]", logVals);
					}
					else if (value instanceof PersistentCollection)
					{
						Object plain = convertToPlainCollection(object, propertyName, value);
						ObjectHelper.setValue(object, propertyName, plain);

						LOG.debug("Replacing initialized collection [#{} {}.{}]", logVals);

						toWalk.add(plain);
					}
					else
					{
						value = ObjectHelper.deproxy(value);
						ObjectHelper.setValue(object, propertyName, value);

						LOG.debug("Deproxying intialized value [#{} {}.{}]", logVals);

						toWalk.add(value);
					}
				}
			}
		}

		return toWalk;
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object convertToPlainCollection(Object object, String propertyName, Object value)
	{
		PersistentCollection collection = (PersistentCollection) value;
		Object plainCollection = HibernateCollectionType.determineType(collection).createPlainCollection(
			collection);

		// Deproxy all the elements in the collection
		if (plainCollection instanceof List<?>)
		{
			List list = (List) plainCollection;
			for (Iterator<?> iter = ((PersistentBag) collection).iterator(); iter.hasNext();)
			{
				list.add(ObjectHelper.deproxy(iter.next()));
			}
		}
		else if (plainCollection instanceof Set<?>)
		{
			Set set = (Set) plainCollection;
			for (Iterator<?> iter = ((PersistentSet) collection).iterator(); iter.hasNext();)
			{
				set.add(ObjectHelper.deproxy(iter.next()));
			}
		}
		else
		{
			Map map = (Map) plainCollection;
			for (Iterator<Entry<?, ?>> iter = ((PersistentMap) collection).entrySet().iterator(); iter.hasNext();)
			{
				Entry<?, ?> entry = iter.next();
				map.put(ObjectHelper.deproxy(entry.getKey()), ObjectHelper.deproxy(entry.getValue()));
			}
		}

		ObjectHelper.setValue(object, propertyName, plainCollection);
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
	private void handleProxy(Object object, Serializable identifier, String propertyName, Object value)
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
		ObjectHelper.setValue(object, propertyName, null); // Reset to null
	}
}
