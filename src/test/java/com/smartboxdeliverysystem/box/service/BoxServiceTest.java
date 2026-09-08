package com.smartboxdeliverysystem.box.service;

//import com.example.boxdelivery.box.dto.BoxCreateRequest;
//import com.example.boxdelivery.box.dto.BoxResponse;
//import com.example.boxdelivery.exception.DuplicateTxrefException;
//import com.example.boxdelivery.exception.InsufficientBatteryException;
//import com.example.boxdelivery.exception.InvalidBoxStateException;
//import com.example.boxdelivery.exception.WeightLimitExceededException;
//import com.example.boxdelivery.item.ItemRepository;
//import com.example.boxdelivery.item.dto.ItemRequest;
import com.smartboxdeliverysystem.box.dto.requests.CreateBoxRequest;
import com.smartboxdeliverysystem.box.dto.responses.CreateBoxResponse;
import com.smartboxdeliverysystem.box.models.Box;
import com.smartboxdeliverysystem.box.models.BoxState;
import com.smartboxdeliverysystem.box.repository.BoxRepository;
import com.smartboxdeliverysystem.exceptions.DuplicateTxrefException;
import com.smartboxdeliverysystem.exceptions.InsufficientBatteryException;
import com.smartboxdeliverysystem.exceptions.InvalidBoxStateException;
import com.smartboxdeliverysystem.exceptions.WeightLimitExceededException;
import com.smartboxdeliverysystem.item.dto.requests.ItemRequest;
import com.smartboxdeliverysystem.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BoxService boxService;

    private Box box;

    @BeforeEach
    void setUp() {
        box = new Box();
        box.setId(1L);
        box.setTxref("BOXTEST0000000001");
        box.setWeightLimit(BigDecimal.valueOf(500));
        box.setBatteryCapacity(80);
        box.setState(BoxState.IDLE);
    }

    @Test
    void createBox_savesWithIdleState_whenTxrefIsUnique() {
        CreateBoxRequest request = new CreateBoxRequest();
        request.setTxref("BOXNEW00000000001");
        request.setWeightLimit(BigDecimal.valueOf(500));
        request.setBatteryCapacity(90);

        when(boxRepository.existsByTxref(request.getTxref())).thenReturn(false);
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateBoxResponse response = boxService.createBox(request);

        assertThat(response.getState()).isEqualTo("IDLE");
        assertThat(response.getTxref()).isEqualTo(request.getTxref());
    }

    @Test
    void createBox_throws_whenTxrefAlreadyExists() {
        CreateBoxRequest request = new CreateBoxRequest();
        request.setTxref(box.getTxref());
        request.setWeightLimit(BigDecimal.valueOf(500));
        request.setBatteryCapacity(90);

        when(boxRepository.existsByTxref(box.getTxref())).thenReturn(true);

        assertThatThrownBy(() -> boxService.createBox(request))
                .isInstanceOf(DuplicateTxrefException.class);
    }

    @Test
    void loadBox_rejectsLoading_whenBatteryBelow25Percent() {
        box.setBatteryCapacity(10);
        when(boxRepository.findByTxref(box.getTxref())).thenReturn(Optional.of(box));

        assertThatThrownBy(() -> boxService.loadBox(box.getTxref(), List.of(validItem())))
                .isInstanceOf(InsufficientBatteryException.class);

        verify(itemRepository, never()).saveAll(any());
    }

    @Test
    void loadBox_rejectsLoading_whenBoxNotIdle() {
        box.setState(BoxState.DELIVERING);
        when(boxRepository.findByTxref(box.getTxref())).thenReturn(Optional.of(box));

        assertThatThrownBy(() -> boxService.loadBox(box.getTxref(), List.of(validItem())))
                .isInstanceOf(InvalidBoxStateException.class);
    }

    @Test
    void loadBox_rejectsLoading_whenTotalWeightExceedsLimit() {
        when(boxRepository.findByTxref(box.getTxref())).thenReturn(Optional.of(box));
        when(itemRepository.sumWeightByBoxTxref(box.getTxref())).thenReturn(BigDecimal.ZERO);

        ItemRequest tooHeavy = new ItemRequest();
        tooHeavy.setName("heavy-item");
        tooHeavy.setCode("HEAVY_1");
        tooHeavy.setWeight(BigDecimal.valueOf(600));

        assertThatThrownBy(() -> boxService.loadBox(box.getTxref(), List.of(tooHeavy)))
                .isInstanceOf(WeightLimitExceededException.class);

        verify(itemRepository, never()).saveAll(any());
    }

    @Test
    void loadBox_succeeds_andTransitionsStateToLoaded() {
        when(boxRepository.findByTxref(box.getTxref())).thenReturn(Optional.of(box));
        when(itemRepository.sumWeightByBoxTxref(box.getTxref())).thenReturn(BigDecimal.ZERO);
        when(itemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        boxService.loadBox(box.getTxref(), List.of(validItem()));

        assertThat(box.getState()).isEqualTo(BoxState.LOADED);
    }

    private ItemRequest validItem() {
        ItemRequest item = new ItemRequest();
        item.setName("widget-1");
        item.setCode("WIDGET_1");
        item.setWeight(BigDecimal.valueOf(50));
        return item;
    }
}