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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TasksAdapter.OnItemClickListener, TasksAdapter.OnLongClickListener {

//    private static final int PERMISSION_REQUEST_CODE_STOREG = 123;
//    private static final int PERMISSION_REQUEST_CODE_CAMERA = 124;

    private static ArrayList<String> permissionsList;

    FloatingActionButton addGroup;
    private SharedPreferences mainFolder;
    private static final String MAIN_FOLDER = "MAIN_FOLDER";
    private static final String ROOT_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DCIM;
    private static String mainFolderPath = "";
    private static ArrayList<File> filesList = null;

    private String TAG = "logM";


    RecyclerView recyclerView;
    TasksAdapter adapter;
    RecyclerView.LayoutManager manager;

    private static String INTENT_PATH = "Path";
    private String MAIN = " MainActivity";
    private File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Created permission list for checking permission
        permissionsList = createPremissionGroup();

        super.onCreate(savedInstanceState);
        Log.d("logM", "onCreate" + MAIN);
        setContentView(R.layout.activity_main);
        addGroup = (FloatingActionButton) findViewById(R.id.addGroup);
        addGroup.setOnClickListener(this);

        PhotoTag tag = new PhotoTag();
        Log.d("logM", "Tag for photo = " + tag.transformTag("Text→someText↕FolderName↕Year↕Month↕Number↕Week↕Day↕hh↕mm↕ss", "/foldrs/One_Folder"));

        createRecicleView();
    }

    private ArrayList<String> createPremissionGroup() {
        ArrayList<String> permission = new ArrayList<>();
        permission.add(Manifest.permission.CAMERA);
        permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        return permission;
    }

    private void createRecicleView() {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;

        recyclerView = findViewById(R.id.group_view);
        //manager = new LinearLayoutManager(this);
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        adapter = new TasksAdapter(screenWidth);

        recyclerView.setAdapter(adapter);

        mainFolder = getPreferences(MODE_PRIVATE);

        mainFolderPath = mainFolder.getString(MAIN_FOLDER, "");
        folder = new File(mainFolderPath);
        if (folder.exists()) {
            filesList = loadFilesAndFolders(folder.getAbsolutePath());
            if (!filesList.isEmpty()) {
                adapter = new TasksAdapter(filesList);
                recyclerView.setAdapter(adapter);
            }
        }
//        else {
//            createFolder(this, "Enter name of main folder", 1);
//        }
        adapter.SetOnItemClickListener(this);
        adapter.SetOnLongClickListener(this);


    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("logM", "OnStart" + MAIN);
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

        if (hasPermissions()) {
            switch (v.getId()) {
                case R.id.addGroup:

                    mainFolder = getPreferences(MODE_PRIVATE);

                    // our app has permissions.
                    if (mainFolderPath == "") {

                        createFolder(this, "Enter name of main folder", 1);

                    } else {
                        createFolder(this, "Enter new group", 2);
                    }


                    break;
            }
        } else {
            //our app doesn't have permissions, So i m requesting permissions.
            requestPermissionWithRationale();
        }
    }

    private void createFolder(Context context, String hintTest, int dialogButton) {


        //Получаем вид с файла prompt.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.new_group, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final TextView extext = (TextView) promptsView.findViewById(R.id.groupName);
        extext.setHint(hintTest);
        // final EditText userInput = (EditText) promptsView.findViewById(R.id.input_text);

        //Настраиваем сообщение в диалоговом окне:
        if (dialogButton == 1) {
            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //Вводим текст и отображаем в строке ввода на основном экране:
                                    Log.d("logM", "diagonal id" + id);
                                    String name = (String) extext.getText().toString();
                                    mainFolderPath = makeFolder(ROOT_FOLDER, name);

                                    SharedPreferences.Editor editor = mainFolder.edit();
                                    editor.putString(MAIN_FOLDER, mainFolderPath);
                                    editor.commit();

                                    Log.d("logM", "Entered name = " + mainFolderPath);
                                    createRecicleView();
                                    dialog.cancel();
                                }
                            });
        } else {

            mDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //Вводим текст и отображаем в строке ввода на основном экране:
                                    String name = (String) extext.getText().toString();
                                    String newFolder = makeFolder(mainFolderPath, name);

                                    Log.d("logM", "New folder group " + newFolder);

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

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();


        //и отображаем его:
        alertDialog.show();

        while (!alertDialog.isShowing()) {
            Log.d("logM", "Alert dialog is showing");
            updateRecycleView();
        }

    }

    private void updateRecycleView() {
        filesList = loadFilesAndFolders(mainFolderPath);
        if (filesList.size() > 0) {
            adapter.setTaskArrayList(filesList);
            adapter.notifyDataSetChanged();
        }
    }

    private String makeFolder(String mainPath, String name) {
        String folderPath = mainPath + "/" + name + "/";
        // добавляем свой каталог к пути
        // проверка есть ли указаная папка на диске

        File folder = new File(folderPath);
        Log.d("logM", "Path folder " + folderPath);
        if (folder.exists() == false) {
            folder.mkdir();

            Log.d("logM", "Folder is exist " + folder.getAbsolutePath());
        }

        return folder.getAbsolutePath();
    }


    public static Intent createGetContentIntent() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); //задаем mime тип
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    private ArrayList<File> loadFilesAndFolders(String mainPath) {
        ArrayList<File> folderList = new ArrayList<>();
        Log.d("logM", "Search files in folder = " + mainPath);
        for (File dir : new File(mainPath).listFiles()) {
            if (dir.isDirectory()) {
                folderList.add(dir);
                //System.out.println(dir.getAbsolutePath() + " dir name " + dir.getName());
            }
        }
        return folderList;
    }


    @Override
    public void onItemClick(View view, int position) {

        switch (view.getId()) {
            case R.id.imag_card:
                // Toast.makeText(this, "clicked " + position, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, GroupActivity.class);
                intent.putExtra(INTENT_PATH, filesList.get(position).getAbsolutePath());
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
                PopupMenu popup = new PopupMenu(this, view); //Inflating the Popup using xml file

                Log.d(TAG, "Folder");
                popup.getMenuInflater().inflate(R.menu.folder_popup_menu, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //Toast.makeText(GroupActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        switch (item.getTitle().toString()) {
                            case "Select main and make photo":
                                Log.d(TAG, "Set Tag");
                                // Проходим по всем папка методом в глубину

                                // Если папка содержит тег МаинФолдер, тогда добавляем его в список

                                // Выводим чеклист со всем папками , которые имеют тег МаинФолдер.

                                // После этого открываем активити для фотографирования и делаем фотки
                                // с указаным тегом в указаную папку.

                                break;
                            case "Open":
                                Log.d(TAG, "Open");
                                Intent intent = null;
                                Log.d(TAG, "Folder");
                                intent = new Intent(MainActivity.this, GroupActivity.class);
                                intent.putExtra(INTENT_PATH, filesList.get(position).getAbsolutePath());
                                startActivity(intent);
                                break;
                            case "Delete":
                                Log.d(TAG, "Delete");
                                File fileForDelete = filesList.get(position);
                                Toast.makeText(MainActivity.this, fileForDelete.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                fileForDelete.delete();
                            case "Tag Setting":
                                Log.d(TAG, "Set Tag");

                                break;
                            case "Copy Tag":
                                Log.d(TAG, "Set Tag");

                                break;
                            case "Past Tag":
                                Log.d(TAG, "Set Tag");

                                break;
                            default:

                        }
                        updateRecycleView();
                        return true;
                    }
                });
                popup.show(); //showing popup men

                break;
        }

    }

    // This all method for permissions for user

    private void checkPermission(ArrayList<String> permissionsList) {

        String[] permissions = new String[permissionsList.size()];
        for (int i = 0; i < permissionsList.size(); i++) {
            permissions[i] = permissionsList.get(i);
        }

        for (int i = 0; i < permissionsList.size(); i++) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    permissionsList.get(i));
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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

    // This function need for normal work with folders and files after connection phone to PC with using USB
    public void scanningFolder(String mainFolderPath) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(mainFolderPath));
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}

