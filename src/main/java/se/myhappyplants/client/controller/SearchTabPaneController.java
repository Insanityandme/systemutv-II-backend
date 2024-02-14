package se.myhappyplants.client.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import se.myhappyplants.client.model.*;
import se.myhappyplants.client.service.ServerConnection;
import se.myhappyplants.client.view.AutocompleteSearchField;
import se.myhappyplants.client.view.MessageBox;
import se.myhappyplants.client.view.PopupBox;
import se.myhappyplants.client.view.SearchPlantPane;
import se.myhappyplants.shared.Message;
import se.myhappyplants.shared.MessageType;
import se.myhappyplants.shared.Plant;
import se.myhappyplants.client.model.SetAvatar;
import se.myhappyplants.shared.PlantDetails;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Class that controls the logic of the "search"-tab
 * Created by: Christopher O'Driscoll
 * Updated by: Christopher O'Driscoll, 2021-05-14
 */

public class SearchTabPaneController {
    @FXML
    public ListView lstFunFacts;
    @FXML
    private MainPaneController mainPaneController;
    @FXML
    private Circle imgUserAvatar;
    @FXML
    private Label lblUsername;
    @FXML
    private Button btnSearch;
    @FXML
    private AutocompleteSearchField txtFldSearchText;
    @FXML
    private ComboBox<SortingOption> cmbSortOption;
    @FXML
    private ListView listViewResult;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    public ImageView imgFunFactTitle;
    @FXML
    public TextField txtNbrOfResults;

    private ArrayList<Plant> searchResults;
    private ArrayList<PlantDetails> plantDetailsList = new ArrayList<>();

    private Set<String> addedPlantIds = new HashSet<>();

    private String trefleApiKey;


    /**
     * Method to initialize the GUI
     * @throws IOException
     */
    //Tar hand om krav F.SI.1
    @FXML
    public void initialize() {
        //Tar hand om krav F.SI.1
        trefleApiKey = System.getenv("TREFLE_API_KEY");
        LoggedInUser loggedInUser = LoggedInUser.getInstance();
        lblUsername.setText(loggedInUser.getUser().getUsername());
        imgUserAvatar.setFill(new ImagePattern(new Image(SetAvatar.setAvatarOnLogin(loggedInUser.getUser().getEmail()))));
        cmbSortOption.setItems(ListSorter.sortOptionsSearch());
        showFunFact(LoggedInUser.getInstance().getUser().areFunFactsActivated());
    }

    /**
     * Method to message the right controller-class that the log out-button has been pressed
     * @throws IOException
     */
    public void setMainController(MainPaneController mainPaneController) {
        this.mainPaneController = mainPaneController;
    }
    /**
     * Method to set and display the fun facts
     * @param factsActivated boolean, if the user has activated the option to true
     */
    public void showFunFact(boolean factsActivated) {

        FunFacts funFacts = new FunFacts();
        if (factsActivated) {
            imgFunFactTitle.setVisible(true);
            lstFunFacts.setItems(funFacts.getRandomFact());
        }
        else {
            imgFunFactTitle.setVisible(false);
            lstFunFacts.setItems(null);
        }
    }

    /**
     * Method to add a plant to the logged in users library. Asks the user if it wants to add a nickname to the plant and receives a string if the answer is yes
     * @param plantAdd the selected plant to add
     */
    @FXML
    public void addPlantToCurrentUserLibrary(Plant plantAdd) {
        String plantNickname = plantAdd.getCommonName();

        int answer = MessageBox.askYesNo(BoxTitle.Add, "Do you want to add a nickname for your plant?");
        if (answer == 1) {
            do {
                plantNickname = MessageBox.askForStringInput("Add a nickname", "Nickname:");
            } while (plantNickname.trim().isEmpty());
        }

        PlantDetails plantDetails = getPlantDetails(plantAdd);
        mainPaneController.getMyPlantsTabPaneController().addPlantToCurrentUserLibrary(plantAdd, plantNickname, plantDetails);
    }


    /**
     * Method to show the search result on the pane
     */
    private void showResultsOnPane() {
        ObservableList<SearchPlantPane> searchPlantPanes = FXCollections.observableArrayList();
        for (Plant plant : searchResults) {
            searchPlantPanes.add(new SearchPlantPane(this, ImageLibrary.getLoadingImageFile().toURI().toString(), plant));
        }
        listViewResult.getItems().clear();
        listViewResult.setItems(searchPlantPanes);

        Task getImagesTask =
                new Task() {
                    @Override
                    protected Object call() {
                        long i = 1;
                        for (SearchPlantPane spp : searchPlantPanes) {
                            Plant Plant = spp.getPlant();
                            if (Plant.getImageURL().equals("")) {
                                spp.setDefaultImage(ImageLibrary.getDefaultPlantImage().toURI().toString());
                            }
                            else {
                                try {
                                    spp.updateImage(Plant.getImageURL());//Tar hand om krav F.SI.1
                                }
                                catch (IllegalArgumentException e) {
                                    spp.setDefaultImage(ImageLibrary.getDefaultPlantImage().toURI().toString());
                                }
                            }
                            updateProgress(i++, searchPlantPanes.size());
                        }
                            Text text = (Text) progressIndicator.lookup(".percentage");
                            if(text.getText().equals("90%") || text.getText().equals("Done")){
                                text.setText("Done");
                                progressIndicator.setPrefWidth(text.getLayoutBounds().getWidth());
                            }
                        return true;
                    }
                };
        Thread imageThread = new Thread(getImagesTask);
        progressIndicator.progressProperty().bind(getImagesTask.progressProperty());
        imageThread.start();
    }

