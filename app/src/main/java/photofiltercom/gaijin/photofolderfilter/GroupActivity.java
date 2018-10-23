package photofiltercom.gaijin.photofolderfilter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import photofiltercom.gaijin.photofolderfilter.events.NeedCreate;
import photofiltercom.gaijin.photofolderfilter.events.NeedUpdate;
import photofiltercom.gaijin.photofolderfilter.recyclerview.FileAdapter;

/**
 * Created by Kachulyak Ivan.
 */
public class GroupActivity extends MyActivity implements FileAdapter.OnItemClickListener, FileAdapter.OnLongClickListener {

    /*Tags for floatButton addPhoto, for change bitmap of button*/
    private static final String CAMERA = "CAMERA";
    private static final String FOLDER = "FOLDER";


    /*Component for activity*/
    @BindView(R.id.add_photo)
    FloatingActionButton addPhoto;
    @BindView(R.id.picture_view)
    RecyclerView recyclerView;

    private FileAdapter adapter;
    private RecyclerView.LayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Component initialization*/
        setContentView(R.layout.second_activity);
        ButterKnife.bind(this);
        addPhoto.setTag(CAMERA);

        /*Get path to main folder from intent*/
        mainFolderPath = getIntent().getStringExtra(INTENT_PATH);

        createRecycleView(new NeedCreate());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (new File(mainFolderPath).exists()) {
            updateRecycleView(new NeedUpdate());
        }
        // EventBus.getDefault().register(this);
    }

    /**
     * Function of recycle view creation
     */
    @Override
    public void createRecycleView(NeedCreate needCreate) {
        /*Initialization of recycle view*/
        recyclerView = findViewById(R.id.picture_view);
        //manager = new LinearLayoutManager(this);
        //recyclerView.addOnScrollListener(new CustomScrollListener());
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        adapter = new FileAdapter();
        recyclerView.setAdapter(adapter);

        /*Load name of group folder*/
        File folder = new File(mainFolderPath);
        if (folder.exists()) {
            filesList = loadFilesAndFolders(folder.getAbsolutePath());
            if (!filesList.isEmpty()) {
                adapter = new FileAdapter(filesList, this);
                recyclerView.setAdapter(adapter);
            }
        }

        adapter.SetOnItemClickListener(this);
        adapter.SetOnLongClickListener(this);
    }

    /**
     * Function of updating of recycle view
     * View will upload and new view card, if was be create new folders or files
     */
    @Override
    public void updateRecycleView(NeedUpdate needUpdate) {
        filesList = loadFilesAndFolders(mainFolderPath);
        if (filesList.size() > 0) {
            adapter.setFileArrayList(filesList);
        } else {
        createRecycleView( new NeedCreate());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        /*Check of work camera intent*/
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    Log.d("12", "Intent is null");
                } else {
                    Log.d("12", "Photo uri: " + intent.getData());
                    Bundle bndl = intent.getExtras();
                    if (bndl != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) obj;
                            Log.d("12", "bitmap " + bitmap.getWidth() + " x "
                                    + bitmap.getHeight());
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("12", "Canceled");
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        switch (view.getId()) {
            case R.id.imag_card:
                /*Open folder or photo*/
                Intent intent = null;
                File checkFile = new File(filesList.get(position));
                if (checkFile.isDirectory()) {
                    Log.d("12", "Folder");
                    intent = new Intent(this, GroupActivity.class);
                    intent.putExtra(INTENT_PATH, filesList.get(position));
                    startActivity(intent);
                } else {
                    Log.d("12", "Image");
                    openPicture(filesList.get(position));
                }
                break;
        }

    }

    @Override
    public void onLongClick(View view, int position) {
        switch (view.getId()) {
            case R.id.imag_card:
                showPopupMenu(this, view, position, filesList);
                break;
        }
    }


    @OnClick(R.id.add_photo)
    public void onAddPictureClicked() {
        if (addPhoto.getTag() == CAMERA) {
            PhotoTag tag = new PhotoTag();
            //Calculation of photo name
            String photoName = tag.transformTag("Text→FF_↕Year↕Month↕Day↕hh↕mm↕ss", mainFolderPath);
            //Log.d(LOG_TAG, "Tag for photo = " + photoName);
            makePhoto(mainFolderPath, photoName + ".jpg");
        } else {
            createFolder(this, "Enter new group", 2);
            onResume();
        }
    }

    @OnLongClick(R.id.add_photo)
    public boolean onAddPictureLongClicked() {

        if (addPhoto.getTag() == CAMERA) {
            addPhoto.setImageResource(R.drawable.folder);
            addPhoto.setTag(FOLDER);
        } else {
            addPhoto.setImageResource(R.drawable.camera);
            addPhoto.setTag(CAMERA);
        }
        /*Connect animation to floating button and run them*/
        Animation recursion = AnimationUtils.loadAnimation(this, R.anim.float_button_anim_2);
        addPhoto.startAnimation(recursion);

        return true;
    }
}
