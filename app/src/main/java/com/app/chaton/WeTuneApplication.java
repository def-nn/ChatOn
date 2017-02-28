package com.app.chaton;

import android.support.multidex.MultiDexApplication;

import com.app.chaton.sockets.SocketHelper;
import com.app.chaton.Utils.ToastHelper;

public class WeTuneApplication extends MultiDexApplication {

    private SocketHelper socketHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        ToastHelper.createToast(getApplicationContext());
    }

    public void connectToSocket(Long _u, String _s) {
        socketHelper = new SocketHelper(getApplicationContext(), _u, _s);
        socketHelper.connect();
    }

    public SocketHelper getSocketHelper() { return this.socketHelper; }
}
