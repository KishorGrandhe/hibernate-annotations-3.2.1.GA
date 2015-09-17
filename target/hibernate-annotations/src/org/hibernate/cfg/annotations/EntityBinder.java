//$Id: EntityBinder.java 10917 2006-12-04 20:41:45Z epbernard $
package org.hibernate.cfg.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.UniqueConstraint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForceDiscriminator;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.PolymorphismType;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;
import org.hibernate.annotations.Tables;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.cache.CacheFactory;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.ExtendedMappings;
import org.hibernate.cfg.InheritanceState;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.engine.Versioning;
import org.hibernate.engine.ExecuteUpdateResultCheckStyle;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TableOwner;
import org.hibernate.mapping.Value;
import org.hibernate.reflection.XAnnotatedElement;
import org.hibernate.reflection.XClass;
import org.hibernate.util.ReflectHelper;
import org.hibernate.util.StringHelper;

/**
 * Stateful holder and processor for binding Entity information
 *
 * @author Emmanuel Bernard
 */
public class EntityBinder {
	private String name;
	private XClass annotatedClass;
	private PersistentClass persistentClass;
	private ExtendedMappings mappings;
	private static Log log = LogFactory.getLog( EntityBinder.class );
	private String discriminatorValue = "";
	private boolean isPropertyAnnotated = false;
	private boolean dynamicInsert;
	private boolean dynamicUpdate;
	private boolean explicitHibernateEntityAnnotation;
	private boolean mutable;
	private OptimisticLockType optimisticLockType;
	private String persister;
	private PolymorphismType polymorphismType;
	private boolean selectBeforeUpdate;
	private int batchSize;
	private boolean lazy;
	private XClass proxyClass;
	private String where;
	private java.util.Map<String, Join> secondaryTables = new HashMap<String, Join>();
	private java.util.Map<String, Object> secondaryTableJoins = new HashMap<String, Object>();
	private String cacheConcurrentStrategy;
	private String cacheRegion;
	private java.util.Map<String, String> filters = new HashMap<String, String>();
	private InheritanceState inheritanceState;
	private boolean ignoreIdAnnotations;
	private boolean cacheLazyProperty;
	private String propertyAccessor;

	public boolean isPropertyAnnotated() {
		return isPropertyAnnotated;
	}

	/**
	 * Use as a fake one for Collection of elements
	 */
	public EntityBinder() {
	}

	public EntityBinder(
			Entity ejb3Ann, org.hibernate.annotations.Entity hibAnn,
			XClass annotatedClass, PersistentClass persistentClass,
			ExtendedMappings mappings
	) {
		this.mappings = mappings;
		this.persistentClass = persistentClass;
		this.annotatedClass = annotatedClass;
		bindEjb3Annotation( ejb3Ann );
		bindHibernateAnnotation( hibAnn );
	}

	private void bindHibernateAnnotation(org.hibernate.annotations.Entity hibAnn) {
		if ( hibAnn != null ) {
			dynamicInsert = hibAnn.dynamicInsert();
			dynamicUpdate = hibAnn.dynamicUpdate();
			mutable = hibAnn.mutable();
			optimisticLockType = hibAnn.optimisticLock();
			persister = hibAnn.persister();
			selectBeforeUpdate = hibAnn.selectBeforeUpdate();
			polymorphismType = hibAnn.polymorphism();
			explicitHibernateEntityAnnotation = true;
		}
		else {
			//default values when the annotation is not there
			dynamicInsert = false;
			dynamicUpdate = false;
			mutable = true;
			optimisticLockType = OptimisticLockType.VERSION;
			persister = "";
			polymorphismType = PolymorphismType.IMPLICIT;
			selectBeforeUpdate = false;
		}
	}

	private void bindEjb3Annotation(Entity ejb3Ann) {
		if ( ejb3Ann == null ) throw new AssertionFailure( "@Entity should always be not null" );
		if ( BinderHelper.isDefault( ejb3Ann.name() ) ) {
			name = StringHelper.unqualify( annotatedClass.getName() );
		}
		else {
			name = ejb3Ann.name();
		}
	}

