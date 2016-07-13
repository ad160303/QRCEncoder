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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hw.qrcencoder.data.Contents;
import com.example.hw.qrcencoder.qrcode.QRCodeEncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/16.
 */
public final class EncoderActivity extends Activity implements View.OnClickListener{
    private static final String TAG = EncoderActivity.class.getSimpleName();
    private Bitmap bitmap;

    private Button addBt;
    private ImageView view;

    private String msg;
    private String color;
    private static final int PICK_IMAGE = 3;
    private String filePath;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.encoder);

        // This assumes the view is full screen, which is a good assumption
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;
        Intent intent = getIntent();
        msg = intent.getStringExtra("msg");
        color = intent.getStringExtra("color");
        filePath  = intent.getStringExtra("file_path");

        try {
            QRCodeEncoder qrCodeEncoder = null;
            // qrCodeEncoder = new QRCodeEncoder("AT", null, Contents.Type.TEXT,
            // BarcodeFormat.CODABAR.toString(), smallerDimension);
            // qrCodeEncoder = new QRCodeEncoder("HI", null, Contents.Type.TEXT,
            // BarcodeFormat.CODE_39.toString(), smallerDimension);
            // qrCodeEncoder = new QRCodeEncoder("Hello", null,
            // Contents.Type.TEXT, BarcodeFormat.CODE_128.toString(),
            // smallerDimension);
            // qrCodeEncoder = new QRCodeEncoder("1234567891011", null,
            // Contents.Type.TEXT, BarcodeFormat.EAN_13.toString(),
            // smallerDimension);
            // qrCodeEncoder = new QRCodeEncoder("12345678", null,
            // Contents.Type.TEXT, BarcodeFormat.EAN_8.toString(),
            // smallerDimension);
            // qrCodeEncoder = new QRCodeEncoder("1234", null,
            // Contents.Type.TEXT, BarcodeFormat.ITF.toString(),
            // smallerDimension);
            // qrCodeEncoder = new QRCodeEncoder("2345", null,
            // Contents.Type.TEXT, BarcodeFormat.PDF_417.toString(),
            // smallerDimension);
            qrCodeEncoder = new QRCodeEncoder(msg, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), smallerDimension, color,filePath);
            // qrCodeEncoder = new QRCodeEncoder("12345678910", null,
            // Contents.Type.TEXT, BarcodeFormat.UPC_A.toString(),
            // smallerDimension);

            bitmap = qrCodeEncoder.encodeAsBitmap();
            view = (ImageView) findViewById(R.id.image_view);
            view.setImageBitmap(bitmap);
            TextView contents = (TextView) findViewById(R.id.contents_text_view);
            contents.setText(qrCodeEncoder.getDisplayContents());
            setTitle(getString(R.string.app_name) + " - " + qrCodeEncoder.getTitle());

            Button saveBt = (Button) findViewById(R.id.save_bt);

            saveBt.setOnClickListener(this);
        } catch (WriterException e) {
            Log.e(TAG, "Could not encode barcode", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Could not encode barcode", e);
        }

    }

    @Override
    public void onClick(View v){
        // checks if external card is available,
        // if not, use internal card
        boolean isExternal = isExternalStorageWritable();
        if (isExternal){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();
            String formattedTime = formatter.format(date);
            String fileName = "qrc_" + formattedTime + ".jpg";
            File fileDir = null;
            if (fileDir == null){
                fileDir = getAlbumStorageDir("qrc");
            }
            File file = new File(fileDir.getPath() + '/' + fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                boolean isSaved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                if (isSaved) {Toast.makeText(EncoderActivity.this, "saved in sdcard0/Pictures/qrc", Toast.LENGTH_SHORT).show();}
            }catch (Exception ex){
                String name = file.getAbsolutePath();
                Toast.makeText(EncoderActivity.this, "cannot save in sdcard0", Toast.LENGTH_SHORT).show();
                Toast.makeText(EncoderActivity.this, name, Toast.LENGTH_SHORT).show();
            }

            // put the image into gallery
            try {
                MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            }catch (Exception ex){
                Toast.makeText(EncoderActivity.this, "saved, but cannot check in gallery", Toast.LENGTH_SHORT).show();
            }

            // inform gallery to update
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
        } else{
            Toast.makeText(EncoderActivity.this, "cannot save", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    private File getAlbumStorageDir(String album){
        // get the dir for public pics
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), album);
        if (!file.mkdirs()){}
        return file;
    }
}
