package com.kpi.slava.a9gag_client.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.kpi.slava.a9gag_client.R;
import com.kpi.slava.a9gag_client.api.Image;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageRVAdapter extends RecyclerView.Adapter<ImageRVAdapter.ViewHolder>{

    public static final String SHAREVK = "Vkontakte";
    public static final String SHAREINSTAGRAM = "Instagram";
    public static final String SHAREFACEBOOK = "Facebook";

    public interface OnBitmapShareClickListener {
        void onClick(String type, Bitmap bitmap);
    }

    public interface OnBitmapSaveClickListener {
        void onClick(Bitmap bitmap, String id);
    }

    private OnBitmapShareClickListener mShareListener;
    private OnBitmapSaveClickListener mSaveListener;

    public void setShareListener(OnBitmapShareClickListener shareListener) {
        mShareListener = shareListener;
    }

    public void setSaveListener(OnBitmapSaveClickListener saveListener) {
        mSaveListener = saveListener;
    }

    private List<Image> imageList;

    public ImageRVAdapter(List<Image> imageList) {
        this.imageList = imageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Uri uri = Uri.parse(imageList.get(position).getImage_full());
        final Context context = holder.imageView.getContext();
        Picasso.with(context).load(uri).into(holder.imageView);
        holder.btnShareVk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShareListener != null) {
                    mShareListener.onClick(SHAREVK, ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap());
                }
            }
        });
        holder.btnShareInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShareListener != null) {
                    mShareListener.onClick(SHAREINSTAGRAM, ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap());
                }
            }
        });
        holder.btnShareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShareListener != null) {
                    mShareListener.onClick(SHAREFACEBOOK, ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap());
                }
            }
        });

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSaveListener != null) {
                    mSaveListener.onClick(((BitmapDrawable) holder.imageView.getDrawable()).getBitmap(), imageList.get(position).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageButton btnShareVk, btnShareInstagram, btnShareFacebook;
        Button btnSave;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view_main);
            btnShareVk = (ImageButton) itemView.findViewById(R.id.btn_image_share_vk);
            btnShareInstagram = (ImageButton) itemView.findViewById(R.id.btn_image_share_instagram);
            btnShareFacebook = (ImageButton) itemView.findViewById(R.id.btn_image_share_facebook);
            btnSave = (Button) itemView.findViewById(R.id.btn_image_save);
        }
    }

}
