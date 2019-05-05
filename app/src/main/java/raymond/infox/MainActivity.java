package raymond.infox;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int UPDATE_REQUEST_CODE = 1;
    private static final int ADD_REQUEST_CODE    = 2;
    Database db;
    ArrayList<String> currentDisplayedEntries;
    ListView list;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        list = findViewById(R.id.entry_list);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEntry();
            }
        });
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = new Database(this);
        currentDisplayedEntries = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currentDisplayedEntries);
        list.setAdapter(adapter);
        fillEntries();
    }

    /**
     * Helper method that fills the main activity with item descriptions contained in the database
     */
    public void fillEntries() {
        currentDisplayedEntries.clear();
        for (Cursor c : db.getCategorizedData()) {
            ArrayList<String> depItems = new ArrayList<>();
            while (c.moveToNext()) {
                depItems.add("       " + c.getString(1));
            }
            String departmentHeader = c.getExtras().getString("department") + " - (" + depItems.size() + ")";
            currentDisplayedEntries.add(departmentHeader);
            currentDisplayedEntries.addAll(depItems);
        }
        ((ArrayAdapter) adapter).notifyDataSetChanged();
        list.invalidateViews();


        // Performs the long-click delete action
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                String name = parent.getItemAtPosition(position).toString().trim();
                Cursor data = db.getItem(name);
                if (data.moveToNext()) {
                    openDeleteDialogue(data);
                    return true;
                }
                return false;
            }
        });

        // Performs the click action, opens the edit activity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString().trim();
                Cursor data = db.getItem(name);
                if (data.moveToNext()) {
                    updateEntry(data.getString(0), data.getString(1),
                                data.getString(2), data.getString(3),
                                data.getString(4), data.getString(5));
                }
            }
        });
    }

    /**
     * When called, a dialogue will appear confirming the user if long clicked entry shall be deleted.
     *
     * @param data  The cursor element containing the entry in the database
     */
    protected void openDeleteDialogue(final Cursor data) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this entry?")
                .setCancelable(false)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        db.removeData(data.getString(0));
                        currentDisplayedEntries.remove("       " + data.getString(1));
                        ((ArrayAdapter) adapter).notifyDataSetChanged();
                        list.invalidateViews();
                        Toast.makeText(MainActivity.this, "Entry deleted!", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }

    /**
     * Opens a new Barcode Activity with supplied parameters. Called when an existing entry is selected
     *
     * @param barcode   Barcode of the item
     * @param desc      Description of the item
     * @param price     Last known price of the item
     * @param img       Path to an image of the item
     * @param size      Size (weight/size) of the item
     * @param dep       Department of the item
     */
    protected void updateEntry(String barcode, String desc, String price, String img, String size, String dep) {
        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra("code", barcode);
        intent.putExtra("desc", desc);
        intent.putExtra("price", price);
        intent.putExtra("imgPath", img);
        intent.putExtra("size", size);
        intent.putExtra("department", dep);
        intent.putExtra("requestCode", UPDATE_REQUEST_CODE);
        startActivityForResult(intent, UPDATE_REQUEST_CODE);
    }

    /**
     * Opens a blank Barcode Activity to be filled by the user
     */
    protected void addEntry() {
        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra("requestCode", ADD_REQUEST_CODE);
        startActivityForResult(intent, ADD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == CommonStatusCodes.SUCCESS) {
           fillEntries();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    currentDisplayedEntries.clear();
                    Cursor cursor = db.getMatchingData(newText);
                    while (cursor.moveToNext()) {
                        currentDisplayedEntries.add("       " + cursor.getString(1));
                    }
                    ((ArrayAdapter) adapter).notifyDataSetChanged();
                    list.invalidateViews();
                } else {
                    fillEntries();
                }
                return true;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fillEntries();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
