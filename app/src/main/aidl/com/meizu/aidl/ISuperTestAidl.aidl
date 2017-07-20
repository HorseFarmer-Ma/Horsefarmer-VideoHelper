package com.meizu.aidl;

interface ISuperTestAidl {
   void exec(String command);                 // 执行adb命令
   String execWithResult(String command);     // 执行adb命令，并返回结果
   void fpsClear();                           // 清除FPS缓存区
   void fpsClearBuffer(String packageName, String activityName);    // 清除FPS页面缓冲
   boolean fpsDumpFrameLatency(String packageName, String activityName);   // Dump Fps页面内容
   int fpsGetSM();       // 获取掉帧率
   void runMonkey(String monkeyCommand);     // 执行Monkey
   void stopMonkey();         // 停止Monkey
}
