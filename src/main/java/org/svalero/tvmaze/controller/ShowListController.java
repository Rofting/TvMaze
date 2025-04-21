package org.svalero.tvmaze.controller;

import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.svalero.tvmaze.model.Episode;
import org.svalero.tvmaze.model.SearchResult;
import org.svalero.tvmaze.model.Show;
import org.svalero.tvmaze.service.ApiClient;
import org.svalero.tvmaze.service.TVMazeService;
import javafx.scene.image.Image;

import java.util.List;

public class ShowListController {

    @FXML private TableView<Show> showTable;
    @FXML private TableColumn<Show, String> nameColumn;
    @FXML private TableColumn<Show, String> languageColumn;
    @FXML private TableColumn<Show, String> premieredColumn;

    @FXML private TableView<Episode> episodeTable;
    @FXML private TableColumn<Episode, Integer> seasonColumn;
    @FXML private TableColumn<Episode, Integer> numberColumn;
    @FXML private TableColumn<Episode, String> episodeNameColumn;
    @FXML private TableColumn<Episode, String> airdateColumn;
    @FXML private TextField searchField;

    @FXML
    private TableColumn<Show, ImageView> imageColumn;

    private final TVMazeService service = ApiClient.getApiService();

    @FXML
    public void initialize() {
        // Configurar columnas
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        premieredColumn.setCellValueFactory(new PropertyValueFactory<>("premiered"));

        seasonColumn.setCellValueFactory(new PropertyValueFactory<>("season"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        episodeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        airdateColumn.setCellValueFactory(new PropertyValueFactory<>("airdate"));


        imageColumn.setCellValueFactory(data -> {
            String imageUrl = data.getValue().getImage() != null ? data.getValue().getImage().getMedium() : null;
            ImageView imageView = new ImageView();

            if (imageUrl != null) {
                Image image = new Image(imageUrl, 100, 150, true, true);
                imageView.setImage(image);
            }

            return new SimpleObjectProperty<>(imageView);
        });


        // Llamar a la API para cargar las series
        service.getShows()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline()) // Si da error aquí, cambia por Platform.runLater
                .subscribe(
                        this::cargarSeries,
                        error -> System.out.println("Error al cargar series: " + error)
                );

        // Evento de clic en una serie
        showTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                cargarEpisodios(selected.getId());
            }
        });
    }

    private void cargarSeries(List<Show> shows) {
        ObservableList<Show> showList = FXCollections.observableArrayList(shows);
        javafx.application.Platform.runLater(() -> showTable.setItems(showList));
    }

    private void cargarEpisodios(int showId) {
        service.getEpisodes(showId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline()) // Igual, puede necesitar Platform.runLater
                .subscribe(
                        episodes -> javafx.application.Platform.runLater(() -> {
                            episodeTable.setItems(FXCollections.observableArrayList(episodes));
                        }),
                        error -> System.out.println("Error al cargar episodios: " + error)
                );
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            return;
        }
        service.searchShows(query)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline())
                .subscribe(
                        searchResults -> {
                            List<Show> foundShows = searchResults.stream()
                                    .map(SearchResult::getShow)
                                    .toList();
                            javafx.application.Platform.runLater(() -> {
                                showTable.setItems(FXCollections.observableArrayList(foundShows));
                            });
                        },
                        error -> System.out.println("Error al cargar episodios: " + error)
                );
    }
}
