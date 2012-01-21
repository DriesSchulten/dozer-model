package nl.dries.wicket.hibernate.dozer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Hibernate;

/**
 * @author dries
 */
@Entity(name = "treeobject")
public abstract class AbstractTreeObject implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	@Id
	private Long id;

	/** */
	@ManyToOne(cascade = CascadeType.ALL)
	private AbstractTreeObject parent;

	/** */
	@OneToMany
	private List<AbstractTreeObject> children = new ArrayList<>();

	/** */
	@Column
	private String name;

	/**
	 * Construct
	 */
	public AbstractTreeObject()
	{

	}

	/**
	 * Construct
	 * 
	 * @param id
	 * @param name
	 */
	public AbstractTreeObject(Long id, String name)
	{
		this.id = id;
		this.name = name;
	}

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
	 * @return the parent
	 */
	public AbstractTreeObject getParent()
	{
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(AbstractTreeObject parent)
	{
		this.parent = parent;
	}

	/**
	 * @return the children
	 */
	public List<AbstractTreeObject> getChildren()
	{
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<AbstractTreeObject> children)
	{
		this.children = children;
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		AbstractTreeObject other = (AbstractTreeObject) obj;
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
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		return true;
	}
}
