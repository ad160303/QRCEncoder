/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.hw.qrcencoder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText textEt;
    private Button deployBt;
    private String msg;
    private RadioGroup radioGroup;
    private RadioButton blackRadioBt;
    private RadioButton blueRadioBt;
    private RadioButton redRadioBt;
    private boolean checked;
    private String color;

    private Button addBt;
    private ImageView logoView;
    private String filePath = null;
    private static final int PICK_IMAGE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textEt = (EditText) findViewById(R.id.text_et);
        deployBt = (Button) findViewById(R.id.deploy_encoder);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        blackRadioBt = (RadioButton) findViewById(R.id.black_radio_bt);
        blueRadioBt = (RadioButton) findViewById(R.id.blue_radio_bt);
        redRadioBt = (RadioButton) findViewById(R.id.red_radio_bt);
        logoView = (ImageView) findViewById(R.id.logo_image_view);
        addBt = (Button) findViewById(R.id.add_bt);

        deployBt.setOnClickListener(this);
        addBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.deploy_encoder:
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.black_radio_bt:
                        color = "black";
                        break;
                    case R.id.blue_radio_bt:
                        color =  "blue";
                        break;
                    case R.id.red_radio_bt:
                        color = "red";
                        break;

                    default:
                        color = "black";
                }

                msg = textEt.getText().toString();
                Intent intent = new Intent(MainActivity.this, EncoderActivity.class);
                intent.putExtra("msg", msg);
                intent.putExtra("color", color);
                intent.putExtra("file_path", filePath);
                startActivity(intent);
                break;

            case R.id.add_bt:
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);

                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                final Uri selectedImage = data.getData();
                filePath = getPath(this, selectedImage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logoView.setImageURI(selectedImage);
                        Toast.makeText(MainActivity.this, filePath, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public String getPath(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            return getRealPathFromUriKitKatPlus(context, uri);
        } else {
            return getRealPathFromUriMinusKitKat(context, uri);
        }
    }

    private String getRealPathFromUriKitKatPlus(Context context, Uri uri) {
        Cursor cursor = null;
        String wholeId = DocumentsContract.getDocumentId(uri);
        String id = wholeId.split(":")[1];

        try {
            String proj[] = {MediaStore.Images.Media.DATA};
            String sel = MediaStore.Images.Media._ID + "=?";
            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, sel, new String[]{id}, null);
            int column_index = cursor.getColumnIndexOrThrow(proj[0]);
            String filePath = "";
            if (cursor.moveToFirst()){
                filePath = cursor.getString(column_index);
            }
            cursor.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    private String getRealPathFromUriMinusKitKat(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        System.out.println(picturePath);
        return picturePath;
    }

}
