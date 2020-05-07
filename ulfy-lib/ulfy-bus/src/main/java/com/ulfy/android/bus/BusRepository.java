package com.ulfy.android.bus;

import android.content.Context;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

class BusRepository {
    private static BusRepository busRepository = new BusRepository();
    private Map<Context, UlfyBus> busMap;

    private BusRepository() {
        busMap = new WeakHashMap<>();
    }

    static BusRepository getInstance() {
        return busRepository;
    }

    UlfyBus findBusByContext(Context context) {
        UlfyBus bus = busMap.get(context);
        if (bus == null) {
            bus = new UlfyBus();
            busMap.put(context, bus);
        }
        return bus;
    }

    void releaseBusOnLifecycleCallback(Context context) {
        Iterator<Context> iterator = busMap.keySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == context) {
                iterator.remove();
            }
        }
    }
}
