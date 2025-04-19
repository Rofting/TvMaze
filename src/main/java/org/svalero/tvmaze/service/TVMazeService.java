package org.svalero.tvmaze.service;

import io.reactivex.rxjava3.core.Observable;
import org.svalero.tvmaze.model.Show;
import retrofit2.http.GET;

import java.util.List;

public interface TVMazeService {

    @GET("shows")
    Observable<List<Show>> getShows();
}
