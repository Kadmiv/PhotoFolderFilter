package photofiltercom.gaijin.photofolderfilter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import photofiltercom.gaijin.photofolderfilter.events.NeedCreate;
import photofiltercom.gaijin.photofolderfilter.events.NeedUpdate;

/**
 * Created by Kachulyak Ivan.
 * <p>
 * This Class contain same function which used in different activity class
 */
public class MyActivity extends AppCompatActivity {

    /**
     * Key for getting path to root user folder from shared preferences
     */
    protected static final String MAIN_FOLDER = "MAIN_FOLDER";

    /*Request code for intent filter*/
    protected final int REQUEST_CODE_PHOTO = 123;

    /**
     * Path to root folder for saving else folder group and files
     */
    private static final String ROOT_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DCIM;
    /* Key word for intent*/
    protected static final String INTENT_PATH = "Path";
    /*Variable of  root user folder . Sample - .../DCIM/MyFolder    */
    protected String mainFolderPath = "";
    /*Preferences for load name of root user folder*/
    protected SharedPreferences mainFolder;
    /*List which contains paths all files and folder in current folder. This list need for RecyclerView*/
    protected ArrayList<String> filesList = null;

    protected List<String> folderMenuItems = Arrays.asList("Open", "Delete", "Copy", "Past", "Settings");
    protected List<String> photoMenuItems = Arrays.asList("Open", "Delete", "Retake");


//    @Target(ElementType.METHOD)
//    @Retention(RetentionPolicy.RUNTIME)
//    public @interface MyAnnotation {
//        int type() default 1;
//    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Function of folder creation
     *
     * @param mainPath - main path to new folder
     * @param name     - name of new folder
     * @return - absolute path of folder
     */
    protected String makeFolder(String mainPath, String name) {
        /*Create path for new folder*/
        String folderPath = mainPath + "/" + name + "/";
        File folder = new File(folderPath);
        //Log.d(LOG_TAG, "Path folder " + folderPath);
        /*Check folder on disc*/
        if (!folder.exists()) {
            folder.mkdir();
            //Log.d(LOG_TAG, "Folder is exist " + folder.getAbsolutePath());
        }
        return folder.getAbsolutePath();
    }

    /**
     * Function of loading all folders from specified path
     *
     * @param mainPath - specified path for check
     * @return - list of path for all folder in specified path folder
     */
    protected ArrayList<String> loadFolders(String mainPath) {
        ArrayList<String> folderList = new ArrayList<>();
        //Log.d(LOG_TAG, "Search files in folder = " + mainPath);
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
    protected static void loadFilesAndFolders(String mainPath, ArrayList<String> folderList) {
        for (File file : new File(mainPath).listFiles()) {
            folderList.add(file.getAbsolutePath());
        }
    }

    /**
     * Function of loading all folders from specified path
     *
     * @param mainPath - specified path for check
     * @return - list of path for all folder in specified path folder
     */
    protected ArrayList<String> loadFilesAndFolders(String mainPath) {
        ArrayList<String> folderList = new ArrayList<>();
        //Log.d(LOG_TAG, "Search files in folder = " + mainPath);
        for (File file : new File(mainPath).listFiles()) {
            folderList.add(file.getAbsolutePath());
        }
        return folderList;
    }

    /**
     * Function for view of folder settings
     */
    protected void showPopupMenu(Object activity, View view, int position, ArrayList<String> filesList) {
        PopupMenu popup = new PopupMenu((Context) activity, view); //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.empty_popup_menu, popup.getMenu());
        File fileForCheck = new File(filesList.get(position));
        List<String> items = null;
        if (fileForCheck.isDirectory()) {
            //Log.d(LOG_TAG, "Folder");

            items = folderMenuItems;
        } else {
            //Log.d(LOG_TAG, "Image");

            items = photoMenuItems;
        }

        /*Load items to menu*/
        for (String item : items) {
            popup.getMenu().add(item);
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(GroupActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                switch (item.getTitle().toString()) {
                    case "Open":
                        //Log.d(LOG_TAG, "Open");
                        if (fileForCheck.isDirectory()) {
                            Intent intent = null;
                            //Log.d(LOG_TAG, "Folder");
                            intent = new Intent((Context) activity, GroupActivity.class);
                            intent.putExtra(INTENT_PATH, filesList.get(position));
                            startActivity(intent);
                        } else {
                            openPicture(fileForCheck.getAbsolutePath());
                        }
                        break;
                    case "Delete":
                        String infos = "";
                        // Search all file and folders in folder
                        if (fileForCheck.isDirectory()) {
                            if (MainActivity.complexDeleting(fileForCheck)) {
                                infos = String.format("Folder %s was delete", fileForCheck.getName());
                            }
                        } else {
                            fileForCheck.delete();
                            infos = String.format("File %s was delete", fileForCheck.getName());
                        }
                        Toast.makeText((Context) activity, infos, Toast.LENGTH_SHORT).show();
                        break;

                    case "Retake":

                        makePhoto(fileForCheck);
                        break;
                    // FIXME: 09.09.2018 Next for future version
                    case "Copy":
                        Log.d("12", "Copy");

                        break;
                    case "Past":
                        Log.d("12", "Past");

                        break;
                    case "Settings":
                        Log.d("12", "Setting");
                        Intent intent = null;
                        //Log.d(LOG_TAG, "Folder");
                        intent = new Intent((Context) activity, SettingFolderActivity.class);
                        intent.putExtra(INTENT_PATH, filesList.get(position));
                        startActivity(intent);
                        break;
                    default:

                }
                sendNeedUpdate();

                return true;
            }
        });
        //showing popup menu
        popup.show();
    }

