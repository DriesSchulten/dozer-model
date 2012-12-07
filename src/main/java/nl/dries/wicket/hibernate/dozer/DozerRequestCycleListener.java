package nl.dries.wicket.hibernate.dozer;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Dozer request cycle listener, records when a request is ending, but before everything is detached. We are using this
 * to record when we end a request because Wicket also calls <code>onDetatch</code> while replacing components and we
 * only want to detach when the request is over.
 * 
 * @author dries
 */
public class DozerRequestCycleListener extends AbstractRequestCycleListener
{
	/**
	 * Meta data key, records when we are ending a request
	 */
	public static final MetaDataKey<Boolean> ENDING_REQUEST = new MetaDataKey<Boolean>()
	{
		/** Default */
		private static final long serialVersionUID = 1L;
	};

	/**
	 * @see org.apache.wicket.request.cycle.AbstractRequestCycleListener#onBeginRequest(org.apache.wicket.request.cycle.RequestCycle)
	 */
	@Override
	public void onBeginRequest(RequestCycle cycle)
	{
		cycle.setMetaData(ENDING_REQUEST, false);
	}

	/**
	 * @see org.apache.wicket.request.cycle.AbstractRequestCycleListener#onEndRequest(org.apache.wicket.request.cycle.RequestCycle)
	 */
	@Override
	public void onEndRequest(RequestCycle cycle)
	{
		cycle.setMetaData(ENDING_REQUEST, true);
	}
}
