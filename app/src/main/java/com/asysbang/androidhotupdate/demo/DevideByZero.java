package com.asysbang.androidhotupdate.demo;

import android.util.Log;

public class DevideByZero {

    public static void  test() {
        int i = 0;
        if (i == 0) {
            // 修复之前注释
            Log.e("====", "==after fixed ==");
            i = 10;
            // 添加这个代码之后重新生成一个文件的dex
            int j = 10 / i;
        }
    }
}
