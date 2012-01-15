package nl.dries.wicket.hibernate.dozer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nl.dries.wicket.hibernate.dozer.helper.HibernateFieldMapper;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.PropertyDefinition;

import org.apache.wicket.model.IModel;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.hibernate.Hibernate;

/**
 * @author dries
 * 
 * @param <T>
 *            type of model object
 */
public class DozerModel<T> implements IModel<T>
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Object instance */
	private T object;

	/** Detached object instance */
	private T detachedObject;

	/** The object's {@link Class} */
	private Class<T> objectClass;

	/** Map containing detached collections */
	private Map<PropertyDefinition, Collection<HibernateProperty>> detachedCollections;

	/** Map containing detached properties */
	private Map<PropertyDefinition, HibernateProperty> detachedProperties;

	/**
	 * Construct
	 * 
	 * @param object
	 */
	@SuppressWarnings("unchecked")
	public DozerModel(T object)
	{
		this.object = object;

		if (object != null)
		{
			this.objectClass = Hibernate.getClass(object);
		}
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public T getObject()
	{
		// Possibly restore detached state
		if (object == null && detachedObject != null)
		{
			object = detachedObject;

			// Remove detached state
			detachedObject = null;
			detachedCollections = null;
			detachedProperties = null;
		}
		return object;
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	@Override
	public void setObject(T object)
	{
		this.object = object;
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		if (object != null && detachedObject == null)
		{
			detachedObject = createMapper().map(object, objectClass);
			object = null;
		}
	}

	/**
	 * Add a detached collection
	 * 
	 * @param property
	 *            the {@link PropertyDefinition} it maps to
	 * @param collection
	 *            the detached collection
	 */
	public void addDetachedCollection(PropertyDefinition property, Collection<HibernateProperty> collection)
	{
		if (detachedCollections == null)
		{
			detachedCollections = new HashMap<>();
		}

		detachedCollections.put(property, collection);
	}

	/**
	 * Add a detached property
	 * 
	 * @param property
	 *            the {@link PropertyDefinition}
	 * @param value
	 *            its value
	 */
	public void addDetachedProperty(PropertyDefinition property, HibernateProperty value)
	{
		if (detachedProperties == null)
		{
			detachedProperties = new HashMap<>();
		}

		detachedProperties.put(property, value);
	}

	/**
	 * @return {@link Mapper} instance
	 */
	private Mapper createMapper()
	{
		DozerBeanMapper mapper = new DozerBeanMapper();
		mapper.setCustomFieldMapper(new HibernateFieldMapper(this));
		return mapper;
	}
}
