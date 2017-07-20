package com.meizu.testdevVideo.task.performs;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetFps {
    public static final String CLEAR_BUFFER_CMD = "dumpsys SurfaceFlinger --latency-clear";
    public static final String FRAME_LATENCY_CMD = "dumpsys SurfaceFlinger --latency";
    private static int BUFFER_SIZE = 128;
    private static List<List<String>> mFrameBufferData;
    private static long mRefreshPeriod;
    public static final String PENDING_FENCE_TIME;
    private static int mFrameLatencySampleSize;
    public static final String SWAP_CMD = "input swipe 50 830 50 30";
    private static final int PAUSE_LATENCY = 20;
    private static long[] mDeltaVsync;
    private static long[] mDelta2Vsync;
    private static long mMaxDeltaVsync;
    private static double[] mNormalizedDelta2Vsync;
    private static int[] mRoundNormalizedDelta2Vsync;

    static {
        mFrameBufferData = new ArrayList(BUFFER_SIZE);
        mRefreshPeriod = -1L;
        PENDING_FENCE_TIME = (new Long(9223372036854775807L)).toString();
        mFrameLatencySampleSize = 0;
        mDeltaVsync = new long[BUFFER_SIZE];
        mDelta2Vsync = new long[BUFFER_SIZE];
        mNormalizedDelta2Vsync = new double[BUFFER_SIZE];
        mRoundNormalizedDelta2Vsync = new int[BUFFER_SIZE];
    }

    public GetFps() {
    }

    public static void swap() throws IOException {
        Runtime.getRuntime().exec("input swipe 50 830 50 30");
    }

    public static void clearBuffer(String windowName) throws IOException {
        if(windowName != null) {
            Runtime.getRuntime().exec(CLEAR_BUFFER_CMD + " " + windowName);
        }

    }

    public static boolean dumpFrameLatency(String windowName, boolean ignorePendingFenceTime) throws IOException, InterruptedException {
        mFrameLatencySampleSize = 0;
        mFrameBufferData = new ArrayList(BUFFER_SIZE);
        Process p = null;
        BufferedReader resultReader = null;
        if(windowName != null) {
            p = Runtime.getRuntime().exec(FRAME_LATENCY_CMD + " " + windowName);
            p.waitFor();
            resultReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            try {
                String line = resultReader.readLine();
                mRefreshPeriod = Long.parseLong(line.trim());
                Log.e("GetFps", "reading refresh period: " + mRefreshPeriod);
                if(mRefreshPeriod < 0L) {
                    return false;
                }

                boolean e = false;

                while((line = resultReader.readLine()) != null && !line.trim().isEmpty()) {
                    String[] bufferValues = line.split("\\s+");
                    if(bufferValues[0].trim().compareTo("0") != 0) {
                        if(bufferValues[1].trim().compareTo(PENDING_FENCE_TIME) == 0) {
                            if(!ignorePendingFenceTime) {
                                System.out.println("the data contains unfinished frame time, please allow the animation to finish in the entrance before calling dumpFrameLatency.");
                                return false;
                            }

                            System.out.println("ignore pending fence time");
                            e = true;
                        }

                        List delayArray = Arrays.asList(bufferValues);
                        mFrameBufferData.add(delayArray);
                        if(!e) {
                            ++mFrameLatencySampleSize;
                        }
                    }
                }

//                System.out.println("frame latency sample size: " + mFrameLatencySampleSize);
                resultReader.close();
                p.destroy();
            } catch (IOException var9) {
                return false;
            }
        }

        return true;
    }

    public static double getFrameRate() {
        if(mRefreshPeriod < 0L) {
            return -1.0D;
        }else if (mFrameBufferData.isEmpty()){
            return -1.0D;   // 新增，当页面静止时，出现异常数据，则返回负数：-1.0D
        } else if(mFrameBufferData.get(0) == null) {
            return -1.0D;
        } else if(0 != mFrameLatencySampleSize){    // 防止出现界面停止后mFrameLatencySampleSize为0的情况
            long startTime = 0L;
            long endTime = 0L;
            startTime = Long.parseLong((String)((List)mFrameBufferData.get(0)).get(1));
            endTime = Long.parseLong((String)((List)mFrameBufferData.get(mFrameLatencySampleSize - 1)).get(1));
            long totalDuration = endTime - startTime;
            return (double)(mFrameLatencySampleSize - 1) * Math.pow(10.0D, 9.0D) / (double)totalDuration;
        }else{
			return -1.0D;      // 返回其他异常情况
		}
    }

    public static long[] getDeltaVsync() {
        if(mRefreshPeriod < 0L) {
            return null;
        } else {
            if(mDeltaVsync[0] < 0L) {
                mMaxDeltaVsync = 0L;
                long preVsyncTime = Long.parseLong((String)((List)mFrameBufferData.get(0)).get(1));

                for(int i = 0; i < mFrameLatencySampleSize - 1; ++i) {
                    long curVsyncTime = Long.parseLong((String)((List)mFrameBufferData.get(i + 1)).get(1));
                    mDeltaVsync[i] = curVsyncTime - preVsyncTime;
                    preVsyncTime = curVsyncTime;
                    if(mMaxDeltaVsync < mDeltaVsync[i]) {
                        mMaxDeltaVsync = mDeltaVsync[i];
                    }
                }
            }

            return mDeltaVsync;
        }
    }

    public static long[] getDelta2Vsync() {
        if(mRefreshPeriod < 0L) {
            return null;
        } else {
            if(mDeltaVsync[0] < 0L) {
                getDeltaVsync();
            }

            if(mDelta2Vsync[0] < 0L) {
                int numDeltaVsync = mFrameLatencySampleSize - 1;

                for(int i = 0; i < numDeltaVsync - 1; ++i) {
                    mDelta2Vsync[i] = mDeltaVsync[i + 1] - mDeltaVsync[i];
                }
            }

            return mDelta2Vsync;
        }
    }

    public static double[] getNormalizedDelta2Vsync() {
        if(mRefreshPeriod < 0L) {
            return null;
        } else {
            if(mDelta2Vsync[0] < 0L) {
                getDelta2Vsync();
            }

            if(mNormalizedDelta2Vsync[0] < 0.0D) {
                for(int i = 0; i < mFrameLatencySampleSize - 2; ++i) {
                    mNormalizedDelta2Vsync[i] = (double)mDelta2Vsync[i] / (double)mRefreshPeriod;
                }
            }

            return mNormalizedDelta2Vsync;
        }
    }

    public static int[] getRoundNormalizedDelta2Vsync() {
        if(mRefreshPeriod < 0L) {
            return null;
        } else {
            if(mNormalizedDelta2Vsync[0] < 0.0D) {
                getNormalizedDelta2Vsync();
            }

            for(int i = 0; i < mFrameLatencySampleSize - 2; ++i) {
                int value = (int) Math.round(Math.max(mNormalizedDelta2Vsync[i], 0.0D));
                mRoundNormalizedDelta2Vsync[i] = value;
            }

            return mRoundNormalizedDelta2Vsync;
        }
    }

    public static int getVsyncJankiness() {
        if(mRefreshPeriod < 0L) {
            return -1;
        } else {
            if(mRoundNormalizedDelta2Vsync[0] < 0) {
                getRoundNormalizedDelta2Vsync();
            }

            int numberJankiness = 0;

            for(int i = 0; i < mFrameLatencySampleSize - 2; ++i) {
                int value = mRoundNormalizedDelta2Vsync[i];
                if(value > 0 && value < 20) {
                    ++numberJankiness;
                }
            }

            return numberJankiness;
        }
    }

    public static int getSM() {
        if(mRefreshPeriod < 0L) {
            return -1;
        } else if (mFrameBufferData.isEmpty()){
            return -1;          // 新增，当页面静止时，出现异常数据，则返回-1
        } else if(mFrameBufferData.get(0) == null) {
            return -1;
        } else {
            if(mRoundNormalizedDelta2Vsync[0] < 0) {
                getRoundNormalizedDelta2Vsync();
            }

            long startTime = 0L;
            long endTime = 0L;
            startTime = Long.parseLong((String)((List)mFrameBufferData.get(0)).get(1));
            endTime = Long.parseLong((String)((List)mFrameBufferData.get(mFrameLatencySampleSize - 1)).get(1));
            long totalDuration = endTime - startTime;
            int SF_Sum = 0;

            int SM;
            for(SM = 0; SM < mFrameLatencySampleSize - 2; ++SM) {
                int value = mRoundNormalizedDelta2Vsync[SM];
                if(value > 0 && value < 20) {
                    SF_Sum += value;
                }
            }

            SM = (int)(60.0D - (double)SF_Sum / ((double)totalDuration / Math.pow(10.0D, 9.0D)));
            return SM;
        }
    }

    public static int getMaxDeltaVsync() {
        return Math.round((float)mMaxDeltaVsync / (float)mRefreshPeriod);
    }

    public static void clear() {
        if(mFrameBufferData != null) {
            mFrameBufferData.clear();
        }

        Arrays.fill(mDeltaVsync, -1L);
        Arrays.fill(mDelta2Vsync, -1L);
        Arrays.fill(mNormalizedDelta2Vsync, -1.0D);
        Arrays.fill(mRoundNormalizedDelta2Vsync, -1);
        mRefreshPeriod = -1L;
        mFrameLatencySampleSize = 0;
        mMaxDeltaVsync = 0L;
    }
}
