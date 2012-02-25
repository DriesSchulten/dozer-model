package nl.dries.wicket.hibernate.dozer.model;

import java.io.Serializable;

public class NonHibernateObject implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	private Person person;

	/**
	 * @return the person
	 */
	public Person getPerson()
	{
		return person;
	}

	/**
	 * @param person
	 *            the person to set
	 */
	public void setPerson(Person person)
	{
		this.person = person;
	}
}
