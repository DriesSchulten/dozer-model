package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Visits a map with key - value combinations
 * 
 * @author dries
 */
public class MapVisitor implements VisitorStrategy
{
	/**
	 * @see nl.dries.wicket.hibernate.dozer.visitor.VisitorStrategy#visit(java.lang.Object)
	 */
	@Override
	public Set<Object> visit(Object object)
	{
		Map<?, ?> map = (Map<?, ?>) object;

		HashSet<Object> toWalk = new HashSet<>();
		toWalk.addAll(map.values());
		toWalk.addAll(map.keySet());

		return toWalk;
	}
}
