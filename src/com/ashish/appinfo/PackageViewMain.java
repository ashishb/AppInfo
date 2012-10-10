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
  // Format: packageName => packageLabel, [codeSize, dataSize, cacheSize]
  // Sizes are in KiB.
  static HashMap<String, Pair<String, ArrayList<Long>>> packageMap;
  static List<PackageInfo> packageInfos;
  // packageList contains package names while packageListFancy contains names appended with
  // some info like size etc.
  // The second one is meant purely for display.
  ArrayList<String> packageList, packageListFancy;
  TextView mainTextView;
  ListView listView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.package_list);
    listView = getListView();
    listView.setAdapter(
        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {}));
    final PackageManager pm = this.getPackageManager();
    final ListActivity mainActivity = this;
    packageList = new ArrayList<String>();
    packageListFancy = new ArrayList<String>();
    new Thread(new Runnable() {
        public void run() {
          // Do not call init again if it has already been called by some other activity (Main).
          if (packageMap == null) {
            packageInfoInit(pm);
          }
          final int numOfTotalPackages = packageInfos.size();
          while  (packageMap.size() < numOfTotalPackages) {
            mainActivity.runOnUiThread(new Runnable() {
              public void run() {
                ((TextView)listView.getEmptyView()).setText(
                    "Loading " + 100 * packageMap.size()/numOfTotalPackages + "%");
              }});
            try { Thread.sleep(100); }
            catch (InterruptedException e) { Log.e(TAG, e.toString(), e); }
            continue;
          }

          for (Object packageName: packageMap.keySet()) {
            packageList.add((String)packageName);
          }
          // Now sort the list by packagesize in descending order.
          Collections.sort(packageList, new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                ArrayList<Long> arr1 = packageMap.get(str1).second;
                ArrayList<Long> arr2 = packageMap.get(str2).second;
                long s1 = arr1.get(0) + arr1.get(1) + arr1.get(2);
                long s2 = arr2.get(0) + arr2.get(1) + arr2.get(2);
                Log.e(TAG, " " + s1 + " " + s2);
                return - (int)(s1 - s2);
                }
              });
          // Now generate packageListFancy which will contain sorted data.
          for (String packageName: packageList) {
            String packageLabel = packageMap.get(packageName).first;
            ArrayList<Long> sizes = packageMap.get(packageName).second;
            long size = sizes.get(0) + sizes.get(1) + sizes.get(2);
            packageListFancy.add((String)packageLabel + "(" + size/1024 + " MB)");
          }
          mainActivity.runOnUiThread(new Runnable() {
            public void run() {
              listView.setAdapter(
                new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1,
                  packageListFancy));
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
    String entry = (String)l.getAdapter().getItem(position);
    //Log.i(TAG, String.format("Entry %d value %s clicked", position, entry));
    String packageName = packageList.get(position);
    Uri packageUri = Uri.parse("package:" + packageName);
    PermissionViewMain.log("Invoking " + packageUri);
    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
    startActivity(i);
  }

  static void packageInfoInit(final PackageManager pm) {
    packageMap = new HashMap<String, Pair<String, ArrayList<Long>>>(100);
    packageInfos = pm.getInstalledPackages(0);
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
                // TODO(ashishb): Also check for these fields which were added after api 10.
                // packageStats.externalCodeSize;
                // packageStats.externalDataSize
                // packageStats.externalCacheSize;
                ArrayList<Long> sizes = new ArrayList<Long>(3);
                sizes.add(packageStats.codeSize/1024);
                sizes.add(packageStats.dataSize/1024);
                sizes.add(packageStats.cacheSize/1024);
                packageMap.put(incompletePackageInfo.packageName,
                  new Pair(incompletePackageInfo.applicationInfo.loadLabel(pm),
                    sizes));
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
