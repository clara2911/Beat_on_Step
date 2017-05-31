package com.example.clara.accelerometer;

/*
MusicLib.java
editor: Jelle Manders
date: 14/02/2016
---------------------
The Class MusicLib checks for a file called lib.ser
on initialisation and deserialises the hashmap
containing the previous bpms and corresponding song-
titles if found. If not, it instantiates a new hashmap
that contains per key(int bpm) a value(ArrayList<String>
songtitles). The arrayList ensures multiple songs with
equal bpm can be stored.

functions:
- init MusicLib
- void newSong
- void serLibrary
*/

import java.util.*;
import java.io.*;

public class MusicLib {
    private static File folder = new File("C:/Users/Jelle/Documents/2016_UvA Kunstmatige Intelligentie/Media Understanding/Bitfit/Music");
    private static final int UPPER_BOUND = 240;
    private static final int LOWER_BOUND = 40;
    private static final double close = 0.03;

    private HashMap<Integer, ArrayList<String>> lib;

    // init function, check for existing library
    // if not found, declare new HashMap
    public MusicLib() {
        lib = null;
        try {
            FileInputStream fileIn = new FileInputStream("lib.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            lib = (HashMap) in.readObject();
            in.close();
            fileIn.close();
        } catch(Exception i) {
            System.out.println("No library found, initialising new...");
            lib = new HashMap<Integer, ArrayList<String>>();
        }
    }

    // enter new song into music library
    public void newSong(int bpm, String title) {
        /* TOEGEVEOGD */
        title = bpm + " " + title;
        if(lib.containsKey(bpm)) {
            ArrayList<String> songlist = lib.get(bpm);
            if(!songlist.contains(title)) {
                songlist.add(title);
            } else {
                System.out.println("Song duplicate found, rename " + title);
            }
        } else {
            ArrayList<String> songlist = new ArrayList<String>();
            songlist.add(title);
            lib.put(bpm, songlist);
        }
        System.out.println("Added "+title);
    }

    // serialize current library into file "lib.ser"
    public void serLibrary() {
        try {
            FileOutputStream fileOut = new FileOutputStream("lib.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(lib);
            out.close();
            fileOut.close();
            System.out.println("Library succesfully serialized...");
        } catch(IOException i) {
            i.printStackTrace();
        }
    }

    // get a shuffled list of songs with a specified bpm
    public ArrayList<String> getSonglist(int bpm) {
        ArrayList<String> songs = lib.get(bpm);
        if(songs == null) {
            ArrayList tempResult;
            ArrayList close;
            int closeSize;
            System.out.println("MusicLib.java->get_songlist():\n  No songs with specified bpm found, looking for alternatives...");
            ArrayList<Integer> mults = multiples(bpm);
            int multSize = mults.size();
            for(int i = 0; i < multSize; i++) {
                close = closeEnough(mults.get(i));
                closeSize = close.size();
                for (int j = 0; j < closeSize; j++) {
                    //System.out.println("trying "+close.get(j)+"...");
                    tempResult = lib.get(close.get(j));
                    if(tempResult != null) {
                        System.out.println("Found alternative: " + close.get(j) + " bpm");
                        return shuffle(tempResult);
                    }
                }
            }
            System.out.println("Found no alternatives, shutting down.");
            ArrayList empty = new ArrayList<Integer>();
            return empty;
        }
        return shuffle(songs);
    }

    // shuffle arraylist according to Fisher-Yates algorithm
    private ArrayList<String> shuffle(ArrayList<String> list) {
        int size = list.size();
        if(size == 1) {
            return list;
        }
        int rand;
        String temp;
        Random r = new Random();
        for (int i = 0; i < size-2; i++) {
            rand = r.nextInt(((size-1)-i)+1)+i;
            temp = list.get(i);
            list.set(i, list.get(rand));
            list.set(rand, temp);
        }
        return list;
    }

    // return a list of integers within *close* fraction of value
    private ArrayList closeEnough(int value) {
        ArrayList result = new ArrayList<Integer>();
        int upperBound = (int) (value * (1+close));
        int lowerBound = (int) (value * (1-close));
        int currentUp = value;
        int currentDown = value;
        boolean searchingUp = true;
        boolean searchingDown = true;
        result.add(value);
        while(true) {
            if(searchingUp) {
                currentUp += 1;
                if(currentUp <= upperBound) {
                    result.add(currentUp);
                } else {
                    searchingUp = false;
                }
            }
            if(searchingDown) {
                currentDown -= 1;
                if(currentDown >= lowerBound) {
                    result.add(currentDown);
                } else {
                    searchingDown = false;
                }
            }
            if(!(searchingUp || searchingDown)) {
                return result;
            }
        }
    }

    // return a list of multiples of value within bounds *UPPER/LOWER_BOUND*
    private ArrayList multiples(int value) {
        ArrayList result = new ArrayList<Integer>();
        result.add(value);
        int times = 1;
        int divided = 1;
        boolean searchingUp = true;
        boolean searchingDown = true;
        while(true) {
            if(searchingUp) {
                times *= 2;
                if(value * times <= UPPER_BOUND) {
                    result.add(value*times);
                } else {
                    searchingUp = false;
                }
            }
            if(searchingDown) {
                divided *= 2;
                if(value / divided >= LOWER_BOUND) {
                    result.add(value/divided);
                } else {
                    searchingDown = false;
                }
            }
            if(!(searchingUp || searchingDown)) {
                return result;
            }
        }
    }
}