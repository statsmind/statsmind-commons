package com.statsmind.commons.jpa;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

/**
 * Usage:
 * public class Attachment implements JpaEntity {
 *     protected void beforeSave(boolean isNewRecord) {
 *         if (isNewRecord) {
 *             this.xxx = xxx;
 *         }
 *     }
 * }
 */
@MappedSuperclass
@EntityListeners({JpaEntityListener.class})
public class JpaEntity {

    /**
     * callback when entity is found
     */
    protected void afterLoad() {

    }

    /**
     * callback when entity is being saved to database
     */
    protected void beforeSave(boolean isNewRecord) {

    }

    /**
     * callback when entity has been saved to database
     */
    protected void afterSave(boolean isNewRecord) {

    }

    /**
     * callback when entity is being deleted from database
     */
    protected void beforeDelete() {

    }

    /**
     * callback when entity has been deleted from database
     */
    protected void afterDelete() {

    }
}
