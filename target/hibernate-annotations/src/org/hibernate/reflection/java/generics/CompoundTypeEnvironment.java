package org.hibernate.reflection.java.generics;

import java.lang.reflect.Type;

/**
 * A composition of two <code>TypeEnvironment</code> functions.
 *
 * @author Davide Marchignoli
 * @author Paolo Perrotta
 */
public class CompoundTypeEnvironment implements TypeEnvironment {

	private final TypeEnvironment f;

	private final TypeEnvironment g;
    private final int hashCode;

    public CompoundTypeEnvironment(TypeEnvironment f, TypeEnvironment g) {
		this.f = f;
		this.g = g;
        hashCode = doHashCode();
    }

	public Type bind(Type type) {
		return f.bind( g.bind( type ) );
	}

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( ! ( o instanceof CompoundTypeEnvironment ) ) return false;

		final CompoundTypeEnvironment that = (CompoundTypeEnvironment) o;

        if ( differentHashCode( that ) ) return false;

        if ( !f.equals( that.f ) ) return false;
        return g.equals( that.g );

    }

    private boolean differentHashCode(CompoundTypeEnvironment that) {
        return hashCode != that.hashCode;
    }

    private int doHashCode() {
		int result;
		result = f.hashCode();
		result = 29 * result + g.hashCode();
		return result;
	}

    public int hashCode() {
        //cached because the inheritance can be big
        return hashCode;
    }
}
