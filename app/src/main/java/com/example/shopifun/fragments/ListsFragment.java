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

import com.example.shopifun.activitys.MainActivity;
import com.example.shopifun.adapters.ListsAdapter;
import com.example.shopifun.databinding.FragmentListsBinding;
import com.example.shopifun.models.UserList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListsFragment extends Fragment implements ListsAdapter.OnListActionListener {

    private FragmentListsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private ListsAdapter adapter;
    private List<UserList> listsData = new ArrayList<>();

    // Два режима сортировки: ASC или DESC
    private boolean ascending = true; // начнём с A→Z

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        userId = user.getUid();

        // RecyclerView
        binding.rvLists.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListsAdapter(this);
        binding.rvLists.setAdapter(adapter);

        // "Создать список"
        binding.btnCreateList.setOnClickListener(v -> {
            showCreateListDialog();
        });

        // "Сортировка по аски" – переключаем ascending <-> !ascending
        binding.btnSortLists.setOnClickListener(v -> {
            ascending = !ascending;
            loadLists(ascending);
        });

        // Загружаем списки первый раз
        loadLists(ascending);
    }

    /**
     * Загрузка списков из Firestore, сортируем по title (ASC или DESC).
     */
    private void loadLists(boolean asc) {
        CollectionReference ref = db.collection("Users")
                .document(userId)
                .collection("Lists");

        Query query = ref.orderBy("title",
                asc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING);

        query.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Log.e("ListsFragment", "listen error", e);
                return;
            }
            if (snap != null) {
                List<UserList> temp = new ArrayList<>();
                for (DocumentSnapshot doc : snap) {
                    UserList ul = doc.toObject(UserList.class);
                    if (ul != null) {
                        ul.setId(doc.getId());
                        temp.add(ul);
                    }
                }
                listsData = temp;
                adapter.setLists(listsData);
            }
        });
    }

    private void showCreateListDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Create new list");

        final EditText etName = new EditText(getContext());
        etName.setHint("List name");

        builder.setView(etName);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String title = etName.getText().toString().trim();
            if (!title.isEmpty()) {
                createNewList(title);
            } else {
                Toast.makeText(getContext(), "Empty name", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createNewList(String title) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);

        db.collection("Users")
                .document(userId)
                .collection("Lists")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ListsFragment", "List created: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("ListsFragment", "Error creating list", e);
                });
    }

    // ===== Реализация OnListActionListener =====

    @Override
    public void onListClicked(UserList userList) {
        // Открываем ShoppingListFragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).openShoppingList(userList.getId(), userList.getTitle());
        }
    }

    @Override
    public void onRenameList(UserList userList) {
        showRenameDialog(userList);
    }

    @Override
    public void onDeleteList(UserList userList) {
        db.collection("Users")
                .document(userId)
                .collection("Lists")
                .document(userList.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "List deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error deleting list", Toast.LENGTH_SHORT).show();
                });
    }

    private void showRenameDialog(UserList userList) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Rename list");

        final EditText etName = new EditText(getContext());
        etName.setText(userList.getTitle());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etName);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            if (!newName.isEmpty()) {
                renameListInFirestore(userList.getId(), newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void renameListInFirestore(String listId, String newName) {
        db.collection("Users")
                .document(userId)
                .collection("Lists")
                .document(listId)
                .update("title", newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "List renamed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error renaming list", Toast.LENGTH_SHORT).show();
                });
    }
}
