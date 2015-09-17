//$Id: Employer.java 10066 2006-06-28 10:02:24Z epbernard $
package org.hibernate.test.annotations.manytomany;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

/**
 * Employer in a employer-Employee relationship
 *
 * @author Emmanuel Bernard
 */
@Entity()
@Table(name="`Employer`")
public class Employer implements Serializable {
	private Integer id;
	private Collection employees;

	@ManyToMany(
			targetEntity = org.hibernate.test.annotations.manytomany.Employee.class,
			cascade = {CascadeType.PERSIST, CascadeType.MERGE}
	)
	@JoinTable(
			name = "EMPLOYER_EMPLOYEE",
			joinColumns = {@JoinColumn(name = "EMPER_ID")},
			inverseJoinColumns = {@JoinColumn(name = "EMPEE_ID")}
	)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@OrderBy("name")
	public Collection getEmployees() {
		return employees;
	}

	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	public void setEmployees(Collection set) {
		employees = set;
	}

	public void setId(Integer integer) {
		id = integer;
	}
}
