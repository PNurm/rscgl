package com.rscgl.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rscgl.Config;
import com.rscgl.assets.model.RSArchiveModel;
import com.rscgl.assets.util.Utility;

public class RSModels {

    private static RSArchiveModel[] models;

    public static void loadModels() {
        getModelIndex("torcha2");
        getModelIndex("torcha3");
        getModelIndex("torcha4");
        getModelIndex("skulltorcha2");
        getModelIndex("skulltorcha3");
        getModelIndex("skulltorcha4");
        getModelIndex("firea2");
        getModelIndex("firea3");
        getModelIndex("fireplacea2");
        getModelIndex("fireplacea3");
        getModelIndex("firespell2");
        getModelIndex("firespell3");
        getModelIndex("lightning2");
        getModelIndex("lightning3");
        getModelIndex("clawspell2");
        getModelIndex("clawspell3");
        getModelIndex("clawspell4");
        getModelIndex("clawspell5");
        getModelIndex("spellcharge2");
        getModelIndex("spellcharge3");

        FileHandle fileHandle = Gdx.files.local(Config.CACHE_DIR + "models36.jag");
        byte[] modelData = Utility.readDataFile(fileHandle);
        if (modelData == null) {
            throw new NullPointerException("Failed to load models");
        }
        models = new RSArchiveModel[modelCount];
        for (int j = 0; j < modelCount; j++) {
            int length = Utility.getDataFileOffset(modelName[j] + ".ob3", modelData);
            if (length != 0)
                models[j] = new RSArchiveModel(modelName[j], modelData, length);
            else
                models[j] = new RSArchiveModel(1, 1);
           /* if (RSCache.modelName[j].equals("giantcrystal"))
                jagModels[j].transparent = true;*/
        }


        //showLoadingProgress(70, "Loading 3d models");
        /*for (int i = 0; i < RSCache.modelCount; i++) {
            jagModels[i] = new RSArchiveModel("../gamedata/models/" + RSCache.modelName[i] + ".ob2");
            if (GameData.modelName[i].equals("giantcrystal"))
                jagModels[i].transparent = true;
        }*/
    }

    public static String[] modelName = new String[5000];
    public static int modelCount;

    public static int getModelIndex(String s) {
        if (s.equalsIgnoreCase("na")) {
            return 0;
        }
        for (int i = 0; i < modelCount; i++) {
            if (modelName[i].equalsIgnoreCase(s)) {
                return i;
            }
        }
        modelName[modelCount++] = s;
        return modelCount - 1;
    }

    public static RSArchiveModel[] getModels() {
        return models;
    }
}
