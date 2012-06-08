package nl.dries.wicket.hibernate.dozer.properties;

import nl.dries.wicket.hibernate.dozer.helper.HibernateProperty;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;

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
	private final HibernateProperty hibernateProperty;

	/**
	 * Construct
	 * 
	 * @param owner
	 *            the property owner
	 * @param property
	 *            the name of the field
	 * @parma modelCallback the {@link ModelCallback}
	 * @param hibernateProperty
	 *            it's {@link HibernateProperty}
	 */
	public SimplePropertyDefinition(Object owner, String property, ModelCallback modelCallback,
		HibernateProperty hibernateProperty)
	{
		super(owner, property, modelCallback);
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
	 * @see nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition#getPropertyType()
	 */
	@Override
	public Class<?> getPropertyType()
	{
		return getHibernateProperty().getEntityClass();
	}
}
