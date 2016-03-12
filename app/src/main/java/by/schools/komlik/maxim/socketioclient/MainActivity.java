package by.schools.komlik.maxim.socketioclient;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MyLogs";

    Socket Socket;
    ScrollView Scroll;
    LinearLayout mainLin;
    EditText myMessage;
    TextView userLog;
    ProgressBar Bar;

    ConnectionDetector detector;
    TextWatcher TW = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (bool == false) {
                Socket.emit("typing");
                bool = true;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            h.removeCallbacks(r);
            h.postDelayed(r, 1000);
        }
    };

    LayoutInflater inflater;

    ArrayList <Message> Messages;
    ArrayList <User> Users;
    ArrayList <String> areTyping;

    String myName;

    boolean bool = false;

    Handler h = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            bool = false;
            Socket.emit("stop typing");
        }
    };

    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(LOG_TAG, "there is new message");
            JSONObject data = (JSONObject) args[0];
            User user;
            Message msg;
            try {
                user = new User(data.getString("username"), "");
                if (findUser(user) == -1){
                    Users.add(user);
                    msg = new Message(user, data.getString("message"));
                }else {
                    msg = new Message(Users.get(findUser(user)), data.getString("message"));
                }



            } catch (JSONException e) {
                return;
            }
            addToScroll(msg);
        }
    };
    Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            User user;
            JSONObject data = (JSONObject) args[0];
            int numUsers;
            try {
                user = new User(data.getString("username"), "");
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }
            Users.add(user);
            if (numUsers >= 3) {
                setLogMassage(user.getName() + " joined the chat.There are " + Integer.toString(numUsers - 1) + " participants.");
            }else {

                setLogMassage(user.getName() + " joined the chat.There is " + Integer.toString(numUsers - 1) + " participant.");
            }

        }
    };

    Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            JSONObject data = (JSONObject) args[0];
            String username;
            int numUsers;
            try {
                username = data.getString("username");
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            for(int i = 0; i < Users.size(); i++){
                if (Users.get(i).getName().equals(username)){
                    Users.remove(i);
                }
            }
            if (numUsers >= 3) {
                setLogMassage(username + " left the chat.There are " + Integer.toString(numUsers - 1) + " participants.");
            }else {
                if (numUsers == 1) {
                    setLogMassage(username + " left the chat.There is no more participants.");
                } else{
                    setLogMassage(username + " left the chat.There is " + Integer.toString(numUsers - 1) + " participant.");
                }
            }

        }
    };

    Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            JSONObject data = (JSONObject) args[0];
            String username;
            String log = "";
            try {
                username = data.getString("username");
            } catch (JSONException e) {
                return;
            }

            areTyping.add(username);
            for (int i = 0; i < areTyping.size(); i++){
                log = log + areTyping.get(i);
                if (i != areTyping.size() - 1){
                    log = log + ", ";
                }else {
                    if (areTyping.size()>1){
                        log = log + " are typing...";
                    }else {
                        log = log + " is typing...";
                    }
                }
            }
            setLogMassage(log);
        }
    };

    Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            String username;
            String log = "";
            try {
                username = data.getString("username");
            } catch (JSONException e) {
                return;
            }
            areTyping.remove(username);
            if (areTyping.size()>0){
                for (int i = 0; i < areTyping.size(); i++){
                    log = log + areTyping.get(i);
                    if (i != areTyping.size() - 1){
                        log = log + ", ";
                    }else {
                        if (areTyping.size()>1){
                            log = log + " are typing...";
                        }else {
                            log = log + " is typing...";
                        }
                    }
                }
            }
            setLogMassage(log);

        }
    };

    Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }
            Users.add(new User(myName, "Me"));

            if (numUsers >= 3) {
                setLogMassage("Hello! There are " + Integer.toString(numUsers - 1) + " participants.");
            }else {
                if (numUsers == 1) {
                    setLogMassage("Hello! There is not any participants.");
                }
                setLogMassage("Hello! There is " + Integer.toString(numUsers - 1) + " participant.");
            }
            unblockUI();
            setBarVisible(false);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detector = new ConnectionDetector(this);

        Scroll = (ScrollView)findViewById(R.id.Scroll);
        mainLin = (LinearLayout) findViewById(R.id.mainLin);
        myMessage = (EditText) findViewById(R.id.messageText);
        userLog = (TextView) findViewById(R.id.userLog);
        Bar = (ProgressBar) findViewById(R.id.sendingProgressBar);

        areTyping = new ArrayList<String>();
        Messages = new ArrayList<Message>();
        Users = new ArrayList<User>();

        myName = getMyName();
        Users.add(new User(myName, "me"));

        inflater = getLayoutInflater();
        if ( detector.isConnectedToInternet()){
            if(!connectToServer()){
                changeServerAddress();
            }
        }else{
            connectionErrorUI();
        }

    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        if (Socket.connected()) {
            Socket.disconnect();
            Socket.off("login", onLogin);
            Socket.off("new message", onNewMessage);
            Socket.off("user joined", onUserJoined);
            Socket.off("user left", onUserLeft);
            Socket.off("typing", onTyping);
            Socket.off("stop typing", onStopTyping);
        }
    }


    boolean connectToServer(){
        blockUI();
        String serverAddress = "http://46.101.96.234/";
        userLog.setText("Connecting...");
        try {
            Socket = IO.socket(serverAddress);
            Socket.on("login", onLogin);
            Socket.on("new message", onNewMessage);
            Socket.on("user joined", onUserJoined);
            Socket.on("user left", onUserLeft);
            Socket.on("typing", onTyping);
            Socket.on("stop typing", onStopTyping);
            Socket.connect();
            Socket.emit("add user", myName);
            myMessage.addTextChangedListener(TW);
            return true;
        } catch (URISyntaxException e) {
            Log.d(LOG_TAG, "Uri syntax error. Failed to connect the io.socket server. Server's address is " + serverAddress);
            return false;
        }
    }

    void connectionErrorUI(){
        Log.d(LOG_TAG, "ConnectError");
        Toast.makeText(this, "Connection error. Check up your internet connection.", Toast.LENGTH_LONG);
        this.onDestroy();
    }

    void changeServerAddress(){

    }

    String getMyName(){
        String name = "";
        DBHelper DBH = new DBHelper(this);
        SQLiteDatabase DB = DBH.getWritableDatabase();
        Cursor c = DB.query("Login", null, null, null, null, null, null);
        if (c.moveToFirst()) {

            name = c.getString(c.getColumnIndex("name"));

        }

        c.close();

        Cursor c1 = DB.query("bool", null, null, null, null, null, null);
        if (c1.moveToFirst()){
            if (c1.getInt(c1.getColumnIndex("rem")) != 1){
                DB.delete("Login", null, null);
                DB.delete("bool", null, null);
            }
        }

        c1.close();

        return name;
    }

    int findUser(User user){
        for (int i = 0; i < Users.size(); i++){
            if (Users.get(i).getName().equals(user.getName())){
                return i;
            }
        }
        return -1;
    }

    User getUserByName (String name){
        for (int i = 0; i < Users.size(); i++){
            if (Users.get(i).getName().equals(name)){
                return Users.get(i);
            }
        }
        return null;
    }



    public void sendMessage(View v){

        setBarVisible(true);
        String message = myMessage.getText().toString().trim();

        if (!message.equals("")) {
            Socket.emit("new message", message);


            if (getUserByName(myName) != null) {
                addToScroll(new Message(getUserByName(myName), message));
            } else {
                Log.d(LOG_TAG, "ДИЧЬ");
            }


            Log.d(LOG_TAG, "Message has sent.");

        }

        setBarVisible(false);
        myMessage.setText("");

    }

    public void addToScroll(final Message msg) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout l = (LinearLayout) inflater.inflate(R.layout.scroll_item, mainLin, false);

                ((TextView) l.findViewById(R.id.TVid)).setText(msg.getUser().getName());
                ((TextView) l.findViewById(R.id.TVvalue)).setText(msg.getMessage());


                if (msg.getUser().getName().equals(myName)) {
                    ((TextView) l.findViewById(R.id.TVid)).setText("Me");
                    ((LinearLayout) l.findViewById(R.id.scrollItem)).setGravity(Gravity.RIGHT);


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(100, 0, 0, 0);
                    (l.findViewById(R.id.innerLinear)).setLayoutParams(params);
                }

                Log.d(LOG_TAG, "Adding in Scroll");
                msg.setLayout(l);
                Messages.add(msg);
                mainLin.addView(msg.getLayout());
                scrollToBottom();

            }
        });
    }

    void setLogMassage(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userLog.setText(msg);
            }
        });
    }

    void scrollToBottom() {
        Scroll.post(new Runnable() {
            public void run() {
                Scroll.scrollTo(0, Scroll.getBottom());
            }
        });
    }

    void blockUI(){
        myMessage.setClickable(false);
        findViewById(R.id.sendMassage).setClickable(false);

    }

    void unblockUI(){
        myMessage.setClickable(true);
        findViewById(R.id.sendMassage).setClickable(true);
    }

    void setBarVisible(final boolean b){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (b ==true) {
                    Bar.setVisibility(View.VISIBLE);
                }else {
                    Bar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

}

