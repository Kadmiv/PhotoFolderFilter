package photofiltercom.gaijin.photofolderfilter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import photofiltercom.gaijin.photofolderfilter.folderbd.AppDB;

public class MainActivity extends MyActivity implements FileAdapter.OnItemClickListener, FileAdapter.OnLongClickListener {

    /*For log text, name of activity*/
    private String MAIN = " MainActivity";

    /*List of Permissions for user*/
    private static ArrayList<String> permissionsList;


    /*Component for activity*/
    @BindView(R.id.group_view)
    RecyclerView groupView;
    @BindView(R.id.addGroup)
    FloatingActionButton addGroup;

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private RecyclerView.LayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Created permission list for checking permission
        permissionsList = createPermissionGroup();

        super.onCreate(savedInstanceState);
        //Log.d(LOG_TAG, "onCreate" + MAIN);
        /*Component initialization*/
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        createRecycleView();
        appDatabase = AppDB.getDatabase(getApplicationContext());
    }

    /**
     * Function of load permission to permission list
     */
    private ArrayList<String> createPermissionGroup() {
        ArrayList<String> permission = new ArrayList<>();
        permission.add(Manifest.permission.CAMERA);
        permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        return permission;
    }

    /**
     * Function of recycle view creation
     */
    @MyAnnotation
    protected void createRecycleView() {

        /*Get display metrics for calculation of normal view*/
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;

        /*Initialization of recycle view*/
        recyclerView = findViewById(R.id.group_view);
        recyclerView.addOnScrollListener(new CustomScrollListener());
        //manager = new LinearLayoutManager(this); //fixme Just in case
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        /*Initialization of adapter for  recycle view*/
        adapter = new FileAdapter(screenWidth);
        recyclerView.setAdapter(adapter);

        /*Load name of root user folder*/
        mainFolder = getPreferences(MODE_PRIVATE);
        mainFolderPath = mainFolder.getString(MAIN_FOLDER, "");
        File folder = new File(mainFolderPath);
        /*Check of root user folder*/
        if (folder.exists()) {
            filesList = loadFolders(folder.getAbsolutePath());
            if (!filesList.isEmpty()) {
                adapter = new FileAdapter(filesList);
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
        filesList = loadFolders(mainFolderPath);
        if (filesList.size() > 0) {
            adapter.setFileArrayList(filesList);
            adapter.notifyDataSetChanged();
        } else {
            createRecycleView();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check permission and ask user
        checkPermission(permissionsList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Resume" + MAIN);
        if (new File(mainFolderPath).exists()) {
            updateRecycleView();
        }
    }

    @Override
    protected void onStop() {
        scanningFolder(mainFolderPath);
        super.onStop();
        Log.d(LOG_TAG, "Stop");
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.imag_card:
                /*This intent load new activity, which open group folder */
                Intent intent = new Intent(this, GroupActivity.class);
                intent.putExtra(INTENT_PATH, filesList.get(position));
//                Log.d(LOG_TAG, "Intent must be started");
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onLongClick(View view, int position) {

        switch (view.getId()) {
            case R.id.imag_card:
                //Toast.makeText(this, "clicked " + position, Toast.LENGTH_SHORT).show();
                showPopupMenu(this, view, position, filesList);
                break;
        }

    }

    @OnClick(R.id.group_view)
    public void onGroupViewClicked() {
    }

    @OnClick(R.id.addGroup)
    public void onAddGroupClicked() {
        if (hasPermissions()) {

            mainFolder = getPreferences(MODE_PRIVATE);
            /*Check root user folder in DCIM. Create them is not exist*/
            if (mainFolderPath == "") {
                createFolder(this, "Enter name of main folder", 1);
            } else {
                /*Creation of group folders*/
                createFolder(this, "Enter name of group folder", 2);
            }
        } else {
            //our app doesn't have permissions, so we requesting permissions again.
            requestPermissionWithRationale();
        }
    }

    @OnClick(R.id.activity_view)
    public void onActivityViewClicked() {
    }


    // This all method for permissions for user
    private void checkPermission(ArrayList<String> permissionsList) {
        //Write permissions to array
        String[] permissions = permissionsList.toArray(new String[permissionsList.size()]);

        for (int i = 0; i < permissions.length; i++) {
            //Check permissions
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    permissions[i]);
            //View permissions to user
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        boolean allowed = true;
        for (int res : grantResults) {
            // if user granted all permissions.
            allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);

            if (!allowed)
                // we will give warning to user that they haven't granted permissions.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    switch (res) {
                        case 0:
                            if (shouldShowRequestPermissionRationale(permissionsList.get(0))) {
                                Toast.makeText(this, "Camera Permissions denied.", Toast.LENGTH_SHORT).show();

                            } else {
                                showNoStoragePermissionSnackbar(0);
                            }
                            break;

                        case 1:
                        case 2:
                            if (shouldShowRequestPermissionRationale(permissionsList.get(1))) {
                                Toast.makeText(this, "Storage Permissions denied.", Toast.LENGTH_SHORT).show();

                            } else {
                                showNoStoragePermissionSnackbar(1);
                            }
                            if (shouldShowRequestPermissionRationale(permissionsList.get(2))) {
                                Toast.makeText(this, "Storage Permissions denied.", Toast.LENGTH_SHORT).show();

                            } else {
                                showNoStoragePermissionSnackbar(2);
                            }

                            break;
                    }

                }
        }
    }

    /**
     * This function check, has program permission or not
     */
    private boolean hasPermissions() {
        int res = 0;
        //string array of permissions,

        for (String perms : permissionsList) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissionsList.size(); i++) {
                requestPermissions(new String[]{permissionsList.get(i)}, i);
            }
        }
    }

    // This function for permission was be denny always.
    public void showNoStoragePermissionSnackbar(int permissionInditificator) {
        Snackbar.make(MainActivity.this.findViewById(R.id.activity_view), "Storage permission isn't granted", Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openApplicationSettings(permissionInditificator);

                        Toast.makeText(getApplicationContext(),
                                "Open Permissions and grant the Storage permission",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .show();
    }

    /*This function open App Setting for change permission for app*/
    public void openApplicationSettings(int permissionInditificator) {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, permissionInditificator);
    }

    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            final String message = "Storage permission is needed to show files count";
            Snackbar.make(MainActivity.this.findViewById(R.id.activity_view), message, Snackbar.LENGTH_LONG)
                    .setAction("GRANT", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPerms();
                        }
                    })
                    .show();
        } else {
            requestPerms();
        }
    }

    /**
     * Function of scanning all files and folder in root user folder
     * This function need for normal work with folders and files after connection phone to PC with using USB
     *
     * @param mainFolderPath - path to folder for scanning
     */
    protected void scanningFolder(String mainFolderPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(mainFolderPath));
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}

