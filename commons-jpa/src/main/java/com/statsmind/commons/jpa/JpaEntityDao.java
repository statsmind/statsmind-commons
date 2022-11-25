package com.statsmind.commons.jpa;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Getter
public class JpaEntityDao<T, ID extends Serializable> implements JpaRepository<T, ID> {
    private static final EntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";

    private SimpleJpaRepository<T, ID> repository;
    private EntityManager em;
    private EntityPath<T> path;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public final void initializeContext(EntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.em = entityManager;

        Type[] params = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        Class<T> domainClass = (Class<T>) params[0];

        this.repository = new SimpleJpaRepository<T, ID>(domainClass, entityManager);
        this.path = this.resolver.createPath(domainClass);
    }

    @Override
    public List<T> findAll() {
        return this.repository.findAll();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return this.repository.findAll(sort);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        return this.repository.findAllById(ids);
    }

    @Override
    public long count() {
        return this.repository.count();
    }

    @Override
    public void deleteById(ID id) {
        this.repository.deleteById(id);
    }

    @Override
    public void delete(T entity) {
        this.repository.delete(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        this.repository.deleteAllById(ids);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        this.repository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        this.repository.deleteAll();
    }

    @Override
    public <S extends T> S save(S entity) {
        return this.repository.save(entity);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return this.repository.saveAll(entities);
    }

    @Override
    public Optional<T> findById(ID id) {
        return this.repository.findById(id);
    }

    @Override
    public boolean existsById(ID id) {
        return this.repository.existsById(id);
    }

    @Override
    public void flush() {
        this.repository.flush();
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        return this.repository.saveAndFlush(entity);
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        return this.repository.saveAllAndFlush(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<T> entities) {
        this.repository.deleteAllInBatch(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {
        this.repository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllInBatch() {
        this.repository.deleteAllInBatch();
    }

    @Override
    public T getOne(ID id) {
        return this.repository.findById(id).orElse(null);
    }

    @Override
    public T getById(ID id) {
        return this.repository.findById(id).orElse(null);
    }

    @Override
    public T getReferenceById(ID id) {
        return this.repository.getReferenceById(id);
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return this.repository.findOne(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return this.repository.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return this.repository.findAll(example, sort);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return this.repository.findAll(example, pageable);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return this.repository.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return this.repository.exists(example);
    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return this.repository.findBy(example, queryFunction);
    }

    public T getOneById(ID id) {
        return id == null? null : this.repository.findById(id).orElse(null);
    }

    public JPAQuery<T> find() {
        return new JPAQuery<T>(this.em).from(this.path);
    }

    public JPAUpdateClause update() {
        return new JPAUpdateClause(this.em, this.path);
    }

    public JPADeleteClause delete() {
        return new JPADeleteClause(this.em, this.path);
    }

    public JPAInsertClause insert() {
        return new JPAInsertClause(this.em, this.path);
    }

    /**
     * 找到所有满足条件的对象
     *
     * @param predicate
     * @return
     */
    public List<T> getAll(Predicate predicate) {
        return this.find().where(predicate).fetch();
    }

    /**
     * 找到所有满足条件的对象
     *
     * @param query
     * @return
     */
    public List<T> getAll(JPAQuery<T> query) {
        return query.fetch();
    }

    public Page<T> find(JPAQuery<T> query, Pageable pageable) {
        long total = query.clone().select(Wildcard.count).fetchOne();

        if (pageable.isPaged()) {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        return new PageImpl<>(query.fetch(), pageable, total);
    }

    public Page<T> find(Predicate predicate, Pageable pageable) {
        return find(find().where(predicate), pageable);
    }

    public List<Map<String, Object>> find(String sql, Object... params) {
        return this.jdbcTemplate.queryForList(sql, params);
    }

    public Number insert(String sql, Object... params) {
        KeyHolder holder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; ++i) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, holder);

        return Objects.requireNonNull(holder.getKey());
    }

    public int update(String sql, Object... params) {
        return this.jdbcTemplate.update(sql, params);
    }
}
