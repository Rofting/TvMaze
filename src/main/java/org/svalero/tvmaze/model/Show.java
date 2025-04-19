package org.svalero.tvmaze.model;

import java.awt.*;

public class Show {
    private int id;
    private String name;
    private String description;
    private String language;
    private String premiered;


    @SerializedName("image")
    private Image image;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLanguage() { return language; }
    public String getPremiered() { return premiered; }
    public Image getImage() { return image; }
}
