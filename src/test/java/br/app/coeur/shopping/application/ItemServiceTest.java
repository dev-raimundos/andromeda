package br.app.coeur.shopping.application;

import br.app.coeur.shared.exception.ResourceNotFoundException;
import br.app.coeur.shopping.application.dto.request.CreateItemRequest;
import br.app.coeur.shopping.application.dto.request.UpdateItemRequest;
import br.app.coeur.shopping.application.dto.response.ItemResponse;
import br.app.coeur.shopping.domain.Item;
import br.app.coeur.shopping.domain.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID NON_EXISTENT_ID = UUID.randomUUID();

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item existingItem() {
        Item item = Item.create("Rice", "White rice, type 1", 20.0, null, "Ben's");
        ReflectionTestUtils.setField(item, "id", ITEM_ID);
        return item;
    }

    @Test
    void createShouldPersistAndReturnItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.create(
                new CreateItemRequest("Rice", "White rice, type 1", 20.0, "https://store.com/rice", "Ben's")
        );

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Rice");
        assertThat(response.name()).isEqualTo("Rice");
        assertThat(response.brand()).isEqualTo("Ben's");
    }

    @Test
    void findByIdShouldReturnItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(existingItem()));

        ItemResponse response = itemService.findById(ITEM_ID);

        assertThat(response.id()).isEqualTo(ITEM_ID);
        assertThat(response.name()).isEqualTo("Rice");
    }

    @Test
    void findByIdShouldFailWhenItemDoesNotExist() {
        when(itemRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(NON_EXISTENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found.");
    }

    @Test
    void findAllShouldMapAllItems() {
        Pageable pageable = PageRequest.of(0, 20);
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingItem())));

        Page<ItemResponse> responses = itemService.findAll(pageable);

        assertThat(responses.getContent()).extracting(ItemResponse::name).containsExactly("Rice");
    }

    @Test
    void updateShouldChangeAllFields() {
        Item item = existingItem();
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.update(
                ITEM_ID, new UpdateItemRequest("Beans", "Carioca beans", 15.0, "https://store.com/beans", "Acme")
        );

        assertThat(response.name()).isEqualTo("Beans");
        assertThat(response.description()).isEqualTo("Carioca beans");
        assertThat(response.price()).isEqualTo(15.0);
        assertThat(response.brand()).isEqualTo("Acme");
    }

    @Test
    void updateShouldFailWhenItemDoesNotExist() {
        when(itemRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(NON_EXISTENT_ID, new UpdateItemRequest("a", "b", 1.0, null, "c")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found.");
    }

    @Test
    void deleteShouldRemoveExistingItem() {
        Item item = existingItem();
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        itemService.delete(ITEM_ID);

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteShouldFailWhenItemDoesNotExist() {
        when(itemRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.delete(NON_EXISTENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found.");

        verify(itemRepository, never()).delete(any());
    }
}
