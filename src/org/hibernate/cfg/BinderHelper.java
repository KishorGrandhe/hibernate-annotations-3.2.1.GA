//$Id: BinderHelper.java 10876 2006-11-29 14:19:45Z epbernard $
package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Properties;

import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.MultipleHiLoPerTableGenerator;
import org.hibernate.cfg.annotations.TableBinder;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.IdGenerator;
import org.hibernate.util.StringHelper;

/**
 * @author Emmanuel Bernard
 */
public class BinderHelper {

	public static final String ANNOTATION_STRING_DEFAULT = "";

	private BinderHelper() {
	}

	static {
		Set<String> primitiveNames = new HashSet<String>();
		primitiveNames.add( byte.class.getName() );
		primitiveNames.add( short.class.getName() );
		primitiveNames.add( int.class.getName() );
		primitiveNames.add( long.class.getName() );
		primitiveNames.add( float.class.getName() );
		primitiveNames.add( double.class.getName() );
		primitiveNames.add( char.class.getName() );
		primitiveNames.add( boolean.class.getName() );
		PRIMITIVE_NAMES = Collections.unmodifiableSet( primitiveNames );
	}

	public static final Set<String> PRIMITIVE_NAMES;

	/**
	 * create a property copy reusing the same value
	 */
	public static Property shallowCopy(Property property) {
		Property clone = new Property();
		clone.setCascade( property.getCascade() );
		clone.setInsertable( property.isInsertable() );
		clone.setLazy( property.isLazy() );
		clone.setName( property.getName() );
		clone.setNaturalIdentifier( property.isNaturalIdentifier() );
		clone.setOptimisticLocked( property.isOptimisticLocked() );
		clone.setOptional( property.isOptional() );
		clone.setPersistentClass( property.getPersistentClass() );
		clone.setPropertyAccessorName( property.getPropertyAccessorName() );
		clone.setSelectable( property.isSelectable() );
		clone.setUpdateable( property.isUpdateable() );
		clone.setValue( property.getValue() );
		return clone;
	}

	public static void createSyntheticPropertyReference(
			Ejb3JoinColumn[] columns,
			PersistentClass ownerEntity,
			PersistentClass associatedEntity,
			Value value,
			boolean inverse, ExtendedMappings mappings
	) {
		//associated entity only used for more precise exception, yuk!
		if ( columns[0].isImplicit() || StringHelper.isNotEmpty( columns[0].getMappedBy() ) ) return;
		int fkEnum = Ejb3JoinColumn.checkReferencedColumnsType( columns, ownerEntity, mappings );
		PersistentClass associatedClass = columns[0].getPropertyHolder() != null ?
				columns[0].getPropertyHolder().getPersistentClass() :
				null;
		if ( Ejb3JoinColumn.NON_PK_REFERENCE == fkEnum ) {
			/**
			 * Create a synthetic property to refer to including an
			 * embedded component value containing all the properties
			 * mapped to the referenced columns
			 * We need to shallow copy those properties to mark them
			 * as non insertable / non updatable
			 */
			StringBuilder propertyNameBuffer = new StringBuilder( "_" );
			propertyNameBuffer.append( associatedClass.getEntityName().replace( '.', '_' ) );
			propertyNameBuffer.append( "_" ).append( columns[0].getPropertyName() );
			String syntheticPropertyName = propertyNameBuffer.toString();
			//find properties associated to a certain column
			Object columnOwner = findColumnOwner( ownerEntity, columns[0].getReferencedColumn(), mappings );
			List<Property> properties = findPropertiesByColumns( columnOwner, columns, mappings );
			//create an embeddable component
			Property synthProp = null;
			if ( properties != null ) {
				//todo how about properties.size() == 1, this should be much simpler
				Component embeddedComp = columnOwner instanceof PersistentClass ?
						new Component( (PersistentClass) columnOwner ) :
						new Component( (Join) columnOwner );
				embeddedComp.setEmbedded( true );
				embeddedComp.setComponentClassName( embeddedComp.getOwner().getClassName() );
				for ( Property property : properties ) {
					Property clone = BinderHelper.shallowCopy( property );
					clone.setInsertable( false );
					clone.setUpdateable( false );
					clone.setNaturalIdentifier( false );
					embeddedComp.addProperty( clone );
				}
				synthProp = new Property();
				synthProp.setName( syntheticPropertyName );
				//synthProp.setNodeName(syntheticPropertyName);
				synthProp.setPersistentClass( ownerEntity );
				synthProp.setUpdateable( false );
				synthProp.setInsertable( false );
				synthProp.setValue( embeddedComp );
				synthProp.setPropertyAccessorName( "embedded" );
				ownerEntity.addProperty( synthProp );
				//make it unique
				TableBinder.createUniqueConstraint( embeddedComp );
			}
			else {
				//TODO use a ToOne type doing a second select
				StringBuilder columnsList = new StringBuilder();
				columnsList.append( "referencedColumnNames(" );
				for ( Ejb3JoinColumn column : columns ) {
					columnsList.append( column.getReferencedColumn() ).append( ", " );
				}
				columnsList.setLength( columnsList.length() - 2 );
				columnsList.append( ") " );

				if ( associatedEntity != null ) {
					//overidden destination
					columnsList.append( "of " )
							.append( associatedEntity.getEntityName() )
							.append( "." )
							.append( columns[0].getPropertyName() )
							.append( " " );
				}
				else {
					if ( columns[0].getPropertyHolder() != null ) {
						columnsList.append( "of " )
								.append( columns[0].getPropertyHolder().getEntityName() )
								.append( "." )
								.append( columns[0].getPropertyName() )
								.append( " " );
					}
				}
				columnsList.append( "referencing " )
						.append( ownerEntity.getEntityName() )
						.append( " not mapped to a single property" );
				throw new AnnotationException( columnsList.toString() );
			}

			/**
			 * creating the property ref to the new synthetic property
			 */
			if ( value instanceof ToOne ) {
				( (ToOne) value ).setReferencedPropertyName( syntheticPropertyName );
				mappings.addUniquePropertyReference( ownerEntity.getEntityName(), syntheticPropertyName );
			}
			else if ( value instanceof Collection ) {
				( (Collection) value ).setReferencedPropertyName( syntheticPropertyName );
				//not unique because we could create a mtm wo association table
				mappings.addPropertyReference( ownerEntity.getEntityName(), syntheticPropertyName );
			}
			else {
				throw new AssertionFailure(
						"Do a property ref on an unexpected Value type: "
								+ value.getClass().getName()
				);
			}
			mappings.addPropertyReferencedAssociation(
					( inverse ? "inverse__" : "" ) + associatedClass.getEntityName(),
					columns[0].getPropertyName(),
					syntheticPropertyName
			);
		}
	}


