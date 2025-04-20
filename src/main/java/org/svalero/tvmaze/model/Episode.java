package org.svalero.tvmaze.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Episode {
    private int id;
    private String name;
    private String description;
    private int season;
    private int number;
    private String airDate;
}
