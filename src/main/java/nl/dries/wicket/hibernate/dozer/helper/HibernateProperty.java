package nl.dries.wicket.hibernate.dozer.helper;

import java.io.Serializable;

/**
 * Represents a detached Hibernate property
 * 
 * @author dries
 */
public class HibernateProperty implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** The class of the entity */
	private final Class<? extends Serializable> entityClass;

	/** The identifier of the entity */
	private final Serializable id;

	/**
	 * Construct
	 * 
	 * @param entityClass
	 *            the class of the entity
	 * @param id
	 *            it's identifier
	 */
	public HibernateProperty(Class<? extends Serializable> entityClass, Serializable id)
	{
		this.entityClass = entityClass;
		this.id = id;
	}

	/**
	 * @return the entityClass
	 */
	public Class<? extends Serializable> getEntityClass()
	{
		return entityClass;
	}

	/**
	 * @return the id
	 */
	public Serializable getId()
	{
		return id;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityClass == null) ? 0 : entityClass.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		HibernateProperty other = (HibernateProperty) obj;
		if (entityClass == null)
		{
			if (other.entityClass != null)
			{
				return false;
			}
		}
		else if (!entityClass.equals(other.entityClass))
		{
			return false;
		}
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}
		return true;
	}
}
