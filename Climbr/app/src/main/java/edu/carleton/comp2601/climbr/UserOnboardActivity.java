package edu.carleton.comp2601.climbr;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class UserOnboardActivity extends AppCompatActivity {

    EditText pass;
    EditText bio;
    EditText pullups;
    EditText grade;
    EditText name;
    EditText age;
    ImageButton dp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_onboard);
        name  = (EditText)findViewById(R.id.name);
        pass = (EditText)findViewById(R.id.password);
        bio = (EditText)findViewById(R.id.bio);
        pullups = (EditText)findViewById(R.id.pullups);
        grade = (EditText)findViewById(R.id.grade);
        age = (EditText)findViewById(R.id.age);
        dp = (ImageButton)findViewById(R.id.imageButton);

        Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                HashMap<String, Serializable> map = new HashMap<String, Serializable>();
                map.put("name", name.getText().toString());
                map.put("pass",pass.getText().toString());
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
}


