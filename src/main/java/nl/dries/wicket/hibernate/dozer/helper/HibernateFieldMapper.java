package nl.dries.wicket.hibernate.dozer.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import nl.dries.wicket.hibernate.dozer.DozerModel;

import org.dozer.CustomFieldMapper;
import org.dozer.classmap.ClassMap;
import org.dozer.fieldmap.FieldMap;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentSet;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper which handles Hibernate fields that are still un-initialized (proxy) by recording them in the model. This way
 * we don't force initialization of each un-initalized property when detaching the model.
 * 
 * @author dries
 */
public class HibernateFieldMapper implements CustomFieldMapper
{
	/** Logger */
	private final Logger LOG = LoggerFactory.getLogger(HibernateFieldMapper.class);

	/** Reference to the model */
	private final DozerModel<?> model;

	/**
	 * Construct
	 * 
	 * @param model
	 *            the containing {@link DozerModel}
	 */
	public HibernateFieldMapper(DozerModel<?> model)
	{
		this.model = model;
	}

	/**
	 * @see org.dozer.CustomFieldMapper#mapField(java.lang.Object, java.lang.Object, java.lang.Object,
	 *      org.dozer.classmap.ClassMap, org.dozer.fieldmap.FieldMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean mapField(Object source, Object destination, Object sourceFieldValue, ClassMap classMap,
		FieldMap fieldMapping)
	{
		if (!Hibernate.isInitialized(sourceFieldValue))
		{
			try
			{
				PropertyDefinition def = new PropertyDefinition((Class<? extends Serializable>)
					Class.forName(fieldMapping.getSrcFieldType()), fieldMapping.getSrcFieldName());

				// Collection
				if (sourceFieldValue instanceof PersistentCollection)
				{
					Collection<HibernateProperty> detached = detachCollection((PersistentCollection) sourceFieldValue);
					model.addDetachedCollection(def, detached);
				}
				// Other
				else
				{
					LazyInitializer initializer = ((HibernateProxy) sourceFieldValue).getHibernateLazyInitializer();
					HibernateProperty property = new HibernateProperty(initializer.getPersistentClass(),
						initializer.getIdentifier());
					model.addDetachedProperty(def, property);
				}
			}
			catch (ClassNotFoundException e)
			{
				LOG.debug("Class not found, should not happen", e);
			}

			destination = null;
			return true;
		}

		return false;
	}

	/**
	 * Returns a detached version of a Hibernate attached and proxied collection
	 * 
	 * @param proxiedCollection
	 *            a {@link PersistentCollection} instance
	 * @return a {@link Collection} (can be a {@link java.util.Set} of {@link java.util.List} instance) containing
	 *         {@link HibernateProperty} objects
	 */
	@SuppressWarnings("unchecked")
	private Collection<HibernateProperty> detachCollection(PersistentCollection proxiedCollection)
	{
		Collection<HibernateProperty> detached = null;

		if (proxiedCollection instanceof PersistentBag)
		{
			detached = new ArrayList<>();

			PersistentBag persistentBag = (PersistentBag) proxiedCollection;
			for (Iterator<?> iter = persistentBag.iterator(); iter.hasNext();)
			{
				LazyInitializer initializer = ((HibernateProxy) iter.next()).getHibernateLazyInitializer();
				detached.add(new HibernateProperty(initializer.getPersistentClass(), initializer.getIdentifier()));
			}
		}
		else if (proxiedCollection instanceof PersistentSet)
		{
			detached = new HashSet<>();

			PersistentSet persistentSet = (PersistentSet) proxiedCollection;
			for (Iterator<?> iter = persistentSet.iterator(); iter.hasNext();)
			{
				LazyInitializer initializer = ((HibernateProxy) iter.next()).getHibernateLazyInitializer();
				detached.add(new HibernateProperty(initializer.getPersistentClass(), initializer.getIdentifier()));
			}
		}
		else
		{
			LOG.warn("Persistent collection of type {} not supported", proxiedCollection.getClass());
		}

		return detached;
	}
}
