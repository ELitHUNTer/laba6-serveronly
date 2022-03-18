package main;

public class Message {

    String[] message;

    public Message(String... strings){
        message = strings.clone();
    }

    public String[] getMessage(){
        return message;
    }

}
