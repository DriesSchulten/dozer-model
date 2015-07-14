package nl.dries.wicket.hibernate.dozer.helper;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the {@link Seen} class
 *
 * @author Christian Hersevoort
 */
public class SeenTest
{
	/**
	 * new Strings because of java string pool
	 */
	private String pet1 = new String("Cat");

	private String pet2 = new String("Dog");

	private String pet3 = new String("Cat");

	/**
	 * Test whether new Seen object is empty
	 */
	@Test
	public void testEmptySeen()
	{
		Seen seen = new Seen();
		Assert.assertFalse(seen.contains(pet1));
	}

	/**
	 * Test not equals object comparison
	 */
	@Test
	public void testNotEqualCompare()
	{
		Seen seen = new Seen();
		seen.add(pet1);

		Assert.assertFalse(seen.contains(pet2));
	}

	/**
	 * Test Identity only compare
	 */
	@Test
	public void testIdentityOnlyCompare()
	{
		Seen seen = new Seen();
		seen.add(pet1);

		Assert.assertTrue(seen.contains(pet1));
		Assert.assertFalse(seen.contains(pet3));
	}

	/**
	 * Test multiple add
	 */
	@Test
	public void testMultipleAdd()
	{
		Seen seen = new Seen();
		seen.add(pet1);
		seen.add(pet1);

		Assert.assertTrue(seen.contains(pet1));
	}
}
