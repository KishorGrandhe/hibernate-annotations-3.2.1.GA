package org.hibernate.reflection.java;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityListeners;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.TableGenerator;

import org.dom4j.Element;
import org.hibernate.reflection.ReflectionManager;
import org.hibernate.reflection.XClass;
import org.hibernate.reflection.XMethod;
import org.hibernate.reflection.XPackage;
import org.hibernate.reflection.XProperty;
import org.hibernate.reflection.java.generics.IdentityTypeEnvironment;
import org.hibernate.reflection.java.generics.TypeEnvironment;
import org.hibernate.reflection.java.generics.TypeEnvironmentFactory;
import org.hibernate.reflection.java.generics.TypeSwitch;
import org.hibernate.reflection.java.generics.TypeUtils;
import org.hibernate.reflection.java.xml.XMLContext;
import org.hibernate.util.ReflectHelper;
import org.hibernate.AssertionFailure;

/**
 * The factory for all the objects in this package.
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Emmanuel Bernard
 */
public class JavaXFactory implements ReflectionManager {

	private XMLContext xmlContext;
	private HashMap defaults;
    private static Constructor[] annotationReaderConstructors;

    static {
        buildAnnotationReaderConstructor();
    }


    public JavaAnnotationReader buildAnnotationReader(AnnotatedElement annotatedElement) {
        try {
            if ( xmlContext.hasContext() ) {
                return (JavaAnnotationReader) annotationReaderConstructors[1].newInstance(annotatedElement, xmlContext);
            }
            else {
                return (JavaAnnotationReader) annotationReaderConstructors[0].newInstance(annotatedElement);
            }
        }
        catch (IllegalAccessException e) {
            throw new AssertionFailure( "Unable to call AnnotationReader constructor", e );
        }
        catch (InvocationTargetException e) {
            throw new AssertionFailure( "Unable to call AnnotationReader constructor", e );
        }
        catch (InstantiationException e) {
            throw new AssertionFailure( "Unable to call AnnotationReader constructor", e );
        }
    }

    private static void buildAnnotationReaderConstructor() {
        annotationReaderConstructors = new Constructor[2];
        try {
            Class readerClass = ReflectHelper.classForName( "org.hibernate.reflection.java.JavaAnnotationReader");
            annotationReaderConstructors[0] = readerClass.getConstructor( AnnotatedElement.class );
        }
        catch (ClassNotFoundException e) {
            throw new AssertionFailure( "Unable to load org.hibernate.reflection.java.JavaAnnotationReader", e );
        }
        catch (NoSuchMethodException e) {
            throw new AssertionFailure( "Unable to call constructor org.hibernate.reflection.java.JavaAnnotationReader(AnnotatedElement)", e );
        }

        try {
            Class readerClass = ReflectHelper.classForName( "org.hibernate.reflection.java.EJB3OverridenAnnotationReader");
            annotationReaderConstructors[1] = readerClass.getConstructor( AnnotatedElement.class, XMLContext.class );
        }
        catch (ClassNotFoundException e) {
            throw new AssertionFailure( "Unable to load org.hibernate.reflection.java.EJB3OverridenAnnotationReader", e );
        }
        catch (NoSuchMethodException e) {
            throw new AssertionFailure( "Unable to call constructor org.hibernate.reflection.java.EJB3OverridenAnnotationReader(AnnotatedElement, XMLContext)", e );
        }
    }

    private static class TypeKey extends Pair<Type, TypeEnvironment> {
		TypeKey(Type t, TypeEnvironment context) {
			super( t, context );
		}
	}

	private static class MemberKey extends Pair<Member, TypeKey> {
		MemberKey(Member member, Type owner, TypeEnvironment context) {
			super( member, new TypeKey( owner, context ) );
		}
	}

	private final Map<TypeKey, JavaXClass> xClasses = new HashMap<TypeKey, JavaXClass>();

	private final Map<Package, JavaXPackage> packagesToXPackages = new HashMap<Package, JavaXPackage>();

	private final Map<MemberKey, JavaXProperty> xProperties = new HashMap<MemberKey, JavaXProperty>();

	private final Map<MemberKey, JavaXMethod> xMethods = new HashMap<MemberKey, JavaXMethod>();

