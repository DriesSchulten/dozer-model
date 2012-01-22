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
}
