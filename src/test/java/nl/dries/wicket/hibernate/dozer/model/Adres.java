package nl.dries.wicket.hibernate.dozer.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.Hibernate;

/**
 * @author dries
 */
@Entity(name = "adres")
public class Adres implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	@Id
	private Long id;

	/** */
	@Column
	private String street;

	/** */
	@ManyToOne
	private Person person;

	/**
	 * @return the id
	 */
	public Long getId()
	{
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id)
	{
		this.id = id;
	}

	/**
	 * @return the street
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * @param street
	 *            the street to set
	 */
	public void setStreet(String street)
	{
		this.street = street;
	}

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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((person == null) ? 0 : person.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (Hibernate.getClass(getClass()) != Hibernate.getClass(obj.getClass()))
		{
			return false;
		}
		Adres other = (Adres) obj;
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}
		if (person == null)
		{
			if (other.person != null)
			{
				return false;
			}
		}
		else if (!person.equals(other.person))
		{
			return false;
		}
		if (street == null)
		{
			if (other.street != null)
			{
				return false;
			}
		}
		else if (!street.equals(other.street))
		{
			return false;
		}
		return true;
	}
}
