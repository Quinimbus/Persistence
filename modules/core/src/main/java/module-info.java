import cloud.quinimbus.persistence.PersistenceContextImpl;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.schema.json.SingleJsonSchemaProvider;
import cloud.quinimbus.persistence.schema.record.RecordSchemaProvider;
import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;

module cloud.quinimbus.persistence.core {
    provides PersistenceContext with
            PersistenceContextImpl;
    provides PersistenceStorageProvider with
            InMemoryPersistenceStorageProvider;
    provides PersistenceSchemaProvider with
            SingleJsonSchemaProvider,
            RecordSchemaProvider;

    uses PersistenceContext;
    uses PersistenceSchemaProvider;
    uses PersistenceStorageProvider;

    requires java.logging;
    requires cloud.quinimbus.common.annotations;
    requires cloud.quinimbus.common.tools;
    requires cloud.quinimbus.config.api;
    requires cloud.quinimbus.persistence.api;
    requires cloud.quinimbus.persistence.common;
    requires cloud.quinimbus.tools;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.core;
    requires org.apache.commons.collections4;
    requires throwing.streams;
    requires throwing.interfaces;
    requires static lombok;
}
