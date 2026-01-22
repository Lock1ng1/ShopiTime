package com.example.shopifun.fragments;  // или com.example.shopifun.fragments

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.shopifun.activitys.LoginActivity;
import com.example.shopifun.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Диалог с настройками (показывает email пользователя и кнопку "Выйти").
 */
public class SettingsDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_settings_dialog, null);

        // UI элементы
        TextView tvUserEmail  = view.findViewById(R.id.tv_user_email);
        Button btnSignOut     = view.findViewById(R.id.btn_sign_out);

        // Получаем текущего пользователя
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tvUserEmail.setText(currentUser.getEmail());
        }

        // Кнопка "Выйти из аккаунта"
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Возврат к LoginActivity
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        builder.setView(view)
                .setTitle("Настройки")
                .setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