	public void setDiscriminatorValue(String discriminatorValue) {
		this.discriminatorValue = discriminatorValue;
	}

	public void bindEntity() {
		persistentClass.setAbstract( annotatedClass.isAbstract() );
		persistentClass.setClassName( annotatedClass.getName() );
		//persistentClass.setDynamic(false); //no longer needed with the Entity name refactoring?
		persistentClass.setEntityName( annotatedClass.getName() );
		bindDiscriminatorValue();

		persistentClass.setLazy( lazy );
		if ( proxyClass != null ) {
			persistentClass.setProxyInterfaceName( proxyClass.getName() );
		}
		persistentClass.setDynamicInsert( dynamicInsert );
		persistentClass.setDynamicUpdate( dynamicUpdate );

		if ( persistentClass instanceof RootClass ) {
			RootClass rootClass = (RootClass) persistentClass;
			rootClass.setMutable( mutable );
			rootClass.setExplicitPolymorphism( isExplicitPolymorphism( polymorphismType ) );
			if ( StringHelper.isNotEmpty( where ) ) rootClass.setWhere( where );
			if ( cacheConcurrentStrategy != null ) {
				rootClass.setCacheConcurrencyStrategy( cacheConcurrentStrategy );
				rootClass.setCacheRegionName( cacheRegion );
				rootClass.setLazyPropertiesCacheable( cacheLazyProperty );
			}
			rootClass.setForceDiscriminator( annotatedClass.isAnnotationPresent( ForceDiscriminator.class ) );
		}
		else {
			if ( explicitHibernateEntityAnnotation ) {
				log.warn( "@org.hibernate.annotations.Entity used on a non root entity: ignored for "
						+ annotatedClass.getName() );
			}
		}
		persistentClass.setOptimisticLockMode( getVersioning( optimisticLockType ) );
		persistentClass.setSelectBeforeUpdate( selectBeforeUpdate );
		if ( !BinderHelper.isDefault( persister ) ) {
			try {
				persistentClass.setEntityPersisterClass( ReflectHelper.classForName( persister ) );
			}
			catch (ClassNotFoundException cnfe) {
				throw new AnnotationException( "Could not find persister class: " + persister );
			}
		}
		persistentClass.setBatchSize( batchSize );

		//SQL overriding
		SQLInsert sqlInsert = annotatedClass.getAnnotation( SQLInsert.class );
		SQLUpdate sqlUpdate = annotatedClass.getAnnotation( SQLUpdate.class );
		SQLDelete sqlDelete = annotatedClass.getAnnotation( SQLDelete.class );
		SQLDeleteAll sqlDeleteAll = annotatedClass.getAnnotation( SQLDeleteAll.class );
		Loader loader = annotatedClass.getAnnotation( Loader.class );
		if ( sqlInsert != null ) {
			persistentClass.setCustomSQLInsert( sqlInsert.sql().trim(), sqlInsert.callable(),
					ExecuteUpdateResultCheckStyle.parse( sqlInsert.check().toString().toLowerCase() )
			);

		}
		if ( sqlUpdate != null ) {
			persistentClass.setCustomSQLUpdate( sqlUpdate.sql(), sqlUpdate.callable(),
					ExecuteUpdateResultCheckStyle.parse( sqlUpdate.check().toString().toLowerCase() )
			);
		}
		if ( sqlDelete != null ) {
			persistentClass.setCustomSQLDelete( sqlDelete.sql(), sqlDelete.callable(),
					ExecuteUpdateResultCheckStyle.parse( sqlDelete.check().toString().toLowerCase() )
			);
		}
		if ( sqlDeleteAll != null ) {
			persistentClass.setCustomSQLDelete( sqlDeleteAll.sql(), sqlDeleteAll.callable(),
					ExecuteUpdateResultCheckStyle.parse( sqlDeleteAll.check().toString().toLowerCase() )
			);
		}
		if ( loader != null ) {
			persistentClass.setLoaderName( loader.namedQuery() );
		}

		if ( !inheritanceState.hasParents ) {
			Iterator<Map.Entry<String, String>> iter = filters.entrySet().iterator();
			while ( iter.hasNext() ) {
				Map.Entry<String, String> filter = iter.next();
				String filterName = filter.getKey();
				String cond = filter.getValue();
				if ( BinderHelper.isDefault( cond ) ) {
					cond = mappings.getFilterDefinition( filterName ).getDefaultFilterCondition();
					if ( StringHelper.isEmpty( cond ) ) {
						throw new AnnotationException(
								"no filter condition found for filter " + filterName + " in " + this.name
						);
					}
				}
				persistentClass.addFilter( filterName, cond );
			}
		}
		else {
			if ( filters.size() > 0 ) {
				log.warn( "@Filter not allowed on subclasses (ignored): " + persistentClass.getEntityName() );
			}
		}
		log.debug( "Import with entity name=" + name );
		try {
			mappings.addImport( persistentClass.getEntityName(), name );
			String entityName = persistentClass.getEntityName();
			if ( !entityName.equals( name ) ) {
				mappings.addImport( entityName, entityName );
			}
		}
		catch (MappingException me) {
			throw new AnnotationException( "Use of the same entity name twice: " + name, me );
		}
	}

