package cloud.quinimbus.persistence.cdi;

import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.config.cdi.ConfigPath;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

@ApplicationScoped
public class PersistenceContextProducer {

    private final PersistenceContext persistenceContext;

    @Inject
    @ConfigPath(value = "persistence", optional = true)
    private ConfigNode configNode;

    public PersistenceContextProducer() {
        this.persistenceContext = ServiceLoader.load(PersistenceContext.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find any PersistenceContext implementation"));
    }

    @PostConstruct
    public void init() throws PersistenceException, InvalidSchemaException {
        if (this.configNode != null) {
            ThrowingStream.of(
                            this.configNode
                                    .asNode("schemas")
                                    .map(cn -> cn.stream())
                                    .orElse(Stream.empty()),
                            InvalidSchemaException.class)
                    .forEach(this::initConfiguredSchema);
            ThrowingStream.of(
                            this.configNode
                                    .asNode("storages")
                                    .map(cn -> cn.stream())
                                    .orElse(Stream.empty()),
                            PersistenceException.class)
                    .forEach(this::initConfiguredStorage);
        }
    }

    @Produces
    public PersistenceContext getContext() {
        return this.persistenceContext;
    }

    private void initConfiguredSchema(ConfigNode node) throws InvalidSchemaException {
        var type = node.asString("type")
                .orElseThrow(() -> new InvalidSchemaException(
                        "Cannot autoconfigure schema %s, type configuration is missing".formatted(node.name())));
        var provider = this.persistenceContext
                .getSchemaProvider(type)
                .orElseThrow(() -> new InvalidSchemaException(
                        "Cannot autoconfigure schema %s, no provider for type %s found".formatted(node.name(), type)));
        this.persistenceContext.importSchema(provider, node);
    }

    private void initConfiguredStorage(ConfigNode node) throws PersistenceException {
        var type = node.asString("type")
                .orElseThrow(() -> new PersistenceException(
                        "Cannot autoconfigure storage %s, type configuration is missing".formatted(node.name())));
        var schema = node.asString("schema")
                .orElseThrow(() -> new PersistenceException(
                        "Cannot autoconfigure storage %s, schema configuration is missing".formatted(node.name())));
        var provider = this.persistenceContext
                .getStorageProvider(type)
                .orElseThrow(() -> new PersistenceException(
                        "Cannot autoconfigure storage %s, no provider for type %s found".formatted(node.name(), type)));
        var storage = provider.createSchema(this.persistenceContext, node);
        this.persistenceContext.setSchemaStorage(schema, storage);
        this.persistenceContext.upgradeSchema(storage);
    }
}
