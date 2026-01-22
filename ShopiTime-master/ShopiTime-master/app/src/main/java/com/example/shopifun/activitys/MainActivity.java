package com.example.shopifun.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.shopifun.R;
import com.example.shopifun.databinding.ActivityMainBinding;
import com.example.shopifun.fragments.ListsFragment;
import com.example.shopifun.fragments.SettingsDialogFragment;
import com.example.shopifun.fragments.ShoppingListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Проверка авторизации
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2) Загружаем макет
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3) Toolbar без заголовка
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Кнопка «сэндвич» → открываем ListsFragment заново
        binding.btnSwitchLists.setOnClickListener(v -> openListsFragment());

        // Кнопка «Настройки» → показываем диалог настроек (выход и т.п.)
        binding.btnSettings.setOnClickListener(v -> {
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.show(getSupportFragmentManager(), "SettingsDialog");
        });

        // При старте → фрагмент со списками
        openListsFragment();
    }

    public void openListsFragment() {
        ListsFragment fragment = new ListsFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    public void openShoppingList(String listId, String listTitle) {
        ShoppingListFragment fragment = ShoppingListFragment.newInstance(listId, listTitle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}
