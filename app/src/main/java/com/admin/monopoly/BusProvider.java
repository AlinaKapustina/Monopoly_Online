package com.admin.monopoly;

import com.squareup.otto.Bus;

public class BusProvider {
    private static final Bus bus = new Bus();

    public static Bus getInstance(){
        return bus;
    }

    private BusProvider(){
    }
}
