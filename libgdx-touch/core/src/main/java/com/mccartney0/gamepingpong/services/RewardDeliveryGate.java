package com.mccartney0.gamepingpong.services;

public final class RewardDeliveryGate {

    private boolean delivered;

    public synchronized boolean tryDeliver() {
        if (delivered) {
            return false;
        }
        delivered = true;
        return true;
    }

    public synchronized boolean isDelivered() {
        return delivered;
    }
}
