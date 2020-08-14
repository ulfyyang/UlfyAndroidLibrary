package com.ulfy.android.system.media_picker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ulfy.android.adapter.ListAdapter;
import com.ulfy.android.bus.Subscribe;
import com.ulfy.android.dialog.DialogUtils;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.system.ActivityUtils;
import com.ulfy.android.system.AppUtils;
import com.ulfy.android.system.R;
import com.ulfy.android.system.event.OnTakePhotoEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 多媒体选择的Activity
 * 可以选择图片、视频、音频
 */
public final class MediaPickerActivity extends Activity {
    private static final int REQUEST_CODE_TAKE_PHOTO = 8921;            // 请求码：点击相机拍照图片
    private FrameLayout backFL;
    private TextView titleTV;
    private GridView multiMediaGV;
    private TextView cancelTV;
    private TextView completeTV;

    private ListAdapter adapter;

    private int searchType = MediaRepository.SEARCH_TYPE_PICTURE;// 音频暂时没有对应的实现
    private int maxCount;
    private List<MediaEntity> selectMultiMediaEntities;

    private MediaPickerVM vm;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.onCreated(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ulfy_system_activity_media_picker);
        initActivityIfHaveReadPhotosPermission();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityUtils.onActivityResultForReceiveData(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onDestroy() {
        ActivityUtils.onDestroy(this);
        super.onDestroy();
    }

    private void initActivityIfHaveReadPhotosPermission() {
        AppUtils.requestPermission(new AppUtils.OnRequestPermissionListener() {
            public void onSuccess() {
                initViews();
                initListeners();
                processActivityReceiveData();
                initActivityTitle();
                initModelAndData();
                updateUiData();
            }
            public void onFail() {
                Toast.makeText(MediaPickerActivity.this, "未授予访问系统存储空间权限", Toast.LENGTH_LONG).show();
                finish();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initViews() {
        backFL = (FrameLayout) findViewById(R.id.backFL);
        titleTV = (TextView) findViewById(R.id.titleTV);
        multiMediaGV = (GridView) findViewById(R.id.multiMediaGV);
        cancelTV = (TextView) findViewById(R.id.cancelTV);
        completeTV = (TextView) findViewById(R.id.completeTV);
        adapter = new ListAdapter();
        multiMediaGV.setAdapter(adapter);
    }

    private void initListeners() {
        backFL.setOnClickListener(new OnClickImpl());
        cancelTV.setOnClickListener(new OnClickImpl());
        completeTV.setOnClickListener(new OnClickImpl());
        multiMediaGV.setOnItemClickListener(new OnItemClickImpl());
    }

    @SuppressWarnings("unchecked")
    private void processActivityReceiveData() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        searchType = extras.getInt("search", MediaRepository.SEARCH_TYPE_PICTURE);
        maxCount = extras.getInt("max", 0);
        selectMultiMediaEntities = (List<MediaEntity>) extras.getSerializable("entities");
    }

    private void initActivityTitle() {
        switch (searchType) {
            case MediaRepository.SEARCH_TYPE_PICTURE:
                titleTV.setText("相册");
                break;
            case MediaRepository.SEARCH_TYPE_VIDEO:
                titleTV.setText("视频");
                break;
            case MediaRepository.SEARCH_TYPE_VOICE:
                titleTV.setText("声音");
                break;
            default:
                titleTV.setText("无法识别的类型");
                break;
        }
    }

    private void initModelAndData() {
        vm = new MediaPickerVM(maxCount, searchType);
        adapter.setData(vm.cmList);
        vm.loadMultiMedias(this, selectMultiMediaEntities);
    }

    private void updateUiData() {
        adapter.notifyDataSetChanged();
        completeTV.setText(vm.getCompleteText());
    }

    private class OnClickImpl implements View.OnClickListener {
        @Override public void onClick(View v) {
            if (v == backFL || v == cancelTV) {
                onBackPressed();
            } else if (v == completeTV) {
                returnSelectedMultiMediaEntities(vm.getSelectEntities());
            }
        }
    }

    private void returnSelectedMultiMediaEntities(List<MediaEntity> selectEntities) {
        /*
        返回数据的实现尽量不要采用事件总线的方式发布
        1. 容易造成内存泄露，返回的实体数据被修改。通过bundle是以序列化的方式发布的，不会产生被另一个界面修改的情况
        2. 如果有多个界面订阅了这个事件，则容易造成一个界面选择图片，其它多个界面都会收到影响的情况。而使用bundle
        返回的方式能够保证只有上一个界面收到这些数据
         */
        Bundle data = new Bundle();
        data.putInt("search", searchType);
        data.putInt("max", maxCount);
        data.putSerializable("entities", (Serializable) selectEntities);
        ActivityUtils.returnData(data);
    }

    private class OnItemClickImpl implements AdapterView.OnItemClickListener {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 点击了拍照
            if (vm.cmList.get(position) instanceof MediaPickPictureAddPictureCM) {
                if (vm.canPickMultiMedia()) {
                    ActivityUtils.takePhoto(REQUEST_CODE_TAKE_PHOTO);
                } else {
                    showOverMaxCountDialog();
                }
            }
            // 点击了其它位置
            else {
                try {
                    vm.clickItem(searchType == MediaRepository.SEARCH_TYPE_PICTURE ? position - 1 : position);
                    updateUiData();
                } catch (OverMaxSelectMediaCountException e) {
                    showOverMaxCountDialog();
                }
            }
        }
    }

    private void showOverMaxCountDialog() {
        MediaOverMaxSelectCountVM model = new MediaOverMaxSelectCountVM(vm.getMediaPickerControl().getMaxSelectCount(), vm.getMediaPickerControl().getSearchType());
        View view = UiUtils.createViewFromClazz(this, (Class<? extends View>) model.getViewClass());
        if (view instanceof IView) {
            ((IView)view).bind(model);
        }
        DialogUtils.showDialog(this, view);
    }

    @Subscribe public void onTakePhoto(OnTakePhotoEvent event) {
        if (event.requestCode != REQUEST_CODE_TAKE_PHOTO) {
            return;
        }

        List<MediaEntity> entityList = new ArrayList<>();
        MediaEntity mediaEntity = new MediaEntity(0,event.file.getName(),event.file.getAbsolutePath(),event.file.length());
        entityList.add(mediaEntity);
        returnSelectedMultiMediaEntities(entityList);

//        AppUtils.insertPictureToSystem(event.file, event.file.getName(), "app 拍照生成");
//        vm.reloadMultiMedias(this);
//        try {
//            vm.clickItem(0);
//            updateUiData();
//        } catch (OverMaxSelectMediaCountException e) {
//            showOverMaxCountDialog();
//        }
    }
}
