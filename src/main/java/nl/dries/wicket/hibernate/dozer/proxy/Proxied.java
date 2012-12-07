package nl.dries.wicket.hibernate.dozer.proxy;

import java.io.ObjectStreamException;

/**
 * Tag interface
 * 
 * @author dries
 */
public interface Proxied
{
	/**
	 * @return object that will replace this object in serialized state
	 * @throws ObjectStreamException
	 */
	Object writeReplace() throws ObjectStreamException;
}