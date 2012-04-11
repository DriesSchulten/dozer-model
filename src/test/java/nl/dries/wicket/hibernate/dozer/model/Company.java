package nl.dries.wicket.hibernate.dozer.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Hibernate;

/**
 * @author dries
 */
@Entity(name = "company")
public class Company extends AbstractOrganization
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	@ManyToOne
	private Adres adres;

	/** */
	@OneToMany
	private Set<Person> persons = new HashSet<>();

	/**
	 * @return the adres
	 */
	public Adres getAdres()
	{
		return adres;
	}

	/**
	 * @param adres
	 *            the adres to set
	 */
	public void setAdres(Adres adres)
	{
		this.adres = adres;
	}

	/**
	 * @return the persons
	 */
	public Set<Person> getPersons()
	{
		return persons;
	}

	/**
	 * @param persons
	 *            the persons to set
	 */
	public void setPersons(Set<Person> persons)
	{
		this.persons = persons;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((adres == null) ? 0 : adres.hashCode());
		result = prime * result + ((getPersons() == null) ? 0 : getPersons().hashCode());
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
		if (!super.equals(obj))
		{
			return false;
		}
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
		{
			return false;
		}
		Company other = (Company) obj;
		if (adres == null)
		{
			if (other.adres != null)
			{
				return false;
			}
		}
		else if (!adres.equals(other.adres))
		{
			return false;
		}
		if (getPersons() == null)
		{
			if (other.getPersons() != null)
			{
				return false;
			}
		}
		else if (!getPersons().equals(other.getPersons()))
		{
			return false;
		}
		return true;
	}
}
