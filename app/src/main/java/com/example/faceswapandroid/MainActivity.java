package com.example.faceswapandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
    }


    public void selectPicture(View imageButton) {
        LinearLayout linearLayout = (LinearLayout) imageButton;
        this.imageButton = (ImageView) linearLayout.getChildAt(0);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void switcher(View v) {
        ImageView imageButton1 = findViewById(R.id.imgButton1);
        ImageView imageButton2 = findViewById(R.id.imgButton2);
        Bitmap bitmap1 = ((BitmapDrawable) imageButton1.getDrawable()).getBitmap();
        Bitmap bitmap2 = ((BitmapDrawable) imageButton2.getDrawable()).getBitmap();
        if (bitmap1 != null && bitmap2 != null) {
            imageButton2.setImageBitmap(bitmap1);
            imageButton1.setImageBitmap(bitmap2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("IntentFinish", "RequestCode: " + requestCode + " resultCode: " + requestCode);
        if (requestCode == 1 && resultCode == RESULT_OK && data.getData() != null) {
            Uri targetUri = data.getData();
            Bitmap bitmap;
            try {

                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                imageButton.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == 2 && (resultCode == -100 || resultCode == -200)) {
            String target;
            if (resultCode == -100)
                target = "source picture";
            else
                target = "destination picture";

            Toast.makeText(this, "No face detected in " + target + ". Please make sure the face does exist in the picture.", Toast.LENGTH_LONG).show();
        }
    }


    public void swap(View view) {
        ImageView imageButton1 = findViewById(R.id.imgButton1);
        ImageView imageButton2 = findViewById(R.id.imgButton2);
        Bitmap bitmap1 = ((BitmapDrawable) (imageButton1.getDrawable())).getBitmap();
        Bitmap bitmap2 = ((BitmapDrawable) (imageButton2.getDrawable())).getBitmap();


        String src = tempFileImage(bitmap1, "src");
        String dest = tempFileImage(bitmap2, "dest");

        Intent anotherIntent = new Intent(this, OutputActivity.class);

        anotherIntent.putExtra("src", src);
        anotherIntent.putExtra("dest", dest);

        startActivityForResult(anotherIntent, 2);
    }

    public String tempFileImage(Bitmap bitmap, String name) {

        File outputDir = getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }

}