	public void bindDiscriminatorValue() {
		if ( StringHelper.isEmpty( discriminatorValue ) ) {
			Value discriminator = persistentClass.getDiscriminator();
			if ( discriminator == null ) {
				persistentClass.setDiscriminatorValue( name );
			}
			else if ( "character".equals( discriminator.getType().getName() ) ) {
				throw new AnnotationException(
						"Using default @DiscriminatorValue for a discriminator of type CHAR is not safe"
				);
			}
			else if ( "integer".equals( discriminator.getType().getName() ) ) {
				persistentClass.setDiscriminatorValue( String.valueOf( name.hashCode() ) );
			}
			else {
				persistentClass.setDiscriminatorValue( name ); //Spec compliant
			}
		}
		else {
			//persistentClass.getDiscriminator()
			persistentClass.setDiscriminatorValue( discriminatorValue );
		}
	}

	int getVersioning(OptimisticLockType type) {
		switch (type) {
			case VERSION:
				return Versioning.OPTIMISTIC_LOCK_VERSION;
			case NONE:
				return Versioning.OPTIMISTIC_LOCK_NONE;
			case DIRTY:
				return Versioning.OPTIMISTIC_LOCK_DIRTY;
			case ALL:
				return Versioning.OPTIMISTIC_LOCK_ALL;
			default:
				throw new AssertionFailure( "optimistic locking not supported: " + type );
		}
	}

	private boolean isExplicitPolymorphism(PolymorphismType type) {
		switch (type) {
			case IMPLICIT:
				return false;
			case EXPLICIT:
				return true;
			default:
				throw new AssertionFailure( "Unknown polymorphism type: " + type );
		}
	}

	public void setBatchSize(BatchSize sizeAnn) {
		if ( sizeAnn != null ) {
			batchSize = sizeAnn.size();
		}
		else {
			batchSize = -1;
		}
	}

	public void setProxy(Proxy proxy) {
		if ( proxy != null ) {
			lazy = proxy.lazy();
			if ( !lazy ) {
				proxyClass = null;
			}
			else {
				if ( AnnotationBinder.isDefault(
						mappings.getReflectionManager().toXClass( proxy.proxyClass() ), mappings
				) ) {
					proxyClass = annotatedClass;
				}
				else {
					proxyClass = mappings.getReflectionManager().toXClass( proxy.proxyClass() );
				}
			}
		}
		else {
			lazy = true; //needed to allow association lazy loading.
			proxyClass = annotatedClass;
		}
	}

	public void setWhere(Where whereAnn) {
		if ( whereAnn != null ) {
			where = whereAnn.clause();
		}
	}

	private String getClassTableName(String tableName) {
		if ( StringHelper.isEmpty( tableName ) ) {
			return mappings.getNamingStrategy().classToTableName( name );
		}
		else {
			return mappings.getNamingStrategy().tableName( tableName );
		}
	}

