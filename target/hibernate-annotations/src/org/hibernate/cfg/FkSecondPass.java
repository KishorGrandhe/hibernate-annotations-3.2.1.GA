// $Id: FkSecondPass.java 10321 2006-08-23 22:00:58Z epbernard $
package org.hibernate.cfg;

import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.util.StringHelper;
import org.hibernate.cfg.annotations.TableBinder;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Property;

/**
 * Enable a proper set of the FK columns in respect with the id column order
 * Allow the correct implementation of the default EJB3 values which needs both
 * sides of the association to be resolved
 *
 * @author Emmanuel Bernard
 */
public class FkSecondPass implements SecondPass {
	private ToOne value;
	private Ejb3JoinColumn[] columns;
	private boolean unique;
	private ExtendedMappings mappings;
	private String path;
	private String entityClassName;

	FkSecondPass(
			ToOne value, Ejb3JoinColumn[] columns, boolean unique, String entityClassName, String path, ExtendedMappings mappings
	) {
		this.mappings = mappings;
		this.value = value;
		this.columns = columns;
		this.unique = unique;
		this.entityClassName = entityClassName;
		this.path = entityClassName != null ? path.substring( entityClassName.length() + 1 ) : path;
	}

	public ToOne getValue() {
		return value;
	}

	public boolean isInPrimaryKey() {
		if (entityClassName == null) return false;
		Property property = mappings.getClass( entityClassName ).getIdentifierProperty();
		return property != null && path != null && path.startsWith( property.getName() );
	}

	public void doSecondPass(java.util.Map persistentClasses) throws MappingException {
		if ( value instanceof ManyToOne ) {
			ManyToOne manyToOne = (ManyToOne) value;
			PersistentClass ref = (PersistentClass) persistentClasses.get( manyToOne.getReferencedEntityName() );
			if ( ref == null ) {
				throw new AnnotationException(
						"@OneToOne or @ManyToOne on "
								+ StringHelper.qualify(entityClassName, path)
								+ " references an unknown entity: "
								+ manyToOne.getReferencedEntityName()
				);
			}
			BinderHelper.createSyntheticPropertyReference( columns, ref, null, manyToOne, false, mappings );
			TableBinder.bindFk( ref, null, columns, manyToOne, unique, mappings );
			/*
			 * HbmBinder does this only when property-ref != null, but IMO, it makes sense event if it is null
			 */
			if ( ! manyToOne.isIgnoreNotFound() ) manyToOne.createPropertyRefConstraints( persistentClasses );
		}
		else if ( value instanceof OneToOne ) {
			( (OneToOne) value ).createForeignKey();
		}
		else {
			throw new AssertionFailure( "FkSecondPass for a wrong value type: " + value.getClass().getName() );
		}
	}
}
