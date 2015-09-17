//$Id: $
package org.hibernate.search.test.query;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Text;
import org.hibernate.search.annotations.Keyword;
import org.hibernate.search.annotations.Unstored;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Indexed(index = "Book" )
public class Book {

	private Integer id;
	private String body;
	private String summary;

	public Book() {
	}

	public Book(Integer id, String summary, String body) {
		this.id = id;
		this.summary = summary;
		this.body = body;
	}

	@Unstored
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Id @Keyword(id=true)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Text
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}
