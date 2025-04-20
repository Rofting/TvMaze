package org.svalero.tvmaze.service;

import io.reactivex.rxjava3.core.Observable;
import org.svalero.tvmaze.model.Episode;
import org.svalero.tvmaze.model.SearchResult;
import org.svalero.tvmaze.model.Show;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface TVMazeService {

    @GET("shows")
    Observable<List<Show>> getShows();

    @GET("shows/{id}/episodes")
    Observable<List<Episode>> getEpisodes(@Path("id") int showId);

    @GET("search/shows")
    Observable<List<SearchResult>>searchShows(@Query("q")String query);
}
