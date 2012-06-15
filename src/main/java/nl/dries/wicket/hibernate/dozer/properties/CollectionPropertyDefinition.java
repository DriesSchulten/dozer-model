package nl.dries.wicket.hibernate.dozer.properties;

import java.lang.reflect.Field;

import nl.dries.wicket.hibernate.dozer.helper.HibernateCollectionType;
import nl.dries.wicket.hibernate.dozer.helper.ModelCallback;

/**
 * Collecition property definition
 * 
 * @author dries
 */
public class CollectionPropertyDefinition extends AbstractPropertyDefinition
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Collection type */
	private final HibernateCollectionType type;

	/**
	 * Construct
	 * 
	 * @param owner
	 *            the property owner
	 * @param property
	 *            the name of the field
	 * @param modelCallback
	 *            the {@link ModelCallback}
	 * @param type
	 *            {@link HibernateCollectionType}
	 */
	public CollectionPropertyDefinition(Object owner, String property, ModelCallback modelCallback,
		HibernateCollectionType type)
	{
		super(owner, property, modelCallback);
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public HibernateCollectionType getCollectionType()
	{
		return type;
	}

	/**
	 * @return role property (for a collection)
	 */
	public String getRole()
	{
		return getPropertyOwnerClass(getOwner().getClass()) + "." + getProperty();
	}

	/**
	 * Determines the owner of the current property (could be a superclass of the current class)
	 * 
	 * @param clazz
	 *            {@link Class}
	 * @return found ownen (class name)
	 */
	private String getPropertyOwnerClass(Class<?> clazz)
	{
		for (Field field : clazz.getDeclaredFields())
		{
			if (getProperty().equals(field.getName()))
			{
				return clazz.getName();
			}
		}

		return getPropertyOwnerClass(clazz.getSuperclass());
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition#getPropertyType()
	 */
	@Override
	public Class<?> getPropertyType()
	{
		return getCollectionType().getPlainInterface();
	}
}
