package br.app.coeur.modules.shopping.service;

import br.app.coeur.modules.shopping.dto.CreateItemRequest;
import br.app.coeur.modules.shopping.dto.ItemResponse;
import br.app.coeur.modules.shopping.dto.UpdateItemRequest;
import br.app.coeur.modules.shopping.model.Item;
import br.app.coeur.modules.shopping.repository.ItemRepository;
import br.app.coeur.shared.exception.ResourceNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item existingItem() {
        Item item = Item.create("Arroz", "Arroz branco tipo 1", 20.0, null, "Tio João");
        ReflectionTestUtils.setField(item, "id", 1L);
        return item;
    }

    @Test
    void createShouldPersistAndReturnItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.create(
                new CreateItemRequest("Arroz", "Arroz branco tipo 1", 20.0, "https://store.com/arroz", "Tio João")
        );

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Arroz");
        assertThat(response.name()).isEqualTo("Arroz");
        assertThat(response.brand()).isEqualTo("Tio João");
    }

    @Test
    void findByIdShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem()));

        ItemResponse response = itemService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Arroz");
    }

    @Test
    void findByIdShouldFailWhenItemDoesNotExist() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item não encontrado.");
    }

    @Test
    void findAllShouldMapAllItems() {
        Pageable pageable = PageRequest.of(0, 20);
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingItem())));

        Page<ItemResponse> responses = itemService.findAll(pageable);

        assertThat(responses.getContent()).extracting(ItemResponse::name).containsExactly("Arroz");
    }

    @Test
    void updateShouldChangeAllFields() {
        Item item = existingItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.update(
                1L, new UpdateItemRequest("Feijão", "Feijão carioca", 15.0, "https://store.com/feijao", "Camil")
        );

        assertThat(response.name()).isEqualTo("Feijão");
        assertThat(response.description()).isEqualTo("Feijão carioca");
        assertThat(response.price()).isEqualTo(15.0);
        assertThat(response.brand()).isEqualTo("Camil");
    }

    @Test
    void updateShouldFailWhenItemDoesNotExist() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(99L, new UpdateItemRequest("a", "b", 1.0, null, "c")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item não encontrado.");
    }

    @Test
    void deleteShouldRemoveExistingItem() {
        Item item = existingItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteShouldFailWhenItemDoesNotExist() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item não encontrado.");

        verify(itemRepository, never()).delete(any());
    }
}
