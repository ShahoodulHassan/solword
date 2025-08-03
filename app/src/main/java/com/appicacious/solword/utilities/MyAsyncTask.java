package com.appicacious.solword.utilities;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class MyAsyncTask<Params, Progress, Result> {
    private static final String TAG = MyAsyncTask.class.getSimpleName();

    private static final Executor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(5, 128, 1,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsInterrupted = false;

    protected void onPreExecute(){}
    protected abstract Result doInBackground(Params params);
    protected void onPostExecute(Result result){}
    protected void onProgressUpdate(Progress progress){}
    protected void onCancelled(){}

    public final void execute(Params params) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                checkInterrupted();
                mHandler.post(this::onPreExecute);

                checkInterrupted();
                Result result = doInBackground(params);

                checkInterrupted();
                mHandler.post(() -> onPostExecute(result));
            } catch (InterruptedException ex) {
                mHandler.post(this::onCancelled);
            } catch (Exception ex) {
                Log.e(TAG, "execute: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void checkInterrupted() throws InterruptedException {
        if (isInterrupted()){
            throw new InterruptedException();
        }
    }

    public void cancel(boolean mayInterruptIfRunning){
        setInterrupted(mayInterruptIfRunning);
    }

    public boolean isInterrupted() {
        return mIsInterrupted;
    }

    public void setInterrupted(boolean interrupted) {
        mIsInterrupted = interrupted;
    }

    public void publishProgress(Progress progress) {
        onProgressUpdate(progress);
    }
}
