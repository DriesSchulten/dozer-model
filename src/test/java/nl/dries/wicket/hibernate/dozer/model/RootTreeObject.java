package nl.dries.wicket.hibernate.dozer.model;

import javax.persistence.Entity;

/**
 * @author schulten
 * 
 */
@Entity
public class RootTreeObject extends AbstractTreeObject
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RootTreeObject()
	{
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public RootTreeObject(Long id, String name)
	{
		super(id, name);
		// TODO Auto-generated constructor stub
	}
}
