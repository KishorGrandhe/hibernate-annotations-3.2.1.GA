package org.hibernate.test.reflection.java.generics.deep;

import javax.persistence.MappedSuperclass;

import org.hibernate.test.reflection.java.generics.deep.Dummy;
import org.hibernate.test.reflection.java.generics.deep.GenericSuperclass1;

/**
 *
 */
@MappedSuperclass
public class GenericSuperclass2<T extends Dummy> extends GenericSuperclass1<T> {
}
