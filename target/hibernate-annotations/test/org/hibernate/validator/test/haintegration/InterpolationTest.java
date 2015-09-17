//$Id: $
package org.hibernate.validator.test.haintegration;

import java.util.MissingResourceException;

import org.hibernate.test.annotations.TestCase;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.cfg.Configuration;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.Environment;
import org.hibernate.validator.event.ValidatePreInsertEventListener;
import org.hibernate.validator.event.ValidatePreUpdateEventListener;
import org.hibernate.validator.test.PrefixMessageInterpolator;

/**
 * @author Emmanuel Bernard
 */
public class InterpolationTest extends TestCase {
    public void testMissingKey() {
        Session s = openSession();
        Transaction tx = s.beginTransaction();
        Building b = new Building();
        b.setAddress( "2323 Younge St");
        try {
            s.persist( b );
            s.flush();
        }
        catch (MissingResourceException e) {
            fail("message should be interpolated lazily in DefaultMessageInterpolator");
        }
        tx.rollback();
        s.close();

        s = openSession();
        tx = s.beginTransaction();
        b = new Building();
        b.setAddress("");
        boolean failure = false;
        try {
            s.persist( b );
            s.flush();
            fail("Insert should fail");
        }
        catch (InvalidStateException e) {
            assertEquals( "Missing key should be left unchanged",
                    "{notpresent.Key} and #{key.notPresent} and {key.notPresent2} 1",
                    e.getInvalidValues()[0].getMessage() );
            failure = true;
        }
        assertTrue( "No invalid state found", failure );
        tx.rollback();
        s.close();
    }

    protected Class[] getMappings() {
        return new Class[] {
            Building.class
        };
    }

    protected void configure(Configuration cfg) {
		cfg.getEventListeners()
				.setPreInsertEventListeners( new PreInsertEventListener[]{new ValidatePreInsertEventListener()} );
		cfg.getEventListeners()
				.setPreUpdateEventListeners( new PreUpdateEventListener[]{new ValidatePreUpdateEventListener()} );
	}
}
