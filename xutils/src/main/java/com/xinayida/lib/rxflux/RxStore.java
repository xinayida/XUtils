package com.xinayida.lib.rxflux;

/**
 * RxFlux注册类
 * RxStore store = new RxStore(this);
 * store.register(ActionType.XXX, ActionType.XXX);
 *
 * Created by ww on 2017/10/16.
 */

public class RxStore extends Store {
    public RxStore(StoreObserver observer) {
        setObserver(observer);
    }

    @Override
    protected boolean onAction(Action action) {
        return true;
    }

    @Override
    protected boolean onError(Action action, Throwable throwable) {
        return true;
    }
}
