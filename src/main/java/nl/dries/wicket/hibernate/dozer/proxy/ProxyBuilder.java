package nl.dries.wicket.hibernate.dozer.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import nl.dries.wicket.hibernate.dozer.helper.Attacher;
import nl.dries.wicket.hibernate.dozer.helper.ObjectHelper;
import nl.dries.wicket.hibernate.dozer.properties.AbstractPropertyDefinition;

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
		Enhancer enhancer = new Enhancer();

		Class<?> superType = property.getPropertyType();

		if (!superType.isInterface())
		{
			enhancer.setSuperclass(property.getPropertyType());
			enhancer.setInterfaces(new Class[] { Serializable.class, Proxied.class });
		}
		else
		{
			enhancer.setInterfaces(new Class[] { superType, Serializable.class, Proxied.class });
		}

		enhancer.setCallback(new LoaderCallback(property));

		enhancer.setNamingPolicy(new DefaultNamingPolicy()
		{
			/** */
			@Override
			public String getClassName(String prefix, String source, Object key, Predicate names)
			{
				return super.getClassName("DOZER_" + prefix, source, key, names);
			}
		});

		return enhancer.create();
	}

	/**
	 * CGLib callback to load the original Hibernate object on invocation
	 * 
	 * @author dries
	 */
	private static class LoaderCallback implements MethodInterceptor, Serializable
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
		 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method,
		 *      java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
		 */
		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
		{
			LOG.debug("Intercept: " + method.getName());

			if ("writeReplace".equals(method.getName()))
			{
				return new ProxyReplacement(propertyDefinition);
			}
			else if ("finalize".equals(method.getName()))
			{
				return null;
			}

			// Attach the 'real' value
			Object realValue = new Attacher(propertyDefinition.getModel().getSessionFinder())
				.attach(propertyDefinition);

			// Set the value in the original object, thus replacing the proxy
			ObjectHelper.setValue(propertyDefinition.getOwner(), propertyDefinition.getProperty(), realValue);

			// Invoke the requested method on the real value
			return proxy.invoke(realValue, args);
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
