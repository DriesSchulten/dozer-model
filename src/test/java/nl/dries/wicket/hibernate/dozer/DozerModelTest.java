package nl.dries.wicket.hibernate.dozer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import nl.dries.wicket.hibernate.dozer.model.AbstractOrganization;
import nl.dries.wicket.hibernate.dozer.model.AbstractTreeObject;
import nl.dries.wicket.hibernate.dozer.model.Adres;
import nl.dries.wicket.hibernate.dozer.model.Company;
import nl.dries.wicket.hibernate.dozer.model.DescTreeObject;
import nl.dries.wicket.hibernate.dozer.model.Person;
import nl.dries.wicket.hibernate.dozer.model.RootTreeObject;

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
	 * Detach with list proxy
	 */
	@Test
	public void testLoadedListDetach()
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

		person.setAdresses(Arrays.asList((Adres) getSession().load(Adres.class, 1L))); // Forcing proxy
		DozerModel<Person> model = new DozerModel<>(person);
		model.detach();

		assertEquals(person, model.getObject());
		assertEquals(adres, model.getObject().getAdresses().get(0));
	}

	/**
	 * Tree test
	 */
	@Test
	public void testWithTree()
	{
		DozerModel<RootTreeObject> model = new DozerModel<>(RootTreeObject.class, buildTree().getId());

		model.getObject().getName(); // Root initialization
		model.detach();

		assertEquals(1, model.getObject().getChildren().size());
		assertEquals(1, model.getObject().getChildren().get(0).getChildren().size());
	}

	/**
	 * Tree test, using child as a starting point
	 */
	@Test
	public void testWithTreeUsingChild()
	{
		buildTree();

		DescTreeObject child = (DescTreeObject) getSession().load(DescTreeObject.class, 2L);
		child.setParent((RootTreeObject) getSession().load(RootTreeObject.class, 1L));

		DozerModel<DescTreeObject> model = new DozerModel<>(child);
		model.detach();

		assertEquals(1, model.getObject().getChildren().size());
		assertEquals("root", model.getObject().getParent().getName());
	}

	/**
	 * Tree
	 */
	@Test
	public void testTree()
	{
		DozerModel<AbstractTreeObject> model = new DozerModel<>(buildTree());
		model.detach();
	}

	/**
	 * Abstract property in mapping
	 */
	@Test
	public void testWithAbstractProperty()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("name");

		Company company = new Company();
		company.setId(1L);
		person.setOrganization(company);

		DozerModel<Person> model = new DozerModel<>(person);
		model.detach();

		assertEquals(company, model.getObject().getOrganization());
	}

	/**
	 * @return root node
	 */
	private AbstractTreeObject buildTree()
	{
		AbstractTreeObject root = new RootTreeObject(1L, "root");
		getSession().saveOrUpdate(root);

		AbstractTreeObject c1l1 = new DescTreeObject(2L, "c1l1");
		c1l1.setParent(root);
		root.getChildren().add(c1l1);
		getSession().saveOrUpdate(c1l1);

		AbstractTreeObject c1l2 = new DescTreeObject(3L, "c1l2");
		c1l2.setParent(c1l1);
		c1l1.getChildren().add(c1l2);
		getSession().saveOrUpdate(c1l2);

		getSession().flush();
		getSession().clear();

		return root;
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.AbstractWicketHibernateTest#getEntities()
	 */
	@Override
	protected List<Class<? extends Serializable>> getEntities()
	{
		return Arrays.asList(Adres.class, Person.class, AbstractTreeObject.class, DescTreeObject.class,
			RootTreeObject.class, AbstractOrganization.class, Company.class);
	}
}
