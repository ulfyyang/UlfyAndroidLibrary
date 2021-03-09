package com.ulfy.android.system.media_picker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ulfy.android.adapter.ListAdapter;
import com.ulfy.android.bus.Subscribe;
import com.ulfy.android.dialog.DialogUtils;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.system.ActivityUtils;
import com.ulfy.android.system.AppUtils;
import com.ulfy.android.system.R;
import com.ulfy.android.system.event.OnTakePhotoEvent;

import java.io.Serializable;
import java.util.List;

/**
 * 多媒体选择的Activity
 * 可以选择图片、视频、音频
 */
public final class MediaPickerActivity extends Activity {
    public static final String KEY_SEARCH = "search";                   // 外部使用的传参KEY
    public static final String KEY_MAX = "max";                         // 外部使用的传参KEY
    public static final String KEY_ENTITIES = "entities";               // 外部使用的传参KEY
    private ListAdapter<IViewModel> mediaAdapter = new ListAdapter<>();
    private static final int REQUEST_CODE_TAKE_PHOTO = 8921;            // 请求码：点击相机拍照图片时使用
    private MediaPickerVM vm = new MediaPickerVM();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityViewAndClick();
        receiveDataThenUpdateUI();
        requestPermissionThenDo(new Runnable() {
            @Override public void run() {
                initMediaListDataThenUpdateUI();
            }
        });
    }

    private class OnClickImpl implements View.OnClickListener {
        @Override public void onClick(View v) {
            if (v.getId() == R.id.backFL || v.getId() == R.id.cancelTV) {
                onBackPressed();
            } else if (v.getId() == R.id.completeTV) {
                Bundle data = new Bundle();
                data.putInt(MediaPickerActivity.KEY_SEARCH, vm.searchType);
                data.putInt(MediaPickerActivity.KEY_MAX, vm.maxCount);
                data.putSerializable(MediaPickerActivity.KEY_ENTITIES, (Serializable) vm.selectMultiMediaEntities);
                ActivityUtils.returnData(data);
            }
        }
    }

    private class OnItemClickImpl implements AdapterView.OnItemClickListener {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (vm.mediaCMList.get(position) instanceof MediaPickPictureAddPictureCM) {     // 点击了拍照
                if (vm.canPickMore()) {
                    ActivityUtils.takePhoto(REQUEST_CODE_TAKE_PHOTO);
                } else {
                    showOverMaxCountDialog();
                }
            } else {                  // 点击了其它位置
                if (vm.clickItem(position)) {
                    mediaAdapter.notifyDataSetChanged();
                    ((TextView)findViewById(R.id.completeTV)).setText(vm.getCompleteText());
                } else {
                    showOverMaxCountDialog();
                }
            }
        }
    }

    @Subscribe public void onTakePhoto(OnTakePhotoEvent event) {
        if (event.requestCode == REQUEST_CODE_TAKE_PHOTO) {
            AppUtils.insertPictureToSystem(event.file, event.file.getName(), new Runnable() {
                @Override public void run() {
                    vm.initMediaCMListData();
                    if (vm.clickItem(1)) {              // 因为相机占据这0号位，所以应当是模拟点击1号位
                        mediaAdapter.notifyDataSetChanged();
                        ((TextView)findViewById(R.id.completeTV)).setText(vm.getCompleteText());
                    } else {
                        showOverMaxCountDialog();
                    }
                }
            });
        }
    }





    private void initActivityViewAndClick() {
        // Activity 部分
        ActivityUtils.onCreated(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ulfy_system_activity_media_picker);
        // View 部分
        findViewById(R.id.backFL).setOnClickListener(new OnClickImpl());
        GridView multiMediaGV = findViewById(R.id.multiMediaGV);
        mediaAdapter.setData(vm.mediaCMList); multiMediaGV.setAdapter(mediaAdapter);
        multiMediaGV.setOnItemClickListener(new OnItemClickImpl());
        findViewById(R.id.cancelTV).setOnClickListener(new OnClickImpl());
        findViewById(R.id.completeTV).setOnClickListener(new OnClickImpl());
    }

    private void receiveDataThenUpdateUI() {
        if (getIntent().getExtras() != null) {
            vm.init(getIntent().getExtras().getInt(KEY_SEARCH), getIntent().getExtras().getInt(KEY_MAX),
                    (List<MediaEntity>) getIntent().getExtras().getSerializable(KEY_ENTITIES));
        } else {
            vm.init(MediaRepository.SEARCH_TYPE_PICTURE, 0, null);
        }
        ((TextView)findViewById(R.id.titleTV)).setText(vm.getActivityTitile());
        ((TextView)findViewById(R.id.completeTV)).setText(vm.getCompleteText());
    }

    private void initMediaListDataThenUpdateUI() {
        vm.initMediaCMListData();
        mediaAdapter.notifyDataSetChanged();
        ((TextView)findViewById(R.id.completeTV)).setText(vm.getCompleteText());
    }

    private void requestPermissionThenDo(final Runnable runnable) {
        AppUtils.requestPermission(new AppUtils.OnRequestPermissionListener() {
            @Override public void onSuccess() {
                if (runnable != null) {
                    runnable.run();
                }
            }
            @Override public void onFail() {
                Toast.makeText(MediaPickerActivity.this, "未授予访问系统存储空间权限", Toast.LENGTH_LONG).show();
                finish();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void showOverMaxCountDialog() {
        MediaOverMaxSelectCountVM model = new MediaOverMaxSelectCountVM(vm.maxCount, vm.searchType);
        View view = UiUtils.createViewFromClazz(this, (Class<? extends View>) model.getViewClass());
        if (view instanceof IView) {
            ((IView)view).bind(model);
        }
        DialogUtils.showDialog(this, MediaOverMaxSelectCountView.DIALOG_ID, view);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityUtils.onActivityResultForReceiveData(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onDestroy() {
        ActivityUtils.onDestroy(this);
        super.onDestroy();
    }
}
