//$Id: $
package org.hibernate.search.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.reflection.ReflectionManager;
import org.hibernate.reflection.XClass;
import org.hibernate.util.ReflectHelper;
import org.hibernate.util.StringHelper;

/**
 * Create a Lucene directory provider
 * <p/>
 * Lucene directory providers are configured through properties
 * - hibernate.search.default.* and
 * - hibernate.search.<indexname>.*
 * <p/>
 * <indexname> properties have precedence over default
 * <p/>
 * The implementation is described by
 * hibernate.search.[default|indexname].directory_provider
 * <p/>
 * If none is defined the default value is FSDirectory
 *
 * @author Emmanuel Bernard
 * @author Sylvain Vieujot
 */
public class DirectoryProviderFactory {
	public List<DirectoryProvider<?>> providers = new ArrayList<DirectoryProvider<?>>();
	private static String LUCENE_PREFIX = "hibernate.search.";
	private static String LUCENE_DEFAULT = LUCENE_PREFIX + "default.";
	private static String DEFAULT_DIRECTORY_PROVIDER = FSDirectoryProvider.class.getName();

	//TODO for the public?
	//public DirectoryProvider<?> createDirectoryProvider(XClass entity, Configuration cfg) {

	public DirectoryProvider<?> createDirectoryProvider(XClass entity, Configuration cfg) {
		//get properties
		String directoryProviderName = getDirectoryProviderName( entity, cfg );
		Properties indexProps = getDirectoryProperties( cfg, directoryProviderName );

		//set up the directory
		String className = indexProps.getProperty( "directory_provider" );
		if ( StringHelper.isEmpty( className ) ) {
			className = DEFAULT_DIRECTORY_PROVIDER;
		}
		DirectoryProvider<?> provider = null;
		try {
			@SuppressWarnings( "unchecked" )
			Class<DirectoryProvider> directoryClass = ReflectHelper.classForName(
					className, DirectoryProviderFactory.class
			);
			provider = directoryClass.newInstance();
		}
		catch (Exception e) {
			throw new HibernateException( "Unable to instanciate directory provider: " + className, e );
		}
		provider.initialize( directoryProviderName, indexProps );
		int index = providers.indexOf( provider );
		if ( index != -1 ) {
			//share the same Directory provider for the same underlying store
			return providers.get( index );
		}
		else {
			providers.add( provider );
			return provider;
		}
	}

	private static Properties getDirectoryProperties(Configuration cfg, String directoryProviderName) {
		Properties props = cfg.getProperties();
		String indexName = LUCENE_PREFIX + directoryProviderName;
		Properties indexProps = new Properties();
		Properties indexSpecificProps = new Properties();
		for ( Map.Entry entry : props.entrySet() ) {
			String key = (String) entry.getKey();
			if ( key.startsWith( LUCENE_DEFAULT ) ) {
				indexProps.setProperty( key.substring( LUCENE_DEFAULT.length() ), (String) entry.getValue() );
			}
			else if ( key.startsWith( indexName ) ) {
				indexSpecificProps.setProperty( key.substring( indexName.length() ), (String) entry.getValue() );
			}
		}
		indexProps.putAll( indexSpecificProps );
		return indexProps;
	}

	public static String getDirectoryProviderName(XClass clazz, Configuration cfg) {
		//yuk
		ReflectionManager reflectionManager =
				( (AnnotationConfiguration) cfg ).createExtendedMappings().getReflectionManager();
		//get the most specialized (ie subclass > superclass) non default index name
		//if none extract the name from the most generic (superclass > subclass) @Indexed class in the hierarchy
		//FIXME I'm inclined to get rid of the default value
		PersistentClass pc = cfg.getClassMapping( clazz.getName() );
		XClass rootIndex = null;
		do {
			XClass currentClazz = reflectionManager.toXClass( pc.getMappedClass() );
			Indexed indexAnn = currentClazz.getAnnotation( Indexed.class );
			if ( indexAnn != null ) {
				if ( indexAnn.index().length() != 0 ) {
					return indexAnn.index();
				}
				else {
					rootIndex = currentClazz;
				}
			}
			pc = pc.getSuperclass();
		}
		while ( pc != null );
		//there is nobody outthere with a non default @Indexed.index
		if ( rootIndex != null ) {
			return rootIndex.getName();
		}
		else {
			throw new HibernateException(
					"Trying to extract the index name from a non @Indexed class: " + clazz.getName() );
		}
	}
}
