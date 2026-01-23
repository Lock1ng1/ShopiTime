package com.example.shopifun.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopifun.R;
import com.example.shopifun.models.ShoppingItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    private List<ShoppingItem> items = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private String listId; // ID текущего списка

    public ShoppingAdapter(List<ShoppingItem> items, FirebaseFirestore db, String userId, String listId) {
        this.items = items;
        this.db = db;
        this.userId = userId;
        this.listId = listId;
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        // Получаем элемент по позиции (учтите, что positions могут меняться из-за сортировки)
        ShoppingItem item = items.get(position);

        // Заполняем текстовые поля
        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemNote.setText(item.getNote());

        // Отключаем слушатель, чтобы избежать автоматических вызовов при установке состояния
        holder.cbItemDone.setOnCheckedChangeListener(null);
        // Устанавливаем состояние чекбокса согласно данным модели
        holder.cbItemDone.setChecked(item.isCompleted());

        // Повторно назначаем слушатель для обработки изменений состояния
        holder.cbItemDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Обновляем Firestore для данного элемента (если у него есть ID)
            if (item.getId() != null) {
                db.collection("Users")
                        .document(userId)
                        .collection("Lists")
                        .document(listId)
                        .collection("Items")
                        .document(item.getId())
                        .update("completed", isChecked);
            }
            // Обновляем локальную модель
            item.setCompleted(isChecked);
        });

        // Обработка нажатия на кнопку «…» (показываем меню для редактирования/удаления)
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int menuId = menuItem.getItemId();
                if (menuId == R.id.action_edit) {
                    showEditDialog(item, position, v.getContext());
                    return true;
                } else if (menuId == R.id.action_delete) {
                    // Удаляем из Firestore, если ID существует
                    if (item.getId() != null) {
                        db.collection("Users")
                                .document(userId)
                                .collection("Lists")
                                .document(listId)
                                .collection("Items")
                                .document(item.getId())
                                .delete();
                    }
                    // Удаляем локально и уведомляем адаптер
                    items.remove(position);
                    notifyItemRemoved(position);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ShoppingItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    private void showEditDialog(final ShoppingItem item, final int position, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Item");

        // Создаем EditText для названия и заметки, предзаполняем их
        final EditText etTitle = new EditText(context);
        etTitle.setText(item.getTitle());
        etTitle.setHint("Title");

        final EditText etNote = new EditText(context);
        etNote.setText(item.getNote());
        etNote.setHint("Note");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etTitle);
        layout.addView(etNote);

        builder.setView(layout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newTitle = etTitle.getText().toString().trim();
                String newNote = etNote.getText().toString().trim();
                if (newTitle.isEmpty()) {
                    Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (item.getId() != null) {
                    db.collection("Users")
                            .document(userId)
                            .collection("Lists")
                            .document(listId)
                            .collection("Items")
                            .document(item.getId())
                            .update("title", newTitle, "note", newNote);
                }
                // Обновляем локальный объект и уведомляем адаптер
                item.setTitle(newTitle);
                item.setNote(newNote);
                notifyItemChanged(position);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbItemDone;
        TextView tvItemTitle, tvItemNote;
        ImageButton btnMore;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            cbItemDone = itemView.findViewById(R.id.cbItemDone);
            tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            tvItemNote = itemView.findViewById(R.id.tvItemNote);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
