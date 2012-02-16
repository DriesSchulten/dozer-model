package nl.dries.wicket.hibernate.dozer.helper;

import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;

/**
 * Callback to add/register detached properties
 * 
 * @author dries
 */
public interface ModelCallback
{
	/**
	 * Add a detached property
	 * 
	 * @param owner
	 *            the owner (<b>NO</b> Hibernate proxy)
	 * @param def
	 *            the {@link AbstractPropertyDefinition} it maps to
	 */
	void addDetachedProperty(Object owner, AbstractPropertyDefinition def);

	/**
	 * Remove a propery (because it is attached)
	 * 
	 * @param owner
	 *            the owner of the property
	 * @param def
	 *            the {@link CollectionPropertyDefinition}
	 */
	void removeProperty(Object owner, CollectionPropertyDefinition def);
}
