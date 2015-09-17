//$Id: XMethod.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.reflection;

/**
 * Represent an invokable method
 * <p/>
 * The underlying layer does not guaranty that xProperty == xMethod
 * if the underlying artefact is the same
 * However xProperty.equals(xMethod) is supposed to return true
 *
 * @author Emmanuel Bernard
 */
public interface XMethod extends XMember {
}
