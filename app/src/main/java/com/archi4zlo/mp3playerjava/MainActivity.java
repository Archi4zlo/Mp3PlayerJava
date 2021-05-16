package com.archi4zlo.mp3playerjava;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;
    private int RECORD_PERMISSION_CODE = 2;
    private boolean permissionActive = false;
    private boolean permissionRecordActive = false;

    ListView listView;
    String[] items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listViewSong);
//        callStoragePermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        callRecordPermission();
        callStoragePermission();
//        if (permissionActive) {
//            if (permissionRecordActive){
//                Toast.makeText(this,"aaaaaaaaaaaaa",Toast.LENGTH_SHORT).show();
//                displaySong();
//            }
//            else {
//                callRecordPermission();
//                Toast.makeText(this,"2222222222",Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            callStoragePermission();
//            Toast.makeText(this,"333333333",Toast.LENGTH_SHORT).show();
//        }
        if (permissionActive && permissionRecordActive) {
            displaySong();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionActive && permissionRecordActive) {
            displaySong();
        }

    }
    private void callRecordPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) ==PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(MainActivity.this,"we have granted permission record",Toast.LENGTH_SHORT).show();
            permissionRecordActive = true;
        }
        else requestRecordPermission();
    }
    private void requestRecordPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)){
            new AlertDialog.Builder(this)
                    .setTitle("Record permission is needed")
                    .setMessage("This permission is needed to visualizer")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_CODE);
                            permissionRecordActive = true;
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            permissionRecordActive = false;
                        }
                    })
                    .create()
                    .show();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_CODE);
        }
    }


    private void callStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this,"we have granted permission",Toast.LENGTH_SHORT).show();
            permissionActive = true;
        } else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Storage permission needed")
                    .setMessage("This permission is needed to find music on your phone")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            permissionActive = true;
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            permissionActive = false;
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                permissionActive = true;

            } else {
//                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                permissionActive = false;
            }
        }
        if (requestCode == RECORD_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission record GRANTED", Toast.LENGTH_SHORT).show();
                permissionRecordActive = true;

            } else {
//                Toast.makeText(this, "Permission record DENIED", Toast.LENGTH_SHORT).show();
                permissionRecordActive = false;
            }
        }
    }

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        for (File singlefile : files) {
            if (singlefile.isDirectory() && !singlefile.isHidden()) {
                arrayList.addAll(findSong(singlefile));
            } else {
                if (singlefile.getName().endsWith(".mp3")
//                        || singlefile.getName().endsWith(".wav")
                ) {
                    arrayList.add(singlefile);
                }
            }
        }
        return arrayList;
    }

    void displaySong() {
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }
//        ArrayAdapter<String> myAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
//        listView.setAdapter(myAdaptor);

        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) listView.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songName",songName)
                        .putExtra("position",position));
            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textSong = myView.findViewById(R.id.txtSongName);
            textSong.setSelected(true);
            textSong.setText(items[position]);

            return myView;
        }
    }
}