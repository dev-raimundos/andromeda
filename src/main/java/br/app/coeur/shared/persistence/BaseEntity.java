package br.app.coeur.shared.persistence;

import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;

public abstract class BaseEntity<EntityId extends Serializable> {

    public abstract EntityId getId();

    @Override
    public final boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (object == null) {
            return false;
        }

        Class<?> objectEffectiveClass = object instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : object.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();

        if (thisEffectiveClass != objectEffectiveClass) {
            return false;
        }

        BaseEntity<?> other = (BaseEntity<?>) object;

        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
