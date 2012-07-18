package nl.dries.wicket.hibernate.dozer.helper;

import nl.dries.wicket.hibernate.dozer.SessionFinder;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;

/**
 * Callback to add/register detached properties
 * 
 * @author dries
 */
public interface ModelCallback
{
	/**
	 * @return current {@link SessionFinder}
	 */
	SessionFinder getSessionFinder();

	/**
	 * Mark this property as proxied
	 * 
	 * @param property
	 *            the {@link AbstractPropertyDefinition}
	 */
	void addProxiedProperty(AbstractPropertyDefinition property);
}
