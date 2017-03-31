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


    public void connect(String h,int p, String userid ){
        host = h;
        port = p;
        user = userid;
        init();

        try {
            s = new Socket(host,port);
            Log.i("2601", "Host: " +host + " Port: " + port);

            //create socket based connection
            stream = new EventStreamImpl(s.getOutputStream(),s.getInputStream());
            Log.i("2601","Stream connect");

            thread = new ThreadWithReactor(stream, r);
            thread.start();

            JSONObject json = new JSONObject();
            json.put("id", user);

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
                LoginActivity.getInstance().showProgress(false);
            }
        });




        r.register("DISCONNECT_RESPONSE", new EventHandler() {
            @Override
            public void handleEvent(Event e) {
                Log.i("Connection", "Handling DISCONNECT RESPONSE");
                //MainActivity.getInstance().terminate();
            }
        });


    }

    public void sendRequest(String type, HashMap<String ,Serializable> data){
        try {
            //add the sender to every message
            data.put("sender", user);

            JSONObject json = new JSONObject();
            json.put("data", data);

            Event e = new Event(type, thread.getEventSource());
            e.put("json", json.toString());

            Log.i("2601", "Writing object. event: " + e.type);
            //send the event
            e.putEvent();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
