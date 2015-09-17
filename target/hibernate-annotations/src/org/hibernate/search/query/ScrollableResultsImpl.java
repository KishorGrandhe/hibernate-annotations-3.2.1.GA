//$Id: $
package org.hibernate.search.query;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Searcher;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.event.FullTextIndexEventListener;
import org.hibernate.type.Type;

/**
 * @author Emmanuel Bernard
 */
public class ScrollableResultsImpl implements ScrollableResults {
	private final Searcher searcher;
	private final Hits hits;
	private final int first;
	private final int max;
	private int current;
	private final Session session;
	private final FullTextIndexEventListener listener;
	private EntityInfo[] entityInfos;

	public ScrollableResultsImpl(
			Searcher searcher, Hits hits, int first, int max, Session session, FullTextIndexEventListener listener
	) {
		this.searcher = searcher;
		this.hits = hits;
		this.first = first;
		this.max = max;
		this.current = first;
		this.session = session;
		this.listener = listener;
		entityInfos = new EntityInfo[max - first + 1];
	}

	public boolean next() throws HibernateException {
		return ++current <= max;
	}

	public boolean previous() throws HibernateException {
		return --current >= first;
	}

	public boolean scroll(int i) throws HibernateException {
		current = current + i;
		return current >= first && current <= max;
	}

	public boolean last() throws HibernateException {
		current = max;
		return max >= first;
	}

	public boolean first() throws HibernateException {
		current = first;
		return max >= first;
	}

	public void beforeFirst() throws HibernateException {
		current = first - 1;
	}

	public void afterLast() throws HibernateException {
		current = max + 1;
	}

	public boolean isFirst() throws HibernateException {
		return current == first;
	}

	public boolean isLast() throws HibernateException {
		return current == max;
	}

	public void close() throws HibernateException {
		try {
			searcher.close();
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to close Lucene searcher", e );
		}
	}

	public Object[] get() throws HibernateException {
		if ( current < first || current > max ) return null; //or exception?
		EntityInfo info = entityInfos[current - first];
		if ( info == null ) {
			info = new EntityInfo();
			Document document = null;
			try {
				document = hits.doc( current );
			}
			catch (IOException e) {
				throw new HibernateException( "Unable to read Lucene hits[" + current + "]", e );
			}
			info.clazz = DocumentBuilder.getDocumentClass( document );
			//FIXME should check that clazz match classes but this complexify a lot the firstResult/maxResult
			info.id = DocumentBuilder.getDocumentId( listener, info.clazz, document );
			entityInfos[current - first] = info;
		}
		return new Object[]{
				session.get( info.clazz, info.id )
		};
	}

	public Object get(int i) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Type getType(int i) {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Integer getInteger(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Long getLong(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Float getFloat(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Boolean getBoolean(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Double getDouble(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Short getShort(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Byte getByte(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Character getCharacter(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public byte[] getBinary(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public String getText(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Blob getBlob(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Clob getClob(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public String getString(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public BigDecimal getBigDecimal(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public BigInteger getBigInteger(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Date getDate(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Locale getLocale(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public Calendar getCalendar(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public TimeZone getTimeZone(int col) throws HibernateException {
		throw new UnsupportedOperationException( "Lucene does not work on columns" );
	}

	public int getRowNumber() throws HibernateException {
		if ( max < first ) return -1;
		return current - first;
	}

	public boolean setRowNumber(int rowNumber) throws HibernateException {
		if ( rowNumber >= 0 ) {
			current = first + rowNumber;
		}
		else {
			current = max + rowNumber + 1; //max row start at -1
		}
		return current >= first && current <= max;
	}
}
