package org.hibernate.reflection;

import java.util.Map;
import java.lang.reflect.Method;

/**
 * The entry point to the reflection layer (a.k.a. the X* layer).
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 */
public interface ReflectionManager {

	// TODO: turn this Singleton into a plug-in
	//public static final JavaXFactory INSTANCE = new JavaXFactory();

	public <T> XClass toXClass(Class<T> clazz);

	public Class toClass(XClass xClazz);

	public Method toMethod(XMethod method);

	public <T> XClass classForName(String name, Class<T> caller) throws ClassNotFoundException;

	public XPackage packageForName(String packageName) throws ClassNotFoundException;

	public <T> boolean equals(XClass class1, Class<T> class2);

	public Map getDefaults();
}
