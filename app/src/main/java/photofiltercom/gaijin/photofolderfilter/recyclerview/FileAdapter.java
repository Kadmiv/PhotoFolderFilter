package photofiltercom.gaijin.photofolderfilter.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import it.sephiroth.android.library.picasso.Picasso;
import photofiltercom.gaijin.photofolderfilter.R;

/**
 * Created by Kachulyak Ivan.
 * <p>
 * This class was created to work with objects of the class Task in the database
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    /*
    List with contains all task on RecyclerView, for quick repainting
     */
    ArrayList<String> fileArrayList = new ArrayList<>();

    /*
    The following two variables are needed to determine which of the Card we click in the RecyclerViewer
    */

    private OnItemClickListener ItemClickListener;
    private OnLongClickListener OnLongClickListener;
    //private static int width = 0;
    private Context context;


    public FileAdapter() {

    }

//    public FileAdapter(int width) {
//        this.width = (int) ((width - width * 0.01) / 2);
//    }

    public FileAdapter(ArrayList<String> taskArrayList, Context context) {
        this.fileArrayList = taskArrayList;
        this.context = context;
    }

    public void setFileArrayList(ArrayList<String> fileArrayList) {
        this.fileArrayList = fileArrayList;
        this.notifyDataSetChanged();
        Log.d("12", "notifyDataSetChanged()");
    }

    public void updateItem(int position) {

        this.notifyItemChanged(position);
        Log.d("12", "Position is update");

    }


    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        FileViewHolder holder = new FileViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FileViewHolder holder, final int position) {
        File file = new File(fileArrayList.get(position));
        holder.name.setText(file.getName());

        int reqWidth = 320;
        int reqHeight = 240;

        if (isPicture(file.getName())) {
            // Load photo to view
            Picasso.with(context)
                    .load(file)
                    .placeholder(R.drawable.file)
                    .resize(reqWidth, reqHeight)
                    .centerCrop()
                    .into(holder.image);
        } else {
            if (file.isDirectory()) {
                holder.image.setImageResource(R.drawable.folder);
            } else {
                holder.image.setImageResource(R.drawable.file);
            }
        }
//        holder.container.setLayoutParams(new ViewGroup.LayoutParams(width, width));

        //holder.menuButton.setImageResource(task.getImage_id());
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }

    public String getItem(int index) {
        return fileArrayList.get(index);
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener tasksItemClickListener) {
        this.ItemClickListener = tasksItemClickListener;
    }

    public interface OnLongClickListener {
        public void onLongClick(View view, int position);
    }

    public void SetOnLongClickListener(final OnLongClickListener tasksOnLongClickListener) {
        this.OnLongClickListener = tasksOnLongClickListener;
    }

    private static boolean isPicture(String file) {

        if (file.contains(".jpeg") || file.contains(".jpg") || file.contains(".png") || file.contains(".bmp")) {
            return true;
        } else
            return false;
    }

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        /* These are the variables that are included in the Cardview*/
        @BindView(R.id.group_name)
        TextView name;
        @BindView(R.id.imag_card)
        ImageView image;
        @BindView(R.id.group_card)
        CardView card;
        @BindView(R.id.foder_card_layout)
        ConstraintLayout container;

        public FileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public String getName() {
            return (String) name.getText();
        }

        @OnClick({R.id.imag_card, R.id.group_card})
        public void onClick(View view) {
            if (ItemClickListener != null) {
                ItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @OnLongClick({R.id.imag_card, R.id.group_card})
        public boolean onLongClick(View view) {
            if (OnLongClickListener != null) {
                OnLongClickListener.onLongClick(view, getAdapterPosition());
            }
            return true;
        }
    }
}
