package com.example.music_project.utils;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static void copyFile(Context context, Uri sourceUri, File destFile) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            if (in == null) {
                throw new IOException("Failed to open input stream");
            }
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        }
    }

    public static String getFileExtension(Context context, Uri uri) {
        String extension;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream != null && inputStream.available() > 0) {
                extension = getFileExtensionFromStream(inputStream);
            } else {
                extension = getFileExtensionFromUri(uri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting file extension", e);
            extension = getFileExtensionFromUri(uri);
        }
        return extension;
    }

    private static String getFileExtensionFromStream(InputStream inputStream) throws IOException {
        byte[] magic = new byte[4];
        if (inputStream.read(magic) != 4) {
            return "";
        }
        if (magic[0] == 'R' && magic[1] == 'I' && magic[2] == 'F' && magic[3] == 'F') {
            return ".wav";
        }
        if (magic[0] == 'I' && magic[1] == 'D' && magic[2] == '3') {
            return ".mp3";
        }
        // Add more file type checks here if needed
        return "";
    }

    private static String getFileExtensionFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int dot = path.lastIndexOf(".");
            if (dot > 0) {
                return path.substring(dot);
            }
        }
        return "";
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = -1;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size", e);
        }
        return size;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
