package com.example.music_project.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.ArtistController;
import com.example.music_project.models.Artist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogEditArtistFragment extends DialogFragment {

    private ImageView imgArtistAvatar;
    private EditText etArtistName, etArtistDob, etArtistBio;
    private Uri newAvatarUri;
    private String currentAvatarPath;
    private ArtistController artistController;
    private int artistId;
    private OnArtistEditedListener onArtistEditedListener;

    public static DialogEditArtistFragment newInstance(int artistId) {
        DialogEditArtistFragment fragment = new DialogEditArtistFragment();
        Bundle args = new Bundle();
        args.putInt("artist_id", artistId);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    newAvatarUri = result.getData().getData();
                    if (newAvatarUri != null) {
                        Glide.with(this)
                                .load(newAvatarUri)
                                .into(imgArtistAvatar);
                    }
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_artist, container, false);

        imgArtistAvatar = view.findViewById(R.id.image_artist_avatar);
        etArtistName = view.findViewById(R.id.et_artist_name);
        etArtistDob = view.findViewById(R.id.et_artist_dob);
        etArtistBio = view.findViewById(R.id.et_artist_bio);
        Button btnSave = view.findViewById(R.id.btn_artist_save);
        Button btnCancel = view.findViewById(R.id.btn_artist_cancel);
        Button btnDelete = view.findViewById(R.id.btn_artist_delete);
        ImageButton btnSelectDate = view.findViewById(R.id.btn_artist_date);

        final Calendar calendar = Calendar.getInstance();
        btnSelectDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Hiển thị DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        // Cập nhật ngày đã chọn vào EditText
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                        Date selectedDate = selectedCalendar.getTime();

                        // Chuyển đổi Date thành định dạng String để hiển thị
                        String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        etArtistDob.setText(formattedDate); // Cập nhật vào EditText
                    },
                    year, month, day);

            datePickerDialog.show(); // Hiển thị lịch chọn ngày
        });

        artistController = new ArtistController(getContext());

        if (getArguments() != null) {
            artistId = getArguments().getInt("artist_id");
        }

        loadArtistDetails(artistId);

        imgArtistAvatar.setOnClickListener(v -> selectImage());

        btnSave.setOnClickListener(v -> saveArtistDetails());
        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> deleteArtist());

        return view;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        selectImageLauncher.launch(intent);
    }

    private void loadArtistDetails(int artistId) {
        artistController.getArtistById(artistId, new ArtistController.OnArtistLoadedListener() {
            @Override
            public void onArtistLoaded(Artist artist) {
                if (artist != null) {
                    etArtistName.setText(artist.getArtist_name());
                    // Chuyển đổi ngày sinh sang định dạng dd-MM-yyyy
                    Date dob = artist.getDate_of_birth();
                    if (dob != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        etArtistDob.setText(dateFormat.format(dob));
                    } else {
                        etArtistDob.setText(""); // Hoặc một thông báo nếu không có ngày sinh
                    }
                    etArtistBio.setText(artist.getBio());
                    currentAvatarPath = artist.getAvatar();
                    if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            Glide.with(requireContext())
                                    .load(currentAvatarPath)
                                    .into(imgArtistAvatar);
                        });
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Không thể tải ca sĩ: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveArtistDetails() {
        String newName = etArtistName.getText().toString();
        String newDob = etArtistDob.getText().toString();
        String newBio = etArtistBio.getText().toString();
        String newAvatarPath = newAvatarUri != null ? newAvatarUri.toString() : currentAvatarPath;

        // Convert newDob string to Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date parsedDob = null;

        try {
            parsedDob = dateFormat.parse(newDob);
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Ngày sinh không hợp lệ!", Toast.LENGTH_SHORT).show();
            return; // Exit the method if parsing fails
        }

        artistController.updateArtist(artistId, newName, parsedDob, newBio, newAvatarPath, new ArtistController.OnArtistUpdatedListener() {
            @Override
            public void onSuccess() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Cập nhật thông tin ca sĩ thành công!", Toast.LENGTH_SHORT).show();
                    if (onArtistEditedListener != null) {
                        onArtistEditedListener.onArtistEdited();
                    }
                    dismiss();
                });
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Không thể cập nhật ca sĩ: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void deleteArtist() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Ca Sĩ")
                .setMessage("Bạn có chắc chắn muốn xóa ca sĩ này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    artistController.deleteArtist(artistId, new ArtistController.OnArtistDeletedListener() {
                        @Override
                        public void onSuccess() {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(getContext(), "Xóa ca sĩ thành công!", Toast.LENGTH_SHORT).show()
                            );
                            if (onArtistEditedListener != null) {
                                onArtistEditedListener.onArtistEdited();
                            }
                            dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(getContext(), "Không thể xóa ca sĩ: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    public void setOnArtistEditedListener(OnArtistEditedListener listener) {
        this.onArtistEditedListener = listener;
    }

    public interface OnArtistEditedListener {
        void onArtistEdited();
    }
}
