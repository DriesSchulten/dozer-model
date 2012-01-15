package nl.dries.wicket.hibernate.dozer;

import org.dozer.CustomFieldMapper;
import org.dozer.classmap.ClassMap;
import org.dozer.fieldmap.FieldMap;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

public class HibernateFieldMapper implements CustomFieldMapper
{

	/**
	 * @see org.dozer.CustomFieldMapper#mapField(java.lang.Object, java.lang.Object, java.lang.Object,
	 *      org.dozer.classmap.ClassMap, org.dozer.fieldmap.FieldMap)
	 */
	@Override
	public boolean mapField(Object source, Object destination, Object sourceFieldValue, ClassMap classMap,
		FieldMap fieldMapping)
	{
		if (sourceFieldValue instanceof PersistentCollection)
		{
			PersistentCollection collection = (PersistentCollection) sourceFieldValue;
			if (!collection.wasInitialized())
			{
				destination = null;
			}
		}
		else if (sourceFieldValue instanceof HibernateProxy)
		{

		}

		return false;
	}

}
