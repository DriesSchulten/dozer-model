package nl.dries.wicket.hibernate.dozer.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedSet;
import org.junit.Test;

/**
 * {@link HibernateCollectionType} test
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
		assertNotNull(HibernateCollectionType.LIST.createCollection(null));
		assertNotNull(HibernateCollectionType.SET.createCollection(null));
		assertNotNull(HibernateCollectionType.SORTED_SET.createCollection(null));
	}

	/**
	 * Dermine type test
	 */
	@Test
	public void testDetermineType()
	{
		assertEquals(HibernateCollectionType.LIST, HibernateCollectionType.determineType(new PersistentBag()));
		assertEquals(HibernateCollectionType.SET, HibernateCollectionType.determineType(new PersistentSet()));
		assertEquals(HibernateCollectionType.SORTED_SET,
			HibernateCollectionType.determineType(new PersistentSortedSet()));
	}
}
