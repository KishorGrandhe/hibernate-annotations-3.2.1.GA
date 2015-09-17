package org.hibernate.test.reflection.java.generics.deep;

import javax.persistence.Entity;

import org.hibernate.test.reflection.java.generics.deep.DummySubclass;
import org.hibernate.test.reflection.java.generics.deep.GenericSuperclass1;

/**
 *
 */
@Entity
public class Subclass1 extends GenericSuperclass1<DummySubclass> {
}
