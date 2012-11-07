package nl.dries.wicket.hibernate.dozer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.dries.wicket.hibernate.dozer.helper.Attacher;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.visitor.ObjectVisitor;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
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

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(DozerModel.class);

	/** Object with proxied properties */
	private final List<AbstractPropertyDefinition> proxiedProperties = new ArrayList<>();

	/** Object instance */
	private T object;

	/** Detached object instance */
	private T detachedObject;

	/**
	 * Construct
	 * 
	 * @param object
	 */
	public DozerModel(T object)
	{
		this();
		this.object = object;
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

		this.object = (T) SessionFinderHolder.getSessionFinder().getHibernateSession(objectClass).load(objectClass, id);
	}

	/**
	 * Construct
	 */
	public DozerModel()
	{
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
			List<AbstractPropertyDefinition> proxiedClone = new ArrayList<>(proxiedProperties);
			for (AbstractPropertyDefinition def : proxiedClone)
			{
				ObjectHelper.setValue(def.getOwner(), def.getProperty(), new Attacher(def).attach());
			}
			proxiedProperties.clear();

			object = detachedObject;

			// Remove detached state
			detachedObject = null;
		}

		return object;
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	@Override
	public void setObject(T object)
	{
		// Reset previous object state
		detachedObject = null;

		this.object = object;
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void detach()
	{
		boolean doDetach = true;

		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null)
		{
			Boolean val = requestCycle.getMetaData(DozerRequestCycleListener.ENDING_REQUEST);
			doDetach = val == null || val.booleanValue();
		}

		LOG.debug("Detaching in onEndRequest? {}", doDetach);

		if (doDetach && object != null && detachedObject == null)
		{
			if (object instanceof HibernateProxy)
			{
				HibernateProxy proxy = (HibernateProxy) object;
				object = (T) proxy.getHibernateLazyInitializer().getImplementation();
			}

			ObjectVisitor<T> walker = new ObjectVisitor<>(object, getSessionFinder(), this);
			detachedObject = walker.walk();

			object = null;
		}
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.helper.ModelCallback#getSessionFinder()
	 */
	@Override
	public SessionFinder getSessionFinder()
	{
		return SessionFinderHolder.getSessionFinder();
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.helper.ModelCallback#addProxiedProperty
	 *      (nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition)
	 */
	public void addProxiedProperty(AbstractPropertyDefinition property)
	{
		proxiedProperties.add(property);
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
