package photofiltercom.gaijin.photofolderfilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kachulyak Ivan.
 * <p>
 * This class was created to work with objects of the class Task in the database
 */
public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {

    /*
    List with contains all task on RecyclerView, for quick repainting
     */
    ArrayList<File> taskArrayList = new ArrayList<>();

    /*
    The following two variables are needed to determine which of the Card we click in the RecyclerViewer
    */

    private OnItemClickListener TasksItemClickListener;
    private OnLongClickListener TasksOnLongClickListener;
    private static int width = 0;


    public TasksAdapter() {

    }

    public TasksAdapter(int width) {
        this.width = (int) ((width - width * 0.01) / 2);
    }

    public TasksAdapter(ArrayList<File> taskArrayList) {
        this.taskArrayList = taskArrayList;
    }

    public void setTaskArrayList(ArrayList<File> taskArrayList) {
        this.taskArrayList = taskArrayList;
    }


    @NonNull
    @Override
    public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_group, parent, false);
        TasksViewHolder holder = new TasksViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TasksViewHolder holder, final int position) {
        File file = taskArrayList.get(position);
        holder.name.setText(file.getName());

        if (isPicture(file.getName())) {
            // Load photo to view
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            int reqWidth = 100;
            int reqHeight = 100;
            // Resize images for quick load new activity and minimal memory use
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            holder.image.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath(), options));
        } else {
            if (!file.isDirectory()) {
                holder.image.setImageResource(R.drawable.file);
            } else {
                holder.image.setImageResource(R.drawable.folder);
            }
        }
        holder.container.setLayoutParams(new ViewGroup.LayoutParams(width, width));

        //holder.menuButton.setImageResource(task.getImage_id());
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

    public File getItem(int index) {
        return taskArrayList.get(index);
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener tasksItemClickListener) {
        this.TasksItemClickListener = tasksItemClickListener;
    }

    public interface OnLongClickListener {
        public void onLongClick(View view, int position);
    }

    public void SetOnLongClickListener(final OnLongClickListener tasksOnLongClickListener) {
        this.TasksOnLongClickListener = tasksOnLongClickListener;
    }

    public class TasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        /* These are the variables that are included in the Cardview*/
        TextView name;
        ImageView image;
        CardView card;
        ConstraintLayout container;

        public TasksViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.group_name);
            image = itemView.findViewById(R.id.imag_card);
            card = itemView.findViewById(R.id.group_card);
            container = itemView.findViewById(R.id.foder_card_layout);

            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
            image.setOnClickListener(this);
            image.setOnLongClickListener(this);
        }

        public String getName() {
            return (String) name.getText();
        }

        @Override
        public void onClick(View view) {

            if (TasksItemClickListener != null) {
                TasksItemClickListener.onItemClick(view, getAdapterPosition());
            }

        }

        @Override
        public boolean onLongClick(View view) {
            if (TasksOnLongClickListener != null) {
                TasksOnLongClickListener.onLongClick(view, getAdapterPosition());
            }
            return true;
        }
    }

    private static boolean isPicture(String file) {

        if (file.contains(".jpeg") || file.contains(".jpg") || file.contains(".png") || file.contains(".bmp")) {
            return true;
        } else
            return false;
    }
}
