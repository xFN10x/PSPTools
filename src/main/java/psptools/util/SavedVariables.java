package psptools.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import psptools.gson.PathTypeAdapter;
import psptools.psp.PSP;

public class SavedVariables {

    public transient static final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
            .setPrettyPrinting().create();
    public transient static final Path saveLocation = Path.of(System.getProperty("user.home"), "PSPToolsSettings.json");

    public PSP LastSelectedPSP;

    public void Save() {
        try {

            String json = gson.toJson(this);
            Files.write(saveLocation, json.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        } catch (Exception e) {
            ErrorShower.showError(null, "Failed to save settings.", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static SavedVariables Load() {
        try {

            if (saveLocation.toFile().exists())
                return gson.fromJson(Files.readString(saveLocation), SavedVariables.class);
            else
                return new SavedVariables();
        } catch (Exception e) {
            ErrorShower.showError(null, "Failed to get settings.", e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

}
