package com.unionpay.ost.utils;

import java.io.File;

/**
 * 封装了文件常用的操作
 * Created by jsf on 16/8/3..
 */
public class FileUtils {
    /**
     * 创建目录,如果该目录下有同名文件,则进行删除
     * @param file
     */
    public static void deleteExistFile(File file) {
        if (file == null) {
            return ;
        }
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            //如果原来有相同目录下有同名文件,则删除后再进行重新生成
            File[] files = dir.listFiles();
            if (files != null && files.length != 0) {
                for (File f : files) {
                    if (f.getName().equals(file.getName())) {
                        f.delete();
                        break;
                    }
                }
            }
        }
    }

}
