package nl.dries.wicket.hibernate.dozer.helper;

import java.io.Serializable;

public class PropertyDefinition implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** Class of the property */
	private final Class<? extends Serializable> propertyClass;

	/** Property name in its containing instance */
	private final String property;

	/**
	 * Contsruct
	 * 
	 * @param propertyClass
	 *            the class
	 * @param property
	 *            its name
	 */
	public PropertyDefinition(Class<? extends Serializable> propertyClass, String property)
	{
		this.propertyClass = propertyClass;
		this.property = property;
	}

	/**
	 * @return the propertyClass
	 */
	public Class<? extends Serializable> getPropertyClass()
	{
		return propertyClass;
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
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((propertyClass == null) ? 0 : propertyClass.hashCode());
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
		PropertyDefinition other = (PropertyDefinition) obj;
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
		if (propertyClass == null)
		{
			if (other.propertyClass != null)
			{
				return false;
			}
		}
		else if (!propertyClass.equals(other.propertyClass))
		{
			return false;
		}
		return true;
	}
}
