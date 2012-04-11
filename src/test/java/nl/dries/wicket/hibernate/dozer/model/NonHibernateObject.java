package nl.dries.wicket.hibernate.dozer.model;

import java.io.Serializable;

/**
 * @author dries
 */
public class NonHibernateObject implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	private Person person;

	/** */
	private NonHibernateObject other;

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

	/**
	 * @return the other
	 */
	public NonHibernateObject getOther()
	{
		return other;
	}

	/**
	 * @param other
	 *            the other to set
	 */
	public void setOther(NonHibernateObject other)
	{
		this.other = other;
	}
}
