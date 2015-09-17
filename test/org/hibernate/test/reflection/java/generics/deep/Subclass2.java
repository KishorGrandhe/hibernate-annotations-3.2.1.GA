package org.hibernate.test.reflection.java.generics.deep;

import javax.persistence.Entity;

import org.hibernate.test.reflection.java.generics.deep.DummySubclass;
import org.hibernate.test.reflection.java.generics.deep.GenericSuperclass2;

/**
 *
 */
@Entity
public class Subclass2 extends GenericSuperclass2<DummySubclass> {
}