	public void bindTable(
			String schema, String catalog,
			String tableName, List uniqueConstraints,
			String constraints, Table denormalizedSuperclassTable
	) {
		String logicalName = StringHelper.isNotEmpty( tableName ) ?
				tableName :
				StringHelper.unqualify( name );
		Table table = TableBinder.fillTable(
				schema, catalog,
				getClassTableName( tableName ),
				logicalName,
				persistentClass.isAbstract(), uniqueConstraints, constraints,
				denormalizedSuperclassTable, mappings
		);

		if ( persistentClass instanceof TableOwner ) {
			if ( log.isInfoEnabled() ) {
				log.info( "Bind entity " + persistentClass.getEntityName() + " on table " + table.getName() );
			}
			( (TableOwner) persistentClass ).setTable( table );
		}
		else {
			throw new AssertionFailure( "binding a table for a subclass" );
		}
	}

	public void finalSecondaryTableBinding(PropertyHolder propertyHolder) {
		/*
		 * Those operations has to be done after the id definition of the persistence class.
		 * ie after the properties parsing
		 */
		Iterator joins = secondaryTables.values().iterator();
		Iterator joinColumns = secondaryTableJoins.values().iterator();

		while ( joins.hasNext() ) {
			Object uncastedColumn = joinColumns.next();
			Join join = (Join) joins.next();
			createPrimaryColumnsToSecondaryTable( uncastedColumn, propertyHolder, join );
		}
		mappings.addJoins( persistentClass, secondaryTables );
	}

	private void createPrimaryColumnsToSecondaryTable(Object uncastedColumn, PropertyHolder propertyHolder, Join join) {
		Ejb3JoinColumn[] ejb3JoinColumns;
		PrimaryKeyJoinColumn[] pkColumnsAnn = null;
		JoinColumn[] joinColumnsAnn = null;
		if ( uncastedColumn instanceof PrimaryKeyJoinColumn[] ) {
			pkColumnsAnn = (PrimaryKeyJoinColumn[]) uncastedColumn;
		}
		if ( uncastedColumn instanceof JoinColumn[] ) {
			joinColumnsAnn = (JoinColumn[]) uncastedColumn;
		}
		if ( pkColumnsAnn == null && joinColumnsAnn == null ) {
			ejb3JoinColumns = new Ejb3JoinColumn[1];
			ejb3JoinColumns[0] = Ejb3JoinColumn.buildJoinColumn(
					null,
					null,
					persistentClass.getIdentifier(),
					secondaryTables,
					propertyHolder, mappings
			);
		}
		else {
			int nbrOfJoinColumns = pkColumnsAnn != null ?
					pkColumnsAnn.length :
					joinColumnsAnn.length;
			if ( nbrOfJoinColumns == 0 ) {
				ejb3JoinColumns = new Ejb3JoinColumn[1];
				ejb3JoinColumns[0] = Ejb3JoinColumn.buildJoinColumn(
						null,
						null,
						persistentClass.getIdentifier(),
						secondaryTables,
						propertyHolder, mappings
				);
			}
			else {
				ejb3JoinColumns = new Ejb3JoinColumn[nbrOfJoinColumns];
				if ( pkColumnsAnn != null ) {
					for ( int colIndex = 0; colIndex < nbrOfJoinColumns; colIndex++ ) {
						ejb3JoinColumns[colIndex] = Ejb3JoinColumn.buildJoinColumn(
								pkColumnsAnn[colIndex],
								null,
								persistentClass.getIdentifier(),
								secondaryTables,
								propertyHolder, mappings
						);
					}
				}
				else {
					for ( int colIndex = 0; colIndex < nbrOfJoinColumns; colIndex++ ) {
						ejb3JoinColumns[colIndex] = Ejb3JoinColumn.buildJoinColumn(
								null,
								joinColumnsAnn[colIndex],
								persistentClass.getIdentifier(),
								secondaryTables,
								propertyHolder, mappings
						);
					}
				}
			}
		}

		for ( Ejb3JoinColumn joinColumn : ejb3JoinColumns ) {
			joinColumn.forceNotNull();
		}
		bindJoinToPersistentClass( join, ejb3JoinColumns, persistentClass, mappings );
	}

