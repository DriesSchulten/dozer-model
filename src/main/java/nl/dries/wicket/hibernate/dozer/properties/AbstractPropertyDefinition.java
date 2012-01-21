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

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AbstractPropertyDefinition other = (AbstractPropertyDefinition) obj;
		if (owner == null)
		{
			if (other.owner != null)
			{
				return false;
			}
		}
		else if (!owner.equals(other.owner))
		{
			return false;
		}
		if (ownerId == null)
		{
			if (other.ownerId != null)
			{
				return false;
			}
		}
		else if (!ownerId.equals(other.ownerId))
		{
			return false;
		}
		if (property == null)
		{
			if (other.property != null)
			{
				return false;
			}
		}
		else if (!property.equals(other.property))
		{
			return false;
		}
		return true;
	}
}
