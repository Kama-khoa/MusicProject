package com.example.music_project.views.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Song;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SongDialogUpdateFragment extends DialogFragment {
    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;// Mã yêu cầu cho tệp âm thanh

    private EditText etTitle;
    private Spinner spArtist, spGenre;
    private TextView tvDuration;
    private Button btnSave, btnCancel, btnDelete, btnUpload;

    private SongDialogUpdateListener listener;
    private Song song;
    private String audioFilePath;
    private ImageView img_path;
    private String coverImagePath;

    private List<Artist> artists;
    private List<Genre> genres;

    private int songId; // Đổi sang kiểu int
    private String songName;
    private int artistId;
    private int genreId;
    private int duration;

    private long SongDuration;


    private String file_path;
    private String image_path;

    private ActivityResultLauncher<Intent> audioFileLauncher;
    private ActivityResultLauncher <Intent> pickImageLauncher;

    public interface SongDialogUpdateListener {
        void onSongSavedtoUpdate(Song song);
        void onSongDeleted(Song song);
        void onAudioFileRequested();
    }

    public static SongDialogUpdateFragment newInstance(Song song) {
        SongDialogUpdateFragment fragment = new SongDialogUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("song", (Serializable) song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            try {
                songId = Integer.parseInt(getArguments().getString("songId", "-1"));
                songName = getArguments().getString("songName", "");
                artistId = Integer.parseInt(getArguments().getString("artist", "-1"));
                genreId = Integer.parseInt(getArguments().getString("genre", "-1"));
                duration = Integer.parseInt(getArguments().getString("duration", "-1"));
                file_path = getArguments().getString("filepath", "");
                image_path = getArguments().getString("imagepath", "");

                // Kiểm tra các giá trị có hợp lệ không
                if (songId == -1 || artistId == -1 || genreId == -1 || duration == -1 ||
                        songName.isEmpty() || file_path.isEmpty() || image_path.isEmpty()) {

                    Toast.makeText(getContext(), "Thiếu thông tin bài hát hoặc thông tin không hợp lệ", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Thông tin không hợp lệ", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Không nhận được thông tin bài hát", Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_song, container, false);

        etTitle = view.findViewById(R.id.et_ud_title);
        spArtist = view.findViewById(R.id.sp_ud_artist);
        spGenre = view.findViewById(R.id.sp_ud_genre);
        tvDuration = view.findViewById(R.id.tv_ud_duration);
        btnSave = view.findViewById(R.id.btn_ud_save);
        btnCancel = view.findViewById(R.id.btn_ud_cancel);
        btnDelete = view.findViewById(R.id.btn_ud_delete);
      //  btnUpload = view.findViewById(R.id.btn_ud_upload);
        img_path = view.findViewById(R.id.iv_ud_cover);

        Log.e("SongDialogFragment", "Tên bài hát" + songName);

        Log.e("SongDialogUpdateFragment", "link ảnh đc truyền : " +image_path);
        Log.e("SongDialogUpdateFragment", "tgian : " +String.format("Thời lượng: %d:%02d", duration / 60, duration % 60));

        img_path.setOnClickListener(v -> pickImage());

        if (image_path != null && !image_path.isEmpty()) {
            Glide.with(getContext()).load(image_path).into(img_path);
        } else {
            img_path.setImageResource(R.drawable.ic_image_playlist); // Ảnh mặc định nếu không có
        }

        if (artists != null && genres != null) {
            spArtist.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, artists));
            spGenre.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genres));
        } else {
            Log.e("SongDialogFragment", "Danh sách nghệ sĩ, album hoặc thể loại không được khởi tạo!");
        }

        etTitle.setText(songName);
        tvDuration.setText(String.format("Thời lượng: %d:%02d", duration / 60, duration % 60));
        setSpinnerSelections();

        btnSave.setOnClickListener(v -> saveSongUpdate());
        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> deleteSong());
        btnUpload.setOnClickListener(v -> requestAudioFile());

        audioFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri audioUri = result.getData().getData();
                        if (audioUri != null) {
                            audioFilePath = audioUri.toString();
                            setAudioDuration(audioUri);
                        }
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            coverImagePath = imageUri.toString();
                            img_path.setImageURI(imageUri);
                        }
                    }
                }
        );
        return view;
    }

    private void setAudioDuration(Uri audioUri) {
        try (MediaMetadataRetriever mmr = new MediaMetadataRetriever();
             ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(audioUri, "r")) {

            if (pfd != null) {
                mmr.setDataSource(pfd.getFileDescriptor());
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (durationStr != null) {
                    SongDuration = Long.parseLong(durationStr);
                    long durationInMillis = Long.parseLong(durationStr);
                    long durationInSeconds = durationInMillis / 1000;
                    tvDuration.setText(String.format("Thời lượng: %d:%02d", durationInSeconds / 60, durationInSeconds % 60));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            tvDuration.setText("Không thể lấy độ dài bài hát");
        }
    }


    private void setSpinnerSelections() {
        if (artists != null) {
            for (int i = 0; i < artists.size(); i++) {
                if (artists.get(i).getArtist_id() == artistId) {
                    spArtist.setSelection(i);
                    break;
                }
            }
        }

        if (genres != null) {
            for (int i = 0; i < genres.size(); i++) {
                if (genres.get(i).getGenre_id() == genreId) {
                    spGenre.setSelection(i);
                    break;
                }
            }
        }
    }
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent); // Sử dụng pickImageLauncher để chọn hình ảnh
    }


    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }


    private void populateSpinners() {
        if (this.artists != null) {
            ArrayAdapter<Artist> artistAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, artists);
            spArtist.setAdapter(artistAdapter);
        }

        if(this.genres != null) {
            ArrayAdapter<Genre> genreAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, genres);
            spGenre.setAdapter(genreAdapter);
        }
    }

    private void deleteSong() {
        if (song != null && listener != null) {
            listener.onSongDeleted(song);
        }
        dismiss();
    }

    private void requestAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        audioFileLauncher.launch(intent); // Sử dụng audioFileLauncher để yêu cầu tệp âm thanh
    }

    public void setListener(SongDialogUpdateListener listener) {
        this.listener = listener;
    }

    public void setAudioFilePath(String filePath) {
        this.audioFilePath = filePath;
        // Update UI to show that an audio file has been selected
        if (btnUpload != null) {
            btnUpload.setText("Audio đã chọn");
        }
    }

    private void saveSongUpdate() {
        String title = etTitle.getText().toString().trim();
        Artist selectedArtist = (Artist) spArtist.getSelectedItem();

        Genre selectedGenre = (Genre) spGenre.getSelectedItem();

        

        // Kiểm tra thông tin nhập vào không được để trống
        if (title.isEmpty() || selectedArtist == null || selectedGenre == null) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (song == null) {
            song = new Song();
        }

        Log.d("SongDialogUpdate", "Thay đổi tên mới : "+title);


        song.setTitle(title);
        song.setArtist_id(selectedArtist.getArtist_id());
        song.setGenre_id(selectedGenre.getGenre_id());
        song.setDuration((int) SongDuration);
        song.setFile_path(audioFilePath != null ? audioFilePath : file_path); // Cập nhật file_path nếu có
        //song.setImage_path(coverImagePath != null ? coverImagePath : image_path); // Cập nhật image_path nếu có

        Log.d("SongDialogUpdate", "Title: " + title);
        Log.d("SongDialogUpdate", "Artist ID: " + (selectedArtist != null ? selectedArtist.getArtist_id() : "null"));
        Log.d("SongDialogUpdate", "Genre ID: " + (selectedGenre != null ? selectedGenre.getGenre_id() : "null"));
        Log.d("SongDialogUpdate", "Duration: " + SongDuration);
        Log.d("SongDialogUpdate", "Audio File Path: " + (audioFilePath != null ? audioFilePath : "null"));
        Log.d("SongDialogUpdate", "Image File Path: " + (coverImagePath != null ? coverImagePath : "null"));

        if (listener != null) {
            listener.onSongSavedtoUpdate(song); // Gọi listener để lưu thông tin
        } else {
            Log.e("SongDialogUpdate", "Listener is null!"); // Kiểm tra xem listener có null không
        }

        dismiss();
    }
