package com.ashish.appinfo;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Main extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      mainTextView = (TextView)findViewById(R.id.mainTextView);
      pm = this.getPackageManager();
      //showPermissionGroupInfo();
      packageInfoInit();
      showAppsWithThisPermission(permission.SEND_SMS);
      showAppsWithThisPermission(permission.INTERNET);
    }

    private void showPermissionGroupInfo() {
      List<PermissionGroupInfo> permissionGroups = pm.getAllPermissionGroups(PackageManager.GET_META_DATA);
      for (PermissionGroupInfo info: permissionGroups) {
        log(info.toString());
      }
    }

    private void packageInfoInit() {
      permissionToAppMap = new HashMap<String, ArrayList<PackageInfo>>(100);
      List<PackageInfo> packageInfos = pm.getInstalledPackages(0); //PackageManager.GET_PERMISSIONS);
      for (PackageInfo incompletePackageInfo: packageInfos) {
        PackageInfo packageInfo = null;
        try {
          packageInfo = pm.getPackageInfo(incompletePackageInfo.packageName,
               PackageManager.GET_PERMISSIONS);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
          log(e.toString());
          continue;
        }
        //log(packageInfo.packageName + packageInfo.permissions);
        if (packageInfo.requestedPermissions != null) {
          for (String permission /*PermissionInfo permissionInfo*/: packageInfo.requestedPermissions) {
            if (permissionToAppMap.get(permission) == null) {
              permissionToAppMap.put(permission, new ArrayList<PackageInfo>());
            }
            permissionToAppMap.get(permission).add(packageInfo);
            //log (permission + ": " + packageInfo.packageName);
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
//        if (n > 1) {
//          log("Permission " + pairs.getKey() + " is used by " + n);
//        }
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

    private void log(String s) {
      Log.i(TAG, s);
      mainTextView.setText(mainTextView.getText() + "\n" + s);
    }

    static final String TAG = "AppInfo";
    HashMap<String, ArrayList<PackageInfo>> permissionToAppMap;
    PackageManager pm;
    TextView mainTextView;
}
