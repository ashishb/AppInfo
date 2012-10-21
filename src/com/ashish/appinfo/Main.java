package com.ashish.appinfo;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Pair;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Main extends ListActivity
{
  static final String TAG = "AppInfo";
  String[] values = new String[] { "List popular permissions", "List packages (by size)" };
  ArrayAdapter<String> listAdapter;
  TextView mainTextView;
  ListView listView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    listView = getListView();
    listView.setTextFilterEnabled(true);
    listAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, values);
    listView.setAdapter(listAdapter);
    // Starts threads which performs slow activity loading in the background.
    initPackageViewMain();
    initPermissionViewMain();
  }

  private void initPackageViewMain() {
    final PackageManager pm = this.getPackageManager();
    new Thread(new Runnable() {
        public void run() {
          PackageViewMain.packageInfoInit(pm);
        }}).start();
  }

  private void initPermissionViewMain() {
    final PackageManager pm = this.getPackageManager();
    new Thread(new Runnable() {
        public void run() {
          PermissionViewMain.packageInfoInit(pm);
        }}).start();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Intent i;
    switch (position) {
      case 0:
        i = new Intent(this, PermissionViewMain.class);
        startActivity(i);
        break;
      case 1:
        i = new Intent(this, PackageViewMain.class);
        startActivity(i);
      break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.about:
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.about_message);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.create().show();
        return true;
      case R.id.refresh:
        initPackageViewMain();
        initPermissionViewMain();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * A utility method for logging.
   */
  static void log(Object s) {
    String string = (s==null) ? "null" : s.toString();
    if (s != null)
      Log.i(TAG, string);
  }
}
