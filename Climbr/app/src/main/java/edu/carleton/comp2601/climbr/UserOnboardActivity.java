package edu.carleton.comp2601.climbr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import com.mongodb.ServerAddress;
import java.util.Arrays;

public class UserOnboardActivity extends AppCompatActivity {
    Button button;
    String user;
    String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_onboard);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //connect to mongo db

                try{

                    // To connect to mongodb server
                    MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

                    // Now connect to your databases
                    DB db = mongoClient.getDB( "findr" );
                    System.out.println("Connect to database successfully");

                    DBCollection coll = db.getCollection("users");
                    System.out.println("Collection users selected successfully");

                    DBCursor cursor = coll.find();

                    while (cursor.hasNext()) {
                        DBObject updateDocument = cursor.next();
                        updateDocument.put("likes","200");
                        coll.update(null,updateDocument,true,false);//TODO???
                    }

                    System.out.println("Document updated successfully");
                    cursor = coll.find();

                    int i = 1;

                    while (cursor.hasNext()) {
                        System.out.println("Updated Document: "+i);
                        System.out.println(cursor.next());
                        i++;
                    }

                }catch(Exception e){
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                }




            }
        });

    }


}
