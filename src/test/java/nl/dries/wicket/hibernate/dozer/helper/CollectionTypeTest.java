package nl.dries.wicket.hibernate.dozer.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.internal.PersistentSortedMap;
import org.hibernate.collection.internal.PersistentSortedSet;
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
		assertNotNull(HibernateCollectionType.BAG.createCollection(null));
		assertNotNull(HibernateCollectionType.LIST.createCollection(null));
		assertNotNull(HibernateCollectionType.SET.createCollection(null));
		assertNotNull(HibernateCollectionType.SORTED_SET.createCollection(null));
		assertNotNull(HibernateCollectionType.MAP.createCollection(null));
		assertNotNull(HibernateCollectionType.SORTED_MAP.createCollection(null));
	}

	/**
	 * Determine type test
	 */
	@Test
	public void testDetermineType()
	{
		assertEquals(HibernateCollectionType.BAG, HibernateCollectionType.determineType(new PersistentBag()));
		assertEquals(HibernateCollectionType.LIST, HibernateCollectionType.determineType(new PersistentList()));
		assertEquals(HibernateCollectionType.SET, HibernateCollectionType.determineType(new PersistentSet()));
		assertEquals(HibernateCollectionType.SORTED_SET,
			HibernateCollectionType.determineType(new PersistentSortedSet()));
		assertEquals(HibernateCollectionType.MAP, HibernateCollectionType.determineType(new PersistentMap()));
		assertEquals(HibernateCollectionType.SORTED_MAP,
			HibernateCollectionType.determineType(new PersistentSortedMap()));
	}

	/**
	 * Test HibernateCollectionType implements the correct interface
	 */
	@Test
	public void testDetermineTypeInterface() throws Exception
	{
		assertTrue(HibernateCollectionType.BAG.getPlainInterface().isInstance(
			HibernateCollectionType.BAG.createCollection(null)));
		assertTrue(HibernateCollectionType.LIST.getPlainInterface().isInstance(
			HibernateCollectionType.LIST.createCollection(null)));
		assertTrue(HibernateCollectionType.SET.getPlainInterface().isInstance(
			HibernateCollectionType.SET.createCollection(null)));
		assertTrue(HibernateCollectionType.SORTED_SET.getPlainInterface().isInstance(
			HibernateCollectionType.SORTED_SET.createCollection(null)));
		assertTrue(HibernateCollectionType.MAP.getPlainInterface().isInstance(
			HibernateCollectionType.MAP.createCollection(null)));
		assertTrue(HibernateCollectionType.SORTED_MAP.getPlainInterface().isInstance(
			HibernateCollectionType.SORTED_MAP.createCollection(null)));
	}
}
