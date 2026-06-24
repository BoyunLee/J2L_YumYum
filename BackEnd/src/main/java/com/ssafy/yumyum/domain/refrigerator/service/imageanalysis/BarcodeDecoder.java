package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

public interface BarcodeDecoder {
    String decode(byte[] imageBytes);
}
