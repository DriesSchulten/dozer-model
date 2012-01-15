package nl.dries.wicket.hibernate.dozer;

import static org.junit.Assert.assertEquals;
import nl.dries.wicket.hibernate.dozer.model.Person;

import org.junit.Test;

/**
 * Test the {@link DozerModel}
 * 
 * @author dries
 */
public class DozerModelTest
{
	/**
	 * Model ceate and get
	 */
	@Test
	public void testCreateAndGet()
	{
		Person person = new Person();

		DozerModel<Person> model = new DozerModel<Person>(person);

		assertEquals(person, model.getObject());
	}

	/**
	 * Plain detach (no proxies)
	 */
	@Test
	public void testDetach()
	{
		Person person = new Person();
		person.setId(1L);

		DozerModel<Person> model = new DozerModel<Person>(person);
		model.detach();

		assertEquals(person, model.getObject());
	}
}
