package com.lmj.mapdemo.poi;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lmj.mapdemo.R;

import java.util.List;

/**
 * Created by MJ on 2016/8/25.
 */
public class MyTextAdapter extends RecyclerView.Adapter<MyTextAdapter.ViewHolder> {

    //上拉加载更多
    private static final int TYPE_ITEM = 0;  //普通Item View
    private static final int TYPE_FOOTER = 1;  //底部FootView
    private List<String> mList;


    private MyItemClickerListener mItemClickListener;

    void setItemClickListener(MyItemClickerListener listener) {
        mItemClickListener = listener;
    }


    MyTextAdapter(List<String> list) {
        super();
        mList = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_text_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //做这个操作，只是不想position声明为final后报警告
        final int pos = position;
        if (getItemViewType(position) == TYPE_ITEM) {
            holder.mTextView.setText(mList.get(pos));
            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, pos);
                }
            });
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount() && position > 12) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        int count = mList.size();
        if (count > 12) {
            return count + 1;
        } else {
            return count;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        ViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.tv);
        }
    }

    interface MyItemClickerListener {

        void onItemClick(View view, int position);
    }

}
