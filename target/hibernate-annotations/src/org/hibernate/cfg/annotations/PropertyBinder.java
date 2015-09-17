//$Id: PropertyBinder.java 10657 2006-10-27 21:35:48Z epbernard $
package org.hibernate.cfg.annotations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.ExtendedMappings;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Value;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.reflection.XClass;
import org.hibernate.reflection.XProperty;
import org.hibernate.AnnotationException;
import org.hibernate.util.StringHelper;

/**
 * @author Emmanuel Bernard
 */
public class PropertyBinder {
	private static Log log = LogFactory.getLog( PropertyBinder.class );
	private String name;
	private String returnedClassName;
	private boolean lazy;
	private String propertyAccessorName;
	private Ejb3Column[] columns;
	private PropertyHolder holder;
	private ExtendedMappings mappings;
	private Value value;
	private boolean insertable = true;
	private boolean updatable = true;
	private String cascade;
	/*
	 * property can be null
	 * prefer propertyName to property.getName() since some are overloaded
	 */
	private XProperty property;
	private XClass returnedClass;

	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}

	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setReturnedClassName(String returnedClassName) {
		this.returnedClassName = returnedClassName;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public void setPropertyAccessorName(String propertyAccessorName) {
		this.propertyAccessorName = propertyAccessorName;
	}

	public void setColumns(Ejb3Column[] columns) {
		insertable = columns[0].isInsertable();
		updatable = columns[0].isUpdatable();
		//concsistency is checked later when we know the proeprty name
		this.columns = columns;
	}

	public void setHolder(PropertyHolder holder) {
		this.holder = holder;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public void setCascade(String cascadeStrategy) {
		this.cascade = cascadeStrategy;
	}

	public void setMappings(ExtendedMappings mappings) {
		this.mappings = mappings;
	}

	private void validateBind() {
		//TODO check necessary params for a bind		
	}

	private void validateMake() {
		//TODO check necessary params for a make
	}

	public Property bind() {
		validateBind();
		if ( log.isDebugEnabled() ) {
			log.debug( "binding property " + name + " with lazy=" + lazy );
		}
		String containerClassName = holder == null ? null : holder.getClassName();
		SimpleValueBinder value = new SimpleValueBinder();
		value.setMappings( mappings );
		value.setPropertyName( name );
		value.setReturnedClassName( returnedClassName );
		value.setColumns( columns );
		value.setPersistentClassName( containerClassName );
		value.setType( property, returnedClass );
		value.setMappings( mappings );
		SimpleValue propertyValue = value.make();
		setValue( propertyValue );
		Property prop = make();
		holder.addProperty( prop, columns );
		return prop;
	}

	public Property make() {
		validateMake();
		log.debug( "Building property " + name );
		Property prop = new Property();
		prop.setName( name );
		prop.setValue( value );
		prop.setInsertable( insertable );
		prop.setUpdateable( updatable );
		prop.setLazy( lazy );
		prop.setCascade( cascade );
		prop.setPropertyAccessorName( propertyAccessorName );
		Generated ann =  property != null ?
				property.getAnnotation( Generated.class ) :
				null;
		GenerationTime generated = ann != null ?
				ann.value() :
				null;
		if (generated != null) {
			if ( ! GenerationTime.NEVER.equals( generated ) ) {
				if ( property.isAnnotationPresent( javax.persistence.Version.class )
						&& GenerationTime.INSERT.equals( generated ) ) {
					throw new AnnotationException("@Generated(INSERT) on a @Version property not allowed, use ALWAYS: "
							+ StringHelper.qualify( holder.getPath(), name ) );
				}
				if ( prop.isInsertable() ) {
					throw new AnnotationException("Cannot have @Generated property and insertable columns: "
							+ StringHelper.qualify( holder.getPath(), name ) );
				}
				if ( GenerationTime.ALWAYS.equals( generated ) && prop.isUpdateable() ) {
					throw new AnnotationException("Cannot have @Generated(ALWAYS) property and updatable columns: "
							+ StringHelper.qualify( holder.getPath(), name ) );
				}
				prop.setGeneration( PropertyGeneration.parse( generated.toString().toLowerCase() ) );
			}
		}
		log.trace( "Cascading " + name + " with " + cascade );
		return prop;
	}

	public void setProperty(XProperty property) {
		this.property = property;
	}

	public void setReturnedClass(XClass returnedClass) {
		this.returnedClass = returnedClass;
	}
}
