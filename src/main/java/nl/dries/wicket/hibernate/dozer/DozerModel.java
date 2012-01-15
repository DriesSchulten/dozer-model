package nl.dries.wicket.hibernate.dozer;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.apache.wicket.model.IModel;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

/**
 * @author dries
 * 
 * @param <T>
 *            type of model object
 * @param <S>
 *            object to stop traversing object tree (to prevent storing entire db in model), when not needed pass
 *            {@link java.lang.Void}
 */
public class DozerModel<T, S extends Serializable> implements IModel<T>
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Object instance */
	private T object;

	/** Detached object instance */
	private T detachedObject;

	/** The object's {@link Class} */
	private Class<T> objectClass;

	/** Stop */
	private StopProperty<S> stop;

	/**
	 * Construct
	 * 
	 * @param object
	 */
	public DozerModel(T object)
	{
		this(object, null);
	}

	/**
	 * Construct
	 * 
	 * @param object
	 * @param stop
	 *            the {@link StopProperty}
	 */
	@SuppressWarnings("unchecked")
	public DozerModel(T object, StopProperty<S> stop)
	{
		this.object = object;
		this.stop = stop;

		this.objectClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
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
		}
	}

	private Mapper createMapper()
	{
		Mapper mapper = new DozerBeanMapper();
		return mapper;
	}
}
