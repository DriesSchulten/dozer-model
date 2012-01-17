package nl.dries.wicket.hibernate.dozer;

import java.util.ArrayList;
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
		if (objects != null)
		{
			for (T obj : objects)
			{
				models.add(new DozerModel<>(obj));
			}
		}
		else
		{
			models = new ArrayList<>();
		}
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		if (models != null)
		{
			for (DozerModel<T> model : models)
			{
				model.detach();
			}
		}
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public List<T> getObject()
	{
		if (models != null && !models.isEmpty())
		{
			List<T> objects = new ArrayList<>(models.size());
			for (DozerModel<T> model : models)
			{
				objects.add(model.getObject());
			}
			return objects;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
	 */
	@Override
	public void setObject(List<T> object)
	{
		innerSet(object);
	}
}
