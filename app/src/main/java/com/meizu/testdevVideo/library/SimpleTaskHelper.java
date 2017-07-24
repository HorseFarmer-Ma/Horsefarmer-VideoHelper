package com.meizu.testdevVideo.library;

import android.os.AsyncTask;

/**
 * 执行线程
 * Created by maxueming on 2017/3/15.
 */
public abstract class SimpleTaskHelper extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        onPostExecute();
    }

    public void execute() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void executeInSerial() {
        executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    protected abstract void doInBackground();
    protected void onPostExecute() { };

}
