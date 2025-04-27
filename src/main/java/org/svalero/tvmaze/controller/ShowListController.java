package org.svalero.tvmaze.controller;

import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import javafx.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Collectors;

public class ShowListController {

    @FXML private TableView<Show> showTable;
    @FXML private TableColumn<Show, String> nameColumn;
    @FXML private TableColumn<Show, String> languageColumn;
    @FXML private TableColumn<Show, String> premieredColumn;
    @FXML private TableColumn<Show, ImageView> imageColumn;

    @FXML private TableView<Episode> episodeTable;
    @FXML private TableColumn<Episode, Integer> seasonColumn;
    @FXML private TableColumn<Episode, Integer> numberColumn;
    @FXML private TableColumn<Episode, String> episodeNameColumn;
    @FXML private TableColumn<Episode, String> airdateColumn;

    @FXML private TextField searchField;
    @FXML private TextField yearField;   // TextField para filtrar por año
    @FXML private Button exportButton;

    private final TVMazeService service = ApiClient.getApiService();
    private final ObservableList<Show> allShows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        premieredColumn.setCellValueFactory(new PropertyValueFactory<>("premiered"));

        seasonColumn.setCellValueFactory(new PropertyValueFactory<>("season"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        episodeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        airdateColumn.setCellValueFactory(new PropertyValueFactory<>("airDate"));

        imageColumn.setCellValueFactory(data -> {
            String imageUrl = data.getValue().getImage() != null ? data.getValue().getImage().getMedium() : null;
            ImageView imageView = new ImageView();
            if (imageUrl != null) {
                Image image = new Image(imageUrl, 100, 150, true, true);
                imageView.setImage(image);
            }
            return new SimpleObjectProperty<>(imageView);
        });

        // Cargar todas las series de la API
        service.getShows()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline())
                .subscribe(
                        shows -> {
                            allShows.setAll(shows);
                            cargarSeries(allShows);
                        },
                        error -> System.out.println("Error al cargar series: " + error)
                );

        showTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                cargarEpisodios(selected.getId());
            }
        });
    }

    private void cargarSeries(List<Show> shows) {
        javafx.application.Platform.runLater(() -> showTable.setItems(FXCollections.observableArrayList(shows)));
    }

    private void cargarEpisodios(int showId) {
        service.getEpisodes(showId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.trampoline())
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
            cargarSeries(allShows);
            return;
        }

        List<Show> filtered = allShows.stream()
                .filter(show -> show.getName() != null && show.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        cargarSeries(filtered);
    }

    @FXML
    public void onFilterByYear() {
        String yearText = yearField.getText();
        if (yearText == null || yearText.isEmpty()) {
            cargarSeries(allShows);
            return;
        }

        try {
            int year = Integer.parseInt(yearText);

            List<Show> filtered = allShows.stream()
                    .filter(show -> {
                        String premiered = show.getPremiered();
                        if (premiered != null && !premiered.isEmpty()) {
                            try {
                                int premieredYear = Integer.parseInt(premiered.substring(0, 4));
                                return premieredYear == year;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            cargarSeries(filtered);

        } catch (NumberFormatException e) {
            System.out.println("Año inválido: " + yearText);
        }
    }

    @FXML
    public void onExport(ActionEvent event) {
        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Path exportDir = Paths.get("export");
                    Files.createDirectories(exportDir);

                    Path csvFile = exportDir.resolve("series.csv");

                    try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
                        writer.write("ID,Nombre,Idioma,Estreno");
                        writer.newLine();

                        for (Show show : showTable.getItems()) {
                            String line = String.format("%d,%s,%s,%s",
                                    show.getId(),
                                    show.getName(),
                                    show.getLanguage(),
                                    show.getPremiered());
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                    Path zipFile = exportDir.resolve("series.zip");
                    try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                        zipOut.putNextEntry(new ZipEntry("series.csv"));
                        Files.copy(csvFile, zipOut);
                        zipOut.closeEntry();
                    }

                    System.out.println("Exportación completada con éxito");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Error al exportar: " + e.getMessage());
                }
                return null;
            }
        };

        Thread exportThread = new Thread(exportTask);
        exportThread.setDaemon(true);
        exportThread.start();
    }
}
