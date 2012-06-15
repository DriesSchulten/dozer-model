package nl.dries.wicket.hibernate.dozer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

/**
 * @author schulten
 */
@Entity
public class MapObject implements Serializable
{
	/** Default */
	private static final long serialVersionUID = 1L;

	/** */
	@Id
	private Long id;

	/** Map */
	@ElementCollection
	@JoinTable(name = "map_table", joinColumns = @JoinColumn(name = "mapobject_id", nullable = false))
	@Column(name = "stringValue", length = 1000)
	private Map<String, String> map = new HashMap<>();

	/** */
	@OneToMany(orphanRemoval = true)
	private List<Adres> adresses = new ArrayList<>();

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
	 * @return the map
	 */
	public Map<String, String> getMap()
	{
		return map;
	}

	/**
	 * @param map
	 *            the map to set
	 */
	public void setMap(Map<String, String> map)
	{
		this.map = map;
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
}
