package com.example.SpringApi.DataSource;

public class CarrierContextHolder {
    private static final ThreadLocal<Long> contextHolder = new ThreadLocal<>();

    public static void setCarrierId(Long carrierId) {
        contextHolder.set(carrierId);
    }

    public static Long getCarrierId() {
        return contextHolder.get() != null ? contextHolder.get() : 1L;
    }

    public static void clear() {
        contextHolder.remove();
    }
}