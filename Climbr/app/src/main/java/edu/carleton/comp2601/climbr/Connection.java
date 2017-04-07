package edu.carleton.comp2601.climbr;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by olivi on 2017-02-21.
 */

public class Connection {
    private Socket s;
    private String host;
    private int port;
    private String user;
    private String sender;
    private boolean play;
    Reactor r;
    ThreadWithReactor thread;
    EventStreamImpl stream;


    public void connect(String h,int p, String userid, String pass){
        Log.i("2601", "in connect");
        host = h;
        port = p;
        user = userid;
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
        }catch (Exception e){
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
//                        redirect to create new profile
                        Intent i = new Intent(LoginActivity.getInstance().getApplicationContext(), UserOnboardActivity.class);
                        i.putExtra("password", LoginActivity.getInstance().mPasswordView.getText().toString());
                        i.putExtra("email", LoginActivity.getInstance().mEmailView.getText().toString());
                        LoginActivity.getInstance().startActivity(i);
                    }
                    else if (status.equals("returning")){
                        //bring them into app
                        Intent i = new Intent(LoginActivity.getInstance().getApplicationContext(), TabbedActivity.class);
                        //send the users information with them
                        i.putExtra("profile", e.get("profile"));
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
                            LoginActivity.getInstance().mPasswordView.setText("");
                        }
                    });
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
                        i.putExtra("profile", e.get("profile"));
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
                        String msg = (String) e.get("msg");
                        TabbedActivity.ConnectFragment.getInstance().addMsg(msg);
                    }else{
                        //toast TODO
                        Log.i("2601", "Error in messaging:  " + err);
                    }

                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
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

                    JSONObject json = new JSONObject((Map)data);
                    //json.put("data", data);

                    Event e = new Event(type, thread.getEventSource());
                    e.put("json", json.toString());

                    Log.i("2601", "Writing object. event: " + e.type);
                    //send the event
                    e.putEvent();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        t.start();

    }


}
