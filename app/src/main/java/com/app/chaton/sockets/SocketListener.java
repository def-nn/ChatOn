package com.app.chaton.sockets;

import com.app.chaton.API_helpers.Message;

public interface SocketListener {

    Long getCompanionId();
    void onMessageReceived(Message message);
}
