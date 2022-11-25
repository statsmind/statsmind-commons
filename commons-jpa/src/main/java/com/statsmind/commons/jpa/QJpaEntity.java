package com.statsmind.commons.jpa;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;

public class QJpaEntity extends EntityPathBase<JpaEntity> {
    public QJpaEntity(String variable) {
        super(JpaEntity.class, variable);
    }

    public QJpaEntity(PathMetadata metadata) {
        super(JpaEntity.class, metadata);
    }

    public QJpaEntity(Path<? extends JpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }
}
