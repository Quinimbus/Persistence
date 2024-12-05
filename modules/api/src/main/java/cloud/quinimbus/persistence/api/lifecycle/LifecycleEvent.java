package cloud.quinimbus.persistence.api.lifecycle;

public sealed interface LifecycleEvent<K> permits EntityPostLoadEvent, EntityPostSaveEvent, EntityPreSaveEvent {}
