// $Id: AnnotationConfiguration.java 10650 2006-10-25 01:18:29Z epbernard $
package org.hibernate.cfg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Comparator;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.AssertionFailure;
import org.hibernate.cfg.annotations.Version;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.reflection.ReflectionManager;
import org.hibernate.reflection.XClass;
import org.hibernate.reflection.java.JavaXFactory;
import org.hibernate.util.JoinedIterator;
import org.hibernate.util.ReflectHelper;
import org.hibernate.util.StringHelper;
import org.hibernate.validator.ClassValidator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Similar to the {@link Configuration} object but handles EJB3 and Hibernate
 * specific annotations as a metadata facility
 *
 * @author Emmanuel Bernard
 */
public class AnnotationConfiguration extends Configuration {
	private static Log log = LogFactory.getLog( AnnotationConfiguration.class );

	static {
		Version.touch(); //touch version
	}

	public static final String ARTEFACT = "hibernate.mapping.precedence";
	public static final String DEFAULT_PRECEDENCE = "hbm, class";

	private Map namedGenerators;
	private Map<String, Map<String, Join>> joins;
	private Map<String, AnnotatedClassType> classTypes;
	private Set<String> defaultNamedQueryNames;
	private Set<String> defaultNamedNativeQueryNames;
	private Set<String> defaultSqlResulSetMappingNames;
	private Set<String> defaultNamedGenerators;
	private Map<String, Properties> generatorTables;
	private Map<Table, List<String[]>> tableUniqueConstraints;
	private Map<String, String> mappedByResolver;
	private Map<String, String> propertyRefResolver;
	private List<XClass> annotatedClasses;
	private Map<String, XClass> annotatedClassEntities;
	private Map<String, Document> hbmEntities;
	private List<CacheHolder> caches;
	private List<Document> hbmDocuments; //user ordering matters, hence the list
	private String precedence = null;
	private boolean inSecondPass = false;
	private transient ReflectionManager reflectionManager;
	private boolean isDefaultProcessed = false;

	public AnnotationConfiguration() {
		super();
	}

	public AnnotationConfiguration(SettingsFactory sf) {
		super( sf );
	}

	protected List<XClass> orderAndFillHierarchy(List<XClass> original) {
		//TODO remove embeddable
		List<XClass> copy = new ArrayList<XClass>( original );
		//for each class, copy all the relevent hierarchy
		for ( XClass clazz : original ) {
			XClass superClass = clazz.getSuperclass();
			while ( ! reflectionManager.equals( superClass, Object.class ) && ! copy.contains( superClass ) ) {
				if ( superClass.isAnnotationPresent( Entity.class )
						|| superClass.isAnnotationPresent( MappedSuperclass.class ) ) {
					copy.add( superClass );
				}
				superClass = superClass.getSuperclass();
			}
		}
		List<XClass> workingCopy = new ArrayList<XClass>( copy );
		List<XClass> newList = new ArrayList<XClass>( copy.size() );
		while ( workingCopy.size() > 0 ) {
			XClass clazz = workingCopy.get( 0 );
			orderHierarchy( workingCopy, newList, copy, clazz );
		}
		return newList;
	}

	private void orderHierarchy(List<XClass> copy, List<XClass> newList, List<XClass> original, XClass clazz) {
		if ( reflectionManager.equals( clazz, Object.class ) ) return;
		//process superclass first
		orderHierarchy( copy, newList, original, clazz.getSuperclass() );
		if ( original.contains( clazz ) ) {
			if ( !newList.contains( clazz ) ) {
				newList.add( clazz );
			}
			copy.remove( clazz );
		}
	}

	/**
	 * Read a mapping from the class annotation metadata (JSR 175).
	 *
	 * @param persistentClass the mapped class
	 * @return the configuration object
	 */
	public AnnotationConfiguration addAnnotatedClass(Class persistentClass) throws MappingException {
		XClass persistentXClass = reflectionManager.toXClass( persistentClass );
		try {
			annotatedClasses.add( persistentXClass );
			return this;
		}
		catch (MappingException me) {
			log.error( "Could not compile the mapping annotations", me );
			throw me;
		}
	}

