package nl.dries.wicket.hibernate.dozer.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedSet;
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

	/**
	 * Dermine type test
	 */
	@Test
	public void testDetermineType()
	{
		assertEquals(CollectionType.LIST, CollectionType.determineType(new PersistentBag()));
		assertEquals(CollectionType.SET, CollectionType.determineType(new PersistentSet()));
		assertEquals(CollectionType.SORTED_SET, CollectionType.determineType(new PersistentSortedSet()));
	}
}
