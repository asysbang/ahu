package com.asysbang.androidhotupdate.update;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class DexUpdateManager {

    private static final String DEX_FILE_NAME = "/sdcard/classes2.dex";

    public static boolean loadPatchDex(Context context, String dataPath) throws Exception {
        File dexFile = new File(DEX_FILE_NAME);
        File dataFile = new File(dataPath + "/classes2.dex");
        FileInputStream inputStream = new FileInputStream(dexFile);
        FileOutputStream outputStream = new FileOutputStream(dataFile);
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();

        Log.e("====", "==copy dex==");

        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dataFile.getAbsolutePath(), dataPath, null, pathClassLoader);
        Object dexObj = getPathList(dexClassLoader);
        Object pathObj = getPathList(pathClassLoader);


        Object fixDexElements = getField(dexObj, dexObj.getClass(), "dexElements");
        Object[] objs = (Object[]) fixDexElements;
        Log.e("====", "==objs=="+objs.length);
        Object pathDexElements = getField(pathObj, pathObj.getClass(), "dexElements");
        Object[] objss = (Object[]) pathDexElements;
        Log.e("====", "==objss=="+objss.length);

        Object newDexElements = combineArray(fixDexElements, pathDexElements);
        Object[] objsss = (Object[]) newDexElements;
        Log.e("====", "==objsss=="+objsss.length);

        //重新赋值给PathClassLoader 中的exElements数组
        Object pathList = getPathList(pathClassLoader);
        setField(pathList, pathList.getClass(), "dexElements", newDexElements);
        Log.e("====", "==pathClassLoader==");
        return false;
    }

    private static Object getPathList(Object baseDexClassLoader) throws Exception {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getField(Object obj, Class cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class cl, String field, Object value) throws Exception {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

}
