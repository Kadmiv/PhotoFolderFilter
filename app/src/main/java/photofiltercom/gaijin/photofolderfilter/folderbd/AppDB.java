package photofiltercom.gaijin.photofolderfilter.folderbd;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


/**
 * Created by Kachulyak Ivan.
 * <p>
 * This is class of main database of app.
 */
@Database(entities = {UniversalFolder.class}, version = 1)
public abstract class AppDB extends RoomDatabase {

    private static AppDB INSTANCE;

    public abstract IUniversalFolderDao folderDao();

    public static AppDB getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, AppDB.class, "tag_database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
