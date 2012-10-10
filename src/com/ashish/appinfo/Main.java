package com.ashish.appinfo;

import android.Manifest.permission;
import android.app.Activity;
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
  String[] values = new String[] { "List packages (by size)", "List popular permissions"};
  PackageManager pm;
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
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Intent i;
    switch (position) {
      case 0:
        i = new Intent(this, PackageViewMain.class);
        startActivity(i);
        break;
      case 1:
        i = new Intent(this, PermissionViewMain.class);
        startActivity(i);
      break;
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
