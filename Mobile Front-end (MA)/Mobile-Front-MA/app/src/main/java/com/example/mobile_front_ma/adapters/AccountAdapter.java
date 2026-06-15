package com.example.mobile_front_ma.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.dto.AccountListItem;

/**
 * Lists accounts (users/drivers) so an administrator can pick whose ride history to view
 * (spec 2.9.3).
 */
public class AccountAdapter extends ListAdapter<AccountListItem, AccountAdapter.AccountViewHolder> {

    public interface OnAccountClickListener {
        void onAccountClick(AccountListItem account);
    }

    private final OnAccountClickListener listener;

    public AccountAdapter(OnAccountClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<AccountListItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AccountListItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull AccountListItem o, @NonNull AccountListItem n) {
                    return o.id != null && o.id.equals(n.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull AccountListItem o, @NonNull AccountListItem n) {
                    return o.fullName().equals(n.fullName())
                            && String.valueOf(o.accountType).equals(String.valueOf(n.accountType));
                }
            };

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountListItem account = getItem(position);
        holder.nameText.setText(account.fullName());
        holder.subText.setText(account.email + "  ·  " + account.accountType);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccountClick(account);
            }
        });
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView subText;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.accountNameText);
            subText = itemView.findViewById(R.id.accountSubText);
        }
    }
}
