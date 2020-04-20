package com.ndtlg.driver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;

import static android.app.Activity.RESULT_OK;

public class PaxWebChromeClient extends WebChromeClient {
    private static final int CHOOSE_REQUEST_CODE = 0x9001;
    private static final int CHOOSE_REQUEST_CODE2 = 0x9002;
    private Activity mActivity;
    private Uri imageUri;
    private ValueCallback<Uri> uploadFile;//定义接受返回值
    private ValueCallback<Uri[]> uploadFiles;
    private ProgressBar bar;

    public PaxWebChromeClient(@NonNull Activity mActivity, ProgressBar bar) {
        this.mActivity = mActivity;
        this.bar = bar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            bar.setVisibility(View.GONE); //加载完网页进度条消失
        } else {
            bar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
            bar.setProgress(newProgress);//设置进度值
        }
    }

    @Override

    public void onReceivedTitle(WebView view, String title) {

        super.onReceivedTitle(view, title);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(PermissionRequest request) {
        //                super.onPermissionRequest(request);//必须要注视掉
        request.grant(request.getResources());
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        this.uploadFile = uploadFile;
        openFileChooseProcess();
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsgs) {
        this.uploadFile = uploadFile;
        openFileChooseProcess();
    }

    // For Android  > 4.1.1
//    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        this.uploadFile = uploadFile;
        openFileChooseProcess();
    }

    // For Android  >= 5.0
    @Override
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        this.uploadFiles = filePathCallback;
        openFileChooseProcess();
        return true;
    }

    /**
     * 拍照或选择相册
     */
    private void takePhoto() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
        alertDialog.setTitle("选择");
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (uploadFile != null) {
                    uploadFile.onReceiveValue(null);
                    uploadFile = null;
                }
                if (uploadFiles != null) {
                    uploadFiles.onReceiveValue(null);
                    uploadFiles = null;
                }
            }
        });
        alertDialog.setItems(new CharSequence[]{"相机", "相册", "视频"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File fileUri = new File(mActivity.getExternalCacheDir() + "/" + SystemClock.currentThreadTimeMillis() + ".jpg");
                        imageUri = Uri.fromFile(fileUri);
                        if (which == 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                imageUri = FileProvider.getUriForFile(mActivity, "com.ndtlg.driver.Mapplication", fileUri);//通过FileProvider创建一个content类型的Uri
                            } else {
                                imageUri = Uri.fromFile(fileUri);
                            }
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            mActivity.startActivityForResult(intent, CHOOSE_REQUEST_CODE);
                        } else if (which == 1) {
//                            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                            i.addCategory(Intent.CATEGORY_OPENABLE);
//                            i.setType("image/*");
//                            mActivity.startActivityForResult(Intent.createChooser(i, "File Browser"), CHOOSE_REQUEST_CODE);


                            Intent intent = new Intent(Intent.ACTION_PICK,null);
                            //此处调用了图片选择器
                            //如果直接写intent.setDataAndType("image/*");
                            //调用的是系统图库
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            mActivity.startActivityForResult(Intent.createChooser(intent, "File Browser"), CHOOSE_REQUEST_CODE);
                        } else if (which == 2) {
                            fileUri = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".mp4");
                            imageUri = Uri.fromFile(fileUri);
                            Intent intent = new Intent();
                            intent.setAction("android.media.action.VIDEO_CAPTURE");
                            intent.addCategory("android.intent.category.DEFAULT");
                            //设置视频质量
                            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.8);
                            //设置视频时长
//                            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
//                            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            mActivity.startActivityForResult(intent, 5);
                        }
                    }
                });
        alertDialog.show();
    }

    private void openFileChooseProcess() {
        takePhoto();
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");
//        mActivity.startActivityForResult(Intent.createChooser(i, "Choose"), CHOOSE_REQUEST_CODE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (uploadFiles == null) {
            return;
        }
        if (requestCode != 5) imageUri = getUri(imageUri);
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadFiles.onReceiveValue(results == null ? new Uri[]{imageUri} : results);
        uploadFiles = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_REQUEST_CODE) {
            if (null == uploadFile && null == uploadFiles) return;
            try {
                cropRawPhoto(mActivity, data != null ? data.getData() : imageUri, imageUri, CHOOSE_REQUEST_CODE2);
            } catch (Exception e) {
                e.printStackTrace();
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                if (result != null) result = getUri(result);
                if (uploadFiles != null) {
                    onActivityResultAboveL(requestCode, resultCode, data);
                } else if (uploadFile != null) {
                    uploadFile.onReceiveValue(result);
                    uploadFile = null;
                }
            }
        } else if (requestCode == CHOOSE_REQUEST_CODE2) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result != null) result = getUri(result);
            if (uploadFiles != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadFile != null) {
                uploadFile.onReceiveValue(result);
                uploadFile = null;
            }
        } else if (requestCode == 5) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadFiles != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadFile != null) {
                uploadFile.onReceiveValue(result);
                uploadFile = null;
            }
        }

    }

    public Uri getUri(Uri result) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(result));
            return Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), BitmapRead.decodeBitmapSize(bitmap, 800, 800), null, null));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return result;
        }
    }

    /**
     * 裁剪图片
     *
     * @param activity
     * @param inputUri
     * @param outputUri
     * @param requestCode
     */
    public static void cropRawPhoto(Activity activity, Uri inputUri, Uri outputUri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(inputUri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 600);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        activity.startActivityForResult(intent, requestCode);
    }

}