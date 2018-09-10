package photofiltercom.gaijin.photofolderfilter.folderbd;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Kachulyak Ivan.
 *
 * The interface, which includes methods for easy work with the database and the objects of the Universal folder
 */
@Dao
public interface IUniversalFolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UniversalFolder folder);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UniversalFolder folder);

    @Delete
    void delete(UniversalFolder folder);

    @Query("DELETE from universalfolder")
    void removeAll();

    @Query("SELECT * FROM universalfolder WHERE id=:id")
    UniversalFolder getById(int id);

    @Query("SELECT * FROM universalfolder WHERE folderName=:folderName")
    UniversalFolder getByName(String folderName);

    @Query("SELECT * FROM universalfolder")
    List<UniversalFolder> getAll();

}
