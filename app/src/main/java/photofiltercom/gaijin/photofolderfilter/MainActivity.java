package photofiltercom.gaijin.photofolderfilter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.PriorityQueue;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TasksAdapter.OnItemClickListener, TasksAdapter.OnLongClickListener {

    /**
     * Key for getting path to root user folder from shared preferences
     */
    private static final String MAIN_FOLDER = "MAIN_FOLDER";
    /**
     * Path to root folder for saving else folder group and files
     */
    private static final String ROOT_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DCIM;
    /* Key word for intent*/
    private static final String INTENT_PATH = "Path";
    /*Log tag*/
    private final String LOG_TAG = "MyLog";
    /*For log text, name of activity*/
    private String MAIN = " MainActivity";
    /*Variable of  root user folder . Sample - .../DCIM/MyFolder    */
    private static String mainFolderPath = "";
    /*List witch contains paths all files and folder in current folder. This list need for RecyclerView*/
    private static ArrayList<String> filesList = null;
    /*File of main folder*/
    private File folder;
    /*List of Permissions for user*/
    private static ArrayList<String> permissionsList;
    /*Preferences for load name of root user folder*/
    private SharedPreferences mainFolder;

    /*Component for activity*/
    private FloatingActionButton addGroup;
    private RecyclerView recyclerView;
    private TasksAdapter adapter;
    private RecyclerView.LayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Created permission list for checking permission
        permissionsList = createPremissionGroup();

        super.onCreate(savedInstanceState);
        //Log.d("logM", "onCreate" + MAIN);
        /*Component initialization*/
        setContentView(R.layout.activity_main);
        addGroup = (FloatingActionButton) findViewById(R.id.addGroup);
        addGroup.setOnClickListener(this);
        createRecycleView();
    }

    /**
     * Function of load permission to permission list
     */
    private ArrayList<String> createPremissionGroup() {
        ArrayList<String> permission = new ArrayList<>();
        permission.add(Manifest.permission.CAMERA);
        permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        return permission;
    }

    /**
     * Function of recycle view creation
     */
    private void createRecycleView() {

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
        adapter = new TasksAdapter(screenWidth);
        recyclerView.setAdapter(adapter);

        /*Load name of root user folder*/
        mainFolder = getPreferences(MODE_PRIVATE);
        mainFolderPath = mainFolder.getString(MAIN_FOLDER, "");
        folder = new File(mainFolderPath);
        /*Check of root user folder*/
        if (folder.exists()) {
            filesList = loadFolders(folder.getAbsolutePath());
            if (!filesList.isEmpty()) {
                adapter = new TasksAdapter(filesList);
                recyclerView.setAdapter(adapter);
            }
        }
        adapter.SetOnItemClickListener(this);
        adapter.SetOnLongClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //Log.d("logM", "OnStart" + MAIN);
        // Check permission and ask user
        checkPermission(permissionsList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("logM", "Resume" + MAIN);
        if (folder.exists()) {
            updateRecycleView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("logM", "Pause" + MAIN);
    }

    @Override
    protected void onStop() {
        scanningFolder(mainFolderPath);
        super.onStop();
        Log.d("logM", "Stop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("logM", "Restart" + MAIN);
    }

    @Override
    public void onClick(View v) {
        /*Check permissions */
        if (hasPermissions()) {
            // our app has permissions.
            switch (v.getId()) {
                case R.id.addGroup:
                    mainFolder = getPreferences(MODE_PRIVATE);
                    /*Check root user folder in DCIM. Create them is not exist*/
                    if (mainFolderPath == "") {
                        createFolder(this, "Enter name of main folder", 1);
                        //Toast.makeText(MainActivity.this, "Main folder was created in DCIM folder", Toast.LENGTH_LONG).show();
                    } else {
                        /*Creation of group folders*/
                        createFolder(this, "Enter new group", 2);
                    }
                    break;
            }
        } else {
            //our app doesn't have permissions, so we requesting permissions again.
            requestPermissionWithRationale();
        }
    }

    /**
     * Function of creation of new folder
     *
     * @param hintText     - text for hint
     * @param typeOfDialog - type of alert dialog (1 or 2 button)
     */
    private void createFolder(Context context, String hintText, int typeOfDialog) {

        /*Initialization view for alert dialog*/
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptsView = layoutInflater.inflate(R.layout.new_group, null);

        /*Creation of dialog*/
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(promptsView);

        /*Customize the display field for entering text in an open dialog*/
        final TextInputLayout nameLayout = (TextInputLayout) promptsView.findViewById(R.id.nameLayout);
        final EditText folderName = (EditText) promptsView.findViewById(R.id.groupName);
        nameLayout.setHint(hintText);

        /*Check type of dialog
         * If type is 1 - create root user folder
         * Else - create group folder in root user folder */
        if (typeOfDialog == 1) {
            dialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //Log.d("logM", "diagonal id" + id);
                                    String rootUserFolderName = (String) folderName.getText().toString();
                                    mainFolderPath = makeFolder(ROOT_FOLDER, rootUserFolderName);

                                    /*Change name of root user folder in Shared preferences*/
                                    SharedPreferences.Editor editor = mainFolder.edit();
                                    editor.putString(MAIN_FOLDER, mainFolderPath);
                                    editor.commit();
                                    //Log.d("logM", "Entered name = " + mainFolderPath);
                                    createRecycleView();
                                    dialog.cancel();
                                }
                            });
        } else {
            dialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String name = (String) folderName.getText().toString();
                                    String newFolder = makeFolder(mainFolderPath, name);
                                    //Log.d("logM", "New folder group " + newFolder);
                                    dialog.cancel();
                                    onResume();

                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
        }

        /*Create and shoe of alert dialog*/
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        /*After that we update the recycle view */
        while (!alertDialog.isShowing()) {
            Log.d("logM", "Alert dialog is showing");
            updateRecycleView();
        }

    }

    /**
     * Function of updating of recycle view
     * View will upload and new view card, if was be creater new folders or files
     */
    private void updateRecycleView() {
        filesList = loadFolders(mainFolderPath);
        if (filesList.size() > 0) {
            adapter.setTaskArrayList(filesList);
            adapter.notifyDataSetChanged();
        } else {
            createRecycleView();
        }
    }

    /**
     * Function of folder creation
     *
     * @param mainPath - main path to new folder
     * @param name     - name of new folder
     * @return - absolute path of folder
     */
    private String makeFolder(String mainPath, String name) {
        /*Create path for new folder*/
        String folderPath = mainPath + "/" + name + "/";
        File folder = new File(folderPath);
        Log.d("logM", "Path folder " + folderPath);
        /*Check folder on disc*/
        if (!folder.exists()) {
            folder.mkdir();
            Log.d("logM", "Folder is exist " + folder.getAbsolutePath());
        }
        return folder.getAbsolutePath();
    }

    /**
     * Function of loading all folders from specified path
     *
     * @param mainPath - specified path for check
     * @return - list of path for all folder in specified path folder
     */
    private ArrayList<String> loadFolders(String mainPath) {
        ArrayList<String> folderList = new ArrayList<>();
        //Log.d("logM", "Search files in folder = " + mainPath);
        for (File dir : new File(mainPath).listFiles()) {
            if (dir.isDirectory()) {
                folderList.add(dir.getAbsolutePath());
            }
        }
        return folderList;
    }

    /**
     * Function of loading all files and folders from specified path
     *
     * @param mainPath   - specified path for check
     * @param folderList - list for adding path
     */
    private static void loadFilesAndFolders(String mainPath, ArrayList<String> folderList) {
        Log.d("logM", "Search files in folder = " + mainPath);
        for (File file : new File(mainPath).listFiles()) {
            folderList.add(file.getAbsolutePath());
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.imag_card:
                /*This intent load new activity, witch open group folder */
                Intent intent = new Intent(this, GroupActivity.class);
                intent.putExtra(INTENT_PATH, filesList.get(position));
//                Log.d("logM", "Intent must be started");
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onLongClick(View view, int position) {

        switch (view.getId()) {
            case R.id.imag_card:
                //Toast.makeText(this, "clicked " + position, Toast.LENGTH_SHORT).show();
                showPopupMenu(this, view, position);
                break;
        }

    }

    /**
     * Function for view of folder settings
     */
    private void showPopupMenu(Context context, View view, int position) {
        PopupMenu popup = new PopupMenu(context, view); //Inflating the Popup using xml file
        Log.d(LOG_TAG, "Folder");
        popup.getMenuInflater().inflate(R.menu.folder_popup_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(GroupActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                switch (item.getTitle().toString()) {
                    case "Open":
                        //Log.d(LOG_TAG, "Open");
                        Intent intent = null;
                        //Log.d(LOG_TAG, "Folder");
                        intent = new Intent(MainActivity.this, GroupActivity.class);
                        intent.putExtra(INTENT_PATH, filesList.get(position));
                        startActivity(intent);
                        break;
                    case "Delete":
                        // Log.d(LOG_TAG, "Delete");
                        File folderForDelete = new File(filesList.get(position));
                        if (complexDeleting(folderForDelete)) {
                            Toast.makeText(MainActivity.this, folderForDelete.getName(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // FIXME: 09.09.2018 Next for future version
                    case "Select main and make photo":
                        Log.d(LOG_TAG, "Set Tag");
                        // DFS
                        break;
                    case "Tag Setting":
                        Log.d(LOG_TAG, "Set Tag");

                        break;
                    case "Copy Tag":
                        Log.d(LOG_TAG, "Set Tag");

                        break;
                    case "Past Tag":
                        Log.d(LOG_TAG, "Set Tag");

                        break;
                    default:

                }
                updateRecycleView();
                return true;
            }
        });
        //showing popup menu
        popup.show();
    }

    /**
     * Function of complex deleting folder (delete all files and folders inside main folder)
     *
     * @param mainFolder - folder for delete
     * @return - result of deleting operation
     */
    protected static boolean complexDeleting(File mainFolder) {
        try {
            Runnable dfs = () -> {
                dfsDelete(mainFolder);
            };
            Thread newTread = new Thread(null, dfs, "DFS", 100000);
            newTread.start();
            newTread.join(1000);
            mainFolder.delete();
            return true;
        } catch (InterruptedException e) {
            return false;
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
     * Function of depth-first search deleting file
     *
     * @param folder - start folder for delete and search
     */
    private static void dfsDelete(File folder) {
        ArrayList<String> filesInDir = new ArrayList<>();
        loadFilesAndFolders(folder.getAbsolutePath(), filesInDir);
        for (String path : filesInDir) {
            File dfsFile = new File(path);
            if (dfsFile.isDirectory()) {
                dfsDelete(dfsFile);
            }
            dfsFile.delete();
        }
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
                            if (shouldShowRequestPermissionRationale(permissionsList.get(1))) {
                                Toast.makeText(this, "Storage Permissions denied.", Toast.LENGTH_SHORT).show();

                            } else {
                                showNoStoragePermissionSnackbar(1);
                            }

                            break;
                        case 2:
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
            for (int i = 0; i < permissionsList.size(); i++)
                requestPermissions(new String[]{permissionsList.get(i)}, i);
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
    public void scanningFolder(String mainFolderPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(mainFolderPath));
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}

