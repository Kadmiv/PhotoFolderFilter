package photofiltercom.gaijin.photofolderfilter;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.File;
import java.net.URI;

/**
 * Created by Kachulyak Ivan.
 * This class will be connect to folder group and will contains same  method
 * and some specific method for specific work of creation of photo and ect.
 * Objects of this class will be contains of BD of application.
 */

@Entity
public class UniversalFolder extends File {

    @PrimaryKey(autoGenerate = true)
    int id;

    private String picturePath;
    private String tagFolder;

    public UniversalFolder(@NonNull String pathname) {
        super(pathname);
    }

    @Ignore
    public UniversalFolder(String parent, @NonNull String child) {
        super(parent, child);
    }

    @Ignore
    public UniversalFolder(File parent, @NonNull String child) {
        super(parent, child);
    }

    @Ignore
    public UniversalFolder(@NonNull URI uri) {
        super(uri);
    }

    private boolean renameFolder(String newName) {

        if (this.exists()) {
            this.renameTo(new java.io.File(newName));
            return true;
        } else {
            System.out.println("Folder not found!");
            return false;
        }
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public String getTagFolder() {
        return tagFolder;
    }

    public void setTagFolder(String tagFolder) {
        this.tagFolder = tagFolder;
    }
}
