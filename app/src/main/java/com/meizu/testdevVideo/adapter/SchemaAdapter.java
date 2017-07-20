package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.data.listview.SchemaInfo;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.library.ViewHolderHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;

import java.util.List;

/**
 * Schema列表类
 * Created by maxueming on 2017/7/3.
 */

public class SchemaAdapter extends BaseAdapter{
    private List<SchemaInfo> listSchema;
    private LayoutInflater inflater;
    private Context context;

    public SchemaAdapter(Context context, List<SchemaInfo> listSchema){
        this.listSchema = listSchema;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listSchema.size();
    }

    @Override
    public Object getItem(int position) {
        return listSchema.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(null == convertView){
            convertView = inflater.inflate(R.layout.schema_list_adapter, parent, false);
        }

        TextView schemaType = ViewHolderHelper.get(convertView, R.id.schema_type);
        TextView schema_description = ViewHolderHelper.get(convertView, R.id.schema_description);
        schemaType.setText(getJumpType(listSchema.get(position).getJumpType()));
        schema_description.setText(listSchema.get(position).getDescription());
        RelativeLayout schema_jump_btn = ViewHolderHelper.get(convertView, R.id.schema_jump_btn);
        schema_jump_btn.setOnClickListener(new onClickListener(position));
        return convertView;
    }


    private class onClickListener implements View.OnClickListener{

        private int position;

        onClickListener(int position){
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Logger.d("点击了第" + position + "项");
            if(listSchema.get(position).getJumpType() == 1){
                try {
                    Intent intent = PublicMethod.getValidSchemaIntent(SuperTestApplication.getContext(),
                            listSchema.get(position).getAddress(), 0);
                    if(null != intent){
                        context.startActivity(intent);
                    }else{
                        ToastHelper.addToast("Schema无效，请检查配置", SuperTestApplication.getContext());
                    }
                }catch (Exception e){
                    ToastHelper.addToast("跳转失败", SuperTestApplication.getContext());
                }

            }else if(listSchema.get(position).getJumpType() == 2){

                try {
                    Intent intent = PublicMethod.getVaildActionIntent(SuperTestApplication.getContext(),
                            listSchema.get(position).getAddress(), 0);
                    if(null != intent){
                        context.startActivity(intent);
                    }else{
                        ToastHelper.addToast("Action无效，请检查配置", SuperTestApplication.getContext());
                    }
                }catch (Exception e){
                    ToastHelper.addToast("跳转失败", SuperTestApplication.getContext());
                }

            }else if(listSchema.get(position).getJumpType() == 3){
                try {
                    Intent intent = PublicMethod.getVaildPackageIntent(SuperTestApplication.getContext(),
                            listSchema.get(position).getAddress(), 0);
                    if(null != intent){
                        context.startActivity(intent);
                    }else{
                        ToastHelper.addToast("未安装跳转应用，请检查配置", SuperTestApplication.getContext());
                    }
                }catch (Exception e){
                    ToastHelper.addToast("跳转失败", SuperTestApplication.getContext());
                }
            }else{
                ToastHelper.addToast("未知跳转类型\n请配置Schema|Action|Native", SuperTestApplication.getContext());
            }

        }
    }

    /**
     * 获取跳转类型
     * @param type 类型
     * @return 跳转说明
     */
    private String getJumpType(int type){
        switch (type){
            case 1:
                return "Schema";
            case 2:
                return "Action";
            case 3:
                return "Native";
            default:
                return "未知";
        }
    }

}
