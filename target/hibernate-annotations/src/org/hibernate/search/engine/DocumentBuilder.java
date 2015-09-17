//$Id: DocumentBuilder.java 10865 2006-11-23 23:30:01 +0100 (jeu., 23 nov. 2006) epbernard $
package org.hibernate.search.engine;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.cfg.annotations.Version;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Keyword;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.Text;
import org.hibernate.search.annotations.Unstored;
import org.hibernate.search.bridge.BridgeFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.event.FullTextIndexEventListener;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.search.util.BinderHelper;
import org.hibernate.reflection.ReflectionManager;
import org.hibernate.reflection.XAnnotatedElement;
import org.hibernate.reflection.XClass;
import org.hibernate.reflection.XMember;
import org.hibernate.reflection.XProperty;
import org.hibernate.util.ReflectHelper;

/**
 * Set up and provide a manager for indexes classes
 *
 * @author Gavin King
 * @author Emmanuel Bernard
 * @author Sylvain Vieujot
 * @author Richard Hallier
 */
public class DocumentBuilder<T> {

	static {
		Version.touch(); //touch version
	}

	private final List<XMember> keywordGetters = new ArrayList<XMember>();
	private final List<String> keywordNames = new ArrayList<String>();
	private final List<FieldBridge> keywordBridges = new ArrayList<FieldBridge>();
	private final List<XMember> unstoredGetters = new ArrayList<XMember>();
	private final List<String> unstoredNames = new ArrayList<String>();
	private final List<FieldBridge> unstoredBridges = new ArrayList<FieldBridge>();
	private final List<XMember> textGetters = new ArrayList<XMember>();
	private final List<String> textNames = new ArrayList<String>();
	private final List<FieldBridge> textBridges = new ArrayList<FieldBridge>();
	private final List<String> fieldNames = new ArrayList<String>();
	private final List<XMember> fieldGetters = new ArrayList<XMember>();
	private final List<FieldBridge> fieldBridges = new ArrayList<FieldBridge>();
	private final List<Field.Store> fieldStore = new ArrayList<Field.Store>();
	private final List<Field.Index> fieldIndex = new ArrayList<Field.Index>();

	private final XClass beanClass;
	private final DirectoryProvider directoryProvider;
	private String idKeywordName;
	private final Analyzer analyzer;
	private Float idBoost;
	public static final String CLASS_FIELDNAME = "_hibernate_class";
	private TwoWayFieldBridge idBridge;
	private Set<Class> mappedSubclasses = new HashSet<Class>();
	private ReflectionManager reflectionManager;


	public DocumentBuilder(XClass clazz, Analyzer analyzer, DirectoryProvider directory,
						   ReflectionManager reflectionManager) {
		this.beanClass = clazz;
		this.analyzer = analyzer;
		this.directoryProvider = directory;
		//FIXME get rid of it when boost is stored?
		this.reflectionManager = reflectionManager;

		if ( clazz == null ) throw new AssertionFailure( "Unable to build a DocumentBuilder with a null class" );

		for ( XClass currClass = beanClass; currClass != null; currClass = currClass.getSuperclass() ) {
			//rejecting non properties because the object is loaded from Hibernate, so indexing a non property does not make sense
			List<XProperty> methods = currClass.getDeclaredProperties( XClass.ACCESS_PROPERTY );
			for ( XProperty method : methods ) {
				initializeMember( method );
			}

			List<XProperty> fields = currClass.getDeclaredProperties( XClass.ACCESS_FIELD );
			for ( XProperty field : fields ) {
				initializeMember( field );
			}
		}

		if ( idKeywordName == null ) {
			throw new HibernateException( "No document id for: " + clazz.getName() );
		}
	}

	private void initializeMember(XProperty member) {
		Keyword keywordAnn = member.getAnnotation( Keyword.class );
		if ( keywordAnn != null ) {
			String name = BinderHelper.getAttributeName( member, keywordAnn.name() );
			if ( keywordAnn.id() ) {
				idKeywordName = name;
				idBoost = getBoost( member );
				FieldBridge fieldBridge = BridgeFactory.guessType( member );
				if ( fieldBridge instanceof TwoWayFieldBridge ) {
					idBridge = (TwoWayFieldBridge) fieldBridge;
				}
				else {
					throw new HibernateException(
							"Bridge for document id does not implement IdFieldBridge: " + member.getName() );
				}
			}
			else {
				setAccessible( member );
				keywordGetters.add( member );
				keywordNames.add( name );
				keywordBridges.add( BridgeFactory.guessType( member ) );
			}
		}

		Unstored unstoredAnn = member.getAnnotation( Unstored.class );
		if ( unstoredAnn != null ) {
			setAccessible( member );
			unstoredGetters.add( member );
			unstoredNames.add( BinderHelper.getAttributeName( member, unstoredAnn.name() ) );
			unstoredBridges.add( BridgeFactory.guessType( member ) );
		}

		Text textAnn = member.getAnnotation( Text.class );
		if ( textAnn != null ) {
			setAccessible( member );
			textGetters.add( member );
			textNames.add( BinderHelper.getAttributeName( member, textAnn.name() ) );
			textBridges.add( BridgeFactory.guessType( member ) );
		}

		DocumentId documentIdAnn = member.getAnnotation( DocumentId.class );
		if ( documentIdAnn != null ) {
			if ( idKeywordName != null ) {
				throw new AssertionFailure( "Two document id assigned: "
						+ idKeywordName + " and " + BinderHelper.getAttributeName( member, documentIdAnn.name() ) );
			}
			idKeywordName = BinderHelper.getAttributeName( member, documentIdAnn.name() );
			FieldBridge fieldBridge = BridgeFactory.guessType( member );
			if ( fieldBridge instanceof TwoWayFieldBridge ) {
				idBridge = (TwoWayFieldBridge) fieldBridge;
			}
			else {
				throw new HibernateException(
						"Bridge for document id does not implement IdFieldBridge: " + member.getName() );
			}
			idBoost = getBoost( member );
		}

		org.hibernate.search.annotations.Field fieldAnn =
				member.getAnnotation( org.hibernate.search.annotations.Field.class );
		if ( fieldAnn != null ) {
			setAccessible( member );
			fieldGetters.add( member );
			fieldNames.add( BinderHelper.getAttributeName( member, fieldAnn.name() ) );
			fieldStore.add( getStore( fieldAnn.store() ) );
			fieldIndex.add( getIndex( fieldAnn.index() ) );
			fieldBridges.add( BridgeFactory.guessType( member ) );
		}
	}

