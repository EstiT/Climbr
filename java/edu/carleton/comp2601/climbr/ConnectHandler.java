class ConnectHandler{
	public void handleEvent(Event e) {
		try {
			System.out.println("Handling Connect");

            String jsonString = (String) e.get("json");
            System.out.println(jsonString);

            JSONObject json = new JSONObject(jsonString);

            String username = json.get("username");
            String password = json.get("password");

            Event response = new Event("CONNECTED_RESPONSE");
            ThreadWithReactor twr;

            //check if somone already logged in on another device
            if(Server.clientExists(username)){
            	//Send back fail
            	System.out.println("Err: already logged in");
            	response.put("error", "Already logged in on another device.");

            }else{
            	//Check if new or returning 
	            if(Server.userExists(username)){
	            	System.out.println("user exists");
	            	//check if password is correct
	            	if(Server.authenticate(username, password)){
	            		System.out.println("Authenticated");
	            		//save thread
	            		twr = (ThreadWithReactor) Thread.currentThread();
	            		Server.addClient(username, twr);
	            		//Send back Success
	            		response.put("status", "returning");


	            	}else{
	            		System.out.println("Wrong password");
	            		response.put("error","Wrong password.");
	            	}
	            }else{
	            	//create new user MONGO
	            	//update database
	            	Server.addUserToDB(username,password);
	            	//save thread
	            	twr = (ThreadWithReactor) Thread.currentThread();
	            	Server.addClient(username, twr);

	            	response.put("status", "new");

	            }

	            e.putEvent(response);
            }
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
}



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