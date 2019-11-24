package com.example.spider;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


//Recyclist的适配器
public class GeneralAdapter extends RecyclerView.Adapter<GeneralAdapter.MyViewHolder> {
    //当前上下文对象
    Context context;
    //RecyclerView填充Item数据的List对象
    List<String> datas;

    public GeneralAdapter(Context context, List<String> datas){
        this.context = context;
        this.datas = datas;
    }

    /**
     * 事件回调监听
     */
    private OnitemClick onitemClick;   //定义点击事件接口
    private OnLongClick onLongClick;  //定义长按事件接口

    //定义设置点击事件监听的方法
    public void setOnitemClickLintener (OnitemClick onitemClick) {
        this.onitemClick = onitemClick;
    }
    //定义设置长按事件监听的方法
    public void setOnLongClickListener (OnLongClick onLongClick) {
        this.onLongClick = onLongClick;
    }

    //定义一个点击事件的接口
    public interface OnitemClick {
        void onItemClick(int position);
    }
    //定义一个长按事件的接口
    public interface OnLongClick {
        void onLongClick(int position);
    }





    //创建ViewHolder
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //实例化得到Item布局文件的View对象
        View v = View.inflate(context, R.layout.item,null);
        //返回MyViewHolder的对象
        return new MyViewHolder(v);
    }



    //绑定数据
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.textView.setText(datas.get(position));
        if (onitemClick != null) {
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //在TextView的地方进行监听点击事件，并且实现接口
                    onitemClick.onItemClick(position);
                }
            });
        }
        if (onLongClick != null) {
            holder.textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //在TextView的地方进行长按事件的监听，并实现长按接口
                    onLongClick.onLongClick(position);
                    return true;
                }
            });
        }
    }

    //返回Item的数量
    @Override
    public int getItemCount() {
        return datas.size();
    }

    //继承RecyclerView.ViewHolder抽象类的自定义ViewHolder
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }







}
