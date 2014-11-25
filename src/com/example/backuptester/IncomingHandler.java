package com.example.backuptester;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

class IncomingHandler extends Handler {

    public IncomingHandler(HandlerThread thr) {
        super(thr.getLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MainActivity.REMOTE_READ_DONE:
            Log.d("TEST", "READ completed");
            MainActivity.sync = 0;
            synchronized (MainActivity.sync) {
            	MainActivity.sync.notify();
			}
            break;
        case MainActivity.REMOTE_READ_FAILED:
            Log.d("TEST", "READ failed");
            MainActivity.sync = 404;
            synchronized (MainActivity.sync) {
            	MainActivity.sync.notify();
			}
            break;
        case MainActivity.REMOTE_WRITE_DONE:
            Log.d("TEST", "WRITE completed");
            break;
        default:
            break;
        }
    }
}