package etu.seinksansdoozebank.dechetri.ui.wastereport;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import etu.seinksansdoozebank.dechetri.R;
import etu.seinksansdoozebank.dechetri.databinding.FragmentWasteReportBinding;

public class WasteReportFragment extends Fragment {
    private FragmentWasteReportBinding binding;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWasteReportBinding.inflate(inflater, container, false);

        //Si on appuie sur le bouton annuler alors on reviens en arrière.
        binding.cancelButton.setOnClickListener(view1 -> requireActivity().onBackPressed());



         pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        // Le code ici sera exécuté lorsque l'utilisateur aura sélectionné une image
                        Intent data = result.getData();
                        // Récupérer l'URI de l'image sélectionnée
                        if (data != null) {
                            //TODO : afficher la photo selectionnée dans la view d'après.
                        }
                    } else {
                        Toast.makeText(requireContext(), "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Intent data = result.getData();
                        //TODO : afficher la photo prise dans la view d'après.
                    } else {
                        Toast.makeText(requireContext(), "Aucune photo capturée", Toast.LENGTH_SHORT).show();
                    }
                });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Si on clique sur le bouton de la pellicule alors on ouvre le service du téléphone.
        binding.LibraryPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //création d'une intent pour ouvrir la pellicule
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (pickPhotoIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    //On lance l'instance créée pour ouvrir la pellicule
                    pickImageLauncher.launch(pickPhotoIntent);
                } else {
                    //Si erreur
                    Toast.makeText(requireContext(), "L'application de galerie n'est pas disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Si on clique sur le bouton de l'appareil photo  alors on ouvre le service du téléphone.
        binding.CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    //Créer l'activité pour lancer l'appareil photo
                    takePictureLauncher.launch(takePictureIntent);
                } else {
               // si erreur alors afficher un message d'information
                    Toast.makeText(requireContext(), "L'application de l'appareil photo n'est pas disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}