	private static List<Property> findPropertiesByColumns(
			Object columnOwner, Ejb3JoinColumn[] columns,
			ExtendedMappings mappings
	) {
		Map<Column, Set<Property>> columnsToProperty = new HashMap<Column, Set<Property>>();
		List<Column> orderedColumns = new ArrayList<Column>( columns.length );
		Table referencedTable = null;
		if ( columnOwner instanceof PersistentClass ) {
			referencedTable = ( (PersistentClass) columnOwner ).getTable();
		}
		else if ( columnOwner instanceof Join ) {
			referencedTable = ( (Join) columnOwner ).getTable();
		}
		else {
			throw new AssertionFailure(
					columnOwner == null ?
							"columnOwner is null" :
							"columnOwner neither PersistentClass nor Join: " + columnOwner.getClass()
			);
		}
		//build the list of column names
		for ( Ejb3JoinColumn column1 : columns ) {
			Column column = new Column(
					mappings.getPhysicalColumnName( column1.getReferencedColumn(), referencedTable )
			);
			orderedColumns.add( column );
			columnsToProperty.put( column, new HashSet<Property>() );
		}
		Iterator it = columnOwner instanceof PersistentClass ?
				( (PersistentClass) columnOwner ).getPropertyIterator() :
				( (Join) columnOwner ).getPropertyIterator();
		while ( it.hasNext() ) {
			Property property = (Property) it.next();
			if ( "noop".equals( property.getPropertyAccessorName() )
					|| "embedded".equals( property.getPropertyAccessorName() ) ) {
				continue;
			}
			Iterator columnIt = property.getColumnIterator();
			while ( columnIt.hasNext() ) {
				Object column = columnIt.next(); //can be a Formula so we don't cast
				//noinspection SuspiciousMethodCalls
				if ( columnsToProperty.containsKey( column ) ) {
					columnsToProperty.get( column ).add( property );
				}
			}
		}
		//first naive implementation
		//only check 1 columns properties
		//TODO make it smarter by checking correctly ordered multi column properties
		List<Property> orderedProperties = new ArrayList<Property>();
		for ( Column column : orderedColumns ) {
			boolean found = false;
			for ( Property property : columnsToProperty.get( column ) ) {
				if ( property.getColumnSpan() == 1 ) {
					orderedProperties.add( property );
					found = true;
					break;
				}
			}
			if ( !found ) return null; //have to find it the hard way
		}
		return orderedProperties;
	}

