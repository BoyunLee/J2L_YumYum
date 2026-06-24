package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

@Component
public class ZxingBarcodeDecoder implements BarcodeDecoder {
    private static final List<BarcodeFormat> PRODUCT_BARCODE_FORMATS = List.of(
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.CODE_128
    );

    @Override
    public String decode(byte[] imageBytes) {
        BufferedImage image = readImage(imageBytes);
        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image))
        );
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, PRODUCT_BARCODE_FORMATS);

        try {
            return new MultiFormatReader().decode(bitmap, hints).getText();
        } catch (NotFoundException exception) {
            throw new BusinessException(ExceptionType.BARCODE_NOT_DETECTED);
        }
    }

    private BufferedImage readImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new BusinessException(ExceptionType.BARCODE_NOT_DETECTED);
            }
            return image;
        } catch (IOException exception) {
            throw new BusinessException(ExceptionType.IMAGE_READ_FAILED);
        }
    }
}
