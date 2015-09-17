//$Id: $
package org.hibernate.validator.test.haintegration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Length;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Building {
    @Id
    @GeneratedValue private Long id;

    @Length(min=1, message = "{notpresent.Key} and #{key.notPresent} and {key.notPresent2} {min}")
    private String address;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
