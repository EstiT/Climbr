package edu.carleton.comp2601.climbr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class UserOnboardActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;
    EditText bio;
    EditText pullups;
    EditText grade;
    EditText name;
    EditText age;
    ImageButton dp;
    Button button;
    static UserOnboardActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_onboard);
        name  = (EditText)findViewById(R.id.name);
        bio = (EditText)findViewById(R.id.bio);
        pullups = (EditText)findViewById(R.id.pullups);
        grade = (EditText)findViewById(R.id.grade);
        age = (EditText)findViewById(R.id.age);
        dp = (ImageButton)findViewById(R.id.imageButton);
        button = (Button)findViewById(R.id.button);

        final Intent intent = getIntent();
        name.setText(intent.getStringExtra("username"));

        instance = this;


        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                HashMap<String, Serializable> map = new HashMap<String, Serializable>();
                map.put("username", name.getText().toString());
                map.put("password",intent.getStringExtra("password"));
                map.put("bio",bio.getText().toString());
                //for en/decoding http://mobile.cs.fsu.edu/converting-images-to-json-objects/
                Bitmap bitmap = ((BitmapDrawable)dp.getDrawable()).getBitmap();
                String encodedImage = getStringFromBitmap(bitmap);
                map.put("img",encodedImage);
                map.put("maxPullups",pullups.getText().toString());
                map.put("age",age.getText().toString());
                map.put("maxGrade",grade.getText().toString());

                LoginActivity.getInstance().c.sendRequest("UPDATE_PROFILE",map);

            }
        });

        dp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //https://developer.android.com/training/camera/photobasics.html
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "edu.carleton.comp2601.climbr.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private String getStringFromBitmap(Bitmap bitmapPicture) {
 /*
 * This functions converts Bitmap picture to a string which can be
 * JSONified.
 * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static UserOnboardActivity getInstance(){
        return instance;
    }


}


