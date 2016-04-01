package com.xinay.droid.fm.bus;

import com.squareup.otto.Bus;

/**
 * Created by luisvivero on 9/7/15.
 */
public class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {}
}
