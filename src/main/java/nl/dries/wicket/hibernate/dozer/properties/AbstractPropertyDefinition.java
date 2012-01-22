package nl.dries.wicket.hibernate.dozer.properties;

import java.io.Serializable;

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
	private final Class<? extends Serializable> owner;

	/** Property name in its containing instance */
	private final String property;

	/** Id of the owner */
	private final Serializable ownerId;

	/**
	 * Contsruct
	 * 
	 * @param owner
	 *            the class of the property owner
	 * @param property
	 *            its name
	 */
	public AbstractPropertyDefinition(Class<? extends Serializable> owner, Serializable ownerId, String property)
	{
		this.owner = owner;
		this.property = property;
		this.ownerId = ownerId;
	}

	/**
	 * @return the owner
	 */
	public Class<? extends Serializable> getOwner()
	{
		return owner;
	}

	/**
	 * @return the ownerId
	 */
	public Serializable getOwnerId()
	{
		return ownerId;
	}

	/**
	 * @return the property
	 */
	public String getProperty()
	{
		return property;
	}
}
