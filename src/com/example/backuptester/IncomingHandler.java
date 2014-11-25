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
        case BackupGlobals.REMOTE_READ_DONE:
            Log.d("TEST", "READ completed");
            BackupGlobals.sync = 0;
            synchronized (BackupGlobals.sync) {
            	BackupGlobals.sync.notify();
			}
            break;
        case BackupGlobals.REMOTE_READ_FAILED:
            Log.d("TEST", "READ failed");
            BackupGlobals.sync = 404;
            synchronized (BackupGlobals.sync) {
            	BackupGlobals.sync.notify();
			}
            break;
        case BackupGlobals.REMOTE_WRITE_DONE:
            Log.d("TEST", "WRITE completed");
            break;
        default:
            break;
        }
    }
}