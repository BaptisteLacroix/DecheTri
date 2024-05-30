package etu.seinksansdoozebank.dechetri.ui.wastemap;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import etu.seinksansdoozebank.dechetri.R;
import etu.seinksansdoozebank.dechetri.controller.api.APIController;
import etu.seinksansdoozebank.dechetri.databinding.FragmentWasteDialogBinding;
import etu.seinksansdoozebank.dechetri.model.user.Role;
import etu.seinksansdoozebank.dechetri.model.user.User;
import etu.seinksansdoozebank.dechetri.model.waste.Waste;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A fragment that shows the details of waste on click of map.</p>
 */
public class WasteDialogFragment extends BottomSheetDialogFragment {

    private FragmentWasteDialogBinding binding;

    private Waste waste;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWasteDialogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Button buttonItinary = view.findViewById(R.id.buttonItinerary);
        TextView wasteAddress = view.findViewById(R.id.wasteAddress);
        TextView wasteName = view.findViewById(R.id.wasteName);
        Button buttonDelete = view.findViewById(R.id.btnDelete);
        Button buttonConfirm = view.findViewById(R.id.btnConfirm);
        ImageView wasteImage = view.findViewById(R.id.wasteImage);
        TextView wasteType = view.findViewById(R.id.wasteType);
        LinearLayout wasteAssignLayout = view.findViewById(R.id.wasteAssignLayout);
        TextView textViewWasteAssignedTo = view.findViewById(R.id.wasteAssignedTo);
        Spinner spinnerAssign = view.findViewById(R.id.wasteAssignSpinner);

        if (getArguments() != null) {
            waste = getArguments().getParcelable("waste");
            if (waste != null) {
                wasteName.setText(waste.getName());
                wasteAddress.setText(waste.getAddress());
                wasteType.setText(waste.getType().getName());
                textViewWasteAssignedTo.setText(waste.getAssignee() != null ? waste.getAssignee().getName() : getString(R.string.nobody));
                byte[] imageBytes = waste.getImageData();
                if (imageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    wasteImage.setImageBitmap(bitmap);
                } else {
                    Log.e("WasteDialogFragment", "Given waste image is null");
                }
            } else {
                Log.e("WasteDialogFragment", "Given waste is null");
            }
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.shared_preferences_file_key), MODE_PRIVATE);
        String defaultRole = getResources().getString(R.string.role_user_title); //user by default
        String roleString = sharedPreferences.getString(getString(R.string.shared_preferences_key_role), defaultRole);
        Role role = Role.fromString(roleString);

        if (!role.equals(Role.USER)) {
            wasteAssignLayout.setVisibility(View.VISIBLE);
            if (role.equals(Role.MANAGER)) {
                textViewWasteAssignedTo.setVisibility(View.GONE);
                configSpinnerAssign(waste, spinnerAssign);
            }
        } else {
            wasteAssignLayout.setVisibility(View.GONE);
        }

        if (role.assignable()) {
            buttonItinary.setVisibility(View.VISIBLE);
            buttonConfirm.setVisibility(View.VISIBLE);
            buttonItinary.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("QueryPermissionsNeeded")
                @Override
                public void onClick(View v) {
                    String address = waste.getAddress();
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address));

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(mapIntent);
                }
            });

            buttonConfirm.setOnClickListener(v -> APIController.completeTask(waste.getId(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erreur lors de la complétion de la tâche : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Task completed", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Error completing task : " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }));
        } else {
            buttonItinary.setVisibility(View.GONE);
            buttonConfirm.setVisibility(View.GONE);
        }
        if (role.equals(Role.ADMIN)) {
            this.configureDeleteButton(buttonDelete);
        } else {
            buttonDelete.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    private void configSpinnerAssign(Waste waste, Spinner spinnerAssign) {
        List<Role> roles = new ArrayList<>();
        roles.add(Role.MANAGER);
        roles.add(Role.EMPLOYEE);
        APIController.getUserByRoles(roles, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error while getting users : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            String body = response.body().string();
                            List<User> users = APIController.parseUsers(body);
                            List<String> names = users.stream().map(User::getName).collect(Collectors.toList());
                            names.add(0, getString(R.string.nobody));
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerAssign.setAdapter(adapter);
                            spinnerAssign.setSelection(waste.getAssignee() != null ? names.indexOf(waste.getAssignee().getName()) : 0);
                            spinnerAssign.setVisibility(View.VISIBLE);
                            spinnerAssign.setOnItemSelectedListener(getSpinnerAssignListener(waste, users, spinnerAssign.getSelectedItemId()));
                        } catch (IOException e) {
                            Log.e("WasteDialogFragment", "Error while parsing users : " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(getContext(), "Error while getting users : " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private AdapterView.OnItemSelectedListener getSpinnerAssignListener(Waste waste, List<User> users, long currentSelectedId) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentSelectedId == i) {
                    return;
                }
                String selectedName = (String) adapterView.getItemAtPosition(i);
                Optional<User> optionalUser = users.stream().filter(user -> user.getName().equals(selectedName)).findFirst();
                String assigneeId = optionalUser.map(User::getId).orElse(null);
                APIController.assignTask(waste.getId(), assigneeId, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error while assigning task : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                waste.setAssignee(optionalUser.orElse(null));
                                Toast.makeText(getContext(), "La tâche a été assignée avec succès", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error while assigning task : " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
    }

    private void configureDeleteButton(Button buttonDelete) {
        buttonDelete.setVisibility(View.VISIBLE);
        buttonDelete.setOnClickListener(v -> onClickDeleteWaste());
    }

    public void onClickDeleteWaste() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_deletion_waste_popup)
                .setMessage(R.string.message_deletion_waste_popup)
                .setPositiveButton(R.string.delete_waste_popup_button, (dialog, which) -> APIController.deleteWaste(waste.getId(), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        String message = e.getMessage();
                        Log.e("APIController", "Error while removing waste : " + message);
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erreur lors de la suppression du déchet:" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Déchet supprimé avec succès.", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Erreur lors de la suppression du déchet :" + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }))
                .setNegativeButton(R.string.keep_waste_popup_button, (dialog, which) -> {
                    // Do nothing
                })
                .show();

        Button deletionButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        deletionButton.setTextColor(getResources().getColor(R.color.orange_600, null));
        Button conservationButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        conservationButton.setBackgroundColor(getResources().getColor(R.color.green_700, null));
        conservationButton.setTextColor(getResources().getColor(R.color.white_100, null));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
