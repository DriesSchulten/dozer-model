package nl.dries.wicket.hibernate.dozer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.model.IModel;

/**
 * List model implementation of the {@link DozerModel}
 * 
 * @author dries
 * 
 * @param <T>
 *            model object type
 */
public class DozerListModel<T> implements IModel<List<T>>
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Wrapped list of models */
	private List<DozerModel<T>> models;

	/**
	 * Construct
	 * 
	 * @param objects
	 */
	public DozerListModel(List<T> objects)
	{
		this();

		innerSet(objects);
	}

	/**
	 * Construct
	 */
	public DozerListModel()
	{
		this.models = new ArrayList<>();
	}

	/**
	 * Set model object
	 * 
	 * @param objects
	 */
	private void innerSet(List<T> objects)
	{
		models = new ArrayList<>();

		if (objects != null)
		{
			models = new ArrayList<>();
			for (T obj : objects)
			{
				models.add(new DozerModel<>(obj));
			}
		}
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		for (DozerModel<T> model : models)
		{
			model.detach();
		}
	}

	/**
	 * Returns a read-only list
	 * 
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public List<T> getObject()
	{
		List<T> objects = new ArrayList<>(models.size());
		for (DozerModel<T> model : models)
		{
			objects.add(model.getObject());
		}
		return Collections.unmodifiableList(objects);
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	@Override
	public void setObject(List<T> object)
	{
		innerSet(object);
	}

	/**
	 * @see java.util.List#add(Object)
	 */
	public boolean add(T object)
	{
		addModel(new DozerModel<>(object));
		return true;
	}

	/**
	 * Add a model to the interal list
	 * 
	 * @param model
	 *            the model to add
	 */
	public void addModel(DozerModel<T> model)
	{
		models.add(model);
	}

	/**
	 * @see java.util.List#remove(Object))
	 */
	public boolean remove(T object)
	{
		return removeModel(new DozerModel<>(object));
	}

	/**
	 * Removes a model from the internal list
	 * 
	 * @param model
	 *            the model to remove
	 * @return <code>true</code> if removed
	 */
	public boolean removeModel(DozerModel<T> model)
	{
		return models.remove(model);
	}

	/**
	 * @see java.util.List#size()
	 */
	public int size()
	{
		return models.size();
	}

	/**
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty()
	{
		return models.isEmpty();
	}

	/**
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o)
	{
		return models.contains(new DozerModel<>(o));
	}

	/**
	 * @see java.util.List#clear()
	 */
	public void clear()
	{
		this.models = new ArrayList<>();
	}
}