    /**
     * Method to sent a message to the server to get the results from the database. Displays a message to the user that more info is on its way
     */

    //Tar hand om krav F.SI.1
    @FXML
    private void searchButtonPressed() {
        btnSearch.setDisable(true);
        txtFldSearchText.addToHistory();
        PopupBox.display(MessageText.holdOnGettingInfo.toString());

        String userSearch = txtFldSearchText.getText();
        userSearch = userSearch.replace(" ", "%20");

        URI uriPlants = URI.create("https://trefle.io/api/v1/plants/search?token=" + trefleApiKey + "&q=" + userSearch);

        HttpClient httpClient = HttpClient.newHttpClient();

        CompletableFuture<HttpResponse<String>> responsePlantsFuture = httpClient.sendAsync(HttpRequest.newBuilder()
                .uri(uriPlants)
                .build(), HttpResponse.BodyHandlers.ofString());

        responsePlantsFuture.thenRun(() -> {
            try {
                ArrayList<Plant> plants = parseJsonResponse(responsePlantsFuture.get().body());

                Message apiResponse = new Message(plants, true);

                if (apiResponse != null && apiResponse.isSuccess()) {
                    searchResults = apiResponse.getPlantArray();
                    Platform.runLater(() -> txtNbrOfResults.setText(searchResults.size() + " results"));
                    if (searchResults.size() == 0) {
                        progressIndicator.progressProperty().unbind();
                        progressIndicator.setProgress(100);
                        btnSearch.setDisable(false);
                        Platform.runLater(() -> listViewResult.getItems().clear());
                    } else {
                        Platform.runLater(() -> showResultsOnPane());
                    }
                } else {
                    Platform.runLater(() -> MessageBox.display(BoxTitle.Error, "Failed to process the server response."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MessageBox.display(BoxTitle.Error, "Failed to fetch data from the server."));
            } finally {
                Platform.runLater(() -> btnSearch.setDisable(false));
            }
        });
    }


    //Tar hand om krav F.SI.1
    private ArrayList<Plant> parseJsonResponse(String responseBody) {
        ArrayList<Plant> plants = new ArrayList<>();

        try {
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            if (jsonResponse.has("data")) {
                JsonArray data = jsonResponse.getAsJsonArray("data");

                for (JsonElement plantElement : data) {
                    JsonObject plantData = plantElement.getAsJsonObject();


                    String id = plantData.has("id") && !plantData.get("id").isJsonNull() ? plantData.get("id").getAsString() : null;


                    if (id != null && !addedPlantIds.contains(id)) {
                        String commonName = plantData.has("common_name") && !plantData.get("common_name").isJsonNull() ? plantData.get("common_name").getAsString() : "N/A";
                        String scientificName = plantData.has("scientific_name") && !plantData.get("scientific_name").isJsonNull() ? plantData.get("scientific_name").getAsString() : "N/A";
                        String family = plantData.has("family") && !plantData.get("family").isJsonNull() ? plantData.get("family").getAsString() : "N/A";
                        String imageUrl = plantData.has("image_url") && !plantData.get("image_url").isJsonNull() ? plantData.get("image_url").getAsString() : "No Image";

                        String genus = plantData.has("genus") && !plantData.get("genus").isJsonNull() ? plantData.get("genus").getAsString() : null;
                        int light = plantData.has("data") && plantData.getAsJsonObject("data").has("growth")
                                ? plantData.getAsJsonObject("data").getAsJsonObject("growth").has("light")
                                ? plantData.getAsJsonObject("data").getAsJsonObject("growth").get("light").getAsInt()
                                : 0
                                : 0;

                        int waterFrequency = plantData.has("water_frequency") && !plantData.get("water_frequency").isJsonNull() ? plantData.get("water_frequency").getAsInt() : 0;

                        Plant plant = new Plant(id, commonName, scientificName, family, imageUrl);
                        PlantDetails plantDetails = new PlantDetails(genus, scientificName, light, waterFrequency, family);
                        plantDetailsList.add(plantDetails);
                        plants.add(plant);


                        addedPlantIds.add(id);
                    }


                }
            } else {
                System.out.println("No 'data' found in the JSON response.");
            }


            return plants;
        } catch (Exception e) {
            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    //Tar hand om krav F.SI.1
    private int getIndexOfPlant(Plant plant) {
        for (int i = 0; i < searchResults.size(); i++) {
            if (searchResults.get(i).getPlantId().equals(plant.getPlantId())) {
                return i;
            }
        }
        return -1;
    }




    /**
     * Method to message the right controller-class that the log out-button has been pressed
     * @throws IOException
     */
    @FXML
    private void logoutButtonPressed() throws IOException {
        mainPaneController.logoutButtonPressed();
    }

    //Tar hand om krav F.SI.1
    public PlantDetails getPlantDetails(Plant plant) {
        PopupBox.display(MessageText.holdOnGettingInfo.toString());

        int index = getIndexOfPlant(plant);

        if (index != -1 && index < plantDetailsList.size()) {
            PlantDetails plantDetails = plantDetailsList.get(index);


            return plantDetails;
        } else {
            System.out.println("Details not found for the selected plant.");
            return null;
        }
    }

    /**
     * Method to rearranges the results based on selected sorting option
     */
    @FXML
    public void sortResults() {
        SortingOption selectedOption;
        selectedOption = cmbSortOption.getValue();
        listViewResult.setItems(ListSorter.sort(selectedOption, listViewResult.getItems()));
    }

    /**
     * Method to update the users avatar picture on the tab
     */
    public void updateAvatar() {
        imgUserAvatar.setFill(new ImagePattern(new Image(LoggedInUser.getInstance().getUser().getAvatarURL())));
    }
}
