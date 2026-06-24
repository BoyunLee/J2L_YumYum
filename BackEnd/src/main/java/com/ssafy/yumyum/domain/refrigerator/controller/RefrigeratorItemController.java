package com.ssafy.yumyum.domain.refrigerator.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisBatchResponse;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryItemResponse;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryItemUpdateRequest;
import com.ssafy.yumyum.domain.refrigerator.dto.ManualInventoryCreateBatchRequest;
import com.ssafy.yumyum.domain.refrigerator.service.RefrigeratorItemService;
import com.ssafy.yumyum.domain.refrigerator.service.InventoryImageAnalysisService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/refrigerator/items")
@RequiredArgsConstructor
public class RefrigeratorItemController {
    private final RefrigeratorItemService refrigeratorItemService;
    private final InventoryImageAnalysisService inventoryImageAnalysisService;

    @PostMapping(value = "/analyze/{analysisType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseBody<InventoryImageAnalysisBatchResponse> analyzeImage(
            @PathVariable String analysisType,
            @RequestPart("image") MultipartFile image) {
        InventoryImageAnalysisBatchResponse response = inventoryImageAnalysisService.analyze(
                ImageAnalysisType.from(analysisType),
                image
        );
        return ResponseUtil.createSuccessResponse(response);
    }

    @PostMapping("/manual")
    public ResponseEntity<ResponseBody<List<InventoryItemResponse>>> createManual(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ManualInventoryCreateBatchRequest request) {
        List<InventoryItemResponse> response = refrigeratorItemService.createManuals(user.getId(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.createSuccessResponse(response));
    }

    @GetMapping
    public ResponseBody<List<InventoryItemResponse>> findAll(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseUtil.createSuccessResponse(refrigeratorItemService.findAll(user.getId()));
    }

    @PutMapping("/{itemId}")
    public ResponseBody<InventoryItemResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long itemId,
            @Valid @RequestBody InventoryItemUpdateRequest request) {
        return ResponseUtil.createSuccessResponse(refrigeratorItemService.update(user.getId(), itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseBody<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long itemId) {
        refrigeratorItemService.delete(user.getId(), itemId);
        return ResponseUtil.createSuccessResponse();
    }
}
