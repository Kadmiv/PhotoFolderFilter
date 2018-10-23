package photofiltercom.gaijin.photofolderfilter.events;

/**
 * Created by Kachulyak Ivan.
 */
public class NeedUpdate {


    int position = -1;

    public NeedUpdate() {
    }

    public NeedUpdate(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
