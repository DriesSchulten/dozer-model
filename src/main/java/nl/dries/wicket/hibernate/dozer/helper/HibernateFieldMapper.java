package nl.dries.wicket.hibernate.dozer.helper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.Id;

import nl.dries.wicket.hibernate.dozer.DozerModel;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.dozer.CustomFieldMapper;
import org.dozer.classmap.ClassMap;
import org.dozer.fieldmap.FieldMap;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedSet;
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
			final AbstractPropertyDefinition def;

			// Collection
			if (sourceFieldValue instanceof PersistentCollection)
			{
				final CollectionType type;
				if (sourceFieldValue instanceof PersistentSortedSet)
				{
					type = CollectionType.SORTED_SET;
				}
				else if (sourceFieldValue instanceof PersistentSet)
				{
					type = CollectionType.SET;
				}
				else
				{
					type = CollectionType.LIST;
				}

				def = new CollectionPropertyDefinition((Class<? extends Serializable>) destination.getClass(),
					getObjectId(source), fieldMapping.getSrcFieldName(), type);
			}
			// Other
			else
			{
				LazyInitializer initializer = ((HibernateProxy) sourceFieldValue).getHibernateLazyInitializer();
				HibernateProperty property = new HibernateProperty(initializer.getPersistentClass(),
					initializer.getIdentifier());
				def = new SimplePropertyDefinition((Class<? extends Serializable>) destination.getClass(),
					getObjectId(source), fieldMapping.getSrcFieldName(), property);
			}

			model.addDetachedProperty(destination, def);

			return true;
		}

		return false;
	}

	/**
	 * Determine the given objects id
	 * 
	 * @param source
	 *            the source object
	 * @return found id or <code>null</code>
	 */
	private Serializable getObjectId(Object source)
	{
		Serializable id = null;

		if (!Hibernate.isInitialized(source))
		{
			LazyInitializer initializer = ((HibernateProxy) source).getHibernateLazyInitializer();
			id = initializer.getIdentifier();
		}
		else
		{
			try
			{
				Method idGetter = getIdMethod(source.getClass());
				if (idGetter != null)
				{
					id = (Serializable) idGetter.invoke(source, new Object[] {});
				}
				else
				{
					throw new RuntimeException("Hibernate object, but no @Id field found " + source.getClass());
				}
			}
			catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{
				LOG.error(String.format("Could not invoke getter for id value, ", source.getClass()), e);
			}
		}

		return id;
	}

	/**
	 * Returns a {@link Method} that contains the {@link Id} annotation (of its field has it)
	 * 
	 * @param clazz
	 *            the {@link Class} to search
	 * @return found {@link Method} or <code>null</code>
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private Method getIdMethod(Class<?> clazz) throws NoSuchMethodException, SecurityException
	{
		Field field = null;
		Method method = null;

		if (clazz != Object.class)
		{
			for (Field f : clazz.getDeclaredFields())
			{
				if (f.getAnnotation(Id.class) != null)
				{
					field = f;
				}
			}

			if (field == null)
			{
				for (Method m : clazz.getDeclaredMethods())
				{
					if (m.getAnnotation(Id.class) != null)
					{
						method = m;
					}
				}
			}
		}

		if (method == null && field != null)
		{
			method = clazz.getDeclaredMethod(toGetterMethod(field.getName()), new Class<?>[] {});
		}
		else if (method == null)
		{
			method = getIdMethod(clazz.getSuperclass());
		}

		return method;
	}

	/**
	 * Construct a getter definition for a given field
	 * 
	 * @param field
	 * @return get[F]ield
	 */
	private String toGetterMethod(String field)
	{
		return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}
}
