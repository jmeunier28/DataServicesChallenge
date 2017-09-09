package com.github.jmeunier.codechallenge;

/**
 * Created by jmeunier28 on 9/5/17.
 */
public class Client implements Runnable {

    public void run() {
        System.out.println("Hello from a thread!");
    }

    public static void main(String args[]) {
        (new Thread(new Client())).start();
        String threadName = Thread.currentThread().getName();
        System.out.println("Hello " + threadName);
        System.out.println("Done!");
    }


}
