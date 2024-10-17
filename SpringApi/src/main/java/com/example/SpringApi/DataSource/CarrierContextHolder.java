package com.example.SpringApi.DataSource;

public class CarrierContextHolder {

    private static final ThreadLocal<Long> contextHolder = new ThreadLocal<>();

    public static void setCarrierId(long carrierId) {
        contextHolder.set(carrierId);
    }

    public static long getCarrierId() {
        if(contextHolder.get() == null){
            return 1L;
        }
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}
