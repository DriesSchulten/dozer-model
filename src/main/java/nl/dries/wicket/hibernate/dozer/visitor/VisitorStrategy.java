package nl.dries.wicket.hibernate.dozer.visitor;

import java.util.Set;

public interface VisitorStrategy
{
	/**
	 * Walk a object, visiting its properties and marking them as detached or adding them to the set of objects to
	 * visit.
	 * 
	 * @param object
	 *            the current object
	 * @return a {@link Set} with other objects to visit
	 */
	Set<Object> visit(Object object);
}
