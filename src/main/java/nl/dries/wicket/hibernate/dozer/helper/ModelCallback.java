package nl.dries.wicket.hibernate.dozer.helper;

import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;

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
}
