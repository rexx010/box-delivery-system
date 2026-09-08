package com.smartboxdeliverysystem.box.controller;

import com.smartboxdeliverysystem.box.dto.requests.CreateBoxRequest;
import com.smartboxdeliverysystem.box.dto.responses.BatteryResponse;
import com.smartboxdeliverysystem.box.dto.responses.CreateBoxResponse;
import com.smartboxdeliverysystem.box.service.BoxService;
import com.smartboxdeliverysystem.item.dto.requests.ItemRequest;
import com.smartboxdeliverysystem.item.dto.responses.ItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService boxService;

    @PostMapping
    public ResponseEntity<CreateBoxResponse> createBox(@Valid @RequestBody CreateBoxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boxService.createBox(request));
    }

    @PostMapping("/{txref}/load")
    public ResponseEntity<List<ItemResponse>> loadBox(
            @PathVariable String txref,
            @Valid @RequestBody List<@Valid ItemRequest> items) {
        return ResponseEntity.ok(boxService.loadBox(txref, items));
    }

    @GetMapping("/{txref}/items")
    public ResponseEntity<List<ItemResponse>> getItems(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getItems(txref));
    }

    @GetMapping("/available")
    public ResponseEntity<List<CreateBoxResponse>> getAvailableBoxes() {
        return ResponseEntity.ok(boxService.getAvailableBoxes());
    }

    @GetMapping("/{txref}/battery")
    public ResponseEntity<BatteryResponse> getBattery(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getBattery(txref));
    }
}
