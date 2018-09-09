package photofiltercom.gaijin.photofolderfilter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kachulyak Ivan.
 */
public class GroupActivity extends MyActivity implements View.OnClickListener, TasksAdapter.OnItemClickListener, TasksAdapter.OnLongClickListener, View.OnLongClickListener {

    /*Tags for floatButton addPhoto, for change bitmap of button*/
    private static final String CAMERA = "CAMERA";
    private static final String FOLDER = "FOLDER";
    /*Request code for intent filter*/
    private final int REQUEST_CODE_PHOTO = 123;

    /*Component for activity*/
    private FloatingActionButton addPhoto;
    private RecyclerView recyclerView;
    private TasksAdapter adapter;
    private RecyclerView.LayoutManager manager;

    /*Animation for part of app*/
    private Animation floatButtonAnimation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Component initialization*/
        setContentView(R.layout.second_activity);
        addPhoto = (FloatingActionButton) findViewById(R.id.add_picture);
        addPhoto.setOnClickListener(this);
        addPhoto.setOnLongClickListener(this);
        addPhoto.setTag(CAMERA);
        floatButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.float_button_anim);

        /*Get path to main folder from intent*/
        mainFolderPath = getIntent().getStringExtra(INTENT_PATH);

        createRecycleView();
    }

    /**
     * Function of recycle view creation
     */
    @MyAnnotation
    protected void createRecycleView() {
        /*Initialization of recycle view*/
        recyclerView = findViewById(R.id.picture_view);
        //manager = new LinearLayoutManager(this);
        recyclerView.addOnScrollListener(new CustomScrollListener());
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        adapter = new TasksAdapter();
        recyclerView.setAdapter(adapter);

        /*Load name of group folder*/
        File folder = new File(mainFolderPath);
        if (folder.exists()) {
            filesList = loadFilesAndFolders(folder.getAbsolutePath());
            if (!filesList.isEmpty()) {
                adapter = new TasksAdapter(filesList);
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
    @MyAnnotation(type = 2)
    protected void updateRecycleView() {
        filesList = loadFilesAndFolders(mainFolderPath);
        if (filesList.size() > 0) {
            adapter.setTaskArrayList(filesList);
            adapter.notifyDataSetChanged();
        } else {
            createRecycleView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (new File(mainFolderPath).exists()) {
            updateRecycleView();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // Creation of picture or folder
            case R.id.add_picture:
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
                break;
        }
    }

    /**
     * This function causes the intent of Camera from MediaStore
     *
     * @param path - path for saving photo
     * @param name - name of new photo
     */
    protected void makePhoto(String path, String name) {
        try {
            File file = new File(path, name);
            // Get uri for photo
            Uri mOutputFileUri = Uri.fromFile(file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Put uri to intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
            Log.d(LOG_TAG, "Put extra name = " + name);
            // This part of code need for normal work on android 6+
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            startActivityForResult(intent, REQUEST_CODE_PHOTO);
        } catch (ActivityNotFoundException e) {
            String errorMessage = "Error. Camera is not responding";
            Toast toast = Toast
                    .makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        /*Check of work camera intent*/
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    Log.d(LOG_TAG, "Intent is null");
                } else {
                    Log.d(LOG_TAG, "Photo uri: " + intent.getData());
                    Bundle bndl = intent.getExtras();
                    if (bndl != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) obj;
                            Log.d(LOG_TAG, "bitmap " + bitmap.getWidth() + " x "
                                    + bitmap.getHeight());
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(LOG_TAG, "Canceled");
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
                    Log.d(LOG_TAG, "Folder");
                    intent = new Intent(this, GroupActivity.class);
                    intent.putExtra(INTENT_PATH, filesList.get(position));
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Image");
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

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.add_picture:
                // Подключаем анимацию к нужному View
                FloatingActionButton floatingActionButton = findViewById(R.id.add_picture);
                //floatingActionButton.startAnimation(floatButtonAnimation);
                if (addPhoto.getTag() == CAMERA) {
                    addPhoto.setImageResource(R.drawable.folder);
                    addPhoto.setTag(FOLDER);
                } else {
                    addPhoto.setImageResource(R.drawable.camera);
                    addPhoto.setTag(CAMERA);
                }
                // Подключаем анимацию к нужному View
                Animation recursion = AnimationUtils.loadAnimation(this, R.anim.float_button_anim_2);
                floatingActionButton.startAnimation(recursion);

                break;

        }
        return true;
    }
}
