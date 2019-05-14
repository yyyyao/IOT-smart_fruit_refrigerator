package com.example.iot;

import java.util.ArrayList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> data;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public ViewHolder(TextView v){
            super(v);
            textView = v;
        }
    }

    public MyRecyclerViewAdapter(ArrayList<String> data){
        this.data = data;
    }

    // Create new View
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        TextView v = (TextView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.activity_fruit, viewGroup, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the content of a view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(data.get(position));
    }

    public int getItemCount(){
        return data.size();
    }

    public void addItem(ArrayList<String> moredata){
        data.addAll(moredata);
    }

    public void removeItem(int position){
        if(position < getItemCount()) data.remove(position);
    }

    public void setData(ArrayList<String> data){
        this.data = data;
    }

}