//    public void editSong(Song song) {
//        if (song != null) {
//            Log.d("SongDialogFragment", "Bắt đầu cập nhật bài hát: " + song.getTitle());
//            Log.d("SongDialogFragment", "Số lượng nghệ sĩ: " + artists.size());
//            Log.d("SongDialogFragment", "Số lượng album: " + albums.size());
//            Log.d("SongDialogFragment", "Số lượng thể loại: " + genres.size());
//
//            // Nếu danh sách rỗng, không thực hiện cập nhật
//            if (artists.isEmpty() || albums.isEmpty() || genres.isEmpty()) {
//                Log.e("SongDialogFragment", "Không có dữ liệu nghệ sĩ, album hoặc thể loại");
//                return; // Hoặc hiển thị thông báo lỗi
//            }
//            // Hiển thị thông tin của bài hát lên layout
//            Log.e("SongDialogFragment", "Vào thành công với bài hát: " + song.getTitle());
//            if (etTitle != null) {
//                etTitle.setText(song.getTitle());
//                Log.d("SongDialogFragment", "Tiêu đề bài hát được gán thành công: " + song.getTitle());
//            } else {
//                Log.e("SongDialogFragment", "Không tìm thấy EditText etTitle");
//            }
//
//            // Cập nhật spinner cho nghệ sĩ
//            if (this.artists != null) {
//                boolean artistFound = false; // Để theo dõi xem nghệ sĩ có được tìm thấy không
//                for (int i = 0; i < artists.size(); i++) {
//                    Log.d("SongDialogFragment", "Kiểm tra nghệ sĩ: " + artists.get(i).getArtist_id());
//                    if (artists.get(i).getArtist_id() == song.getArtist_id()) {
//                        spArtist.setSelection(i);
//                        artistFound = true;
//                        Log.d("SongDialogFragment", "Đã chọn nghệ sĩ tại vị trí: " + i);
//                        break;
//                    }
//                }
//                if (!artistFound) {
//                    Log.e("SongDialogFragment", "Nghệ sĩ không được tìm thấy cho bài hát: " + song.getTitle());
//                }
//            } else {
//                Log.e("SongDialogFragment", "Danh sách nghệ sĩ rỗng hoặc null");
//            }
//
//            // Cập nhật spinner cho album
//            if (this.albums != null ) {
//                boolean albumFound = false; // Để theo dõi xem album có được tìm thấy không
//                for (int i = 0; i < albums.size(); i++) {
//                    Log.d("SongDialogFragment", "Kiểm tra album: " + albums.get(i).getAlbum_id());
//                    if (albums.get(i).getAlbum_id() == song.getAlbum_id()) {
//                        spAlbum.setSelection(i);
//                        albumFound = true;
//                        Log.d("SongDialogFragment", "Đã chọn album tại vị trí: " + i);
//                        break;
//                    }
//                }
//                if (!albumFound) {
//                    Log.e("SongDialogFragment", "Album không được tìm thấy cho bài hát: " + song.getTitle());
//                }
//            } else {
//                Log.e("SongDialogFragment", "Danh sách album rỗng hoặc null");
//            }
//
//            // Cập nhật spinner cho thể loại
//            if (this.genres != null ) {
//                boolean genreFound = false; // Để theo dõi xem thể loại có được tìm thấy không
//                for (int i = 0; i < genres.size(); i++) {
//                    Log.d("SongDialogFragment", "Kiểm tra thể loại: " + genres.get(i).getGenre_id());
//                    if (genres.get(i).getGenre_id() == song.getGenre_id()) {
//                        spGenre.setSelection(i);
//                        genreFound = true;
//                        Log.d("SongDialogFragment", "Đã chọn thể loại tại vị trí: " + i);
//                        break;
//                    }
//                }
//                if (!genreFound) {
//                    Log.e("SongDialogFragment", "Thể loại không được tìm thấy cho bài hát: " + song.getTitle());
//                }
//            } else {
//                Log.e("SongDialogFragment", "Danh sách thể loại rỗng hoặc null");
//            }
//
//            // Cập nhật đường dẫn tệp âm thanh và hình ảnh bìa
//            audioFilePath = song.getFile_path();
//            coverImagePath = song.getImage_path();
//
//            // Hiển thị thời lượng bài hát
//            tvDuration.setText("Thời lượng: " + String.format("%d:%02d", song.getDuration() / 60, song.getDuration() % 60));
//            Log.d("SongDialogFragment", "Thời lượng bài hát: " + song.getDuration());
//
//            // Cập nhật UI cho tệp âm thanh đã chọn
//            if (audioFilePath != null) {
//                btnUpload.setText("Audio đã chọn");
//                Log.d("SongDialogFragment", "Đường dẫn tệp âm thanh: " + audioFilePath);
//            } else {
//                Log.e("SongDialogFragment", "Đường dẫn tệp âm thanh null");
//            }
//
//            // Cập nhật hình ảnh bìa nếu có
//            if (coverImagePath != null) {
//                img_path.setImageURI(Uri.parse(coverImagePath));
//                Log.d("SongDialogFragment", "Đường dẫn hình ảnh bìa: " + coverImagePath);
//            } else {
//                Log.e("SongDialogFragment", "Đường dẫn hình ảnh bìa null");
//            }
//        } else {
//            Log.e("SongDialogFragment", "Đối tượng bài hát null");
//        }
//    }
//
//    private String getRealPathFromURI(Context context, Uri uri) {
//        String path = null;
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (cursor != null) {
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            path = cursor.getString(column_index);
//            cursor.close();
//        }
//        return path;
//    }




}
