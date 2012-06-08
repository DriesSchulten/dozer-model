package nl.dries.wicket.hibernate.dozer.properties;

import java.io.Serializable;

import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;

/**
 * The definition of a detached Hibernate property
 * 
 * @author dries
 */
public abstract class AbstractPropertyDefinition implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Owning entity */
	private final Object owner;

	/** Property name in its containing instance */
	private final String property;

	/** Model reference */
	private final ModelCallback modelCallback;

	/**
	 * Contsruct
	 * 
	 * @param owner
	 *            the property owner
	 * @param property
	 *            its name
	 * @param model
	 *            the enclosing model
	 */
	public AbstractPropertyDefinition(Object owner, String property, ModelCallback modelCallback)
	{
		this.owner = owner;
		this.property = property;
		this.modelCallback = modelCallback;
	}

	/**
	 * @return the owner
	 */
	public Object getOwner()
	{
		return owner;
	}

	/**
	 * @return the property
	 */
	public String getProperty()
	{
		return property;
	}

	/**
	 * @return the model
	 */
	public ModelCallback getModel()
	{
		return modelCallback;
	}

	/**
	 * @return the property type
	 */
	public abstract Class<?> getPropertyType();
}
