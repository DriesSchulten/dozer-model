package nl.dries.wicket.hibernate.dozer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import nl.dries.wicket.hibernate.dozer.model.Adres;
import nl.dries.wicket.hibernate.dozer.model.Person;

import org.junit.Test;

/**
 * Test the {@link DozerModel}
 * 
 * @author dries
 */
public class DozerModelTest extends AbstractWicketHibernateTest
{
	/**
	 * Model ceate and get
	 */
	@Test
	public void testCreateAndGet()
	{
		Person person = new Person();

		DozerModel<Person> model = new DozerModel<>(person);

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

		DozerModel<Person> model = new DozerModel<>(person);
		model.detach();

		assertEquals(person, model.getObject());
		assertFalse(person == model.getObject()); // Different instance
	}

	/**
	 * Detach with proxies
	 */
	@Test
	public void testLoadedDetach()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("test");

		Adres adres = new Adres();
		adres.setId(1L);
		adres.setStreet("street");
		adres.setPerson(person);
		person.getAdresses().add(adres);

		getSession().saveOrUpdate(person);
		getSession().flush();
		getSession().clear();

		adres.setPerson((Person) getSession().load(Person.class, 1L)); // Forcing proxy
		DozerModel<Adres> model = new DozerModel<>(adres);
		model.detach();

		assertEquals(adres, model.getObject());
		assertEquals(person, model.getObject().getPerson());
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.AbstractWicketHibernateTest#getEntities()
	 */
	@Override
	protected List<Class<? extends Serializable>> getEntities()
	{
		return Arrays.asList(Adres.class, Person.class);
	}
}
