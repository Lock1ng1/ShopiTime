package com.example.shopifun.adapters;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopifun.R;
import com.example.shopifun.models.UserList;

import java.util.ArrayList;
import java.util.List;

public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.ListViewHolder> {

    public interface OnListActionListener {
        void onListClicked(UserList userList);
        void onRenameList(UserList userList);  // Edit для списка = Rename
        void onDeleteList(UserList userList);
    }

    private List<UserList> lists = new ArrayList<>();
    private OnListActionListener listener;

    public ListsAdapter(OnListActionListener listener) {
        this.listener = listener;
    }

    public void setLists(List<UserList> newLists) {
        this.lists = newLists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false);
        return new ListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        UserList userList = lists.get(position);
        holder.tvListTitle.setText(userList.getTitle());

        // При клике по элементу открываем список
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListClicked(userList);
            }
        });

        // Кнопка «…» – открываем общее меню (options_menu.xml)
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener((MenuItem menuItem) -> {
                int id = menuItem.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) {
                        listener.onRenameList(userList);
                    }
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteList(userList);
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;
        ImageButton btnMore;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListTitle = itemView.findViewById(R.id.tvListTitle);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
