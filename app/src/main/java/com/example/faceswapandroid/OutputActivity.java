package com.example.faceswapandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;

public class OutputActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap src, dest;
    ProgressDialog dialog;
    String localIP = "192.168.1.34";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Swapping. Please wait...");
        Intent intent = getIntent();

        String srcPath = intent.getStringExtra("src");
        String destPath = intent.getStringExtra("dest");

        File file = new File(srcPath);
        src = BitmapFactory.decodeFile(file.getAbsolutePath());
        file = new File(destPath);
        dest = BitmapFactory.decodeFile(file.getAbsolutePath());
        imageView = findViewById(R.id.output);
        imageView.setImageBitmap(dest);
        new UpdateTask().execute();
    }

    private class UpdateTask extends AsyncTask<String, String, String> {

        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        protected String doInBackground(String... urls) {
            try {

                ByteArrayOutputStream srcBao = new ByteArrayOutputStream();
                final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                src.compress(Bitmap.CompressFormat.JPEG, 90, srcBao);
                byte[] srcBytes = srcBao.toByteArray();
                String srcBase64 = new String(Base64.getEncoder().encode(srcBytes));

                nameValuePairs.add(new BasicNameValuePair("src", srcBase64));

                ByteArrayOutputStream destBao = new ByteArrayOutputStream();
                dest.compress(Bitmap.CompressFormat.JPEG, 90, destBao);
                byte[] destBytes = destBao.toByteArray();
                String destBase64 = new String(Base64.getEncoder().encode(destBytes));
                nameValuePairs.add(new BasicNameValuePair("dest", destBase64));

                Log.d("SrcBase64", srcBase64);
                Log.d("DestBase64", destBase64);
                Log.d("NameValuePairs", nameValuePairs.toString());

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://" + localIP + ":8000/api/faceSwap");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                StringBuilder str = new StringBuilder();

                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    str.append(line + "\n");
                }

                JSONObject jsonObject = new JSONObject(String.valueOf(str));
                int success = jsonObject.getInt("success");
                Log.e("ResponseSuccess", "success: " + success);
                if (success == 1) {
                    String data = jsonObject.getString("data");
                    byte[] decodedString = Base64.getDecoder().decode(data);
                    bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Log.e("log_tag", "Image Uploaded  " + decodedString);
                } else {
                    int error = jsonObject.getInt("target");
                    Log.e("ResponseSuccess", "success: " + error);
                    if (error == 0)
                        setResult(-100);
                    else
                        setResult(-200);
                    finishActivity(2);
                    finish();
                }
            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            setImageView(bitmap);
        }
    }

    public void back(View v) {
        finish();
    }

    private void setImageView(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

}