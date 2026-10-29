package br.app.coeur.shared.model;

import br.app.coeur.modules.shopping.model.Item;
import br.app.coeur.modules.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {
    private Item item(UUID id) {
        Item item = Item.create("Rice", "White rice", 20.0, null, "Brand");
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    @Test
    void identityDependsOnClassAndIdRatherThanMutableFields() {
        UUID id = UUID.randomUUID();
        Item first = item(id);
        Item second = item(id);
        second.rename("Beans");
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(second).isEqualTo(first);
        assertThat(first).isNotEqualTo(item(UUID.randomUUID()));
        User user = BeanUtils.instantiateClass(User.class);
        ReflectionTestUtils.setField(user, "id", id);
        assertThat(first).isNotEqualTo(user);
    }

    @Test
    void transientEntitiesRemainDistinctAndHashCodeSurvivesPersistence() {
        Item first = item(null);
        assertThat(first).isEqualTo(first).isNotEqualTo(item(null)).isNotEqualTo(null);
        var items = new HashSet<Item>();
        items.add(first);
        ReflectionTestUtils.setField(first, "id", UUID.randomUUID());
        assertThat(items).contains(first);
    }
}
