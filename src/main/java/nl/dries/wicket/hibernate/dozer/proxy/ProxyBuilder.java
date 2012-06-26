package nl.dries.wicket.hibernate.dozer.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nl.dries.wicket.hibernate.dozer.helper.Attacher;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.CollectionPropertyDefinition;
import nl.dries.wicket.hibernate.dozer.properties.SimplePropertyDefinition;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates proxies
 * 
 * @author dries
 */
public class ProxyBuilder
{
	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(ProxyBuilder.class);

	/** Private construct */
	private ProxyBuilder()
	{
	}

	/**
	 * Create a proxy based on a detachable Hibernate property
	 * 
	 * @param property
	 *            the {@link AbstractPropertyDefinition}
	 * @return the created proxy
	 */
	public static Object buildProxy(AbstractPropertyDefinition property)
	{
		ProxyFactory factory = new ProxyFactory();

		Class<?> superType = property.getPropertyType();

		List<Class<?>> interfaces = new ArrayList<>(Arrays.asList(Serializable.class, Proxied.class));

		if (!superType.isInterface())
		{
			factory.setSuperclass(property.getPropertyType());
		}
		else
		{
			interfaces.add(superType);
		}

		if (property instanceof SimplePropertyDefinition)
		{
			interfaces.add(HibernateProxy.class);
		}
		else if (property instanceof CollectionPropertyDefinition)
		{
			interfaces.add(PersistentCollection.class);
		}

		factory.setInterfaces(interfaces.toArray(new Class[] {}));
		Class<?> proxyClass = factory.createClass();
		Object proxy = null;
		try
		{
			proxy = proxyClass.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOG.error("Error creating Javassist proxy", e);
		}
		((ProxyObject) proxy).setHandler(new LoaderCallback(property));

		return proxy;
	}

	/**
	 * Proxy method handler callback
	 * 
	 * @author dries
	 */
	private static class LoaderCallback implements MethodHandler, Serializable
	{
		/** Default */
		private static final long serialVersionUID = 1L;

		/** */
		private final AbstractPropertyDefinition propertyDefinition;

		/**
		 * Construct
		 * 
		 * @param propertyDefinition
		 */
		public LoaderCallback(AbstractPropertyDefinition propertyDefinition)
		{
			this.propertyDefinition = propertyDefinition;
		}

		/**
		 * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method,
		 *      java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
		{
			LOG.trace("Intercept: " + method.getName());

			if ("writeReplace".equals(method.getName()))
			{
				return new ProxyReplacement(propertyDefinition);
			}
			else if ("finalize".equals(method.getName()))
			{
				return null;
			}
			else if (method.getName().equals("getHibernateLazyInitializer"))
			{
				return new ProxiedHibernateInitializer((SimplePropertyDefinition) propertyDefinition);
			}

			// Attach the 'real' value
			Object realValue = new Attacher(propertyDefinition, self).attach();

			// Set the value in the original object, thus replacing the proxy
			ObjectHelper.setValue(propertyDefinition.getOwner(), propertyDefinition.getProperty(), realValue);

			// Invoke the requested method on the real value
			return method.invoke(realValue, args);
		}
	}

	/**
	 * Holder while serializing the proxy
	 * 
	 * @author dries
	 */
	private static class ProxyReplacement implements Serializable
	{
		/** Default */
		private static final long serialVersionUID = 1L;

		/** */
		private final AbstractPropertyDefinition propertyDefinition;

		/**
		 * Construct
		 * 
		 * @param propertyDefinition
		 */
		public ProxyReplacement(AbstractPropertyDefinition propertyDefinition)
		{
			this.propertyDefinition = propertyDefinition;
		}

		/**
		 * @return newly created proxy
		 * @throws ObjectStreamException
		 */
		private Object readResolve() throws ObjectStreamException
		{
			return buildProxy(propertyDefinition);
		}
	}

	/**
	 * 'Fake' Hibernate {@link LazyInitializer} to make our proxies {@link HibernateProxy}s
	 * 
	 * @author schulten
	 */
	private static class ProxiedHibernateInitializer implements Serializable, LazyInitializer
	{
		/** Default */
		private static final long serialVersionUID = 1L;

		/** The detached property */
		private final SimplePropertyDefinition property;

		/**
		 * Construct
		 * 
		 * @param property
		 */
		public ProxiedHibernateInitializer(SimplePropertyDefinition property)
		{
			this.property = property;
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#initialize()
		 */
		@Override
		public void initialize() throws HibernateException
		{
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#getIdentifier()
		 */
		@Override
		public Serializable getIdentifier()
		{
			return property.getHibernateProperty().getId();
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#setIdentifier(java.io.Serializable)
		 */
		@Override
		public void setIdentifier(Serializable id)
		{
			// No support?
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#getEntityName()
		 */
		@Override
		public String getEntityName()
		{
			return property.getHibernateProperty().getEntityClass().getName();
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#getPersistentClass()
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public Class getPersistentClass()
		{
			return property.getHibernateProperty().getEntityClass();
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#isUninitialized()
		 */
		@Override
		public boolean isUninitialized()
		{
			return true;
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#getImplementation()
		 */
		@Override
		public Object getImplementation()
		{
			// Attach the 'real' value
			Object realValue = new Attacher(property, null).attach();

			// The resulting object may as well be a newly created Hibernate proxy...
			if (realValue instanceof HibernateProxy)
			{
				realValue = ((HibernateProxy) realValue).getHibernateLazyInitializer().getImplementation();
			}

			// Set the value in the original object, thus replacing the proxy
			ObjectHelper.setValue(property.getOwner(), property.getProperty(), realValue);

			return realValue;
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#getImplementation(org.hibernate.engine.spi.SessionImplementor)
		 */
		@Override
		public Object getImplementation(SessionImplementor session) throws HibernateException
		{
			return getImplementation();
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#setImplementation(java.lang.Object)
		 */
		@Override
		public void setImplementation(Object target)
		{
			// Ignore?
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#isReadOnlySettingAvailable()
		 */
		@Override
		public boolean isReadOnlySettingAvailable()
		{
			return false;
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#isReadOnly()
		 */
		@Override
		public boolean isReadOnly()
		{
			return false;
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#setReadOnly(boolean)
		 */
		@Override
		public void setReadOnly(boolean readOnly)
		{
			// Ignore
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#getSession()
		 */
		@Override
		public SessionImplementor getSession()
		{
			return (SessionImplementor) property.getModelCallback().getSessionFinder()
				.getHibernateSession(getPersistentClass());
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#setSession(org.hibernate.engine.spi.SessionImplementor)
		 */
		@Override
		public void setSession(SessionImplementor session) throws HibernateException
		{
			// Not used
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#unsetSession()
		 */
		@Override
		public void unsetSession()
		{
			// Also ignore
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#setUnwrap(boolean)
		 */
		@Override
		public void setUnwrap(boolean unwrap)
		{
			// Ignore
		}

		/**
		 * @see org.hibernate.proxy.LazyInitializer#isUnwrap()
		 */
		@Override
		public boolean isUnwrap()
		{
			return false;
		}

	}

	/**
	 * Tag interface
	 * 
	 * @author dries
	 */
	public static interface Proxied
	{
		/**
		 * @return object that will replace this object in serialized state
		 * @throws ObjectStreamException
		 */
		Object writeReplace() throws ObjectStreamException;
	}
}
