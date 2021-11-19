import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.PersistenceContextImpl;

module cloud.quinimbus.persistence.core {
    
    provides PersistenceContext with PersistenceContextImpl;
    
    requires cloud.quinimbus.persistence.api;
    requires cloud.quinimbus.persistence.common;
    requires cloud.quinimbus.tools;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires throwing.streams;
    requires throwing.interfaces;
    requires lombok;
}

