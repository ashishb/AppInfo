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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PermissionViewMain extends ListActivity
{
  static final String TAG = "AppInfo";
  static final String PACKAGE_EXTRA = "packages";
  static HashMap<String, ArrayList<PackageInfo>> permissionToAppMap;
  PermissionListAdapter listAdapter;
  PackageManager pm;
  TextView mainTextView;
  ListView listView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.permission_list);
    pm = this.getPackageManager();
    listView = getListView();
    listView.setTextFilterEnabled(true);
    //showPermissionGroupInfo();
    // TODO(ashishb): It might be better to add a check here which
    // recomputes the list in case of (un)installation of a
    // package.
    // Compute package list only if its non-null since it might have been
    // init by some other Activity (Main).
    if (permissionToAppMap == null) {
      packageInfoInit(pm);
    }
    listAdapter = new PermissionListAdapter(permissionToAppMap);
    listView.setAdapter(listAdapter);
    log("Count of permissions is " + listAdapter.getCount());
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Intent i = new Intent(this, PackageListActivity.class);
    String[] packageNames = listAdapter.getPackageNames(position).toArray(new String[1]);
    i.putExtra(PACKAGE_EXTRA, packageNames);
    startActivity(i);
  }

  private void showPermissionGroupInfo() {
    List<PermissionGroupInfo> permissionGroups = pm.getAllPermissionGroups(PackageManager.GET_META_DATA);
    for (PermissionGroupInfo info: permissionGroups) {
      log(info.toString());
    }
  }

  static void packageInfoInit(final PackageManager pm) {
    permissionToAppMap = new HashMap<String, ArrayList<PackageInfo>>(100);
    List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
    for (PackageInfo incompletePackageInfo: packageInfos) {
      PackageInfo packageInfo = null;
      try {
        packageInfo = pm.getPackageInfo(incompletePackageInfo.packageName,
            PackageManager.GET_ACTIVITIES | PackageManager.GET_GIDS | PackageManager.GET_CONFIGURATIONS |
            PackageManager.GET_INSTRUMENTATION | PackageManager.GET_PERMISSIONS |
            PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS |
            PackageManager.GET_SERVICES| PackageManager.GET_SIGNATURES);
      } catch (android.content.pm.PackageManager.NameNotFoundException e) {
        log(e.toString());
        continue;
      }
      //log(packageInfo.packageName + packageInfo.permissions);
      if (packageInfo.requestedPermissions != null) {
        if ((packageInfo.applicationInfo != null) &&
            ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)) {
          // Ignore system apps.
          for (String permission /*PermissionInfo permissionInfo*/: packageInfo.requestedPermissions) {
            if (permissionToAppMap.get(permission) == null) {
              permissionToAppMap.put(permission, new ArrayList<PackageInfo>());
            }
            permissionToAppMap.get(permission).add(packageInfo);
            //log (permission + ": " + packageInfo.packageName);
          }
        }
      }
    }

    Iterator<Entry<String, ArrayList<PackageInfo>>> it = permissionToAppMap.entrySet().iterator();
    while (true) {
      if (!it.hasNext()) {
        break;
      }
      Entry pairs = (Entry)it.next();
      int n = ((ArrayList<String>)pairs.getValue()).size();
    }
  }

  private ArrayList<PackageInfo> getAppsWithThisPermission(String permission) {
    assert (permissionToAppMap != null);
    ArrayList<PackageInfo> packages = permissionToAppMap.get(permission);
    ArrayList<PackageInfo> result_packages = new ArrayList<PackageInfo>();
    log("Permission:" + permission);
    if (packages != null) {
      for (PackageInfo packageInfo: packages) {
        if ((packageInfo.applicationInfo != null) &&
            ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)) {
          // Ignore system apps.
        } else {
          result_packages.add(packageInfo);
        }
      }
    }
    return result_packages;
  }

  private void showAppsWithThisPermission(String permission) {
    ArrayList<PackageInfo> packages = getAppsWithThisPermission(permission);
    if (packages.size() == 0) {
      log("No package found with this permission");
    } else {
      for (PackageInfo packageInfo : packages) {
        log("Package: " + packageInfo.packageName);
      }
    }
  }

  /*
   * A utility method for logging.
   */
  static void log(Object s) {
    String string = (s==null) ? "null" : s.toString();
    if (s != null)
      Log.i(TAG, string);
  }

  class PermissionListAdapter extends BaseAdapter {
    ArrayList<Pair<String, ArrayList<PackageInfo>>> permissionsAndPackages;

    public PermissionListAdapter(HashMap<String, ArrayList<PackageInfo>> permissionToAppMap) {
      permissionsAndPackages = new ArrayList<Pair<String, ArrayList<PackageInfo>>>();
      Iterator<Entry<String, ArrayList<PackageInfo>>> it = permissionToAppMap.entrySet().iterator();
      while (true) {
        if (!it.hasNext()) {
          break;
        }
        Entry pairs = (Entry)it.next();
        ArrayList<PackageInfo> packagesForAPermission = ((ArrayList<PackageInfo>)pairs.getValue());
        int n = packagesForAPermission.size();
        if (n > 1) {
          // Ignore permissions with 0 or 1 apps to reduce the clutter.
          permissionsAndPackages.add(new Pair(pairs.getKey(), packagesForAPermission));
          //this.permissionToAppMap.put(pairs.getKey(), pairs.getValue());
        }
      }

      // Now sort the permissionsAndPackages list in descending order.
      Collections.sort(permissionsAndPackages, new Comparator<Pair<String, ArrayList<PackageInfo>>>() {
          @Override
          public int compare(
            Pair<String, ArrayList<PackageInfo>> a, Pair<String, ArrayList<PackageInfo>> b) {
              return -(a.second.size() - b.second.size());
          }
        });
    }

    @Override
    public int getCount() {
      return permissionsAndPackages.size();
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public String getItem(int position) {
      Pair<String, ArrayList<PackageInfo>> permissionAndPackages = permissionsAndPackages.get(position);
      return permissionAndPackages.first + " (" + permissionAndPackages.second.size() + " apps)";
    }

    public ArrayList<String> getPackageNames(int position) {
      ArrayList<String> packagesNames = new ArrayList<String>();
      for (PackageInfo packageInfo: permissionsAndPackages.get(position).second) {
        packagesNames.add(
            packageInfo.applicationInfo.loadLabel(pm).toString() + " (" +
            packageInfo.packageName + ")");
      }
      return packagesNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        ((TextView) convertView).setText(getItem(position));
        return convertView;
    }

  }

}
