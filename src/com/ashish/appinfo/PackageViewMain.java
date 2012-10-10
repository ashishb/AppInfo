package com.ashish.appinfo;

import android.Manifest.permission;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PackageViewMain extends ListActivity
{
  static final String TAG = "AppInfo";
  // Format: packageName => [codeSize, dataSize, cacheSize]
  HashMap<String, ArrayList<Long>> packageToSizeMap;
  ArrayList<String> packageWithSizeList;
  PackageManager pm;
  List<PackageInfo> packageInfos;
  TextView mainTextView;
  ListView listView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.package_list);
    pm = this.getPackageManager();
    listView = getListView();

    //packageToSizeMap = new HashMap<String, ArrayList<Long>>(100);
    listView.setAdapter(
        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {}));
    packageInfos = pm.getInstalledPackages(0);
    final ListActivity mainActivity = this;
    packageWithSizeList = new ArrayList<String>();
    new Thread(new Runnable() {
        public void run() {
          packageInfoInit();
          final int numOfTotalPackages = packageInfos.size();
          while  (packageWithSizeList.size() < numOfTotalPackages) {
            mainActivity.runOnUiThread(new Runnable() {
              public void run() {
                ((TextView)listView.getEmptyView()).setText(
                    "Loading " + 100*packageWithSizeList.size()/numOfTotalPackages + "%");
              }});
            try { Thread.sleep(100); }
            catch (InterruptedException e) { Log.e(TAG, e.toString(), e); }
            continue;
          }

          // Now sort the list by packagesize in descending order.
          // TODO(ashishb): This is ugly and hacky, clean it up.
          Collections.sort(packageWithSizeList, new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                // Assuming format: app-name (size KB).
                int s1 = Integer.parseInt(
                  str1.substring(str1.indexOf("(") + 1, str1.indexOf(" ", str1.indexOf("("))));
                int s2 = Integer.parseInt(
                  str2.substring(str2.indexOf("(") + 1, str2.indexOf(" ", str2.indexOf("("))));
                Log.e(TAG, " " + s1 + " " + s2);
                return - (s1 - s2);
                }
              });
          mainActivity.runOnUiThread(new Runnable() {
            public void run() {
              listView.setAdapter(
                new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1,
                  packageWithSizeList));
          }
        });
      }}).start();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    String entry = packageWithSizeList.get(position);
    Log.i(TAG, String.format("Entry %d value %s clicked", position, entry));
    // TODO(ashishb): Hacky code fix this.
    // Assuming format packageName ( size)
    String packageName = entry.substring(0, entry.indexOf("("));
    Uri packageUri = Uri.parse("package:" + packageName);
    PermissionViewMain.log("Invoking " + packageUri);
    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
    startActivity(i);
  }

  private void packageInfoInit() {
    // Inspired from http://www-jo.se/f.pfleger/android-package-size
    for (final PackageInfo incompletePackageInfo: packageInfos) {
      // Some reflection to use the hidden getPackageInfo method here.
      try {
        Method getPackageInfo = pm.getClass().getMethod(
            "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
        getPackageInfo.invoke(pm, incompletePackageInfo.packageName,
            new IPackageStatsObserver.Stub() {
              @Override
              public void onGetStatsCompleted(PackageStats packageStats, boolean success)
              throws RemoteException {
                long size = packageStats.codeSize + packageStats.dataSize + packageStats.cacheSize;
                  // TODO(ashishb): Also check for these fields which were added after api 10.
                  // packageStats.externalCodeSize;
                  // packageStats.externalDataSize
                  // packageStats.externalCacheSize;
                //packageToSizeMap.put(incompletePackageInfo.packageName, sizeList);
                packageWithSizeList.add(
                  //incompletePackageInfo.applicationInfo.loadLabel(pm)
                  incompletePackageInfo.packageName
                  + "(" + size/(1000*1000) + " MB)");
                //Log.i(TAG, "(" + size/1000 + " KB)");
              }
            });

      } catch (IllegalAccessException e) {
        Log.e(TAG, e.toString(), e);
      } catch (InvocationTargetException e) {
        Log.e(TAG, e.toString(), e);
      } catch (NoSuchMethodException e) {
        Log.e(TAG, e.toString(), e);
      }
    }

  }
}