	private final TypeEnvironmentFactory typeEnvs = new TypeEnvironmentFactory();

	public JavaXFactory() {
		reset();
	}

	public void reset() {
		xmlContext = new XMLContext();
		xClasses.clear();
		packagesToXPackages.clear();
		xProperties.clear();
		xMethods.clear();
		defaults = null;
	}

	public XClass toXClass(Class clazz) {
		return toXClass( clazz, IdentityTypeEnvironment.INSTANCE );
	}

	public Class toClass(XClass xClazz) {
		if ( ! ( xClazz instanceof JavaXClass ) ) {
			throw new IllegalArgumentException( "XClass not coming from this ReflectionManager implementation" );
		}
		return (Class) ( (JavaXClass) xClazz ).toAnnotatedElement();
	}

	public Method toMethod(XMethod xMethod) {
		if ( ! ( xMethod instanceof JavaXMethod ) ) {
			throw new IllegalArgumentException( "XMethod not coming from this ReflectionManager implementation" );
		}
		return (Method) ( (JavaXAnnotatedElement) xMethod ).toAnnotatedElement();
	}

	public XClass classForName(String name, Class caller) throws ClassNotFoundException {
		return toXClass( ReflectHelper.classForName( name, caller ) );
	}

	public XPackage packageForName(String packageName) throws ClassNotFoundException {
		return getXAnnotatedElement( ReflectHelper.classForName( packageName + ".package-info" ).getPackage() );
	}

	public Map getDefaults() {
		if (defaults == null) {
			defaults = new HashMap();
			XMLContext.Default xmlDefaults = xmlContext.getDefault( null );
			List<Class> entityListeners = new ArrayList<Class>();
			for ( String className : xmlContext.getDefaultEntityListeners() ) {
				try {
					entityListeners.add( ReflectHelper.classForName( className, this.getClass() ) );
				}
				catch (ClassNotFoundException e) {
					throw new IllegalStateException( "Default entity listener class not found: " + className );
				}
			}
			defaults.put( EntityListeners.class, entityListeners );
			for( Element element : xmlContext.getAllDocuments() ) {

				List<Element> elements = element.elements( "sequence-generator" );
				List<SequenceGenerator> sequenceGenerators = (List<SequenceGenerator>) defaults.get(SequenceGenerator.class);
				if (sequenceGenerators == null) {
					sequenceGenerators = new ArrayList<SequenceGenerator>();
					defaults.put( SequenceGenerator.class, sequenceGenerators );
				}
				for (Element subelement : elements) {
					sequenceGenerators.add( EJB3OverridenAnnotationReader.buildSequenceGeneratorAnnotation( subelement ) );
				}

				elements = element.elements( "table-generator" );
				List<TableGenerator> tableGenerators = (List<TableGenerator>) defaults.get(TableGenerator.class);
				if (tableGenerators == null) {
					tableGenerators = new ArrayList<TableGenerator>();
					defaults.put( TableGenerator.class, tableGenerators );
				}
				for (Element subelement : elements) {
					tableGenerators.add( EJB3OverridenAnnotationReader.buildTableGeneratorAnnotation( subelement, xmlDefaults ) );
				}

				List<NamedQuery> namedQueries = (List<NamedQuery>) defaults.get(NamedQuery.class);
				if (namedQueries == null) {
					namedQueries = new ArrayList<NamedQuery>();
					defaults.put( NamedQuery.class, namedQueries );
				}
				List<NamedQuery> currentNamedQueries = EJB3OverridenAnnotationReader.buildNamedQueries(element, false, xmlDefaults);
				namedQueries.addAll( currentNamedQueries );

				List<NamedNativeQuery> namedNativeQueries = (List<NamedNativeQuery>) defaults.get(NamedNativeQuery.class);
				if (namedNativeQueries == null) {
					namedNativeQueries = new ArrayList<NamedNativeQuery>();
					defaults.put( NamedNativeQuery.class, namedNativeQueries );
				}
				List<NamedNativeQuery> currentNamedNativeQueries = EJB3OverridenAnnotationReader.buildNamedQueries(element, true, xmlDefaults);
				namedNativeQueries.addAll( currentNamedNativeQueries );

				List<SqlResultSetMapping> sqlResultSetMappings = (List<SqlResultSetMapping>) defaults.get(SqlResultSetMapping.class);
				if (sqlResultSetMappings == null) {
					sqlResultSetMappings = new ArrayList<SqlResultSetMapping>();
					defaults.put( SqlResultSetMapping.class, sqlResultSetMappings );
				}
				List<SqlResultSetMapping> currentSqlResultSetMappings = EJB3OverridenAnnotationReader.buildSqlResultsetMappings(element, xmlDefaults);
				sqlResultSetMappings.addAll( currentSqlResultSetMappings );
			}
		}
		return defaults;
	}

