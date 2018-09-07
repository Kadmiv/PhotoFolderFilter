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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kachulyak Ivan.
 */
public class GroupActivity extends AppCompatActivity implements View.OnClickListener, TasksAdapter.OnItemClickListener, TasksAdapter.OnLongClickListener, View.OnLongClickListener {

    FloatingActionButton addPhoto;

    private static final String MAIN_FOLDER = "MAIN_FOLDER";

    private static final String CAMERA = "CAMERA";
    private static final String FOLDER = "FOLDER";
    private static final String INTENT_PATH = "Path";

    RecyclerView recyclerView;
    TasksAdapter adapter;
    RecyclerView.LayoutManager manager;

    private String mainFolderPath = "";
    private ArrayList<File> filesList = null;
    private SharedPreferences mainFolder;
    private int REQUEST_CODE_PHOTO = 123;
    private String TAG = "logM";
    private File folder;

    private Animation floatButtonAnimation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        floatButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.float_button_anim);
        setContentView(R.layout.second_activity);
        addPhoto = (FloatingActionButton) findViewById(R.id.add_picture);
        addPhoto.setOnClickListener(this);
        addPhoto.setOnLongClickListener(this);
        addPhoto.setTag(CAMERA);

        mainFolderPath = getIntent().getStringExtra(INTENT_PATH);

        createRecycleView();


    }

    private void createRecycleView() {
        recyclerView = findViewById(R.id.picture_view);
        //manager = new LinearLayoutManager(this);
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        adapter = new TasksAdapter();
        recyclerView.setAdapter(adapter);

        mainFolder = getPreferences(MODE_PRIVATE);
        folder = new File(mainFolderPath);
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


    private void updateRecycleView() {
        filesList = loadFilesAndFolders(mainFolderPath + "/");
        if (filesList.size() > 0) {
            Log.d("logM", "Folder not empty ");
            adapter.setTaskArrayList(filesList);
            adapter.notifyDataSetChanged();
//            adapter = new TasksAdapter(filesList);
//            recyclerView.setAdapter(adapter);
//            adapter.SetOnItemClickListener(this);
//            adapter.SetOnLongClickListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        adapter.setTaskArrayList(loadFilesAndFolders(mainFolderPath));
//        adapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (folder.exists()) {
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
//        adapter.setTaskArrayList(loadFilesAndFolders(mainFolderPath));
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_picture:

                if (addPhoto.getTag() == CAMERA) {
                    makePhoto(mainFolderPath, Integer.toString((int) (Math.random() * 100000)) + ".jpg");
                } else {
                    createFolder(this, "Enter new group");
                    onResume();
                }

                break;

        }
    }

    private void makePhoto(String path, String name) {
        try {
            // Намерение для запуска камеры

            File file = new File(path, name);
            Uri mOutputFileUri = Uri.fromFile(file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
            Log.d("logM", "Put extra name = " + name);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            startActivityForResult(intent, REQUEST_CODE_PHOTO);
        } catch (ActivityNotFoundException e) {
            // Выводим сообщение об ошибке
            String errorMessage = "Возникла ошибка при запуске камеры";
            Toast toast = Toast
                    .makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    private void createFolder(Context context, String hintTest) {
        Environment.getExternalStorageState();

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


        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();


        //и отображаем его:
        alertDialog.show();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    Log.d(TAG, "Intent is null");
                } else {
                    Log.d(TAG, "Photo uri: " + intent.getData());
                    Bundle bndl = intent.getExtras();
                    if (bndl != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) obj;
                            Log.d(TAG, "bitmap " + bitmap.getWidth() + " x "
                                    + bitmap.getHeight());
                            //ivPhoto.setImageBitmap(bitmap);
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }
    }

    private ArrayList<File> loadFilesAndFolders(String mainPath) {
        ArrayList<File> folderList = new ArrayList<>();
        Log.d(TAG, "Search files in folder = " + mainPath);
        for (File file : new File(mainPath).listFiles()) {
            folderList.add(file);
            //System.out.println(dir.getAbsolutePath() + " dir name " + dir.getName());
        }
        return folderList;
    }

    @Override
    public void onItemClick(View view, int position) {

        switch (view.getId()) {
            case R.id.imag_card:
                //Toast.makeText(this, "clicked " + position, Toast.LENGTH_SHORT).show();
                Intent intent = null;
                if (filesList.get(position).isDirectory()) {
                    Log.d(TAG, "Folder");
                    intent = new Intent(this, GroupActivity.class);
                    intent.putExtra(INTENT_PATH, filesList.get(position).getAbsolutePath());
//                Log.d("logM", "Intent must be started");
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Image");
                    openPicture(position, intent);
                }
                break;
        }

    }

    private void openPicture(int position, Intent intent) {
        Uri mOutputFileUri = Uri.fromFile(filesList.get(position));
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(mOutputFileUri, "image/*");
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

        switch (view.getId()) {
            case R.id.imag_card:
                //Toast.makeText(this, "clicked " + position, Toast.LENGTH_SHORT).show();
                PopupMenu popup = new PopupMenu(this, view); //Inflating the Popup using xml file

                if (filesList.get(position).isDirectory()) {
                    Log.d(TAG, "Folder");
                    popup.getMenuInflater().inflate(R.menu.folder_popup_menu, popup.getMenu());
                } else {
                    Log.d(TAG, "Image");
                    popup.getMenuInflater().inflate(R.menu.file_popup_menu, popup.getMenu());
                }

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
                                if (filesList.get(position).isDirectory()) {
                                    Log.d(TAG, "Folder");
                                    intent = new Intent(GroupActivity.this, GroupActivity.class);
                                    intent.putExtra(INTENT_PATH, filesList.get(position).getAbsolutePath());
                                } else {
                                    openPicture(position, intent);
                                }
                                startActivity(intent);
                                break;
                            case "Delete":
                                Log.d(TAG, "Delete");
                                File fileForDelete = filesList.get(position);
                                Toast.makeText(GroupActivity.this, fileForDelete.getAbsolutePath(), Toast.LENGTH_SHORT).show();
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
