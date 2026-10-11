package com.hospital.model;

/**
 * Generic interface representing an entity that possesses a unique identifier.
 * Demonstrates Abstraction and Java Generics in OOP.
 *
 * @param <T> Type of the entity identifier (e.g. Integer, Long, String)
 */
public interface Identifiable<T> {
    T getId();
    void setId(T id);
}
