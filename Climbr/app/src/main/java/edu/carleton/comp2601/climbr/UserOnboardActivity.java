package edu.carleton.comp2601.climbr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
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
    EditText age;
    ImageButton dp;
    Button button;
    static UserOnboardActivity instance;

    Uri pURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_onboard);

        bio = (EditText)findViewById(R.id.bio);
        pullups = (EditText)findViewById(R.id.pullups);
        grade = (EditText)findViewById(R.id.grade);
        age = (EditText)findViewById(R.id.age);
        dp = (ImageButton)findViewById(R.id.imageButton);
        button = (Button)findViewById(R.id.button);

        //reset textfields
        bio.setText("");
        pullups.setText("");
        grade.setText("");
        age.setText("");
        bio.setText("");

        final Intent intent = getIntent();
        instance = this;

        if(LoginActivity.getInstance().c.hasSetInfo){
            //set all of the text fields
            bio.setText(LoginActivity.getInstance().c.bio);
            pullups.setText(LoginActivity.getInstance().c.maxPullups);
            grade.setText(LoginActivity.getInstance().c.maxGrade);
            age.setText(LoginActivity.getInstance().c.age);
            bio.setText(LoginActivity.getInstance().c.bio);

            //read the image from the file
            File file = LoginActivity.getInstance().c.myImage;
            try {
                FileReader fr = new FileReader(file.getAbsoluteFile());
                BufferedReader br = new BufferedReader(fr);
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                String pureBase64Encoded = text.toString();
                br.close();
                final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                //set the image
                dp.setImageBitmap(decodedBitmap);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }


        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //submit button, send update profile request with all of the profile fields
                HashMap<String, Serializable> map = new HashMap<String, Serializable>();
                if(intent.hasExtra("email")) {
                    map.put("email", intent.getStringExtra("email").toString());
                }

                if(intent.hasExtra("password")){
                    map.put("password",intent.getStringExtra("password").toString());
                }
                map.put("username", TabbedActivity.getInstance().myUsername);
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
                //bring up camera if display picture is clicked
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_TAKE_PHOTO);
        }

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
                System.out.println("photofile "+photoFile);
                Uri photoURI = FileProvider.getUriForFile(this, "edu.carleton.comp2601.climbr.fileprovider", photoFile);
                System.out.println("photouri "+photoURI);
                pURI = photoURI;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i("2601", "start act for result take pic intent");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("2601", "On activity result reqCode: "+ requestCode+" result code: "+resultCode);
//        Uri photoURI = data.getData();
//        Log.i("2601", "photoURI "+photoURI);
//        dp.setImageURI(photoURI);

        dp.setImageURI(pURI);

        /*
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(
                    pURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        dp.setImageBitmap(decodedBitmap);*/

    }

    public String getStringFromBitmap(Bitmap bitmapPicture) {
 /*
 * This functions converts Bitmap picture to a string which can be
 * JSONified.
 * */
        //lowest quality to save space
        final int COMPRESSION_QUALITY = 0;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }


    public File createImageFile() throws IOException {
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


