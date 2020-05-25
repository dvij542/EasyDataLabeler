package com.example.easydatalabeler;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class UsernameIncorrect extends Exception{
    public UsernameIncorrect(String msg){
        super(msg);
    }
}
