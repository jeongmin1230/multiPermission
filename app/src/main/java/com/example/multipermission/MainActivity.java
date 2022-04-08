package com.example.multipermission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 권한 요청 확인 코드
    private static final int MULTIPLE_PERMISSIONS = 1004;

    private static final int REQUEST_CONTACTS_CODE = 0;
    private static final int REQUEST_CAMERA_CODE = 1;

    Intent intent; // 모든 onClick 에서 사용 할 것이기 때문에 전역변수로 선언

    private String[] permissions = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    private String[] permissionCommentary = {
            "연락처 보기 권한", "카메라 권한", "폰 상태 권한"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) { // 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions();
            Log.i("jeongmin", "Build.VERSION.SDK_INT >= 23");
        }
    }

    public void onClickShowContact(View view) {
        intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, 0);
    }

    public void onClickCamera(View view) {
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    public void onClickCheckPhoneState(View view) {
        TextView tvPhoneStatus = (TextView) findViewById(R.id.tvPhoneStatus);
        // 휴대폰 정보를 위해 TelephonyManager 를 이용
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        tvPhoneStatus.setText("음성통화상태 : " + tm.getCallState() + " \n"
                + "데이터 통신 상태 : " + tm.getDataState() + " \n"
                + "SIM 카드 상태 : " + tm.getSimState() + " \n"
                + "통신사 ISO 국가 코드 : " + tm.getNetworkCountryIso() + " \n"
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        TextView tvContactsShow = (TextView) findViewById(R.id.tvContactsShow);
        ImageView ivCameraShow = (ImageView) findViewById(R.id.ivCameraShow);

        if (resultCode == RESULT_OK) { // 이 안에 코드 작성
            switch(requestCode){
                case REQUEST_CONTACTS_CODE:
                    Cursor cursor = null;
                    if (data != null) {
                        cursor = getContentResolver().query(data.getData(), new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                    }
                    if (cursor != null) {
                        cursor.moveToFirst();
                        tvContactsShow.setText("이름 : " + cursor.getString(0) + "\n" + "번호 : " + cursor.getString(1));
                        cursor.close();
                    }
                    break;

                case REQUEST_CAMERA_CODE:
                    Log.i("jeongmin", "if 문 밖 카메라 사진찍은 거 이미지뷰에 출력하는 부분");
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivCameraShow.setImageBitmap(imageBitmap);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for(String permission: permissions) {
            result = ContextCompat.checkSelfPermission(this, permission);
            if(result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if(!permissionList.isEmpty()) { // permissionList 가 비어 있지 않으면 false 리턴
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case MULTIPLE_PERMISSIONS: {
                if(grantResults.length > 0) {
                    for(int i = 0; i < permissions.length; i++) {
                        if(permissions[i].equals(this.permissions[i])) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                Log.i("jeongmin","거부했을 때 안에서 권한 요청한 이름 : " + permissionCommentary[i]);
                            } else if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                Log.i("jeongmin", "권한 허가 했을 때 요청한 권한 : " + permissionCommentary[i]);
                            }
                        }
                    }
                } else {
                    Log.i("jeongmin", "권한 요청에 동의 해주셔야 이용 가능합니다.");
                }
                return;
            }
        }
    }
}