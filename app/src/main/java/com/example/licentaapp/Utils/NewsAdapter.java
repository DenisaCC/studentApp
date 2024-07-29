package com.example.licentaapp.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsModel> announcements;
    private OnDownloadClickListener downloadClickListener; // Interfața pentru ascultătorul de descărcare

    public NewsAdapter(Context context, List<NewsModel> announcements, OnDownloadClickListener downloadClickListener) {
        this.context = context;
        this.announcements = announcements;
        this.downloadClickListener = downloadClickListener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsModel newsModel = announcements.get(position);
        holder.textViewTitle.setText(newsModel.getTitle());
        holder.textViewDate.setText(newsModel.getDate());
        holder.textViewDescription.setText(newsModel.getDescription());

        // Ascultător pentru click pe butonul de descărcare
        holder.textViewDownload.setOnClickListener(v -> {
            if (downloadClickListener != null) {
                downloadClickListener.onDownloadClick(position);
            }
        });

        // Expandare și restrângere la click pe întregul element
        holder.itemView.setOnClickListener(v -> {
            // Implementare pentru a face toggle între expandid și restrâns
            notifyDataSetChanged();
        });

        // Vizibilitatea descrierii și a butonului de descărcare bazată pe poziția expander-ului
        boolean isExpanded = position == holder.getAdapterPosition();
        holder.textViewDescription.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.textViewDownload.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    // Holder pentru RecyclerView
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDate;
        TextView textViewDescription;
        TextView textViewDownload;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDownload = itemView.findViewById(R.id.textViewDownload);
        }
    }

    // Interfața pentru descărcarea de click-uri
    public interface OnDownloadClickListener {
        void onDownloadClick(int position);
    }
}