	XClass toXClass(Type t, final TypeEnvironment context) {
		return new TypeSwitch<XClass>() {
			@Override
			public XClass caseClass(Class classType) {
				TypeKey key = new TypeKey( classType, context );
				JavaXClass result = xClasses.get( key );
				if ( result == null ) {
					result = new JavaXClass( classType, context, JavaXFactory.this );
					xClasses.put( key, result );
				}
				return result;
			}

			@Override
			public XClass caseParameterizedType(ParameterizedType parameterizedType) {
				return toXClass( parameterizedType.getRawType(), context );
			}
		}.doSwitch( context.bind( t ) );
	}

	XPackage getXAnnotatedElement(Package pkg) {
		JavaXPackage xPackage = packagesToXPackages.get( pkg );
		if ( xPackage == null ) {
			xPackage = new JavaXPackage( pkg, this );
			packagesToXPackages.put( pkg, xPackage );
		}
		return xPackage;
	}

	XProperty getXProperty(Member member, JavaXClass owner) {
		MemberKey key = new MemberKey( member, owner.toClass(), owner.getTypeEnvironment() );
        //FIXME get is as expensive as create most time spent in hashCode and equals
        JavaXProperty xProperty = xProperties.get( key );
		if ( xProperty == null ) {
			xProperty = JavaXProperty.create( member, owner.getTypeEnvironment(), this );
			xProperties.put( key, xProperty );
		}
		return xProperty;
	}

	XMethod getXMethod(Member member, JavaXClass owner) {
		MemberKey key = new MemberKey( member, owner.toClass(), owner.getTypeEnvironment() );
        //FIXME get is as expensive as create most time spent in hashCode and equals
        JavaXMethod xMethod = xMethods.get( key );
		if ( xMethod == null ) {
			xMethod = JavaXMethod.create( member, owner.getTypeEnvironment(), this );
			xMethods.put( key, xMethod );
		}
		return xMethod;
	}

	TypeEnvironment getTypeEnvironment(final Type t) {
		return new TypeSwitch<TypeEnvironment>() {
			@Override
			public TypeEnvironment caseClass(Class classType) {
				return typeEnvs.getEnvironment( classType );
			}

			@Override
			public TypeEnvironment caseParameterizedType(ParameterizedType parameterizedType) {
				return typeEnvs.getEnvironment( parameterizedType );
			}

			@Override
			public TypeEnvironment defaultCase(Type type) {
				return IdentityTypeEnvironment.INSTANCE;
			}
		}.doSwitch( t );
	}

	public JavaXType toXType(TypeEnvironment context, Type propType) {
		Type boundType = toApproximatingEnvironment( context ).bind( propType );
		if ( TypeUtils.isArray( boundType ) ) {
			return new JavaXArrayType( propType, context, this );
		}
		if ( TypeUtils.isCollection( boundType ) ) {
			return new JavaXCollectionType( propType, context, this );
		}
		if ( TypeUtils.isSimple( boundType ) ) {
			return new JavaXSimpleType( propType, context, this );
		}
		throw new IllegalArgumentException( "No PropertyTypeExtractor available for type void " );
	}

	public boolean equals(XClass class1, Class class2) {
		if ( class1 == null ) {
			return class2 == null;
		}
		return ( (JavaXClass) class1 ).toClass().equals( class2 );
	}

	public TypeEnvironment toApproximatingEnvironment(TypeEnvironment context) {
		return typeEnvs.toApproximatingEnvironment( context );
	}

	public XMLContext getXMLContext() {
		return xmlContext;
	}
}
