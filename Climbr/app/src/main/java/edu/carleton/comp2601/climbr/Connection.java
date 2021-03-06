package edu.carleton.comp2601.climbr;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static edu.carleton.comp2601.climbr.TabbedActivity.FindBelayerFragment.bioResources;
import static edu.carleton.comp2601.climbr.TabbedActivity.FindBelayerFragment.mResources;
import static edu.carleton.comp2601.climbr.TabbedActivity.FindBelayerFragment.nameResources;

/**
 * Created by olivi on 2017-02-21.
 */

public class Connection {
    private Socket s;
    private String host;
    private int port;
    static String user;
    static String bio;
    static File myImage;
    static String maxPullups;
    static String age;
    static String maxGrade;
    static boolean hasSetInfo;
    Reactor r;
    ThreadWithReactor thread;
    EventStreamImpl stream;


    public void connect(String h,int p, String userid, String pass){
        Log.i("2601", "in connect");
        host = h;
        port = p;
        user = userid;
        hasSetInfo = false;
        init();
        Log.i("2601", "trying to create socket");
        try {
            s = new Socket(host,port);
            Log.i("2601", "Host: " +host + " Port: " + port);

            //create socket based connection
            stream = new EventStreamImpl(s.getOutputStream(),s.getInputStream());
            Log.i("2601","Stream connect");

            thread = new ThreadWithReactor(stream, r);
            thread.start();

            JSONObject json = new JSONObject();
            json.put("username", user);
            json.put("password", pass);

            Log.i("Connection", "Sending connect request");
            //make a connect request event
            Event e = new Event("CONNECT_REQUEST", thread.getEventSource());
            e.put("json", json.toString());

            Log.i("2601", "Writing object. event: " + e.type);
            e.putEvent();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void init(){
        r = new Reactor();

        r.register("CONNECTED_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("Connection", "Handling CONNECTED RESPONSE");
                //hide Spinner
                LoginActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.getInstance().showProgress(false);
                    }
                });

                final String error = (String) e.get("error");
                if(error.equals("none")){
                    //there is no error
                    //check if new or existing user
                    String status = (String) e.get("status");
                    if(status.equals("new")){
                        //redirect to create new profile
                        Intent i = new Intent(LoginActivity.getInstance().getApplicationContext(), UserOnboardActivity.class);
                        i.putExtra("password", LoginActivity.getInstance().mPasswordView.getText().toString());
                        i.putExtra("email", LoginActivity.getInstance().mEmailView.getText().toString());
                        LoginActivity.getInstance().startActivity(i);
                    }
                    else if (status.equals("returning")){
                        //bring them into app
                        Intent i = new Intent(LoginActivity.getInstance().getApplicationContext(), TabbedActivity.class);
                        try{
                            //save user fields
                            JSONObject profileObject = new JSONObject(e.get("profile").toString());
                            bio = profileObject.get("bio").toString();
                            //img = profileObject.get("img").toString();
                            maxPullups = profileObject.get("maxPullups").toString();
                            maxGrade = profileObject.get("maxGrade").toString();
                            age = profileObject.get("age").toString();
                        }
                        catch(Exception ex){
                            ex.printStackTrace();
                        }
                        //give Tabbed activity the username
                        i.putExtra("username", LoginActivity.getInstance().mEmailView.getText().toString().split("@")[0]);
                        hasSetInfo = true;
                        LoginActivity.getInstance().startActivity(i);
                    }
                }
                else{
                    //there is an error, display in toast and clear password field
                    LoginActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(LoginActivity.getInstance().getApplicationContext(), error, Toast.LENGTH_LONG);
                            t.show();
                            //reload login activity
                            Intent i = new Intent(LoginActivity.getInstance().getApplicationContext(), LoginActivity.class);
                            LoginActivity.getInstance().startActivity(i);
                        }
                    });
                }
            }
        });


        r.register("PROFILE_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("2601", "Handling PROFILE_RESPONSE");
                try{
                    final String profiles = (String) e.get("profiles");
                    JSONArray obj = new JSONArray(profiles);

                    for(int i=0;i<obj.length();i++){
                        String jsonString = obj.getString(i);

                        final JSONObject json = new JSONObject(jsonString);
                        //add to data fill

                        final File file = File.createTempFile((String)json.get("username"), null, TabbedActivity.getInstance().getCacheDir());

                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write((String)json.get("img"));
                        bw.close();

                        TabbedActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //only add profile to resources if not their own
                                    if(!json.get("username").equals(TabbedActivity.getInstance().myUsername)){
                                        bioResources.add((String) json.get("bio"));
                                        nameResources.add((String) json.get("username"));
                                        Log.i("2601", "Adding " + (String) json.get("username"));
                                        mResources.add(file);
                                    }
                                    else{
                                        //otherwise save the fields
                                        myImage = file;
                                        bio = json.get("bio").toString();
                                        maxPullups = json.get("maxPullups").toString();
                                        maxGrade = json.get("maxGrade").toString();
                                        age = json.get("age").toString();
                                    }
                                }
                                catch(Exception ex){
                                    ex.printStackTrace();
                                }
                                Log.i("2601", "Notifying pagAadapter");
                                CustomPagerAdapter.getInstance().notifyDataSetChanged();
                            }
                        });
                        //CustomPagerAdapter.getInstance().notifyDataSetChanged();
                    }
                    TabbedActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CustomPagerAdapter.getInstance().notifyDataSetChanged();
                        }
                    });
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });


        r.register("UPDATE_PROFILE_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("Connection", "Handling UPDATE USER RESPONSE");
                //check if success or fail
                try{
                    String status = (String) e.get("status");
                    if(status.equals("success")){
                        //redirect to tabbed activity, send user name as extra
                        Intent i = new Intent(UserOnboardActivity.getInstance().getApplicationContext(), TabbedActivity.class);
                        i.putExtra("from","UserOnboard");
                        try{
                            //save user fields
                            JSONObject profileObject = new JSONObject(e.get("profile").toString());
                            bio = profileObject.get("bio").toString();
                            maxPullups = profileObject.get("maxPullups").toString();
                            maxGrade = profileObject.get("maxGrade").toString();
                            age = profileObject.get("age").toString();
                        }
                        catch(Exception ex){
                            ex.printStackTrace();
                        }
                        hasSetInfo = true;
                        UserOnboardActivity.getInstance().startActivity(i);
                    }
                    else if (status.equals("failure")){
                        final String reason = (String) e.get("reason");
                        UserOnboardActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast t = Toast.makeText(LoginActivity.getInstance().getApplicationContext(), reason, Toast.LENGTH_LONG);
                                t.show();
                                LoginActivity.getInstance().mPasswordView.setText("");
                            }
                        });
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });


        r.register("MESSAGE_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("Connection", "Handling MESSAGE RESPONSE");
                //check if success or fail
                try{
                    String err = (String) e.get("error");
                    if(err.equals("none")) {
                        final String msg = (String) e.get("message");
                        final String sender = (String) e.get("sender");

                        TabbedActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!sender.equals(TabbedActivity.myUsername)) {
                                    TabbedActivity.recipient = sender;
                                    //bring user to messaging/connect tab
                                    TabbedActivity.getInstance().tabLayout.getTabAt(TabbedActivity.getInstance().CONNECT).select();
                                    //tell the user who they are messaging
                                    TabbedActivity.ConnectFragment.getInstance().changeTitle("Messaging " + sender);
                                    //change message tab icon to show notification
                                    TabbedActivity.getInstance().tabLayout.getTabAt(TabbedActivity.getInstance().CONNECT).setIcon(ResourcesCompat.getDrawable(TabbedActivity.getInstance().getResources(), R.drawable.ic_chat_white_notify, null));
                                }
                                TabbedActivity.ConnectFragment.getInstance().addMsg(msg);
                            }
                        });
                        Log.i("2601", "msg: " + msg);
                    }
                    else{
                        //toast TODO
                        Log.i("2601", "Error in messaging:  " + err);
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });


        r.register("GET_PROFILE_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("Connection", "Handling GET_PROFILE_RESPONSE");
                Intent i = new Intent(LoginActivity.getInstance().getApplicationContext(), UserOnboardActivity.class);
                i.putExtra("username", e.get("username"));
                try{
                    //save user fields
                    JSONObject profileObject = new JSONObject(e.get("profile").toString());
                    bio = profileObject.get("bio").toString();
                    maxPullups = profileObject.get("maxPullups").toString();
                    maxGrade = profileObject.get("maxGrade").toString();
                    age = profileObject.get("age").toString();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                LoginActivity.getInstance().startActivity(i);
            }
        });


        r.register("DISCONNECT_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("Connection", "Handling DISCONNECT RESPONSE");
            }
        });
    }


    public void sendRequest(final String type, final HashMap<String ,Serializable> data){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //add the sender to every message
                    data.put("sender", user);
                    data.put("username", user);

                    JSONObject json = new JSONObject((Map)data);
                    //json.put("data", data);

                    Event e = new Event(type, thread.getEventSource());
                    e.put("json", json.toString());

                    Log.i("2601", "Writing object. event: " + e.type);
                    //send the event
                    e.putEvent();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        t.start();
    }


}