	public static void bindJoinToPersistentClass(
			Join join, Ejb3JoinColumn[] ejb3JoinColumns, PersistentClass persistentClass, ExtendedMappings mappings
	) {
		SimpleValue key = new DependantValue( join.getTable(), persistentClass.getIdentifier() );
		join.setKey( key );
		join.setSequentialSelect( false );
		join.setInverse( false );
		join.setOptional( true ); //perhaps not quite per-spec, but a Good Thing anyway
		key.setCascadeDeleteEnabled( false );
		TableBinder.bindFk( persistentClass, null, ejb3JoinColumns, key, false, mappings );
		join.createPrimaryKey();
		join.createForeignKey();
		persistentClass.addJoin( join );
	}

	public void firstLevelSecondaryTablesBinding(
			SecondaryTable secTable, SecondaryTables secTables
	) {
		if ( secTables != null ) {
			//loop through it
			for ( SecondaryTable tab : secTables.value() ) {
				addJoin( tab, null, null, false );
			}
		}
		else {
			if ( secTable != null ) addJoin( secTable, null, null, false );
		}
	}

	public Join addJoin(JoinTable joinTable, PropertyHolder holder, boolean noDelayInPkColumnCreation) {
		Join join = addJoin( null, joinTable, holder, noDelayInPkColumnCreation );
		join.setOptional( true );
		return join;
	}

	/**
	 * A non null propertyHolder means than we process the Pk creation without delay
	 */
	private Join addJoin(
			SecondaryTable secondaryTable, JoinTable joinTable, PropertyHolder propertyHolder,
			boolean noDelayInPkColumnCreation
	) {
		Join join = new Join();
		join.setPersistentClass( persistentClass );
		String schema;
		String catalog;
		String table;
		String realTable;
		UniqueConstraint[] uniqueCosntraintsAnn;
		if ( secondaryTable != null ) {
			schema = secondaryTable.schema();
			catalog = secondaryTable.catalog();
			table = secondaryTable.name();
			realTable = mappings.getNamingStrategy().tableName( table ); //always an explicit table name
			uniqueCosntraintsAnn = secondaryTable.uniqueConstraints();
		}
		else if ( joinTable != null ) {
			schema = joinTable.schema();
			catalog = joinTable.catalog();
			table = joinTable.name();
			realTable = mappings.getNamingStrategy().tableName( table ); //always an explicit table name
			uniqueCosntraintsAnn = joinTable.uniqueConstraints();
		}
		else {
			throw new AssertionFailure( "Both JoinTable and SecondaryTable are null" );
		}
		List uniqueConstraints = new ArrayList( uniqueCosntraintsAnn == null ?
				0 :
				uniqueCosntraintsAnn.length );
		if ( uniqueCosntraintsAnn != null && uniqueCosntraintsAnn.length != 0 ) {
			for ( UniqueConstraint uc : uniqueCosntraintsAnn ) {
				uniqueConstraints.add( uc.columnNames() );
			}
		}
		Table tableMapping = TableBinder.fillTable(
				schema,
				catalog,
				realTable,
				table, false, uniqueConstraints, null, null, mappings
		);
		//no check constraints available on joins
		join.setTable( tableMapping );

		//somehow keep joins() for later.
		//Has to do the work later because it needs persistentClass id!
		Object joinColumns = null;
		//get the appropriate pk columns
		if ( secondaryTable != null ) {
			joinColumns = secondaryTable.pkJoinColumns();
		}
		else if ( joinTable != null ) {
			joinColumns = joinTable.joinColumns();
		}
		if ( log.isInfoEnabled() ) {
			log.info(
					"Adding secondary table to entity " + persistentClass.getEntityName() + " -> " + join.getTable()
							.getName()
			);
		}
		if ( noDelayInPkColumnCreation ) {
			createPrimaryColumnsToSecondaryTable( joinColumns, propertyHolder, join );

		}
		else {
			secondaryTables.put( table, join );
			secondaryTableJoins.put( table, joinColumns );
		}
		return join;
	}

