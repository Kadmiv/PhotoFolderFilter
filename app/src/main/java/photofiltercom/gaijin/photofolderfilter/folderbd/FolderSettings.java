package photofiltercom.gaijin.photofolderfilter.folderbd;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Kachulyak Ivan.
 */
public class FolderSettings extends RealmObject {

    @PrimaryKey
    int id;
    String fullPath;
    private String folderName;
    private String picturePath;
    private String tagFolder;

    public FolderSettings() {
    }

    public FolderSettings(@NonNull String fullPath) {
        this.fullPath = fullPath;
    }
//
//    public FolderSettings(String parent, @NonNull String child) {
//        this(parent, child);
//    }
//
//    public FolderSettings(File parent, @NonNull String child) {
//        super(parent, child);
//    }
//
//    public FolderSettings(@NonNull URI uri) {
//        super(uri);
//    }

//    private boolean renameFolder(String newName) {
//
//        if (this.exists()) {
//            this.renameTo(new java.io.File(newName));
//            return true;
//        } else {
//            System.out.println("Folder not found!");
//            return false;
//        }
//    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getTagFolder() {
        return tagFolder;
    }

    public void setTagFolder(String tagFolder) {
        this.tagFolder = tagFolder;
    }
}
