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
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PackageListActivity extends ListActivity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.package_list);
      ListView listView = getListView();
      listView.setTextFilterEnabled(true);
      Intent i = getIntent();
      if( i != null && i.hasExtra(Main.PACKAGE_EXTRA)) {
        String[] packages = i.getStringArrayExtra(Main.PACKAGE_EXTRA);
        if (packages != null && packages.length > 0) {
          ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this,
              android.R.layout.simple_list_item_1, packages);
          listView.setAdapter(listAdapter);
        }
      }
    }

    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
      // Assuming format: package_label (packageName).
      String packageName = l.getAdapter().getItem(position).toString();
      packageName = packageName.substring(packageName.indexOf("(") + 1, packageName.indexOf(")"));
      Uri packageUri = Uri.parse("package:" + packageName);
      Main.log("Invoking " + packageUri);
      Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
      startActivity(i);

    }
}
