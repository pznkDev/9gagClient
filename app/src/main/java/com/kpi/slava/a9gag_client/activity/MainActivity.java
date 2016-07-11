package com.kpi.slava.a9gag_client.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kpi.slava.a9gag_client.R;
import com.kpi.slava.a9gag_client.adapter.ImageRVAdapter;
import com.kpi.slava.a9gag_client.api.BaseResponse;
import com.kpi.slava.a9gag_client.api.Image;
import com.kpi.slava.a9gag_client.api.Link;
import com.kpi.slava.a9gag_client.listener.EndlessRecyclerOnScrollListener;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.dialogs.VKShareDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    public static final int IMAGECOUNT = 15;

    private final String URL = "http://api.motti.be";

    private Gson gson = new GsonBuilder().create();

    private Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(URL)
            .build();

    private Link interf = retrofit.create(Link.class);

    public List<Image> imageList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefresh;

    private EndlessRecyclerOnScrollListener recyclerOnScrollListener;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //fragmentManager for VKapi
    private FragmentManager fragmentManager;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facebookInitializer();
        vkInitializer();

        fragmentManager = getSupportFragmentManager();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_main);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerOnScrollListener = new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadImages(current_page);
            }
        };
        recyclerView.setOnScrollListener(recyclerOnScrollListener);

        ImageRVAdapter adapter = new ImageRVAdapter(imageList);

        adapter.setShareListener(new ImageRVAdapter.OnBitmapShareClickListener() {
            @Override
            public void onClick(String shareType, Bitmap bitmap) {

                switch (shareType){
                    case (ImageRVAdapter.SHAREVK) :
                        shareImageVK(bitmap);
                        break;

                    case (ImageRVAdapter.SHAREINSTAGRAM) :
                        shareImageInstagram(bitmap);
                        break;

                    case (ImageRVAdapter.SHAREFACEBOOK) :
                        shareImageFacebook(bitmap);
                        break;

                }
            }
        });

        adapter.setSaveListener(new ImageRVAdapter.OnBitmapSaveClickListener() {
            @Override
            public void onClick(Bitmap bitmap, String id) {
                saveImageToGallery(getApplicationContext(), bitmap, id);
            }
        });

        recyclerView.setAdapter(adapter);

        //refresh recyclerView
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_swipe_images);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.getAdapter().notifyItemRangeRemoved(0, imageList.size());
                imageList.clear();
                recyclerOnScrollListener.refresh();
                loadImages(1);
                swipeRefresh.setRefreshing(false);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab_up);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        });

        loadImages(1);
    }

    private void facebookInitializer() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    private void vkInitializer(){
        VKSdk.login(this, VKScope.WALL, VKScope.PHOTOS, VKScope.OFFLINE);
    }


    private List<Image> loadImages(int index) {

        Map<String, Integer> mapJson = new HashMap<String, Integer>();
        mapJson.put("limit", IMAGECOUNT);
        mapJson.put("offset", (--index) * IMAGECOUNT);

        Call<BaseResponse> call = interf.getImages(mapJson);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                BaseResponse body = response.body();
                if (body != null) {
                    imageList.addAll(body.list);
                }
                int i = imageList.size();

                recyclerView.getAdapter().notifyItemRangeInserted((i - IMAGECOUNT), i);
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ooops, something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        return imageList;
    }

    public static void saveImageToGallery(Context context, Bitmap image, String name) {

        // Find the SD Card path
        File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Create a new folder in SD Card
        File dir = new File(filepath.getAbsolutePath() + "/9GAG/");
        dir.mkdirs();

        // Create a name for the saved image
        File file = new File(dir, name);

        try {
            OutputStream output = new FileOutputStream(file);

            // Compress into png format image from 0% - 100%
            image.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
            output.close();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, file.getPath());
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Toast.makeText(context, "Successfully saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    void shareImageFacebook(Bitmap bitmap) {

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) { Toast.makeText(MainActivity.this, "Successfully posted in Fb", Toast.LENGTH_SHORT).show();}

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {Toast.makeText(MainActivity.this, "An error occurred Fb", Toast.LENGTH_SHORT).show();}
        });

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        shareDialog.show(content);
    }


    void shareImageInstagram(Bitmap bitmap) {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (intent != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage("com.instagram.android");

            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "temp", null);
            Uri imageUri = Uri.parse(path);

            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

            shareIntent.setType("image/jpeg");

            startActivity(shareIntent);
        } else {
            // bring user to the market to download the app.
            // or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
            startActivity(intent);
        }
    }

    void shareImageVK(Bitmap bitmap) {

        VKShareDialogBuilder builder = new VKShareDialogBuilder();
        builder.setText("ko ko ko header");
        builder.setAttachmentImages(new VKUploadImage[]{
                new VKUploadImage(bitmap, VKImageParameters.jpgImage(0.9f))
        });
        builder.setAttachmentLink("ko ko ko link",
                "https://vk.com/dev/android_sdk");
        builder.setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
            @Override
            public void onVkShareComplete(int postId) {
                Toast.makeText(MainActivity.this, "VK success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVkShareCancel() {
                Toast.makeText(MainActivity.this, "VK Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVkShareError(VKError error) {
                Toast.makeText(MainActivity.this, "VK Error", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show(fragmentManager, "VK_SHARE_DIALOG");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(MainActivity.this, "Authorization successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this, "Authorization failed", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
