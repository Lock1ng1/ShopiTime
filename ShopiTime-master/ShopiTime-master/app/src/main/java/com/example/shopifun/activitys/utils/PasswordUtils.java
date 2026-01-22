package com.example.shopifun.activitys.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    // Метод для хеширования пароля с использованием SHA-256
    public static String hashPassword(String password) {
        try {
            // Получение экземпляра MessageDigest для алгоритма SHA-256
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            // Преобразование пароля в байты и вычисление хеша
            byte[] hashBytes = messageDigest.digest(password.getBytes());

            // Преобразование байтов в шестнадцатеричную строку
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Возвращение хеша в виде строки
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при хешировании пароля", e);
        }
    }
}
