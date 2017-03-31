package edu.carleton.comp2601.climbr;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

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

                try {
                    String jsonString = (String) e.get("json");

                    System.out.println("Reading json");
                    Gson gson = new Gson();

                    HashMap<String, Serializable> data = gson.fromJson(jsonString, HashMap.class);
                    System.out.println("json"+data.entrySet());

                    String id = (String) data.get("id");
                    ThreadWithReactor twr = (ThreadWithReactor) Thread.currentThread();
                    //save client
                    System.out.println("Adding "+ id);
                    addClient(id, twr);

                    Event response = new Event("CONNECTED_RESPONSE");
                    Event updateUsers = new Event("USERS_UPDATED");

                    data.put("names", clients.keySet().toArray());
                    updateUsers.put("json", gson.toJson(data));

                    System.out.println("Putting events");

                    //send back to only client who sent req
                    e.putEvent(response);

                    //send to all clients
                    for (String client : clients.keySet()){
                        clients.get(client).getEventSource().putEvent(updateUsers);
                    }
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        });


        //regisster for paly game reqest, send message to the player
        r.register("PLAY_GAME_REQUEST", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("PLAY_GAME_REQUEST");
                String jsonString = (String) e.get("json");
                System.out.println("jsonstring:" +jsonString);

                Gson gson = new Gson();
                HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                System.out.println("json: "+json);

                HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                String opponent = (String) data.get("opponent");

                Event res = new Event("PLAY_GAME_REQUEST");
                res.put("json", gson.toJson(data));

                try {
                    clients.get(opponent).getEventSource().putEvent(res);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        // register for Play game response sned message to the player
        r.register("PLAY_GAME_RESPONSE", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("PLAY_GAME_RESPONSE");

                String jsonString = (String) e.get("json");
                System.out.println("jsonstring:" +jsonString);
                Gson gson = new Gson();
                HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                String sender = (String) data.get("sender");
                String opponent = (String) data.get("opponent");
                boolean play = (boolean) data.get("play");

                //for response
                HashMap<String, Serializable> resData = new HashMap<String, Serializable>();

                // true: start game -> send message to both users true
                if(play == true){
                    //Send both the sender and opponent a play game response
                    resData.put("x", opponent);
                    resData.put("o", sender);
                    resData.put("play", true);

                    Event res = new Event("PLAY_GAME_RESPONSE");
                    res.put("json", gson.toJson(resData));
                    try {
                        //send messages
                        clients.get(opponent).getEventSource().putEvent(res);
                        clients.get(sender).getEventSource().putEvent(res);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                //otherwise send game response false to both users
                }else{
                    //send the game requester (opponent) a message
                    resData.put("play", false);
                    resData.put("sender", sender);
                    Event res = new Event("PLAY_GAME_RESPONSE");
                    res.put("json", gson.toJson(resData));
                    try {
                        clients.get(opponent).getEventSource().putEvent(res);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });

        //register for disconnect request
        r.register("DISCONNECT_REQUEST", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("DISCONNECT_REQUEST");
                // respond with DISCONNECT_RESPONSE
                String jsonString = (String) e.get("json");
                System.out.println("jsonstring:" +jsonString);

                Gson gson = new Gson();
                HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                System.out.println("json: "+json);

                HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                String sender = (String) data.get("sender");
                //remove the client from the list of clients
                removeClient(sender);

                // send USERS_UPDATED message to all connected users
                //message has a list of all connected users'
                Event updateUsers = new Event("USERS_UPDATED");

                data.put("names", clients.keySet().toArray());
                updateUsers.put("json", gson.toJson(data));
                try {
                    //send to all clients
                    for (String client : clients.keySet()) {
                        clients.get(client).getEventSource().putEvent(updateUsers);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        //  register for game on message is sent to player
        r.register("GAME_ON", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("GAME_ON");

                String jsonString = (String) e.get("json");
                System.out.println("jsonstring:" +jsonString);

                Gson gson = new Gson();
                HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                System.out.println("json: "+json);

                HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                HashMap<String, Serializable> resData = new HashMap<String, Serializable>();
                resData.put("sender", data.get("sender"));

                Event res = new Event("GAME_ON");
                res.put("json", gson.toJson(resData));
                //send message to intended user
                try{
                    clients.get(data.get("sender")).getEventSource().putEvent(res);
                    clients.get(data.get("opponent")).getEventSource().putEvent(res);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        //  register for move message send message to the player indicated in the message
        r.register("MOVE_MESSAGE", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("MOVE_MESSAGE");
                try {
                    Gson gson = new Gson();
                    String jsonString = (String) e.get("json");
                    System.out.println("jsonstring:" +jsonString);

                    HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                    System.out.println("json: "+json);

                    HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                    //save the piece and where it played
                    int position = ((Double)data.get("position")).intValue();
                    char piece = ((String) data.get("piece")).charAt(0);

                    HashMap<String, Serializable> moveData = new HashMap<String, Serializable>();
                    moveData.put("piece", piece);
                    moveData.put("position", position);
                    moveData.put("sender", data.get("sender"));

                    Event moveMsg = new Event("MOVE_MESSAGE");
                    moveMsg.put("json", gson.toJson(moveData));
                    //send to both players
                    clients.get(data.get("opponent")).getEventSource().putEvent(moveMsg);
                    clients.get(data.get("sender")).getEventSource().putEvent(moveMsg);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        //  register for game over send message to the player indicated in the message
        r.register("GAME_OVER", new EventHandler(){
            public void handleEvent (Event e){
                System.out.println("GAME_OVER");

                Gson gson = new Gson();
                String jsonString = (String) e.get("json");
                System.out.println("jsonstring:" +jsonString);

                jsonString = jsonString.trim();

                JsonReader reader = new JsonReader(new StringReader(jsonString));
                reader.setLenient(true);

                HashMap<String, Serializable> json = gson.fromJson(jsonString, HashMap.class);
                System.out.println("json: "+json);

                System.out.println((String)json.get("data"));

                HashMap<String, Serializable> data = gson.fromJson(json.get("data").toString(),HashMap.class);

                System.out.println("data" + data);

                HashMap<String, Serializable> myData = new HashMap<String, Serializable>();
                myData.put("reason",data.get("myreason"));
                myData.put("opponent",data.get("opponent"));
                myData.put("sender",data.get("sender"));


                HashMap<String, Serializable> oppData = new HashMap<String, Serializable>();
                oppData.put("reason",data.get("oppreason"));
                oppData.put("opponent",data.get("opponent"));
                oppData.put("sender",data.get("sender"));

                Event oppres = new Event("GAME_OVER");
                oppres.put("json", gson.toJson(oppData));


                Event myres = new Event("GAME_OVER");
                myres.put("json", gson.toJson(myData));


                System.out.println("sender"+data.get("sender"));
                //send message to intended user
                try{
                    clients.get(data.get("sender")).getEventSource().putEvent(myres);
                    clients.get(data.get("opponent")).getEventSource().putEvent(oppres);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

        });
    }

    public void run(){
        ServerSocket listener;
        try{
            init();
            listener = new ServerSocket(PORT);
            System.out.println("Listening");

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
