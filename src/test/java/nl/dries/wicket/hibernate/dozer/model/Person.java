package nl.dries.wicket.hibernate.dozer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Hibernate;

/**
 * @author dries
 */
@Entity(name = "person")
public class Person implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	@Id
	private Long id;

	/** */
	private String name;

	/** */
	@OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
	private List<Adres> adresses = new ArrayList<>();

	/** */
	@ManyToOne
	private AbstractOrganization organization;

	/** */
	@ElementCollection
	private List<String> pets = new ArrayList<>();

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
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the adresses
	 */
	public List<Adres> getAdresses()
	{
		return adresses;
	}

	/**
	 * @param adresses
	 *            the adresses to set
	 */
	public void setAdresses(List<Adres> adresses)
	{
		this.adresses = adresses;
	}

	/**
	 * @return the organization
	 */
	public AbstractOrganization getOrganization()
	{
		return organization;
	}

	/**
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(AbstractOrganization organization)
	{
		this.organization = organization;
	}

	/**
	 * @return the pets
	 */
	public List<String> getPets()
	{
		return pets;
	}

	/**
	 * @param pets
	 *            the pets to set
	 */
	public void setPets(List<String> pets)
	{
		this.pets = pets;
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
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
		{
			return false;
		}
		Person other = (Person) obj;
		if (getId() == null)
		{
			if (other.getId() != null)
			{
				return false;
			}
		}
		else if (!getId().equals(other.getId()))
		{
			return false;
		}
		return true;
	}
}
