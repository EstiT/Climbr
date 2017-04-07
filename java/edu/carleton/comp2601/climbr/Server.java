package edu.carleton.comp2601.climbr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import com.mongodb.ServerAddress;


public class Server{
    public static int PORT = 2601;
    Socket s;
    Reactor r;
    public ConcurrentHashMap<String,ThreadWithReactor> clients;

    DB db;
    MongoClient mongoClient;

    public static void main(String args[]){
        Server ts = new Server();
        ts.run();
    }

    public void run(){
        ServerSocket listener;
        try{
            init();
            listener = new ServerSocket(PORT);
            System.out.println("Listening...");

            // To connect to mongodb server
            mongoClient = new MongoClient( "localhost" , 27017 );

            // Now connect to your databases
            db = mongoClient.getDB( "climbr" );
            System.out.println("Connect to database successfully");

            while(true){
                s = listener.accept();
                System.out.println("Connected");
                EventStreamImpl stream = new EventStreamImpl(s);

                System.out.println("Stream connected");

                ThreadWithReactor thread = new ThreadWithReactor(stream, r);
                thread.start();
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }


    public void init(){
        r = new Reactor();
        clients = new ConcurrentHashMap<String,ThreadWithReactor>();

        //register for connect request to save the user and send message
        r.register("CONNECT_REQUEST", new ConnectHandler());

        r.register("PROFILES", new ProfilesHandler());

        //register for disconnect request
        r.register("DISCONNECT_REQUEST", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("DISCONNECT_REQUEST");/*
                // respond with DISCONNECT_RESPONSE
                String jsonString = (String) e.get("json");
                System.out.println("jsonstring:" +jsonString);

                // Gson gson = new Gson();
                // HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                // System.out.println("json: "+json);

                // HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                String sender = (String) data.get("sender");
                //remove the client from the list of clients
                removeClient(sender);

                // send USERS_UPDATED message to all connected users
                //message has a list of all connected users'
                Event updateUsers = new Event("USERS_UPDATED");

                data.put("names", clients.keySet().toArray());
                // updateUsers.put("json", gson.toJson(data));
                try {
                    //send to all clients
                    for (String client : clients.keySet()) {
                        clients.get(client).getEventSource().putEvent(updateUsers);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }*/
            }
        });


    }



    //add client to hashmap
    void addClient(String n, ThreadWithReactor t){
        clients.put(n,t);
        System.out.println(clients.toString());
    }

    //remove client from hashmap
    void removeClient(String n){
        clients.remove(n);
    }

    void addUserToDB(String username, String password){
        //TODO MONGO
        DBCollection coll = db.getCollection("users");
        System.out.println("Collection users selected successfully");

        DBObject obj = new BasicDBObject();
        obj.put( "username", username);   
        obj.put( "password", password);   
        coll.insert(obj);

    }

    boolean clientExists(String username){
        if(clients.contains(username)){
            return true;
        }
        return false;
    }

    boolean userExists(String username){
        //check database for users
        DBCollection coll = db.getCollection("users");
        System.out.println("Collection users selected successfully");

        DBObject obj = new BasicDBObject();
        obj.put( "username", username);  

        DBCursor cursor = coll.find(obj);
        System.out.println("Cursor: "+ cursor);

        if(cursor.hasNext()){
            System.out.println("User exists");
            return true;

        }
        System.out.println("User does not exist");
        return false;
    }

    boolean authenticate(String username, String password){
        //check database for users
        DBCollection coll = db.getCollection("users");
        System.out.println("Collection users selected successfully");

        DBObject obj = new BasicDBObject();
        obj.put( "username", username); 
        obj.put( "password", password); 

        DBCursor cursor = coll.find(obj);
        if(cursor.hasNext()){
            System.out.println("Authenticated");
            return true;

        }
        System.out.println("Not authenticated");
        return false;
    }


    class ProfilesHandler implements EventHandler{
            public void handleEvent(Event e) {
                try {
                    System.out.println("Handling Profiles");
                    /*
                    String jsonString = (String) e.get("json");
                    System.out.println(jsonString);

                    JSONObject json = new JSONObject(jsonString);*/


                    //Mongo TODO get all profiles
                    //Send back profiles


                    Event response = new Event("PROFILE");


                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    


    class ConnectHandler implements EventHandler{
        public void handleEvent(Event e) {
            try {
                System.out.println("Handling Connect");

                String jsonString = (String) e.get("json");
                System.out.println(jsonString);

                JSONObject json = new JSONObject(jsonString);

                String username = (String)json.get("username");
                String password = (String)json.get("password");

                Event response = new Event("CONNECTED_RESPONSE");
                ThreadWithReactor twr;

                //check if somone already logged in on another device
                if(clientExists(username)){
                    //Send back fail
                    System.out.println("Err: already logged in");
                    response.put("error", "Already logged in on another device.");

                }
                else{
                    //Check if new or returning
                    if(userExists(username)){
                        System.out.println("user exists");
                        //check if password is correct
                        if(authenticate(username, password)){
                            System.out.println("Correct Password");
                            //save thread
                            twr = (ThreadWithReactor) Thread.currentThread();
                            addClient(username, twr);
                            //Send back Success
                            response.put("status", "returning");


                        }
                        else{
                            System.out.println("Wrong password");
                            response.put("error","Wrong password.");
                        }
                    }
                    else{
                        System.out.println("Creating new user");
                        //create new user MONGO
                        //update database
                        addUserToDB(username,password);
                        //save thread
                        twr = (ThreadWithReactor) Thread.currentThread();
                        addClient(username, twr);

                        response.put("status", "new");
                    }
                    e.putEvent(response);
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
