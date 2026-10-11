package com.hospital.dao;

import java.util.List;
import com.hospital.exception.DatabaseException;

/**
 * Generic Data Access Object (DAO) interface.
 * 
 * OOP Principles Demonstrated:
 * 1. Abstraction: Defines standard CRUD contract independent of persistence mechanism
 * 2. Generics: Provides type-safe persistence operations for any entity type T with key ID
 *
 * @param <T>  Entity domain type
 * @param <ID> Primary key identifier type
 */
public interface GenericDAO<T, ID> {

    /**
     * Inserts a new entity into the database.
     *
     * @param entity Entity to persist
     * @return Persisted entity with generated primary key
     * @throws DatabaseException if SQL or constraint error occurs
     */
    T save(T entity) throws DatabaseException;

    /**
     * Updates an existing entity record in the database.
     *
     * @param entity Entity with updated values
     * @return true if updated, false if record not found
     * @throws DatabaseException if SQL error occurs
     */
    boolean update(T entity) throws DatabaseException;

    /**
     * Deletes an entity record by its unique identifier.
     *
     * @param id Identifier of the entity to delete
     * @return true if deleted, false if not found
     * @throws DatabaseException if foreign key or SQL error occurs
     */
    boolean delete(ID id) throws DatabaseException;

    /**
     * Retrieves an entity by its unique identifier.
     *
     * @param id Primary key ID
     * @return Entity or null if not found
     * @throws DatabaseException if SQL error occurs
     */
    T findById(ID id) throws DatabaseException;

    /**
     * Retrieves all records for this entity type.
     *
     * @return List of entities
     * @throws DatabaseException if SQL error occurs
     */
    List<T> findAll() throws DatabaseException;
}