	public java.util.Map<String, Join> getSecondaryTables() {
		return secondaryTables;
	}

	public void setCache(Cache cacheAnn) {
		if ( cacheAnn != null ) {
			cacheRegion = BinderHelper.isDefault( cacheAnn.region() ) ?
					null :
					cacheAnn.region();
			cacheConcurrentStrategy = getCacheConcurrencyStrategy( cacheAnn.usage() );
			if ( "all".equalsIgnoreCase( cacheAnn.include() ) ) {
				cacheLazyProperty = true;
			}
			else if ( "non-lazy".equalsIgnoreCase( cacheAnn.include() ) ) {
				cacheLazyProperty = false;
			}
			else {
				throw new AnnotationException( "Unknown lazy property annotations: " + cacheAnn.include() );
			}
		}
		else {
			cacheConcurrentStrategy = null;
			cacheRegion = null;
			cacheLazyProperty = true;
		}
	}

	public static String getCacheConcurrencyStrategy(CacheConcurrencyStrategy strategy) {
		switch (strategy) {
			case NONE:
				return null;
			case READ_ONLY:
				return CacheFactory.READ_ONLY;
			case READ_WRITE:
				return CacheFactory.READ_WRITE;
			case NONSTRICT_READ_WRITE:
				return CacheFactory.NONSTRICT_READ_WRITE;
			case TRANSACTIONAL:
				return CacheFactory.TRANSACTIONAL;
			default:
				throw new AssertionFailure( "CacheConcurrencyStrategy unknown: " + strategy );
		}
	}

	public void addFilter(String name, String condition) {
		filters.put( name, condition );
	}

	public void setInheritanceState(InheritanceState inheritanceState) {
		this.inheritanceState = inheritanceState;
	}

	public boolean isIgnoreIdAnnotations() {
		return ignoreIdAnnotations;
	}

	public void setIgnoreIdAnnotations(boolean ignoreIdAnnotations) {
		this.ignoreIdAnnotations = ignoreIdAnnotations;
	}

	public void addIndexes(org.hibernate.annotations.Table table) {
		if ( table == null ) return;
		String tableName = table.appliesTo();
		Iterator tables = persistentClass.getTableClosureIterator();
		Table hibTable = null;
		while ( tables.hasNext() ) {
			hibTable = (Table) tables.next();
			if ( hibTable.getName().equals( tableName ) ) {
				//we are in the correct table to find columns
				break;
			}
		}
		if ( hibTable == null ) {
			throw new AnnotationException(
					"@org.hibernate.annotations.Table references an unknown table: " + tableName
			);
		}
		TableBinder.addIndexes( hibTable, table.indexes(), mappings );
	}

	public void addIndexes(Tables tables) {
		if ( tables == null ) return;
		for ( org.hibernate.annotations.Table table : tables.values() ) {
			addIndexes( table );
		}
	}

	public void setPropertyAnnotated(boolean propertyAnnotated) {
		this.isPropertyAnnotated = propertyAnnotated;
	}

	public String getPropertyAccessor() {
		return propertyAccessor;
	}

	public void setPropertyAccessor(String propertyAccessor) {
		this.propertyAccessor = propertyAccessor;
	}

	public boolean isPropertyAnnotated(XAnnotatedElement element) {
		AccessType access = element.getAnnotation( AccessType.class );
		if ( access == null ) return isPropertyAnnotated;
		String propertyAccessor = access.value();
		if ( "property".equals( propertyAccessor ) ) {
			return Boolean.TRUE;
		}
		else if ( "field".equals( propertyAccessor ) ) {
			return Boolean.FALSE;
		}
		else {
			return isPropertyAnnotated;
		}
	}

	public String getPropertyAccessor(XAnnotatedElement element) {
		AccessType access = element.getAnnotation( AccessType.class );
		if ( access == null ) return propertyAccessor;
		return access.value();
	}
}
