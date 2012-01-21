package nl.dries.wicket.hibernate.dozer.properties;

import java.io.Serializable;

import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;

/**
 * 'Simple' property
 * 
 * @author dries
 */
public class SimplePropertyDefinition extends AbstractPropertyDefinition
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Hibernate property */
	private HibernateProperty hibernateProperty;

	/**
	 * Construct
	 * 
	 * @param owner
	 *            the {@link Class} of the property owner
	 * @param ownerId
	 *            it's id
	 * @param property
	 *            the name of the field
	 * @param hibernateProperty
	 *            it's {@link HibernateProperty}
	 */
	public SimplePropertyDefinition(Class<? extends Serializable> owner, Serializable ownerId, String property,
		HibernateProperty hibernateProperty)
	{
		super(owner, ownerId, property);
		this.hibernateProperty = hibernateProperty;
	}

	/**
	 * @return the hibernateProperty
	 */
	public HibernateProperty getHibernateProperty()
	{
		return hibernateProperty;
	}

	/**
	 * @param hibernateProperty
	 *            the hibernateProperty to set
	 */
	public void setHibernateProperty(HibernateProperty hibernateProperty)
	{
		this.hibernateProperty = hibernateProperty;
	}
}
