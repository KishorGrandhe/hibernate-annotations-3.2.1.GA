package org.hibernate.reflection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.hibernate.reflection.XAnnotatedElement;

/**
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 */
abstract class JavaXAnnotatedElement implements XAnnotatedElement {

	// responsible for extracting annotations
	private JavaAnnotationReader annotationReader;

	private final JavaXFactory factory;

	private final AnnotatedElement annotatedElement;

	public JavaXAnnotatedElement(AnnotatedElement annotatedElement, JavaXFactory factory) {
		//this.annotationReader = new EJB3OverridenAnnotationReader( annotatedElement, factory.getXMLContext() );
        this.factory = factory;
		this.annotatedElement = annotatedElement;
	}

	protected JavaXFactory getFactory() {
		return factory;
	}

	private JavaAnnotationReader getAnnotationReader() {
        if (annotationReader == null) {
            annotationReader = factory.buildAnnotationReader(annotatedElement);
        }
        return annotationReader;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return getAnnotationReader().getAnnotation( annotationType );
	}

	public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
		return getAnnotationReader().isAnnotationPresent( annotationType );
	}

	public Annotation[] getAnnotations() {
		return getAnnotationReader().getAnnotations();
	}

	AnnotatedElement toAnnotatedElement() {
		return annotatedElement;
	}

	@Override
	public boolean equals(Object obj) {
		if ( ! ( obj instanceof JavaXAnnotatedElement ) ) return false;
		JavaXAnnotatedElement other = (JavaXAnnotatedElement) obj;
		//FIXME yuk this defeat the type environment
		return annotatedElement.equals( other.toAnnotatedElement() );
	}

	@Override
	public int hashCode() {
		return annotatedElement.hashCode();
	}

	@Override
	public String toString() {
		return annotatedElement.toString();
	}
}
