package com.smartboxdeliverysystem.box.service;

import com.smartboxdeliverysystem.box.dto.requests.CreateBoxRequest;
import com.smartboxdeliverysystem.box.dto.responses.BatteryResponse;
import com.smartboxdeliverysystem.box.dto.responses.CreateBoxResponse;
import com.smartboxdeliverysystem.box.models.Box;
import com.smartboxdeliverysystem.box.models.BoxState;
import com.smartboxdeliverysystem.box.repository.BoxRepository;
import com.smartboxdeliverysystem.exceptions.*;
import com.smartboxdeliverysystem.item.dto.requests.ItemRequest;
import com.smartboxdeliverysystem.item.dto.responses.ItemResponse;
import com.smartboxdeliverysystem.item.model.Item;
import com.smartboxdeliverysystem.item.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoxService {

    private static final int MIN_BATTERY_FOR_LOADING = 25;

    private final BoxRepository boxRepository;
    private final ItemRepository itemRepository;

    public CreateBoxResponse createBox(CreateBoxRequest request){
        if(boxRepository.existsByTxref(request.getTxref())){
            throw new DuplicateTxrefException(request.getTxref());
        }

        Box box = new Box();
        box.setTxref(request.getTxref());
        box.setWeightLimit(request.getWeightLimit());
        box.setBatteryCapacity(request.getBatteryCapacity());
        box.setState(BoxState.IDLE);

        Box saved = boxRepository.save(box);
        log.info("Created box {}", saved.getTxref());
        return CreateBoxResponse.from(saved);
    }

    @Transactional
    public List<ItemResponse> loadBox(String txref, List<ItemRequest> itemRequests) {
        Box box = findBoxOrThrow(txref);

        if (box.getState() != BoxState.IDLE) {
            throw new InvalidBoxStateException(txref, box.getState().name());
        }
        if (box.getBatteryCapacity() < MIN_BATTERY_FOR_LOADING) {
            throw new InsufficientBatteryException(txref);
        }

        BigDecimal existingWeight = itemRepository.sumWeightByBoxTxref(txref);
        BigDecimal newWeight = itemRequests.stream()
                .map(ItemRequest::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (existingWeight.add(newWeight).compareTo(box.getWeightLimit()) > 0) {
            throw new WeightLimitExceededException(txref);
        }

        List<Item> items = itemRequests.stream()
                .map(req -> {
                    Item item = new Item();
                    item.setName(req.getName());
                    item.setWeight(req.getWeight());
                    item.setCode(req.getCode());
                    item.setBox(box);
                    return item;
                })
                .toList();

        itemRepository.saveAll(items);

        box.setState(BoxState.LOADED);
        boxRepository.save(box);

        log.info("Loaded {} item(s) into box {}", items.size(), txref);
        return items.stream().map(ItemResponse::from).toList();
    }

    public List<ItemResponse> getItems(String txref) {
        findBoxOrThrow(txref);
        return itemRepository.findByBoxTxref(txref).stream()
                .map(ItemResponse::from)
                .toList();
    }

    public List<CreateBoxResponse> getAvailableBoxes() {
        return boxRepository
                .findByStateAndBatteryCapacityGreaterThanEqual(BoxState.IDLE, MIN_BATTERY_FOR_LOADING)
                .stream()
                .map(CreateBoxResponse::from)
                .toList();
    }

    public BatteryResponse getBattery(String txref) {
        Box box = findBoxOrThrow(txref);
        return new BatteryResponse(box.getTxref(), box.getBatteryCapacity());
    }

    private Box findBoxOrThrow(String txref) {
        return boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException(txref));
    }
}
