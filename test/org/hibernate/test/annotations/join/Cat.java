//$Id: Cat.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.join;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

/**
 * @author Emmanuel Bernard
 */
@Entity
@SecondaryTables({
@SecondaryTable(name = "`Cat nbr1`"),
@SecondaryTable(name = "Cat2", uniqueConstraints = {@UniqueConstraint(columnNames = {"storyPart2"})})
		})
@Table(appliesTo = "Cat", indexes = @Index(name = "secondname", columnNames = "secondName"))
public class Cat implements Serializable {

	private Integer id;
	private String name;
	private String secondName;
	private String storyPart1;
	private String storyPart2;

	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	@Index(name = "nameindex")
	public String getName() {
		return name;
	}

	public void setId(Integer integer) {
		id = integer;
	}

	public void setName(String string) {
		name = string;
	}

	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

// Bug HHH-36
//	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
//	@JoinColumn(name="CAT_ID", secondaryTable="ExtendedLife")
//	public Set<Life> getLifes() {
//		return lifes;
//	}
//
//	public void setLifes(Set<Life> collection) {
//		lifes = collection;
//	}

	@Column(table = "`Cat nbr1`")
	@Index(name = "story1index")
	public String getStoryPart1() {
		return storyPart1;
	}

	@Column(table = "Cat2")
	public String getStoryPart2() {
		return storyPart2;
	}


	public void setStoryPart1(String string) {
		storyPart1 = string;
	}


	public void setStoryPart2(String string) {
		storyPart2 = string;
	}

}