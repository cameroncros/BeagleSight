package com.cross.beaglesight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cross.beaglesight.fragments.BowListRecyclerViewAdapter;
import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
import com.cross.beaglesightlibs.XmlParser;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.cross.beaglesight.ShowSight.CONFIG_TAG;

public class SightList extends AppCompatActivity implements BowListRecyclerViewAdapter.OnListFragmentInteractionListener {
    private Intent addIntent;
    private List<BowConfig> configList = new ArrayList<>();
    ArrayList<BowConfig> selectedBowConfigs;
    private FloatingActionButton fab;

    private static final int FILE_SELECT_CODE = 0;
    private static final int IMPORT_FILES = 1;
    private static final int ADD_BOW = 2;
    private BowListRecyclerViewAdapter adapter;
    private BowManager bm;
    private boolean actionModeStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bm = BowManager.getInstance(this);

        addIntent = new Intent(this, AddSight.class);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(addIntent, ADD_BOW);
            }
        });

        RecyclerView view = findViewById(R.id.bowlistrecyclerview);
        // Set the adapter
        if (view != null) {
            Context context = view.getContext();
            view.setLayoutManager(new LinearLayoutManager(context));
            adapter = new BowListRecyclerViewAdapter(configList, this);
            view.setAdapter(adapter);
        }

        // Load bows
        updateBows();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sight_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_add:
                startActivityForResult(addIntent, ADD_BOW);
                return true;
            case R.id.action_import:
                String[] permissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
                ActivityCompat.requestPermissions(this, permissions, IMPORT_FILES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        switch (requestCode) {
            case IMPORT_FILES:
                importConfig();
                break;
        }
    }

    @Override
    public boolean onListFragmentInteraction(BowConfig bowConfig) {
        if (actionModeStarted)
        {
            if (selectedBowConfigs.contains(bowConfig))
            {
                selectedBowConfigs.remove(bowConfig);
                return false;
            }
            else
            {
                selectedBowConfigs.add(bowConfig);
                return true;
            }
        }
        else {
            Intent intent = new Intent(this, ShowSight.class);
            intent.putExtra(CONFIG_TAG, bowConfig);
            startActivity(intent);
            return false;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onListFragmentLongPress(BowConfig bowConfig) {
        if (!actionModeStarted) {
            selectedBowConfigs = new ArrayList<>();
            selectedBowConfigs.add(bowConfig);

            startActionMode(selectedActionMode);

            fab.setVisibility(View.GONE);
            fab.invalidate();
            return true;
        } else {
            return onListFragmentInteraction(bowConfig);
        }
    }

    private final ActionMode.Callback selectedActionMode = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_sight_list_selected, menu);

            adapter.enableMultiSelectMode(true);
            adapter.notifyDataSetChanged();

            actionModeStarted = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            final BowManager bm = BowManager.getInstance(getBaseContext());
            switch (item.getItemId()) {
                case R.id.action_delete:
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (BowConfig config : selectedBowConfigs)
                            {
                                bm.deleteBowConfig(config);
                            }
                            selectedBowConfigs = null;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mode.finish();
                                    updateBows();
                                }
                            });
                        }
                    });
                    break;
                case R.id.action_export:
                    File outputDir = getCacheDir();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.setType("text/xml");
                    ArrayList<Uri> uris = new ArrayList<>();

                    for (BowConfig config : selectedBowConfigs)
                    {
                        try {
                            String filename = config.getName();
                            if (filename.length() == 0) {
                                filename = config.getId();
                            }
                            File outputFile = File.createTempFile("BowConfig_" + filename, ".xml", outputDir);
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            XmlParser.serialiseSingleBowConfig(fos, config);
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, outputFile);
                            uris.add(uri);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    startActivity(Intent.createChooser(shareIntent, "Export Configs"));
                    selectedBowConfigs = null;
                    mode.finish();
                    break;
            }
            return true;
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.enableMultiSelectMode(false);

            fab.setVisibility(View.VISIBLE);
            fab.invalidate();

            actionModeStarted = false;
        }
    };

    private void updateBows() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                configList.clear();
                configList.addAll(bm.getAllBowConfigsWithPositions());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Uri uri = data.getData();

                                Log.d("BeagleSight", "File Uri: " + uri.toString());
                                // Get the path

                                File fname = new File(getRealPathFromURI(uri));

                                FileInputStream fis = new FileInputStream(fname);
                                BowConfig bowConfig = XmlParser.parseSingleBowConfigXML(fis);
                                bm.addBowConfig(bowConfig);
                                updateBows();
                            } catch (SAXException | ParserConfigurationException | IOException | NullPointerException ignored) {
                            }
                        }
                    });

                }
                break;
            case ADD_BOW:
                if(resultCode == RESULT_OK) {
                    String bowConfigId = data.getStringExtra(CONFIG_TAG);
                    Intent intent = new Intent(this, ShowSight.class);
                    intent.putExtra(CONFIG_TAG, bowConfigId);
                    startActivity(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void importConfig() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("file/*"); // intent type to filter application based on your requirement
        startActivityForResult(fileIntent, FILE_SELECT_CODE);
    }
}
