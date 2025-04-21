package org.svalero.tvmaze.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Show {
    private int id;
    private String name;
    private String description;
    private String language;
    private String premiered;

    private ShowImage image;
}
