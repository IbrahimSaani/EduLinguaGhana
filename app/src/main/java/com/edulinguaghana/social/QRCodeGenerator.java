package com.edulinguaghana.social;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Generates QR codes for easy friend adding
 */
public class QRCodeGenerator {

    /**
     * Generate a QR code bitmap for the user ID
     * @param userId The user ID to encode
     * @param size The size of the QR code in pixels
     * @return Bitmap of the QR code, or null if generation fails
     */
    public static Bitmap generateQRCode(String userId, int size) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        try {
            // Encode user ID with prefix for app-specific handling
            String data = "EDULINGUA_USER:" + userId;

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract user ID from scanned QR code data
     * @param qrData The data from the QR code
     * @return User ID if valid, null otherwise
     */
    public static String extractUserId(String qrData) {
        if (qrData == null) return null;

        String prefix = "EDULINGUA_USER:";
        if (qrData.startsWith(prefix)) {
            return qrData.substring(prefix.length());
        }
        return null;
    }
}

