package nl.dries.wicket.hibernate.dozer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.dries.wicket.hibernate.dozer.helper.Attacher;
import nl.dries.wicket.hibernate.dozer.helper.HibernateFieldMapper;
import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.PropertyDefinition;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
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
	private List<PropertyDefinition> detachedCollections;

	/** Map containing detached properties */
	private Map<PropertyDefinition, HibernateProperty> detachedProperties;

	/** */
	@SpringBean
	private SessionFinder sessionFinder;

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

		Injector.get().inject(this);
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

			// Re-attach properties
			new Attacher<T>(object, sessionFinder.getSession(), detachedProperties, detachedCollections).doAttach();

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
			DozerBeanMapper mapper = createMapper();
			detachedObject = mapper.map(object, objectClass);
			object = null;
		}
	}

	/**
	 * Add a detached collection
	 * 
	 * @param property
	 *            the {@link PropertyDefinition} it maps to
	 */
	public void addDetachedCollection(PropertyDefinition property)
	{
		if (detachedCollections == null)
		{
			detachedCollections = new ArrayList<>();
		}

		detachedCollections.add(property);
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
	private DozerBeanMapper createMapper()
	{
		DozerBeanMapper mapper = new DozerBeanMapper();
		mapper.setCustomFieldMapper(new HibernateFieldMapper(this));
		return mapper;
	}
}
