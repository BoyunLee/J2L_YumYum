package com.ssafy.yumyum.domain.refrigerator.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yumyum.domain.refrigerator.dao.RefrigeratorItemDao;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryItemResponse;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryItemUpdateRequest;
import com.ssafy.yumyum.domain.refrigerator.dto.ManualInventoryCreateRequest;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefrigeratorItemService {
    private final RefrigeratorItemDao refrigeratorItemDao;

    @Transactional
    public List<InventoryItemResponse> createManuals(Long userId, List<ManualInventoryCreateRequest> requests) {
        refrigeratorItemDao.createDefaultRefrigeratorIfAbsent(userId);
        Long refrigeratorId = refrigeratorItemDao.findRefrigeratorIdByUserId(userId);

        return requests.stream()
                .map(request -> createManualItem(refrigeratorId, request))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> findAll(Long userId) {
        return refrigeratorItemDao.findAllByUserId(userId).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    @Transactional
    public InventoryItemResponse update(Long userId, Long itemId, InventoryItemUpdateRequest request) {
        RefrigeratorItem item = new RefrigeratorItem();
        item.setId(itemId);
        item.setName(request.name().trim());
        item.setCategory(request.category());
        item.setQuantity(request.quantity());
        item.setUnit(request.unit().trim());
        item.setExpirationDate(request.expirationDate());
        item.setStorageLocation(request.storageLocation());
        item.setMemo(request.memo() == null ? null : request.memo().trim());

        if (refrigeratorItemDao.updateByIdAndUserId(item, userId) == 0) {
            throw new BusinessException(ExceptionType.REFRIGERATOR_ITEM_NOT_FOUND);
        }
        return InventoryItemResponse.from(refrigeratorItemDao.findByIdAndUserId(itemId, userId));
    }

    @Transactional
    public void delete(Long userId, Long itemId) {
        if (refrigeratorItemDao.findByIdAndUserId(itemId, userId) == null) {
            throw new BusinessException(ExceptionType.REFRIGERATOR_ITEM_NOT_FOUND);
        }
        refrigeratorItemDao.deleteLogsByIdAndUserId(itemId, userId);
        refrigeratorItemDao.deleteByIdAndUserId(itemId, userId);
    }

    private InventoryItemResponse createManualItem(Long refrigeratorId, ManualInventoryCreateRequest request) {
        RefrigeratorItem item = new RefrigeratorItem();
        item.setRefrigeratorId(refrigeratorId);
        item.setName(request.name().trim());
        item.setCategory(request.category());
        item.setQuantity(request.quantity());
        item.setUnit(request.unit().trim());
        item.setExpirationDate(request.expirationDate());
        item.setStorageLocation(request.storageLocation());
        item.setMemo(request.memo() == null ? null : request.memo().trim());
        refrigeratorItemDao.insertManual(item);
        return InventoryItemResponse.from(refrigeratorItemDao.findById(item.getId()));
    }
}
