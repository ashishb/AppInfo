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
      List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
      for (PackageInfo packageInfo: packageInfos) {
        //log(packageInfo.packageName);
        if (packageInfo.permissions != null) {
          for (PermissionInfo permissionInfo: packageInfo.permissions) {
            String permission = permissionInfo.toString();
            permission = permission.substring(permission.indexOf(" "), permission.indexOf("}")).trim();
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

    private void showAppsWithThisPermission(String permission) {
      assert (permissionToAppMap != null);
      ArrayList<PackageInfo> packages = permissionToAppMap.get(permission);
      if (packages != null) {
        for (PackageInfo packageInfo: packages) {
          if ((packageInfo.applicationInfo != null) &&
              ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)) {
            log("System package \"" + packageInfo.packageName + " \" has permission \"" +
                permission + "\"");
          } else {
            log("Package \"" + packageInfo.packageName + " \" has permission \"" +
                permission + "\"");
          }
        }
      } else {
        log("No package found with permission \"" + permission + "\"");
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