	/**
	 * Read package level metadata
	 *
	 * @param packageName java package name
	 * @return the configuration object
	 */
	public AnnotationConfiguration addPackage(String packageName) throws MappingException {
		log.info( "Mapping package " + packageName );
		try {
			AnnotationBinder.bindPackage( packageName, createExtendedMappings() );
			return this;
		}
		catch (MappingException me) {
			log.error( "Could not compile the mapping annotations", me );
			throw me;
		}
	}

	public ExtendedMappings createExtendedMappings() {
		return new ExtendedMappings(
				classes,
				collections,
				tables,
				namedQueries,
				namedSqlQueries,
				sqlResultSetMappings,
				defaultNamedQueryNames,
				defaultNamedNativeQueryNames,
				defaultSqlResulSetMappingNames,
				defaultNamedGenerators,
				imports,
				secondPasses,
				propertyReferences,
				namingStrategy,
				typeDefs,
				filterDefinitions,
				namedGenerators,
				joins,
				classTypes,
				extendsQueue,
				tableNameBinding, columnNameBindingPerTable, auxiliaryDatabaseObjects,
				generatorTables,
				tableUniqueConstraints,
				mappedByResolver,
				propertyRefResolver,
				reflectionManager
		);
	}

	@Override
	public void setCacheConcurrencyStrategy(
			String clazz, String concurrencyStrategy, String region, boolean cacheLazyProperty
	) throws MappingException {
		caches.add( new CacheHolder( clazz, concurrencyStrategy, region, true, cacheLazyProperty ) );
	}

