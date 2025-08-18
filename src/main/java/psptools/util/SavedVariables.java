package psptools.util;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import psptools.gson.PathTypeAdapter;
import psptools.psp.PSP;

public class SavedVariables {

    private transient static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
            .setPrettyPrinting().create();
            public static transient final Path DataFolder = Path.of(System.getProperty("user.home"), "/PSPTools/");
    static {
        if (!DataFolder.toFile().exists())
            try {
                Files.createDirectory(DataFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    public transient static final Path saveLocation = Path.of(DataFolder.toString(), "PSPToolsSettings.json");

    public PSP LastSelectedPSP;
    public URL DatabaseUrl;
    public Date SinceLastPatchUpdate;

    

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