	private Field.Store getStore(Store store) {
		switch (store) {
			case NO:
				return Field.Store.NO;
			case YES:
				return Field.Store.YES;
			case COMPRESS:
				return Field.Store.COMPRESS;
			default:
				throw new AssertionFailure( "Unexpected Store: " + store );
		}
	}

	private Field.Index getIndex(Index index) {
		switch (index) {
			case NO:
				return Field.Index.NO;
			case NO_NORMS:
				return Field.Index.NO_NORMS;
			case TOKENIZED:
				return Field.Index.TOKENIZED;
			case UN_TOKENISED:
				return Field.Index.UN_TOKENIZED;
			default:
				throw new AssertionFailure( "Unexpected Index: " + index );
		}
	}

	private Float getBoost(XAnnotatedElement element) {
		if ( element == null ) return null;
		Boost boost = element.getAnnotation( Boost.class );
		return boost != null ?
				boost.value() :
				null;
	}

	private Object getMemberValue(T bean, XMember getter) {
		Object value;
		try {
			value = getter.invoke( bean );
		}
		catch (Exception e) {
			throw new IllegalStateException( "Could not get property value", e );
		}
		return value;
	}

	public Document getDocument(T instance, Serializable id) {
		Document doc = new Document();
		XClass instanceClass = reflectionManager.toXClass( instance.getClass() );
		Float boost = getBoost( instanceClass );
		if ( boost != null ) {
			doc.setBoost( boost );
		}
		{
			Field classField =
					new Field( CLASS_FIELDNAME, instanceClass.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED );
			doc.add( classField );
			idBridge.set( idKeywordName, id, doc, Field.Store.YES, Field.Index.UN_TOKENIZED, idBoost );
		}
		for ( int i = 0; i < keywordNames.size(); i++ ) {
			XMember member = keywordGetters.get( i );
			Object value = getMemberValue( instance, member );
			keywordBridges.get( i ).set(
					keywordNames.get( i ), value, doc, Field.Store.YES,
					Field.Index.UN_TOKENIZED, getBoost( member )
			);
		}
		for ( int i = 0; i < textNames.size(); i++ ) {
			XMember member = textGetters.get( i );
			Object value = getMemberValue( instance, member );
			textBridges.get( i ).set(
					textNames.get( i ), value, doc, Field.Store.YES,
					Field.Index.TOKENIZED, getBoost( member )
			);
		}
		for ( int i = 0; i < unstoredNames.size(); i++ ) {
			XMember member = unstoredGetters.get( i );
			Object value = getMemberValue( instance, member );
			unstoredBridges.get( i ).set(
					unstoredNames.get( i ), value, doc, Field.Store.NO,
					Field.Index.TOKENIZED, getBoost( member )
			);
		}
		for ( int i = 0; i < fieldNames.size(); i++ ) {
			XMember member = fieldGetters.get( i );
			Object value = getMemberValue( instance, member );
			fieldBridges.get( i ).set(
					fieldNames.get( i ), value, doc, fieldStore.get( i ),
					fieldIndex.get( i ), getBoost( member )
			);
		}
		return doc;
	}

	public Term getTerm(Serializable id) {
		return new Term( idKeywordName, idBridge.objectToString( id ) );
	}

	public DirectoryProvider getDirectoryProvider() {
		return directoryProvider;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	private static void setAccessible(XMember member) {
		if ( !Modifier.isPublic( member.getModifiers() ) ) {
			member.setAccessible( true );
		}
	}

	public TwoWayFieldBridge getIdBridge() {
		return idBridge;
	}

	public String getIdKeywordName() {
		return idKeywordName;
	}

	public static Class getDocumentClass(Document document) {
		String className = document.get( DocumentBuilder.CLASS_FIELDNAME );
		try {
			return ReflectHelper.classForName( className );
		}
		catch (ClassNotFoundException e) {
			throw new HibernateException( "Unable to load indexed class: " + className, e );
		}
	}

	public static Serializable getDocumentId(FullTextIndexEventListener listener, Class clazz, Document document) {
		DocumentBuilder builder = listener.getDocumentBuilders().get( clazz );
		if ( builder == null ) throw new HibernateException( "No Lucene configuration set up for: " + clazz.getName() );
		return (Serializable) builder.getIdBridge().get( builder.getIdKeywordName(), document );
	}

	public void postInitialize(Set<Class> indexedClasses) {
		//this method does not requires synchronization
		Class plainClass = reflectionManager.toClass( beanClass );
		Set<Class> tempMappedSubclasses = new HashSet<Class>();
		//together with the caller this creates a o(2), but I think it's still faster than create the up hierarchy for each class
		for ( Class currentClass : indexedClasses ) {
			if ( plainClass.isAssignableFrom( currentClass ) ) tempMappedSubclasses.add( currentClass );
		}
		mappedSubclasses = Collections.unmodifiableSet( tempMappedSubclasses );
	}


	public Set<Class> getMappedSubclasses() {
		return mappedSubclasses;
	}
}
