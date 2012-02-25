package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Visits a collection
 * 
 * @author dries
 */
public class CollectionVisitor implements VisitorStrategy
{
	/**
	 * @see nl.dries.wicket.hibernate.dozer.visitor.VisitorStrategy#visit(java.lang.Object)
	 */
	@Override
	public Set<Object> visit(Object object)
	{
		Collection<?> collection = (Collection<?>) object;

		HashSet<Object> toWalk = new HashSet<>();
		toWalk.addAll(collection);

		return toWalk;
	}
}
