package io.royaloaklabs.supergenki.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import io.royaloaklabs.supergenki.R;
import io.royaloaklabs.supergenki.adapter.DictionaryViewAdapter;
import io.royaloaklabs.supergenki.database.DictionaryAdapter;
import io.royaloaklabs.supergenki.domain.SearchResult;
import io.royaloaklabs.supergenki.tasks.DatabaseQueryTask;

import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
  private RecyclerView recyclerView;
  private DictionaryViewAdapter dictionaryViewAdapter;
  private RecyclerView.LayoutManager layoutManager;
  private DictionaryAdapter dictionaryAdapter;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;

  private DatabaseQueryTask.UiUpdater searchResultUiUpdater = new DatabaseQueryTask.UiUpdater() {
    @Override
    public void onTaskSuccess(List<SearchResult> searchResults) {
      dictionaryViewAdapter = new DictionaryViewAdapter(searchResults);
      recyclerView.setAdapter(dictionaryViewAdapter);
    }
  };

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        if(drawerLayout.isDrawerVisible(GravityCompat.START)) {
          drawerLayout.closeDrawer(GravityCompat.START);
        } else {
          drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    drawerLayout = findViewById(R.id.drawer_layout);
    NavigationView navigationView = findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(MenuItem menuItem) {
            // set item as selected to persist highlight
            menuItem.setChecked(true);
            // close drawer when item is tapped
            drawerLayout.closeDrawers();

            Intent i = null;
            switch(menuItem.getItemId()) {
              case R.id.menu_settings:
                i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                break;
              case R.id.menu_favorites:
                i = new Intent(getApplicationContext(), FavoriteViewActivity.class);
                startActivity(i);
                break;
              case R.id.menu_home:
                finish();
                break;
            }
            return true;
          }
        });

    ActionBar actionbar = getSupportActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

    dictionaryAdapter = new DictionaryAdapter(getApplicationContext());
    recyclerView = (RecyclerView) findViewById(R.id.rv);
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    dictionaryViewAdapter = new DictionaryViewAdapter(Collections.EMPTY_LIST);

    recyclerView.setAdapter(dictionaryViewAdapter);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.search_navigation, menu);

    final MenuItem searchMenuItem = menu.findItem(R.id.search);
    final SearchView searchView = (SearchView) searchMenuItem.getActionView();

    // Bring search bar to users attention
    searchView.setMaxWidth(Integer.MAX_VALUE); // fill up the remainder of the action bar
    searchView.setIconifiedByDefault(false);
    searchView.requestFocus();

    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

    searchView.setMaxWidth(Integer.MAX_VALUE); // fill up the remainder of the action bar

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      DatabaseQueryTask task = null;

      @Override
      public boolean onQueryTextSubmit(String q) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String q) {
        if(task != null) {
          task.cancel(Boolean.TRUE);
          task = null;
        }

        task = new DatabaseQueryTask(dictionaryAdapter);
        task.setUpdater(searchResultUiUpdater);
        task.execute(q);

        return Boolean.TRUE;
      }
    });

    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    finish();
    return true;
  }
}
