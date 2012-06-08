package nl.dries.wicket.hibernate.dozer.helper;

import nl.dries.wicket.hibernate.dozer.SessionFinder;

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
}
