package etu.seinksansdoozebank.dechetri.ui.wastereport;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.faltenreich.skeletonlayout.Skeleton;

import org.osmdroid.bonuspack.location.GeocoderNominatim;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import etu.seinksansdoozebank.dechetri.R;


public class WasteDetailsReportFragment extends Fragment {
    interface GeocodeCallback {
        void onAddressFound(String address);

        void onError(Exception e);
    }

    private final Executor executor = Executors.newSingleThreadExecutor();
    private EditText localisationInput;

    private Skeleton skeleton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waste_details_report, container, false);
        localisationInput = view.findViewById(R.id.LocalisationInput);
        EditText locationTextView = view.findViewById(R.id.locationText);
        Button validateButton = view.findViewById(R.id.ValidateButton);
        EditText nameInput = view.findViewById(R.id.NameInput);
        EditText descriptionInput = view.findViewById(R.id.DescriptionInput);
        ImageView wasteImage=view.findViewById(R.id.imageChosen);
        skeleton = view.findViewById(R.id.skeletonLayout);
        skeleton.showSkeleton();

        if (getArguments() == null) {
            Log.e("WasteDetailsReportFragment", "Arguments are null");
            return view;
        }
        double latitude = getArguments().getDouble("latitude");
        double longitude = getArguments().getDouble("longitude");
        byte[] chosenImage=getArguments().getByteArray("image");
        Bitmap imageChosenBitmap = BitmapFactory.decodeByteArray(chosenImage, 0, chosenImage.length);
        wasteImage.setImageBitmap(Bitmap.createScaledBitmap(imageChosenBitmap, 600, 600, false));
        locationTextView.setText("Lat : " + latitude + " Long : " + longitude);

        fetchAddress(latitude, longitude, new GeocodeCallback() {
            @Override
            public void onAddressFound(String address) {
                requireActivity().runOnUiThread(() -> {
                    Log.d("Address", "Address: " + address);
                    localisationInput.setText(address);
                    skeleton.showOriginal();
                    localisationInput.setEnabled(false);
                });
            }

            @Override
            public void onError(Exception e) {
                requireActivity().runOnUiThread(() -> Log.e("Geocoding", "Failed to fetch address", e));
            }
        });

        // Listener pour vérifier les changements de texte
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateButton.setEnabled(
                        !nameInput.getText().toString().trim().isEmpty() &&
                                !descriptionInput.getText().toString().trim().isEmpty() &&
                                !localisationInput.getText().toString().trim().isEmpty()
                );
            }

        };

        nameInput.addTextChangedListener(textWatcher);
        descriptionInput.addTextChangedListener(textWatcher);
        localisationInput.addTextChangedListener(textWatcher);
        return view;
    }

    private void fetchAddress(double latitude, double longitude, GeocodeCallback callback) {
        executor.execute(() -> {
            try {
                GeocoderNominatim geocoder = new GeocoderNominatim(Locale.getDefault(), "user-agent");
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    callback.onAddressFound(address.getAddressLine(0) + ", " + address.getLocality() + ", " + address.getCountryName());
                } else {
                    callback.onAddressFound("No address found");
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }
}
