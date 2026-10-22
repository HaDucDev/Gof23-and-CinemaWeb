package com.td.cinema.payment.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.util.Arrays;

/** Connection and ownership checks; Hibernate validates the mapped fresh schema. */
@Repository
public class DatabaseRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private final String schema;

    public DatabaseRepository(@Value("${cinema.schema}") String schema) {
        this.schema = schema;
    }

    public void validateSchema() {
        if (!"payment".equals(schema)) throw new IllegalStateException("Unexpected service schema");
        Object currentUser = entityManager.createNativeQuery("select current_user").getSingleResult();
        if (!"cinema_payment".equals(currentUser)) throw new IllegalStateException("Wrong application DB role");
        for (String other : Arrays.asList("account", "booking", "seat", "payment")) {
            Object allowed = entityManager.createNativeQuery(
                    "select has_schema_privilege(current_user,:schemaName,'USAGE')")
                    .setParameter("schemaName", other).getSingleResult();
            if (Boolean.TRUE.equals(allowed) != other.equals(schema))
                throw new IllegalStateException("Schema permission mismatch: " + other);
        }
    }

    public void checkConnection() {
        entityManager.createNativeQuery("select 1").getSingleResult();
    }
}
