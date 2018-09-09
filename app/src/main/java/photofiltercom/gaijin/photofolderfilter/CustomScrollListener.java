package photofiltercom.gaijin.photofolderfilter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by Kachulyak Ivan.
 *
 * This class need for animation of floating button
 */


// FIXME: 09.09.2018 THis class for future change

public class CustomScrollListener extends RecyclerView.OnScrollListener {
    public CustomScrollListener() {
    }

    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                Log.d("logM", "The RecyclerView is not scrolling");
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                Log.d("logM", "Scrolling now");
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                Log.d("logM", "Scroll Settling");
                break;

        }

    }

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dx > 0) {
            Log.d("logM", "Scrolled Right");
        } else if (dx < 0) {
            Log.d("logM", "Scrolled Left");
        } else {
            System.out.println("No Horizontal Scrolled");
        }

        if (dy > 0) {
            Log.d("logM", "Scrolled Downwards");
        } else if (dy < 0) {
            Log.d("logM", "Scrolled Upwards");
        } else {
            Log.d("logM", "No Vertical Scrolled");
        }
    }
}
