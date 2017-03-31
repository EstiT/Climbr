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


public class Server{
    public static int PORT = 2601;
    Socket s;
    Reactor r;
    public ConcurrentHashMap<String,ThreadWithReactor> clients;

    public static void main(String args[]){
        Server ts = new Server();
        ts.run();
    }

    public void init(){
        r = new Reactor();
        clients = new ConcurrentHashMap<String,ThreadWithReactor>();

        //register for connect request to save the user and send message
        r.register("CONNECT_REQUEST", new EventHandler() {
            public void handleEvent(Event e) {
/*
                try {
                    String jsonString = (String) e.get("json");
                    JSONObject json = new JSONObject();
                                json.put("json", jsonString);
                    System.out.println("Reading json");
                    // Gson gson = new Gson();

                    // HashMap<String, Serializable> data = gson.fromJson(jsonString, HashMap.class);
                    // System.out.println("json"+data.entrySet());

                    String id = (String) data.get("id");
                    ThreadWithReactor twr = (ThreadWithReactor) Thread.currentThread();
                    //save client
                    System.out.println("Adding "+ id);
                    addClient(id, twr);

                    Event response = new Event("CONNECTED_RESPONSE");
                    Event updateUsers = new Event("USERS_UPDATED");

                    data.put("names", clients.keySet().toArray());
                    // updateUsers.put("json", gson.toJson(data));

                    System.out.println("Putting events");

                    //send back to only client who sent req
                    e.putEvent(response);

                    //send to all clients
                    for (String client : clients.keySet()){
                        clients.get(client).getEventSource().putEvent(updateUsers);
                    }
                } catch (Exception ex) {
                    System.err.println(ex);
                }*/
            }
        });


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

    public void run(){
        ServerSocket listener;
        try{
            init();
            listener = new ServerSocket(PORT);
            System.out.println("Listening...");

            while(true){
                s = listener.accept();
                System.out.println("Connected");
                EventStreamImpl stream = new EventStreamImpl(s);

                System.out.println("Stream connected");

                ThreadWithReactor thread = new ThreadWithReactor(stream, r);
                thread.start();
            }
        } catch(Exception e){
            System.out.println(e);
        }
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

}
