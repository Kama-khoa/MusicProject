//package com.example.music_project.views.fragments;
//
//import android.app.AlertDialog;
//import android.app.DatePickerDialog;
//import android.app.Dialog;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.fragment.app.DialogFragment;
//
//import com.example.music_project.R;
//import com.example.music_project.controllers.ArtistController;
//import com.example.music_project.models.Artist;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class ArtistAddDialogFragment extends DialogFragment {
//
//    private ArtistController artistController;
//
//    @Nullable
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_add_artist, null);
//        builder.setView(dialogView);
//
//        EditText edtArtistName = dialogView.findViewById(R.id.edt_artist_name);
//        EditText edtArtistDob = dialogView.findViewById(R.id.edt_artist_dob);
//        EditText edtArtistBio = dialogView.findViewById(R.id.edt_artist_bio);
//        ImageButton btnSelectDate = dialogView.findViewById(R.id.btn_select_date);
//
//        // DatePickerDialog implementation
//        btnSelectDate.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d",
//                                selectedDay, selectedMonth + 1, selectedYear);
//                        edtArtistDob.setText(selectedDate);
//                    }, year, month, day);
//
//            datePickerDialog.show();
//        });
//
//        builder.setTitle("Thêm Nghệ Sĩ Mới")
//                .setPositiveButton("Tạo", (dialog, id) -> {
//                    String artistName = edtArtistName.getText().toString().trim();
//                    String artistBio = edtArtistBio.getText().toString().trim();
//                    String artistDobString = edtArtistDob.getText().toString().trim();
//
//                    if (!artistName.isEmpty() && !artistBio.isEmpty() && !artistDobString.isEmpty()) {
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//                        try {
//                            Date artistDob = dateFormat.parse(artistDobString);
//                            Artist artist = new Artist(artistName, artistBio, artistDob);
//
//                            artistController.createArtist(artist, new ArtistController.OnArtistCreatedListener() {
//                                @Override
//                                public void onSuccess() {
//                                    Toast.makeText(getContext(), "Nghệ sĩ " + artistName + " được tạo", Toast.LENGTH_SHORT).show();
//                                    loadArtists(); // Reload artists
//                                }
//
//                                @Override
//                                public void onFailure(String error) {
//                                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            Toast.makeText(getContext(), "Định dạng ngày không hợp lệ", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(getContext(), "Tên nghệ sĩ, bio và ngày sinh không được để trống", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss())
//                .create();
//
//        return builder.create();
//    }
//}
//