	/**
	 * Retrieve the property by path in a recursive way, including IndetifierProperty in the loop
	 * If propertyName is null or empty, the IdentifierProperty is returned
	 */
	public static Property findPropertyByName(PersistentClass associatedClass, String propertyName) {
		Property property = null;
		Property idProperty = associatedClass.getIdentifierProperty();
		String idName = idProperty != null ? idProperty.getName() : null;
		try {
			if ( propertyName == null
					|| propertyName.length() == 0
					|| propertyName.equals( idName ) ) {
				//default to id
				property = idProperty;
			}
			else {
				if ( propertyName.indexOf( idName + "." ) == 0 ) {
					property = idProperty;
					propertyName = propertyName.substring( idName.length() + 1 );
				}
				StringTokenizer st = new StringTokenizer( propertyName, ".", false );
				while ( st.hasMoreElements() ) {
					String element = (String) st.nextElement();
					if ( property == null ) {
						property = associatedClass.getProperty( element );
					}
					else {
						if ( ! property.isComposite() ) return null;
						property = ( (Component) property.getValue() ).getProperty( element );
					}
				}
			}
		}
		catch (MappingException e) {
			try {
				//if we do not find it try to check the identifier mapper
				if ( associatedClass.getIdentifierMapper() == null ) return null;
				StringTokenizer st = new StringTokenizer( propertyName, ".", false );
				while ( st.hasMoreElements() ) {
					String element = (String) st.nextElement();
					if ( property == null ) {
						property = associatedClass.getIdentifierMapper().getProperty( element );
					}
					else {
						if ( ! property.isComposite() ) return null;
						property = ( (Component) property.getValue() ).getProperty( element );
					}
				}
			}
			catch (MappingException ee) {
				return null;
			}
		}
		return property;
	}

	public static String getRelativePath(PropertyHolder propertyHolder, String propertyName) {
		if ( propertyHolder == null ) return propertyName;
		String path = propertyHolder.getPath();
		String entityName = propertyHolder.getPersistentClass().getEntityName();
		if ( path.length() == entityName.length() ) {
			return propertyName;
		}
		else {
			return StringHelper.qualify( path.substring( entityName.length() + 1 ), propertyName );
		}
	}

	/**
	 * Find the column owner (ie PersistentClass or Join) of columnName.
	 * If columnName is null or empty, persistentClass is returned
	 */
	public static Object findColumnOwner(
			PersistentClass persistentClass, String columnName, ExtendedMappings mappings
	) {
		if ( StringHelper.isEmpty( columnName ) ) {
			return persistentClass; //shortcut for implicit referenced column names
		}
		PersistentClass current = persistentClass;
		Object result = null;
		boolean found = false;
		do {
			result = current;
			Table currentTable = current.getTable();
			try {
				mappings.getPhysicalColumnName( columnName, currentTable );
				found = true;
			}
			catch (MappingException me) {
				//swallow it
			}
			Iterator joins = current.getJoinIterator();
			while ( ! found && joins.hasNext() ) {
				result = joins.next();
				currentTable = ( (Join) result ).getTable();
				try {
					mappings.getPhysicalColumnName( columnName, currentTable );
					found = true;
				}
				catch (MappingException me) {
					//swallow it
				}
			}
			current = current.getSuperclass();
		}
		while ( !found && current != null );
		return found ? result : null;
	}

	/** apply an id generator to a SimpleValue */
	public static void makeIdGenerator(
			SimpleValue id, String generatorType, String generatorName, ExtendedMappings mappings,
			Map<String, IdGenerator> localGenerators
	) {
		Table table = id.getTable();
		table.setIdentifierValue( id );
		//generator settings
		id.setIdentifierGeneratorStrategy( generatorType );
		Properties params = new Properties();
		//always settable
		params.setProperty(
				PersistentIdentifierGenerator.TABLE, table.getName()
		);

		if ( id.getColumnSpan() == 1 ) {
			params.setProperty(
					PersistentIdentifierGenerator.PK,
					( (org.hibernate.mapping.Column) id.getColumnIterator().next() ).getName()
			);
		}
		if ( ! isDefault( generatorName ) ) {
			//we have a named generator
			IdGenerator gen = mappings.getGenerator( generatorName, localGenerators );
			if ( gen == null ) {
				throw new AnnotationException( "Unknown Id.generator: " + generatorName );
			}
			//This is quite vague in the spec but a generator could override the generate choice
			String identifierGeneratorStrategy = gen.getIdentifierGeneratorStrategy();
			//yuk! this is a hack not to override 'AUTO' even if generator is set
			final boolean avoidOverriding =
					identifierGeneratorStrategy.equals( "identity" )
							|| identifierGeneratorStrategy.equals( "seqhilo" )
							|| identifierGeneratorStrategy.equals( MultipleHiLoPerTableGenerator.class.getName() );
			if ( generatorType == null || ! avoidOverriding ) {
				id.setIdentifierGeneratorStrategy( identifierGeneratorStrategy );
			}
			//checkIfMatchingGenerator(gen, generatorType, generatorName);
			Iterator genParams = gen.getParams().entrySet().iterator();
			while ( genParams.hasNext() ) {
				Map.Entry elt = (Map.Entry) genParams.next();
				params.setProperty( (String) elt.getKey(), (String) elt.getValue() );
			}
		}
		if ( "assigned".equals( generatorType ) ) id.setNullValue( "undefined" );
		id.setIdentifierGeneratorProperties( params );
	}

	public static boolean isDefault(String annotationString) {
		return annotationString != null && annotationString.length() == 0;
		//equivalent to (but faster) ANNOTATION_STRING_DEFAULT.equals( annotationString );
	}
}
