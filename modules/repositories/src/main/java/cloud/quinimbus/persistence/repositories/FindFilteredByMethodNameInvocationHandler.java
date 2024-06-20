package cloud.quinimbus.persistence.repositories;

import static cloud.quinimbus.tools.stream.QCollectors.oneOrNone;
import static java.util.stream.Collectors.*;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.common.filter.FilterFactory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FindFilteredByMethodNameInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityWriter entityWriter;

    private final String propertyName;

    private final ReturnType returnType;

    private static final Pattern FIND_METHOD_NAME_PATTERN = Pattern.compile("find(All|One|Filtered)?By(\\w+)(Equals)?");

    private static enum ReturnType {
        LIST,
        STREAM,
        OPTIONAL
    }

    public FindFilteredByMethodNameInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx)
            throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (this.getEntityType().owningEntity().isEmpty()) {
            if (m.getParameterCount() != 1) {
                throw new InvalidRepositoryDefinitionException(
                        "The %s method should have exactly one parameter".formatted(m.getName()));
            }
        } else {
            if (m.getParameterCount() != 2) {
                throw new InvalidRepositoryDefinitionException(
                        "The %s method should have exactly two parameters for weak entities".formatted(m.getName()));
            }
        }
        var matcher = FIND_METHOD_NAME_PATTERN.matcher(m.getName());
        if (!matcher.matches()) {
            throw new InvalidRepositoryDefinitionException("Cannot read method name %s".formatted(m.getName()));
        }
        this.propertyName = this.findProperty(matcher.group(2));
        var methodReturnType = m.getReturnType();
        if (List.class.equals(methodReturnType)) {
            this.returnType = ReturnType.LIST;
        } else if (Stream.class.equals(methodReturnType)) {
            this.returnType = ReturnType.STREAM;
        } else if (Optional.class.equals(methodReturnType)) {
            this.returnType = ReturnType.OPTIONAL;
        } else {
            throw new InvalidRepositoryDefinitionException(
                    "Unknown return type for a findFiltered method: %s".formatted(methodReturnType.getName()));
        }
        if (ReturnType.LIST.equals(this.returnType) && m.getName().startsWith("findOne")) {
            throw new InvalidRepositoryDefinitionException(
                    "The %s method is named to return just one value but returns a list");
        }
        if (ReturnType.OPTIONAL.equals(this.returnType) && m.getName().startsWith("findAll")) {
            throw new InvalidRepositoryDefinitionException(
                    "The %s method is named to return multiple values but returns an singleton value");
        }
        var genericReturnType = this.getEntityClass();
        if (genericReturnType.isRecord()) {
            try {
                this.entityWriter =
                        ctx.getRecordEntityWriter(this.getEntityType(), (Class<? extends Record>) genericReturnType);
            } catch (EntityWriterInitialisationException ex) {
                throw new InvalidRepositoryDefinitionException(
                        "Exception while creating writer for the repository", ex);
            }
        } else {
            throw new InvalidRepositoryDefinitionException(
                    "Generic return type for a findFiltered method has to be a record: %s"
                            .formatted(methodReturnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        Set<PropertyFilter> filters;
        if (getEntityType().owningEntity().isEmpty()) {
            filters = Set.of(FilterFactory.filterEquals(this.propertyName, args[0]));
        } else {
            var owner = this.getOwningTypeRecord().cast(args[0]);
            var ownerId = this.getOwningTypeIdGetter().apply(owner);
            filters = Set.of(
                    FilterFactory.filterEquals(this.propertyName, args[1]),
                    FilterFactory.filterEquals(
                            getEntityType().owningEntity().orElseThrow().field(), ownerId));
        }

        var resultStream = this.getSchemaStorage().findFiltered(this.getEntityType(), filters);
        return switch (this.returnType) {
            case LIST -> resultStream.map(this.entityWriter::write).collect(toList());
            case STREAM -> resultStream.map(this.entityWriter::write).collect(toList()).stream();
            case OPTIONAL -> resultStream.map(this.entityWriter::write).collect(oneOrNone());
        };
    }

    private String findProperty(String identifier) throws InvalidRepositoryDefinitionException {
        var matchingProperties = this.getEntityType().properties().stream()
                .filter(p -> p.name().equalsIgnoreCase(identifier))
                .collect(toList());
        if (matchingProperties.size() > 1) {
            throw new InvalidRepositoryDefinitionException(
                    "Found multiple matching properties for identifier %s".formatted(identifier));
        } else if (matchingProperties.isEmpty()) {
            throw new InvalidRepositoryDefinitionException(
                    "Found no matching property for identifier %s".formatted(identifier));
        } else {
            return matchingProperties.get(0).name();
        }
    }
}
