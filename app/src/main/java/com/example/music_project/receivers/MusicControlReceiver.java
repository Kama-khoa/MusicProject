package com.example.music_project.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.music_project.services.MusicPlaybackService;

public class MusicControlReceiver extends BroadcastReceiver {

    private static final String TAG = "MusicControlReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Xử lý các ý định (intents) nhận được từ các hành động
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Intent.ACTION_MEDIA_BUTTON:
                    // Xử lý nút media button (như phát/ dừng nhạc)
                    Log.d(TAG, "Media button pressed");
                    // Ví dụ: Gửi một intent tới dịch vụ phát nhạc để xử lý nút media
                    Intent musicIntent = new Intent(context, MusicPlaybackService.class);
                    musicIntent.setAction("PLAY_PAUSE");
                    context.startService(musicIntent);
                    break;

                // Thêm các trường hợp khác nếu cần
                default:
                    break;
            }
        }
    }
}
