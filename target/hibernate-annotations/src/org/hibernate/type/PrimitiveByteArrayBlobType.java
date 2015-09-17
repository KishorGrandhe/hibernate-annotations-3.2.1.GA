//$Id: PrimitiveByteArrayBlobType.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.type;

/**
 * Map a byte[] to a Blob
 *
 * @author Emmanuel Bernard
 */
public class PrimitiveByteArrayBlobType extends ByteArrayBlobType {
	public Class getReturnedClass() {
		return byte[].class;
	}

	protected Object wrap(byte[] bytes) {
		return bytes;
	}

	protected byte[] unWrap(Object bytes) {
		return (byte[]) bytes;
	}
}
