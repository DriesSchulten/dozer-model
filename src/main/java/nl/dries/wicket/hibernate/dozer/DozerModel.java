package nl.dries.wicket.hibernate.dozer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.dries.wicket.hibernate.dozer.helper.Attacher;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;
import nl.dries.wicket.hibernate.dozer.walker.ObjectWalker;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dozer Wicket Hibernate model. This model wil act as a detachable model. When detaching all initalized objects in the
 * object graph are cloned/copied using Dozer. This way all Hibernate information (initializer containing session state)
 * is discarded, so when reattaching the object we won't get Lazy/session closed exceptions.<br>
 * <br>
 * All un-initalized proxies in the object graph are saved to lightweight pointers (not containing Hibernate state).
 * When the object is re-attached these pointers will be restored as 'normal' Hibernate proxies that will get
 * initialized on access.
 * 
 * @author dries
 * 
 * @param <T>
 *            type of model object
 */
public class DozerModel<T> implements IModel<T>, ModelCallback
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	private static final String CGLIB_ID = "$$EnhancerByCGLIB$$";

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(DozerModel.class);

	/** Object instance */
	private T object;

	/** Detached object instance */
	private T detachedObject;

	/** Map containing detached properties */
	private Map<Object, List<SimplePropertyDefinition>> detachedProperties;

	/** Map containing detached collection */
	private Map<Object, List<CollectionPropertyDefinition>> detachedCollections;

	/** */
	@SpringBean
	private SessionFinder sessionFinder;

	/**
	 * Construct
	 * 
	 * @param object
	 */
	public DozerModel(T object)
	{
		this();

		this.object = object;

		if (object != null)
		{
			if (object.getClass().getName().contains(CGLIB_ID))
			{
				LOG.warn("Given object is a Cglib proxy this can cause unexpected behavior, " + object);
			}
		}
	}

	/**
	 * Construct with class/id, <b>will directly load the object!!!</b> (but not initialize it)
	 * 
	 * @param objectClass
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public DozerModel(Class<T> objectClass, Serializable id)
	{
		this();

		this.object = (T) sessionFinder.getHibernateSession().load(objectClass, id);
	}

	/**
	 * Construct
	 */
	public DozerModel()
	{
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

			List<Attacher<Object>> list = buildAttachers(detachedProperties);
			if (list != null)
			{
				for (Attacher<Object> attacher : list)
				{
					attacher.doAttach();
				}
			}

			// Remove detached state
			detachedObject = null;
		}

		// We always need to re-attach unintialized collections, when Hiberate flushes the collections are re-wrapped
		// (creating new collection proxies) this means that is is possible that a flush has happend between our 2
		// getObject calls within the same request-cycle and thus invalidating or created collection proxy.
		List<Attacher<Object>> list = buildAttachers(detachedCollections);
		if (list != null)
		{
			for (Attacher<Object> attacher : list)
			{
				attacher.doAttach();
			}
		}

		return object;
	}

	/**
	 * @param detached
	 *            the map for which to build {@link Attacher}s
	 * @return set with {@link Attacher}s
	 */
	@SuppressWarnings("unchecked")
	private List<Attacher<Object>> buildAttachers(Map<?, ?> detached)
	{
		List<Attacher<Object>> list = null;

		if (detached != null)
		{
			list = new ArrayList<>(detached.size());

			// Re-attach properties
			for (Entry<?, ?> entry : detached.entrySet())
			{
				list.add(new Attacher<Object>(entry.getKey(), sessionFinder.getHibernateSession(), new ArrayList<>(
					(List<? extends AbstractPropertyDefinition>) entry.getValue()), this));
			}
		}

		return list;
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
	@SuppressWarnings("unchecked")
	@Override
	public void detach()
	{
		if (object != null && detachedObject == null)
		{
			// Reset previous detached state
			detachedProperties = null;
			detachedCollections = null;

			if (object instanceof HibernateProxy)
			{
				HibernateProxy proxy = (HibernateProxy) object;
				object = (T) proxy.getHibernateLazyInitializer().getImplementation();
			}

			ObjectWalker<T> walker = new ObjectWalker<>(object, sessionFinder, this);
			detachedObject = walker.walk();

			object = null;
		}
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.helper.ModelCallback#addDetachedProperty(java.lang.Object,
	 *      nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition)
	 */
	@Override
	public void addDetachedProperty(Object owner, AbstractPropertyDefinition def)
	{
		if (def instanceof SimplePropertyDefinition)
		{
			if (detachedProperties == null)
			{
				detachedProperties = new HashMap<>();
			}

			if (!detachedProperties.containsKey(owner))
			{
				detachedProperties.put(owner, new ArrayList<SimplePropertyDefinition>());
			}

			detachedProperties.get(owner).add((SimplePropertyDefinition) def);
		}
		else
		{
			if (detachedCollections == null)
			{
				detachedCollections = new HashMap<>();
			}

			if (!detachedCollections.containsKey(owner))
			{
				detachedCollections.put(owner, new ArrayList<CollectionPropertyDefinition>());
			}

			detachedCollections.get(owner).add((CollectionPropertyDefinition) def);
		}
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.helper.ModelCallback#removeProperty(java.lang.Object,
	 *      nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition)
	 */
	@Override
	public void removeProperty(Object owner, CollectionPropertyDefinition def)
	{
		if (detachedCollections != null)
		{
			List<CollectionPropertyDefinition> list = detachedCollections.get(owner);
			if (list != null)
			{
				list.remove(def);
			}
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getObject() == null) ? 0 : getObject().hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!IModel.class.isAssignableFrom(obj.getClass()))
		{
			return false;
		}
		IModel<T> other = (IModel<T>) obj;
		if (getObject() == null)
		{
			if (other.getObject() != null)
			{
				return false;
			}
		}
		else if (!getObject().equals(other.getObject()))
		{
			return false;
		}
		return true;
	}
}
