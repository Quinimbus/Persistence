package cloud.quinimbus.persistence.api.lifecycle;

public sealed interface LifecycleEvent<T> permits EntityPostLoadEvent, EntityPostSaveEvent, EntityPreSaveEvent {}
