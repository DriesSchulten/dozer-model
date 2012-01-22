package nl.dries.wicket.hibernate.dozer.helper;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * {@link CollectionType} test
 * 
 * @author dries
 */
public class CollectionTypeTest
{
	/**
	 * Test constructing persistent collections
	 */
	@Test
	public void testConstructPersistentInstance()
	{
		assertNotNull(CollectionType.LIST.createCollection(null));
		assertNotNull(CollectionType.SET.createCollection(null));
		assertNotNull(CollectionType.SORTED_SET.createCollection(null));
	}
}
