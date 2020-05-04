package com.RunTimeTerror.Musicorum.model;

import java.util.ArrayList;
import java.util.List;

public class Track {
    static public ArrayList<String> trackNames;//=new ArrayList<>();
    public String name;
    private int current;
    private List<Integer> notes;
    private int length;

    public Track() {
        this.notes = new ArrayList();
    }

    public Track(String str, List<Integer> list) {
        this();
        this.name = str;
        this.notes = list;
        this.length = list.size();
    }

    public int current() {
        if (hasNext()) {
            return ((Integer) this.notes.get(this.current)).intValue();
        }
        return -1;
    }

    public void next() {
        if (hasNext()) {
            this.current++;
        }
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public void reset() {
        this.current = 0;
    }

    public String getAsString(String seperator) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i + 1 < notes.size(); ++i) {
            ans.append(notes.get(i)).append(seperator);
        }
        ans.append(notes.get(notes.size() - 1));
        return ans.toString();
    }

    private boolean hasNext() {
        return this.current < this.notes.size();
    }
    /*static{
        trackNames.add("papa_noel");
        trackNames.add("clair_lune");
        trackNames.add("frere_jacques");
        trackNames.add("meunier_dors");
        trackNames.add("fais_dodo");
        trackNames.add("hymne_joie");
        trackNames.add("la_marseillaise");
        trackNames.add("claire_fontaine");
        trackNames.add("happy_birthday");
        trackNames.add("bon_tabac");
        trackNames.add("merry_christmas");
        trackNames.add("when_saints");
        trackNames.add("love_tender");
        trackNames.add("jingle_bells");
    }*/
}
