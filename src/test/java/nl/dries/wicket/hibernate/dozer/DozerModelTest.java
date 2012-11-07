package nl.dries.wicket.hibernate.dozer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.dries.wicket.hibernate.dozer.model.AbstractOrganization;
import nl.dries.wicket.hibernate.dozer.model.AbstractTreeObject;
import nl.dries.wicket.hibernate.dozer.model.Adres;
import nl.dries.wicket.hibernate.dozer.model.Company;
import nl.dries.wicket.hibernate.dozer.model.DescTreeObject;
import nl.dries.wicket.hibernate.dozer.model.MapObject;
import nl.dries.wicket.hibernate.dozer.model.NonHibernateObject;
import nl.dries.wicket.hibernate.dozer.model.Person;
import nl.dries.wicket.hibernate.dozer.model.RootTreeObject;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.model.Model;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link DozerModel}
 * 
 * @author dries
 */
public class DozerModelTest extends AbstractWicketHibernateTest
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(DozerModelTest.class);

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
		model = serialize(model);

		assertEquals(person, model.getObject());
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
		model = serialize(model);

		assertEquals(person, model.getObject().getPerson());
		assertEquals(person.getAdresses().get(0), model.getObject().getPerson().getAdresses().get(0));
		assertEquals(person.getName(), model.getObject().getPerson().getName());
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
		model = serialize(model);

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
		model = serialize(model);

		assertEquals(2, model.getObject().getChildren().size());
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
		model = serialize(model);

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
		model = serialize(model);
	}

	/**
	 * Test a tree on which 2 branches resolve to the same node
	 */
	@Test
	public void testTreeWithSameBranches()
	{
		DozerModel<AbstractTreeObject> model = new DozerModel<>(buildTree());
		model.detach();
		model = serialize(model);

		AbstractTreeObject newChild = new DescTreeObject();
		newChild.setName("Temp");
		newChild.getChildren().add((AbstractTreeObject) getSession().load(DescTreeObject.class, 3L));
		model.getObject().getChildren().get(1).getChildren().add(newChild);
		model.detach();

		assertFalse(model.getObject().getChildren().get(1).getChildren().get(0).getChildren().get(0) instanceof HibernateProxy);
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

		Adres adres = new Adres();
		adres.setStreet("street");
		company.setAdres(adres);

		person.setOrganization(company);

		DozerModel<Person> model = new DozerModel<>(person);
		model.detach();
		model = serialize(model);

		assertEquals(company, model.getObject().getOrganization());
		assertEquals(adres, ((Company) model.getObject().getOrganization()).getAdres());
	}

	/**
	 * Test a collection that is attached, next the session is flushed. Caused a lazy-init.
	 */
	@Test
	public void testCollectionWithFlush()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("person");
		getSession().saveOrUpdate(person);

		Adres adres = new Adres();
		adres.setId(1L);
		adres.setStreet("street");
		adres.setPerson(person);
		person.getAdresses().add(adres);
		getSession().saveOrUpdate(adres);

		getSession().flush();
		getSession().clear();

		DozerModel<Person> model = new DozerModel<>((Person) getSession().load(Person.class, 1L));
		model.detach();
		model = serialize(model);

		// Trigger attach
		model.getObject();

		// Force flush
		getSession().flush();

		// Check adress
		assertEquals("street", model.getObject().getAdresses().get(0).getStreet());
	}

	/**
	 * Equals
	 */
	@Test
	public void testModelEquals()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("person");

		assertTrue(new DozerModel<Person>(person).equals(new DozerModel<>(person)));
		assertTrue(new DozerModel<Person>(person).equals(new Model<>(person)));

		assertFalse(new DozerModel<Person>(person).equals(new DozerModel<>(new Person())));
	}

	/**
	 * Multiple load
	 */
	@Test
	public void testMultipleLoad()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("person");
		getSession().saveOrUpdate(person);

		DozerModel<Person> model = new DozerModel<>(person);
		model.detach();
		model = serialize(model);

		model.getObject().setName("edited");
		Person loaded = (Person) getSession().load(Person.class, 1L);
		getSession().saveOrUpdate(loaded);
		getSession().saveOrUpdate(model.getObject());
		getSession().flush();

		loaded = (Person) getSession().load(Person.class, 1L);
		assertEquals("edited", loaded.getName());
	}

	@Test
	public void testEvictUnique()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("person");
		getSession().saveOrUpdate(person);

		person = (Person) getSession().load(Person.class, 1L);
		getSession().evict(person);
		getSession().clear();

		getSession().load(Person.class, 1L);
		getSession().saveOrUpdate(person);
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

		AbstractTreeObject c2l1 = new DescTreeObject(4L, "c2l1");
		c2l1.setParent(root);
		root.getChildren().add(c2l1);
		getSession().saveOrUpdate(c2l1);

		getSession().flush();
		getSession().clear();

		return root;
	}

	/**
	 * Test
	 */
	@Test
	public void testListModel()
	{
		Person p1 = new Person();
		p1.setId(1L);
		p1.setName("p1");
		getSession().saveOrUpdate(p1);

		Person p2 = new Person();
		p2.setId(2L);
		p2.setName("p2");
		getSession().saveOrUpdate(p2);

		getSession().flush();
		getSession().clear();

		List<Person> list = new ArrayList<>();
		list.add(p1);
		list.add(p2);

		DozerListModel<Person> model = new DozerListModel<>(list);
		model.detach();
		model = serialize(model);

		assertEquals(list, model.getObject());

		List<Person> emptyList = new ArrayList<>();
		model.setObject(emptyList);
		model.detach();
		model = serialize(model);

		assertEquals(emptyList, model.getObject());

		model.setObject(list);

		assertTrue(model.contains(p2));
		assertEquals(2, model.size());
		assertFalse(model.isEmpty());

		model.add(new Person());
		assertEquals(3, model.size());

		assertTrue(model.remove(p2));

		model.clear();
		assertTrue(model.isEmpty());
	}

	/**
	 * Map mapping test
	 */
	@Test
	public void testMap()
	{
		MapObject map = new MapObject();
		map.setId(1L);

		map.getMap().put("1", "one");
		map.getMap().put("2", "two");

		getSession().saveOrUpdate(map);

		getSession().flush();
		getSession().clear();

		closeSession();
		openSession();

		DozerModel<MapObject> model = new DozerModel<>((MapObject) getSession().load(MapObject.class, 1L));

		model.detach();
		model = serialize(model);

		assertEquals(2, model.getObject().getMap().size());
		assertEquals("two", model.getObject().getMap().get("2"));
	}

	/**
	 * Test initialized map
	 */
	@Test
	public void testInitializedMap()
	{
		MapObject map = new MapObject();
		map.setId(1L);

		map.getMap().put("1", "one");
		map.getMap().put("2", "two");

		getSession().saveOrUpdate(map);

		DozerModel<MapObject> model = new DozerModel<MapObject>(map);
		model.detach();
		model = serialize(model);

		assertEquals(2, model.getObject().getMap().size());
		assertEquals("two", model.getObject().getMap().get("2"));
	}

	/**
	 * Test using set collection
	 */
	@Test
	public void testSet()
	{
		Company company = new Company();
		company.setId(1L);

		Person person = new Person();
		person.setId(1L);

		company.getPersons().add(person);

		getSession().saveOrUpdate(person);
		getSession().saveOrUpdate(company);

		getSession().flush();
		getSession().clear();

		DozerModel<Company> model = new DozerModel<>((Company) getSession().load(Company.class, 1L));
		model.detach();
		model = serialize(model);

		assertEquals(1, model.getObject().getPersons().size());
	}

	/**
	 * Test using initialized set collection
	 */
	@Test
	public void testSetInitialized()
	{
		Company company = new Company();
		company.setId(1L);

		Person person = new Person();
		person.setId(1L);

		company.getPersons().add(person);

		getSession().saveOrUpdate(person);
		getSession().saveOrUpdate(company);

		getSession().flush();
		getSession().clear();

		DozerModel<Company> model = new DozerModel<>((Company) getSession().load(Company.class, 1L));
		model.getObject().getPersons().size();

		model.detach();
		model = serialize(model);

		assertEquals(1, model.getObject().getPersons().size());
	}

	/**
	 * A non Hibernate object as model-object, but its properties are Hibernate objects, so the should be handled
	 * correctly.
	 */
	@Test
	public void testNonHibernateObjectAsRoot()
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

		NonHibernateObject object = new NonHibernateObject();
		object.setPerson((Person) getSession().load(Person.class, 1L));

		DozerModel<NonHibernateObject> model = new DozerModel<>(object);

		model.detach();
		model = serialize(model);

		assertEquals("street", model.getObject().getPerson().getAdresses().get(0).getStreet());
	}

	/**
	 * A non Hibernate object as root, but the containing Hibernate objects are initialized
	 */
	@Test
	public void testNonHibernateObjectAsRootInitialized()
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

		NonHibernateObject object = new NonHibernateObject();
		object.setPerson(person);
		object.setOther(new NonHibernateObject());

		DozerModel<NonHibernateObject> model = new DozerModel<>(object);

		model.detach();
		model = serialize(model);

		assertEquals("test", model.getObject().getPerson().getName());
		assertNotNull(model.getObject().getOther());
	}

	/**
	 * Set other object
	 */
	@Test
	public void testSetOtherObject()
	{
		Person person = new Person();
		person.setId(1L);
		person.setName("test");

		getSession().saveOrUpdate(person);

		DozerModel<Person> model = new DozerModel<>(person);
		model.detach();

		model.setObject(new Person());
		model.detach();

		assertNull(model.getObject().getName());
	}

	/**
	 * Test a collection mapped with: 'orphanRemoval'
	 */
	@Test
	public void testOrphanRemovelAsociation()
	{
		MapObject obj = new MapObject();
		obj.setId(1L);
		getSession().saveOrUpdate(obj);

		Adres adres = new Adres();
		adres.setId(1L);
		adres.setStreet("test");
		obj.getAdresses().add(adres);
		getSession().saveOrUpdate(adres);

		getSession().flush();
		getSession().clear();

		DozerModel<MapObject> model = new DozerModel<>((MapObject) getSession().load(MapObject.class, 1L));
		model.detach();
		model = serialize(model);

		assertEquals("test", model.getObject().getAdresses().get(0).getStreet());
	}

	/**
	 * Detach
	 * 
	 * @throws RuntimeException
	 * @throws {@link ReflectiveOperationException}
	 */
	@Test
	public void testDetachWhenNoEndingRequest() throws RuntimeException, ReflectiveOperationException
	{
		getWicketTester().getRequestCycle().setMetaData(DozerRequestCycleListener.ENDING_REQUEST, false);

		Adres adres = new Adres();
		adres.setId(1L);
		adres.setStreet("test");
		getSession().saveOrUpdate(adres);

		DozerModel<Adres> model = new DozerModel<>(adres);
		model.detach();

		Field field = DozerModel.class.getDeclaredField("detachedObject");
		field.setAccessible(true);

		// Should not have been detached...
		assertNull(field.get(model));

		// And whitout valid request cycle
		ThreadContext.setRequestCycle(null);

		model.detach();

		// Should be detached
		assertNotNull(field.get(model));
	}

	/**
	 * @see nl.dries.wicket.hibernate.dozer.AbstractWicketHibernateTest#getEntities()
	 */
	@Override
	protected List<Class<? extends Serializable>> getEntities()
	{
		return Arrays.asList(Adres.class, Person.class, AbstractTreeObject.class, DescTreeObject.class,
			RootTreeObject.class, AbstractOrganization.class, Company.class, MapObject.class);
	}

	/**
	 * @param in
	 *            input object
	 * @return object
	 */
	@SuppressWarnings("unchecked")
	private <T extends Serializable> T serialize(T in)
	{
		T out = null;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeObject(in);

			closeSession();

			byte[] bytes = baos.toByteArray();

			openSession();

			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream is = new ObjectInputStream(bais);
			out = (T) is.readObject();
		}
		catch (IOException | ClassNotFoundException e)
		{
			LOG.error("Fout bij de-serialiseren", e);
			fail(e.getMessage());
		}

		return out;
	}
}
