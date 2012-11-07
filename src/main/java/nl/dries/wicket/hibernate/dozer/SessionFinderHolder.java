package nl.dries.wicket.hibernate.dozer;

/**
 * Static holder for the {@link SessionFinder}
 * 
 * @author dries
 */
public final class SessionFinderHolder
{
	/** Known finder */
	private static SessionFinder sessionFinder;

	/**
	 * @return the current {@link SessionFinder}
	 */
	public static SessionFinder getSessionFinder()
	{
		return sessionFinder;
	}

	/**
	 * @param sessionFinder
	 *            the {@link SessionFinder} to set
	 */
	public static void setSessionFinder(SessionFinder sessionFinder)
	{
		SessionFinderHolder.sessionFinder = sessionFinder;
	}
}
