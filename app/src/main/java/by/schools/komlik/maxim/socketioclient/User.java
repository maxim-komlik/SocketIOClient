package by.schools.komlik.maxim.socketioclient;

import java.util.Random;

public class User{
    String name;
    String id;
    int numberOfImage;

    public User (String name, String id){
        this.name = name;
        this.id = id;

        numberOfImage = new Random().nextInt(5);
    }
    public void setId(String newId){
        this.id = newId;
    }
    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
    public int getNumberOfImage(){
        return numberOfImage;
    }


}
