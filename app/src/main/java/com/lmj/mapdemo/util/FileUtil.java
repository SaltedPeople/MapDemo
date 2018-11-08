package com.lmj.mapdemo.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {
    //基本路径
    private static String BASE_PATH = Environment.getExternalStorageDirectory() + "/CustomMap/";
    public static String CUSTOM_MAP_PATH = BASE_PATH + "custom_map.data";

    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context Context 使用CopyFiles类的Activity
     */
    public static void copyMapFromAssets(Context context) {
        try {
            InputStream is = context.getAssets().open("map/custom_map.data");
            File file = new File(BASE_PATH);
            file.mkdirs();
            FileOutputStream fos = new FileOutputStream(new File(CUSTOM_MAP_PATH));
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            is.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

