package com.example.droxbox.homeModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.droxbox.R;
import com.example.droxbox.pojo.File;

import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private ArrayList<File> files;
    private Context context;
    private OnItemClickListener listener;

    public FilesAdapter(ArrayList<File> files, Context context, OnItemClickListener listener) {
        this.files = files;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        context = parent.getContext();
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        File currentFile = files.get(position);

        holder.tvFileName.setText(currentFile.getName());
        holder.onItemClickListener(currentFile, listener);

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void add(File file) {
        //TODO: Test it
//        for ( File f :
//                files){
//            delete(f);
//        }
        if (!containsFile(file)) {
            files.add(file);
            notifyItemInserted(files.size() - 1);
        } else if (containsFile(file)) {
            final int index = files.indexOf(file);
            files.set(index, file);
            notifyItemChanged(index);
        }
    }

    public void delete(File file) {
        if (files.contains(file)) {
            final int index = files.indexOf(file);
            files.remove(index);
            notifyItemRemoved(index);
        }
    }

    private boolean containsFile(File f) {
        boolean contains = false;
        for (File file :
                files) {
            if (file.getName().equals(f.getName())) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView tvFileName;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_fileName);
            view = itemView;
        }

        void onItemClickListener(final File file, final OnItemClickListener listener) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener(file);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClickListener(file);
                    return true;
                }
            });
        }
    }
}