	@Override
	public void setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy, String region)
			throws MappingException {
		caches.add( new CacheHolder( collectionRole, concurrencyStrategy, region, false, false ) );
	}

	@Override
	protected void reset() {
		super.reset();
		namedGenerators = new HashMap();
		joins = new HashMap<String, Map<String, Join>>();
		classTypes = new HashMap<String, AnnotatedClassType>();
		generatorTables = new HashMap<String, Properties>();
		defaultNamedQueryNames = new HashSet<String>();
		defaultNamedNativeQueryNames = new HashSet<String>();
		defaultSqlResulSetMappingNames = new HashSet<String>();
		defaultNamedGenerators = new HashSet<String>();
		tableUniqueConstraints = new HashMap<Table, List<String[]>>();
		mappedByResolver = new HashMap<String, String>();
		propertyRefResolver = new HashMap<String, String>();
		annotatedClasses = new ArrayList<XClass>();
		caches = new ArrayList<CacheHolder>();
		hbmEntities = new HashMap<String, Document>();
		annotatedClassEntities = new HashMap<String, XClass>();
		hbmDocuments = new ArrayList<Document>();
		namingStrategy = EJB3NamingStrategy.INSTANCE;
		setEntityResolver( new EJB3DTDEntityResolver() );
		reflectionManager = new JavaXFactory();
	}

	@Override
	protected void secondPassCompile() throws MappingException {
		log.debug( "Execute first pass mapping processing" );
		//build annotatedClassEntities
		{
			List<XClass> tempAnnotatedClasses = new ArrayList<XClass>( annotatedClasses.size() );
			for ( XClass clazz : annotatedClasses ) {
				if ( clazz.isAnnotationPresent( Entity.class ) ) {
					annotatedClassEntities.put( clazz.getName(), clazz );
					tempAnnotatedClasses.add( clazz );
				}
				else if ( clazz.isAnnotationPresent( MappedSuperclass.class ) ) {
					tempAnnotatedClasses.add( clazz );
				}
				//only keep MappedSuperclasses and Entity in this list
			}
			annotatedClasses = tempAnnotatedClasses;
		}

		//process default values first
		if ( ! isDefaultProcessed ) {
			AnnotationBinder.bindDefaults( createExtendedMappings() );
			isDefaultProcessed = true;
		}

		//process entities
		if ( precedence == null ) precedence = getProperties().getProperty( ARTEFACT );
		if ( precedence == null ) precedence = DEFAULT_PRECEDENCE;
		StringTokenizer precedences = new StringTokenizer( precedence, ",; ", false );
		if ( ! precedences.hasMoreElements() ) {
			throw new MappingException( ARTEFACT + " cannot be empty: " + precedence );
		}
		while ( precedences.hasMoreElements() ) {
			String artifact = (String) precedences.nextElement();
			removeConflictedArtifact( artifact );
			processArtifactsOfType( artifact );
		}

		int cacheNbr = caches.size();
		for ( int index = 0; index < cacheNbr ; index++ ) {
			CacheHolder cacheHolder = caches.get( index );
			if ( cacheHolder.isClass ) {
				super.setCacheConcurrencyStrategy(
						cacheHolder.role, cacheHolder.usage, cacheHolder.region, cacheHolder.cacheLazy
				);
			}
			else {
				super.setCollectionCacheConcurrencyStrategy( cacheHolder.role, cacheHolder.usage, cacheHolder.region );
			}
		}
		caches.clear();

		inSecondPass = true;
		processFkSecondPassInOrder();
		Iterator iter = secondPasses.iterator();
		while ( iter.hasNext() ) {
			SecondPass sp = (SecondPass) iter.next();
			//do the second pass of fk before the others and remove them
			if ( sp instanceof CreateKeySecondPass ) {
				sp.doSecondPass( classes );
				iter.remove();
			}
		}
		super.secondPassCompile();
		inSecondPass = false;
		Iterator tables = (Iterator<Map.Entry<Table, List<String[]>>>) tableUniqueConstraints.entrySet().iterator();
		Table table;
		Map.Entry entry;
		String keyName;
		int uniqueIndexPerTable;
		while ( tables.hasNext() ) {
			entry = (Map.Entry) tables.next();
			table = (Table) entry.getKey();
			List<String[]> uniqueConstraints = (List<String[]>) entry.getValue();
			uniqueIndexPerTable = 0;

			for ( String[] columnNames : uniqueConstraints ) {
				//Change for generating key name start
				keyName = "";
//				keyName = "key" + uniqueIndexPerTable++;

				boolean needsUnderScore = false;
				for (String colName:columnNames)
				{
					if (needsUnderScore)
						keyName+="_";
					keyName +=getShortName(colName,3);
					needsUnderScore = true;
				}

				int tabNameLength = 59 - keyName.length();
				String tabName = table.getName();
				//log.info("Table Name ="+tabName+", Tablename Length = "+tabName.length()+", Needed Tablename Length= "+tabNameLength+", keyName = "+keyName);

				if (table.getName().length() > tabNameLength)
				{
					tabName = table.getName().substring(0,tabNameLength-1);
				}
				keyName = "UQ_"+tabName+"$"+keyName;

				if (keyName.length() > 64)
				{
					keyName = keyName.substring(0,63);
				}

				//System.out.println("Table Name = "+table.getName()+" KeyName = "+keyName);
				//Change for generating key name  end
				buildUniqueKeyFromColumnNames( columnNames, table, keyName );
			}
		}
		for ( PersistentClass persistentClazz : (Collection<PersistentClass>) classes.values() ) {
			//integrate the validate framework
			String className = persistentClazz.getClassName();
			if ( StringHelper.isNotEmpty( className ) ) {
				try {
					new ClassValidator( ReflectHelper.classForName( className ), null, null, null, reflectionManager )
                            .apply( persistentClazz );
				}
				catch (ClassNotFoundException e) {
					//swallow them
				}
			}
		}
	}