    /**
     * Function of creation of new folder
     *
     * @param hintText     - text for hint
     * @param typeOfDialog - type of alert dialog (1 or 2 button)
     */
    protected void createFolder(Object activity, String hintText, int typeOfDialog) {

        /*Initialization view for alert dialog*/
        LayoutInflater layoutInflater = LayoutInflater.from((Context) activity);
        View promptsView = layoutInflater.inflate(R.layout.new_group, null);

        /*Creation of dialog*/
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder((Context) activity);
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

                                    //Log.d(LOG_TAG, "diagonal id" + id);
                                    String rootUserFolderName = (String) folderName.getText().toString();
                                    mainFolderPath = makeFolder(ROOT_FOLDER, rootUserFolderName);

                                    /*Change name of root user folder in Shared preferences*/
                                    SharedPreferences.Editor editor = mainFolder.edit();
                                    editor.putString(MAIN_FOLDER, mainFolderPath);
                                    editor.commit();
                                    //Log.d(LOG_TAG, "Entered name = " + mainFolderPath);
                                    sendNeedCreate();
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
                                    //Log.d(LOG_TAG, "New folder group " + newFolder);
                                    sendNeedUpdate();
                                    dialog.cancel();
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
        while (!alertDialog.isShowing()) {
            //Log.d("logM", "Alert dialog is showing");

        }

    }

    /**
     * This function causes the intent of Camera from MediaStore
     */
    protected void makePhoto(File file) {
        try {
            // Get uri for photo
            Uri mOutputFileUri = Uri.fromFile(file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Put uri to intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
            Log.d("12", "Put extra name = " + file.getName());
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


    /**
     * This function causes the intent of Camera from MediaStore
     *
     * @param path - path for saving photo
     * @param name - name of new photo
     */
    protected void makePhoto(String path, String name) {
        File file = new File(path, name);
        makePhoto(file);
    }


    /**
     * This function of opening photo in new intent on other app
     *
     * @param path -  path to photo
     */

    protected void openPicture(String path) {
        Uri mOutputFileUri = Uri.fromFile(new File(path));
        /*For normal work in android 6+*/
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(mOutputFileUri, "image/*");
        startActivity(intent);
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

    private void sendNeedUpdate() {
        EventBus.getDefault().post(new NeedUpdate());
    }

    private void sendNeedCreate() {
        EventBus.getDefault().post(new NeedCreate());
    }

    @Subscribe
    public void updateRecycleView(NeedUpdate needUpdate) {
    }

    @Subscribe
    public void createRecycleView(NeedCreate needCreate) {

    }

//    /**
//     * This function allows to run functions of other classes with certain annotations
//     *
//     * @param object         - object, which method will be run
//     * @param typeAnnotation - type number of annotation
//     */
//    void doAnnotationMethod(Object object, int typeAnnotation) {
//        Class classObject = object.getClass();
//        for (Method method : classObject.getDeclaredMethods()) {
//            MyAnnotation annotation = (MyAnnotation) method.getAnnotation(MyAnnotation.class);
//
//            if (annotation != null && (annotation.type() == typeAnnotation)) {
//                try {
//                    method.invoke(object);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    }

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
