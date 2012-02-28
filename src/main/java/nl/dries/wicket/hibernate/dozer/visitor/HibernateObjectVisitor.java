package nl.dries.wicket.hibernate.dozer.visitor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import nl.dries.wicket.hibernate.dozer.helper.HibernateCollectionType;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;

public class HibernateObjectVisitor implements VisitorStrategy
{
	/** */
	private final SessionImplementor sessionImpl;

	/** */
	private final ModelCallback callback;

	/** */
	private final ClassMetadata metadata;

	/**
	 * Construct
	 * 
	 * @param sessionImpl
	 * @param metadata
	 */
	public HibernateObjectVisitor(SessionImplementor sessionImpl, ModelCallback callback, ClassMetadata metadata)
	{
		this.sessionImpl = sessionImpl;
		this.callback = callback;
		this.metadata = metadata;
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

				if (value != null)
				{
					if (!Hibernate.isInitialized(value))
					{
						handleProxy(object, identifier, propertyName, value);
					}
					else if (value instanceof PersistentCollection)
					{
						Object plain = convertToPlainCollection(object, propertyName, value);
						ObjectHelper.setValue(object, propertyName, plain);
						toWalk.add(plain);
					}
					else
					{
						value = ObjectHelper.deproxy(value);
						ObjectHelper.setValue(object, propertyName, value);
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
	private Object convertToPlainCollection(Object object, String propertyName, Object value)
	{
		PersistentCollection collection = (PersistentCollection) value;
		Object plainCollection = HibernateCollectionType.determineType(collection).createPlainCollection(
			collection);
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