//Change for generating key name Begin
	private String getShortName(String name,int length )
	{
		String keyName = "";
		//String str= name.replaceAll("[AEIOUaeiou]", "");
		String[] ccNames = name.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
		for (String ccName:ccNames)
		{
			ccName = ccName.substring(0,1) +ccName.substring(1).replaceAll("[AEIOUaeiou]", "");
			if (ccName.length() > length)
			{
				keyName+=ccName.substring(0,length);
			}
			else
			{
				keyName+=ccName;
			}
		}
		return keyName;
	}
	//Change for generating key name  End
	private void processFkSecondPassInOrder() {
		log.debug( "processing manytoone fk mappings" );
		Iterator iter = secondPasses.iterator();
		/* We need to process FKSecond pass trying to resolve any
		 * graph circularity (ie PK made of a many to one linking to
		 * an entity having a PK made of a ManyToOne ...
		 */
		SortedSet<FkSecondPass> fkSecondPasses = new TreeSet<FkSecondPass>(
				new Comparator() {
					//The comparator implementation has to respect the compare=0 => equals() = true for sets
					public int compare(Object o1, Object o2) {
						if (! (o1 instanceof FkSecondPass &&  o2 instanceof FkSecondPass) ) {
							throw new AssertionFailure("comparint FkSecondPass with non FkSecondPass");
						}
						FkSecondPass f1 = (FkSecondPass) o1;
						FkSecondPass f2 = (FkSecondPass) o2;
						int compare = f1.getValue().getTable().getQuotedName().compareTo(
								f2.getValue().getTable().getQuotedName()
						);
						if (compare == 0) {
							//same table, we still need to differenciate true equality
							if ( f1.hashCode() < f2.hashCode() ) {
								compare = -1;
							}
							else if ( f1.hashCode() == f2.hashCode() ) {
								compare = 0;
							}
							else {
								compare = 1;
							}
						}
						return compare;
					}
				}
		);
		while ( iter.hasNext() ) {
			SecondPass sp = (SecondPass) iter.next();
			//do the second pass of fk before the others and remove them
			if ( sp instanceof FkSecondPass ) {
				fkSecondPasses.add( (FkSecondPass) sp );
				iter.remove();
			}
		}
		if ( fkSecondPasses.size() > 0 ) {
			Map<String, Set<String>> isADependencyOf = new HashMap<String, Set<String>>();
			List orderedFkSecondPasses = new ArrayList( fkSecondPasses.size() );
			List endOfQueueFkSecondPasses = new ArrayList( fkSecondPasses.size() );
			List orderedTable = new ArrayList( fkSecondPasses.size() );
			Iterator it = fkSecondPasses.iterator();
			while ( it.hasNext() ) {
				FkSecondPass sp = (FkSecondPass) it.next();
				String referenceEntityName = sp.getValue().getReferencedEntityName();
				PersistentClass classMapping = getClassMapping( referenceEntityName );
				if ( sp.isInPrimaryKey() ) {
					String dependentTable = classMapping.getTable().getQuotedName();
					if ( ! isADependencyOf.containsKey( dependentTable ) ) {
						isADependencyOf.put( dependentTable, new HashSet<String>() );
					}
					String table = sp.getValue().getTable().getQuotedName();
					isADependencyOf.get( dependentTable ).add( table );
					int beAfter = orderedTable.indexOf( dependentTable );
					int beBefore = orderedFkSecondPasses.size();
					Set<String> dependencies = isADependencyOf.get( table );
					if ( dependencies != null ) {
						for ( String tableDep : dependencies ) {
							//for each declared dependency take the lowest index
							int index = orderedTable.indexOf( tableDep );
							//index = -1 when we have a self dependency
							beBefore = index != -1 && index < beBefore ? index : beBefore;
						}
					}
					int currentIndex = orderedTable.indexOf( table );
					if ( beBefore < beAfter ||
							( currentIndex != -1 && ( currentIndex < beAfter || currentIndex > beBefore ) )
							) {
						StringBuilder sb = new StringBuilder(
								"Foreign key circularity dependency involving the following tables: "
						);
						//TODO deduplicate tables
						sb.append( table );
						if ( beAfter > -1 ) sb.append( ", " ).append( dependentTable );
						if ( beBefore < orderedFkSecondPasses.size() ) {
							sb.append( ", " ).append( orderedTable.get( beBefore ) );
						}
						throw new AnnotationException( sb.toString() );
					}
					currentIndex = currentIndex == -1 ? beBefore : currentIndex;
					orderedTable.add( currentIndex, table );
					orderedFkSecondPasses.add( currentIndex, sp );
				}
				else {
					endOfQueueFkSecondPasses.add( sp );
				}
			}
			it = orderedFkSecondPasses.listIterator();
			while ( it.hasNext() ) {
				( (SecondPass) it.next() ).doSecondPass( classes );
			}
			it = endOfQueueFkSecondPasses.listIterator();
			while ( it.hasNext() ) {
				( (SecondPass) it.next() ).doSecondPass( classes );
			}
		}
	}

	private void processArtifactsOfType(String artifact) {
		if ( "hbm".equalsIgnoreCase( artifact ) ) {
			log.debug( "Process hbm files" );
			for ( Document document : hbmDocuments ) {
				super.add( document );
			}
			hbmDocuments.clear();
			hbmEntities.clear();
		}
		else if ( "class".equalsIgnoreCase( artifact ) ) {
			log.debug( "Process annotated classes" );
			//bind classes in the correct order calculating some inheritance state
			List<XClass> orderedClasses = orderAndFillHierarchy( annotatedClasses );
			Map<XClass, InheritanceState> inheritanceStatePerClass = AnnotationBinder.buildInheritanceStates(
					orderedClasses, reflectionManager
			);
			ExtendedMappings mappings = createExtendedMappings();
			for ( XClass clazz : orderedClasses ) {
				//todo use the same extended mapping
				AnnotationBinder.bindClass( clazz, inheritanceStatePerClass, mappings );
			}
			annotatedClasses.clear();
			annotatedClassEntities.clear();
		}
		else {
			log.warn( "Unknown artifact: " + artifact );
		}
	}

	private void removeConflictedArtifact(String artifact) {
		if ( "hbm".equalsIgnoreCase( artifact ) ) {
			for ( String entity : hbmEntities.keySet() ) {
				if ( annotatedClassEntities.containsKey( entity ) ) {
					annotatedClasses.remove( annotatedClassEntities.get( entity ) );
					annotatedClassEntities.remove( entity );
				}
			}
		}
		else if ( "class".equalsIgnoreCase( artifact ) ) {
			for ( String entity : annotatedClassEntities.keySet() ) {
				if ( hbmEntities.containsKey( entity ) ) {
					hbmDocuments.remove( hbmEntities.get( entity ) );
					hbmEntities.remove( entity );
				}
			}
		}
	}

	private void buildUniqueKeyFromColumnNames(String[] columnNames, Table table, String keyName) {
		UniqueKey uc;
		int size = columnNames.length;
		Column[] columns = new Column[size];
		Set<Column> unbound = new HashSet<Column>();
		ExtendedMappings mappings = createExtendedMappings();
		for ( int index = 0; index < size ; index++ ) {
			columns[index] = new Column( mappings.getPhysicalColumnName( columnNames[index], table ) );
			unbound.add( columns[index] );
			//column equals and hashcode is based on column name
		}
		for ( Column column : columns ) {
			if ( table.containsColumn( column ) ) {
				uc = table.getOrCreateUniqueKey( keyName );
				uc.addColumn( table.getColumn( column ) );
				unbound.remove( column );
			}
		}
		if ( unbound.size() > 0 ) {
			StringBuilder sb = new StringBuilder( "Unable to create unique key constraint (" );
			for ( String columnName : columnNames ) {
				sb.append( columnName ).append( ", " );
			}
			sb.setLength( sb.length() - 2 );
			sb.append( ") on table " ).append( table.getName() ).append( ": " );
			for ( Column column : unbound ) {
				sb.append( column.getName() ).append( ", " );
			}
			sb.setLength( sb.length() - 2 );
			sb.append( " not found" );
			throw new AnnotationException( sb.toString() );
		}
	}

	@Override
	protected void parseMappingElement(Element subelement, String name) {
		Attribute rsrc = subelement.attribute( "resource" );
		Attribute file = subelement.attribute( "file" );
		Attribute jar = subelement.attribute( "jar" );
		Attribute pckg = subelement.attribute( "package" );
		Attribute clazz = subelement.attribute( "class" );
		if ( rsrc != null ) {
			log.debug( name + "<-" + rsrc );
			addResource( rsrc.getValue() );
		}
		else if ( jar != null ) {
			log.debug( name + "<-" + jar );
			addJar( new File( jar.getValue() ) );
		}
		else if ( file != null ) {
			log.debug( name + "<-" + file );
			addFile( file.getValue() );
		}
		else if ( pckg != null ) {
			log.debug( name + "<-" + pckg );
			addPackage( pckg.getValue() );
		}
		else if ( clazz != null ) {
			log.debug( name + "<-" + clazz );
			Class loadedClass = null;
			try {
				loadedClass = ReflectHelper.classForName( clazz.getValue() );
			}
			catch (ClassNotFoundException cnf) {
				throw new MappingException(
						"Unable to load class declared as <mapping class=\"" + clazz.getValue() + "\"/> in the configuration:",
						cnf
				);
			}
			catch (NoClassDefFoundError ncdf) {
				throw new MappingException(
						"Unable to load class declared as <mapping class=\"" + clazz.getValue() + "\"/> in the configuration:",
						ncdf
				);
			}

			addAnnotatedClass( loadedClass );
		}
		else {
			throw new MappingException( "<mapping> element in configuration specifies no attributes" );
		}
	}

	@Override
	protected void add(org.dom4j.Document doc) throws MappingException {
		boolean ejb3Xml = "entity-mappings".equals( doc.getRootElement().getName() );
		if ( inSecondPass ) {
			//if in second pass bypass the queueing, getExtendedQueue reuse this method
			if ( !ejb3Xml ) super.add( doc );
		}
		else {
			if ( ! ejb3Xml ) {
				final Element hmNode = doc.getRootElement();
				Attribute packNode = hmNode.attribute( "package" );
				String defaultPackage = packNode != null
						? packNode.getValue()
						: "";
				Set<String> entityNames = new HashSet<String>();
				findClassNames( defaultPackage, hmNode, entityNames );
				for ( String entity : entityNames ) {
					hbmEntities.put( entity, doc );
				}
				hbmDocuments.add( doc );
			}
			else {
				List<String> classnames = ( (JavaXFactory) reflectionManager ).getXMLContext().addDocument( doc );
				for ( String classname : classnames ) {
					try {
						annotatedClasses.add( reflectionManager.classForName( classname, this.getClass() ) );
					}
					catch (ClassNotFoundException e) {
						throw new AnnotationException( "Unable to load class defined in XML: " + classname, e );
					}
				}
			}
		}
	}

	private static void findClassNames(
			String defaultPackage, final Element startNode,
			final java.util.Set names
	) {
		// if we have some extends we need to check if those classes possibly could be inside the
		// same hbm.xml file...
		Iterator[] classes = new Iterator[4];
		classes[0] = startNode.elementIterator( "class" );
		classes[1] = startNode.elementIterator( "subclass" );
		classes[2] = startNode.elementIterator( "joined-subclass" );
		classes[3] = startNode.elementIterator( "union-subclass" );

		Iterator classIterator = new JoinedIterator( classes );
		while ( classIterator.hasNext() ) {
			Element element = (Element) classIterator.next();
			String entityName = element.attributeValue( "entity-name" );
			if ( entityName == null ) entityName = getClassName( element.attribute( "name" ), defaultPackage );
			names.add( entityName );
			findClassNames( defaultPackage, element, names );
		}
	}

	private static String getClassName(Attribute name, String defaultPackage) {
		if ( name == null ) return null;
		String unqualifiedName = name.getValue();
		if ( unqualifiedName == null ) return null;
		if ( unqualifiedName.indexOf( '.' ) < 0 && defaultPackage != null ) {
			return defaultPackage + '.' + unqualifiedName;
		}
		return unqualifiedName;
	}

	public void setPrecedence(String precedence) {
		this.precedence = precedence;
	}

	private static class CacheHolder {
		public CacheHolder(String role, String usage, String region, boolean isClass, boolean cacheLazy) {
			this.role = role;
			this.usage = usage;
			this.region = region;
			this.isClass = isClass;
			this.cacheLazy = cacheLazy;
		}

		public String role;
		public String usage;
		public String region;
		public boolean isClass;
		public boolean cacheLazy;
	}

	@Override
	public Configuration addInputStream(InputStream xmlInputStream) throws MappingException {
		try {
			List errors = new ArrayList();
			SAXReader saxReader = xmlHelper.createSAXReader( "XML InputStream", errors, getEntityResolver() );
			try {
				saxReader.setFeature( "http://apache.org/xml/features/validation/schema", true );
				//saxReader.setFeature( "http://apache.org/xml/features/validation/dynamic", true );
				//set the default schema locators
				saxReader.setProperty(
						"http://apache.org/xml/properties/schema/external-schemaLocation",
						"http://java.sun.com/xml/ns/persistence/orm orm_1_0.xsd"
				);
			}
			catch (SAXException e) {
				saxReader.setValidation( false );
			}
			org.dom4j.Document doc = saxReader
					.read( new InputSource( xmlInputStream ) );

			if ( errors.size() != 0 ) {
				throw new MappingException( "invalid mapping", (Throwable) errors.get( 0 ) );
			}
			add( doc );
			return this;
		}
		catch (DocumentException e) {
			throw new MappingException( "Could not parse mapping document in input stream", e );
		}
		finally {
			try {
				xmlInputStream.close();
			}
			catch (IOException ioe) {
				log.warn( "Could not close input stream", ioe );
			}
		}
	}

	//not a public API
	public ReflectionManager getReflectionManager() {
		return reflectionManager;
	}
}
