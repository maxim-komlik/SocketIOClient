package by.schools.komlik.maxim.socketioclient;

import android.widget.LinearLayout;

public class Message {
    User user;
    String message;
    LinearLayout layout;

    public Message(User user, String message){
        this.user = user;
        this.message = message;

    }

    public User getUser(){
        return user;
    }
    public String getMessage(){
        return message;
    }

    public void setLayout(LinearLayout l){
        this.layout = l;
    }

    public LinearLayout getLayout(){
        return layout;
    }

}
