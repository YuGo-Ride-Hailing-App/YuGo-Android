package com.example.uberapp.gui.fragments.account;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberapp.R;
import com.example.uberapp.core.dto.DocumentDTO;
import com.example.uberapp.core.dto.UserDetailedDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.DriverService;
import com.example.uberapp.core.services.ImageService;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDocumentsFragment extends Fragment {
    UserDetailedDTO user;
    DriverService driverService = APIClient.getClient().create(DriverService.class);
    ImageService imageService = APIClient.getClient().create(ImageService.class);
    Button buttonPickDrivingLicence;
    Button buttonPickRegistrationLicence;
    ImageButton buttonPreviewDrivingLicence;
    ImageButton buttonPreviewRegistrationLicence;
    TextView drivingLicenceText;
    TextView registrationLicenceText;
    DocumentDTO drivingLicence;
    DocumentDTO registrationLicence;
    public UserDocumentsFragment(UserDetailedDTO userDetailedDTO) {
        this.user = userDetailedDTO;
    }

    public UserDocumentsFragment(){}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = new CursorLoader(getContext(), uri, projection, null, null, null).loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_documents, container, false);

        buttonPickDrivingLicence = view.findViewById(R.id.buttonPickDrivingLicence);
        buttonPickRegistrationLicence = view.findViewById(R.id.buttonPickRegistrationLicence);
        buttonPreviewDrivingLicence = view.findViewById(R.id.buttonDrivingLicencePreview);
        buttonPreviewRegistrationLicence = view.findViewById(R.id.buttonRegistrationLicencePreview);
        drivingLicenceText = view.findViewById(R.id.drivingLicenceText);
        registrationLicenceText = view.findViewById(R.id.registrationLicenceText);

        this.loadDocuments();
        this.setupSelectorButtons();
        this.setupPreviewButtons();

        return view;
    }

    public void setupSelectorButtons(){
        ActivityResultLauncher<Intent> pickDrivingLicence =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        File file = new File(getPath(result.getData().getData()));
                        RequestBody body =  RequestBody.create(file, MediaType.parse("multipart/form-data"));
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), body);
                        Call<DocumentDTO> createDocumentCall = driverService.createDocument(user.getId(), "DRIVING_LICENCE", filePart);
                        createDocumentCall.enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<DocumentDTO> call, @NonNull Response<DocumentDTO> response) {
                                DocumentDTO oldDrivingLicence = drivingLicence;
                                drivingLicence = response.body();
                                drivingLicenceText.setText("Registration licence present. Click icon for preview");
                                Toast.makeText(getContext(), "Driving Licence uploaded successfully!", Toast.LENGTH_SHORT).show();

                                if (oldDrivingLicence != null) {
                                    Call<ResponseBody> deleteDocumentCall = driverService.deleteDocument(oldDrivingLicence.getId());

                                    deleteDocumentCall.enqueue(new Callback<>() {
                                        @Override
                                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                            Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<DocumentDTO> call, @NonNull Throwable t) {
                                Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(getContext(), "You didn't select any image!", Toast.LENGTH_SHORT).show();
                    }
                });

        ActivityResultLauncher<Intent> pickRegistrationLicence =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        File file = new File(getPath(result.getData().getData()));
                        RequestBody body =  RequestBody.create(file, MediaType.parse("multipart/form-data"));
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), body);
                        Call<DocumentDTO> createDocumentCall = driverService.createDocument(user.getId(), "VEHICLE_REGISTRATION", filePart);
                        createDocumentCall.enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<DocumentDTO> call, @NonNull Response<DocumentDTO> response) {
                                DocumentDTO oldRegistrationLicence = registrationLicence;
                                registrationLicence = response.body();
                                registrationLicenceText.setText("Registration licence present. Click icon for preview");
                                Toast.makeText(getContext(), "Driving Licence uploaded successfully!", Toast.LENGTH_SHORT).show();

                                if (oldRegistrationLicence != null) {
                                    Call<ResponseBody> deleteDocumentCall = driverService.deleteDocument(oldRegistrationLicence.getId());

                                    deleteDocumentCall.enqueue(new Callback<>() {
                                        @Override
                                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                            Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<DocumentDTO> call, @NonNull Throwable t) {
                                Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(getContext(), "You didn't select any image!", Toast.LENGTH_SHORT).show();
                    }
                });

        buttonPickDrivingLicence.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                Intent getPermission = new Intent();
                getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermission);
            }

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickDrivingLicence.launch(intent);
        });

        buttonPickRegistrationLicence.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                Intent getPermission = new Intent();
                getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermission);
            }

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickRegistrationLicence.launch(intent);
        });
    }

    public void setupPreviewButtons(){
        buttonPreviewDrivingLicence.setOnClickListener(v -> {
            if (drivingLicence != null){
                Call<ResponseBody> imageCall = imageService.getDocumentPicture(drivingLicence.getName());
                imageCall.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200){
                            try {
                                byte[] bytes = response.body().bytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ImageView image = new ImageView(getContext());
                                image.setImageBitmap(bitmap);

                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(getContext()).
                                                setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).
                                                setView(image);
                                builder.create().show();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        buttonPreviewRegistrationLicence.setOnClickListener(v -> {
            if (registrationLicence != null){
                Call<ResponseBody> imageCall = imageService.getDocumentPicture(registrationLicence.getName());
                imageCall.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200){
                            try {
                                byte[] bytes = response.body().bytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ImageView image = new ImageView(getContext());
                                image.setImageBitmap(bitmap);

                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(getContext()).
                                                setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).
                                                setView(image);
                                builder.create().show();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void loadDocuments(){
        Call<DocumentDTO[]> documentsCall = driverService.getDocuments(user.getId());
        documentsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DocumentDTO[]> call, @NonNull Response<DocumentDTO[]> response) {
                if (response.code() == 200){
                    DocumentDTO[] documentsDTOs = response.body();
                    for (DocumentDTO document : documentsDTOs){
                        if (document.getDocumentType().equals("DRIVING_LICENCE")){
                            drivingLicence = document;
                            drivingLicenceText.setText("Driving licence present. Click icon for preview");
                        }
                        else if (document.getDocumentType().equals("VEHICLE_REGISTRATION")){
                            registrationLicence = document;
                            registrationLicenceText.setText("Registration licence present. Click icon for preview");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DocumentDTO[]> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}