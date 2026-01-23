package com.example.shopifun.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shopifun.adapters.ShoppingAdapter;
import com.example.shopifun.databinding.FragmentShoppingListBinding;
import com.example.shopifun.models.ShoppingItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private static final String ARG_LIST_ID = "listId";
    private static final String ARG_LIST_TITLE = "listTitle";

    private String listId;
    private String listTitle;

    private FragmentShoppingListBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private List<ShoppingItem> itemsList = new ArrayList<>();
    private ShoppingAdapter adapter;

    public ShoppingListFragment() {
        // required empty constructor
    }

    public static ShoppingListFragment newInstance(String listId, String listTitle) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_ID, listId);
        args.putString(ARG_LIST_TITLE, listTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            listId = getArguments().getString(ARG_LIST_ID);
            listTitle = getArguments().getString(ARG_LIST_TITLE, "Shopping");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShoppingListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        userId = user.getUid();

        // Ставим название списка в textView
        binding.tvListName.setText(listTitle);

        // Настраиваем RecyclerView
        binding.rvShoppingList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShoppingAdapter(itemsList, db, userId, listId);
        binding.rvShoppingList.setAdapter(adapter);

        loadItems();

        // "+ Add Item"
        binding.btnAddItem.setOnClickListener(v -> showAddItemDialog());
    }

    private void loadItems() {
        db.collection("Users")
                .document(userId)
                .collection("Lists")
                .document(listId)
                .collection("Items")
                .orderBy("completed", Query.Direction.ASCENDING) // можно менять, если хотите
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("ShoppingListFragment", "listen error", e);
                        return;
                    }
                    if (snap != null) {
                        List<ShoppingItem> temp = new ArrayList<>();
                        for (DocumentSnapshot doc : snap) {
                            ShoppingItem si = doc.toObject(ShoppingItem.class);
                            if (si != null) {
                                si.setId(doc.getId());
                                temp.add(si);
                            }
                        }
                        itemsList = temp;
                        adapter.setItems(itemsList);
                        binding.tvItemsCount.setText(itemsList.size() + " ITEMS");
                    }
                });
    }

    private void showAddItemDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Add Item");

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Title");

        final EditText etNote = new EditText(getContext());
        etNote.setHint("Note");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etTitle);
        layout.addView(etNote);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String note  = etNote.getText().toString();
            addItemToFirestore(title, note);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addItemToFirestore(String title, String note) {
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        ShoppingItem newItem = new ShoppingItem(title, note, false);

        db.collection("Users")
                .document(userId)
                .collection("Lists")
                .document(listId)
                .collection("Items")
                .add(newItem)
                .addOnSuccessListener(documentReference -> {
                    // ok
                })
                .addOnFailureListener(e -> {
                    Log.e("ShoppingListFragment", "Add item error", e);
                });
    }
}
