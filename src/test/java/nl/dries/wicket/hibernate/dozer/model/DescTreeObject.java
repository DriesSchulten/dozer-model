package nl.dries.wicket.hibernate.dozer.model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author dries
 */
@Entity
public class DescTreeObject extends AbstractTreeObject
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	@Column
	private String desc;

	/**
	 * Construct
	 */
	public DescTreeObject()
	{
		super();
	}

	/**
	 * Construct
	 * 
	 * @param id
	 * @param name
	 */
	public DescTreeObject(Long id, String name)
	{
		super(id, name);
	}

	/**
	 * @return the desc
	 */
	public String getDesc()
	{
		return desc;
	}

	/**
	 * @param desc
	 *            the desc to set
	 */
	public void setDesc(String desc)
	{
		this.desc = desc;
	}
}
