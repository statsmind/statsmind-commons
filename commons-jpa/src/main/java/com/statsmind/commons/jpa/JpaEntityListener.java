package com.statsmind.commons.jpa;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.persistence.*;

/**
 * JpaEntity 事件侦听
 * <p>
 * 参考 https://www.baeldung.com/jpa-entity-lifecycle-events
 */
@Component
public class JpaEntityListener {
    @Nullable
    private ObjectFactory<AuditingHandler> auditingHandler;

    public JpaEntityListener() {

    }

    public JpaEntityListener(ObjectFactory<AuditingHandler> auditingHandler) {
        Assert.notNull(auditingHandler, "AuditingHandler must not be null!");
        this.auditingHandler = auditingHandler;
    }

    /**
     * after an entity has been loaded
     * 实体加载后存储原始的信息，便于后面进行数据留痕处理
     *
     * @param target
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @PostLoad
    public void touchForPostLoad(JpaEntity target) {
        target.afterLoad();
    }

    /**
     * before persist is called for a new entity
     * 实体创建前自动填充字段，isNewRecord = true
     *
     * @param target
     */
    @PrePersist
    public void touchForPrePersist(JpaEntity target) {
        Assert.notNull(target, "Entity must not be null!");

        if (auditingHandler != null) {
            AuditingHandler object = auditingHandler.getObject();
            if (object != null) {
                object.markCreated(target);
            }
        }

        target.beforeSave(true);
    }

    /**
     * before the update operation
     * 实体更新，isNewRecord = false
     *
     * @param target
     */
    @PreUpdate
    public void touchForPreUpdate(JpaEntity target) {
        Assert.notNull(target, "Entity must not be null!");

        if (auditingHandler != null) {

            AuditingHandler object = auditingHandler.getObject();
            if (object != null) {
                object.markModified(target);
            }
        }

        target.beforeSave(false);
    }

    /**
     * after persist is called for a new entity
     * isNewRecord = true
     *
     * @param target
     */
    @PostPersist
    public void touchForPostPersist(JpaEntity target) {
        target.afterSave(true);
    }

    /**
     * after an entity is updated
     * isNewRecord = false
     *
     * @param target
     */
    @PostUpdate
    public void touchForPostUpdate(JpaEntity target) {
        target.afterSave(false);
    }

    /**
     * before an entity is removed
     * 实体删除前回调
     *
     * @param target
     */
    @PreRemove
    public void touchForPreRemove(JpaEntity target) {
        target.beforeDelete();
    }

    /**
     * after an entity has been deleted
     * 实体删除后回调
     *
     * @param target
     */
    @PostRemove
    public void touchForPostRemove(JpaEntity target) {
        target.afterDelete();
    }


}
