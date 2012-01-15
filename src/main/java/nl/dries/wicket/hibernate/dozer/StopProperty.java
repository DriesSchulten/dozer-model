package nl.dries.wicket.hibernate.dozer;

import java.io.Serializable;

/**
 * The property that indicates the end of traversing the object tree of the model object, this is to prevent
 * reading/initializing the entire db into the model when detaching.
 * 
 * @author dries
 * 
 * @param <S>
 *            type of the stop property
 */
public class StopProperty<S extends Serializable>
{
	/** Property name */
	private final String property;

	/** The type of the property */
	private final Class<S> type;

	/**
	 * Construct
	 * 
	 * @param property
	 *            name of the property
	 * @param type
	 *            it's type
	 */
	public StopProperty(String property, Class<S> type)
	{
		this.property = property;
		this.type = type;
	}

	/**
	 * @return the property
	 */
	public String getProperty()
	{
		return property;
	}

	/**
	 * @return the type
	 */
	public Class<S> getType()
	{
		return type;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("[stop %s (%s)]", property, type);
	}
}
