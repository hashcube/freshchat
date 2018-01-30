package com.tealeaf.plugin.plugins;

import com.tealeaf.logger;
import com.tealeaf.plugin.IPlugin;
import com.tealeaf.TeaLeaf;
import com.tealeaf.EventQueue;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.lang.Runnable;

import com.freshchat.consumer.sdk.*;

public class FreshchatPlugin implements IPlugin {

  private Context appContext;
  private Activity appActivity;
  private FreshchatUser fchatUser;

  private final String TAG = "{freshchat}";

  public void onCreateApplication(Context applicationContext) {
    appContext = applicationContext;
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    String appId = "";
    String appKey = "";

    PackageManager manager = activity.getBaseContext().getPackageManager();

    appActivity = activity;

    logger.log(TAG + "inside onCReate");
    try {
      Bundle meta = manager.getApplicationInfo(activity.getApplicationContext().getPackageName(),
        PackageManager.GET_META_DATA).metaData;
      appId = meta.get("FRESHCHAT_APP_ID").toString();
      appKey = meta.get("FRESHCHAT_APP_KEY").toString();
    } catch (Exception ex) {
      logger.log(TAG + "{exception}", "" + ex.getMessage());
    }

    logger.log(TAG + "before config init app: " + appId + " app key: " + appKey);
    FreshchatConfig fchatConfig = new FreshchatConfig(appId, appKey);
    // TODO: Read these from config
    fchatConfig.setGallerySelectionEnabled(false);
    fchatConfig.setCameraCaptureEnabled(false);

    Freshchat.getInstance(appContext).init(fchatConfig);
    fchatUser = Freshchat.getInstance(appContext).getUser();
    logger.log(TAG + "end of onCreate");
  }

  public void onResume() {
  }
  
  public void onRenderResume() {
  }
  
  public void onStart() {
  }
  
  public void onFirstRun() {
  }
  
  public void onPause() {
  }
  
  public void onRenderPause() {
  }
  
  public void onStop() {
  }
  
  public void onDestroy() {
  }
  
  public void onNewIntent(Intent intent) {
  }
  
  public void setInstallReferrer(String referrer) {
  }
  
  public void onActivityResult(Integer request, Integer result, Intent data) {
  }
  
  public boolean consumeOnBackPressed() {
    return true;
  }
  
  public void onBackPressed() {
  }

  public void setName (String param) {
    JSONObject reqJson;

    try {
      reqJson = new JSONObject(param);
      fchatUser.setFirstName(reqJson.getString("first_name"));
      fchatUser.setLastName(reqJson.getString("last_name"));
    } catch (Exception e) {
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
  }

  public void setEmail (String param) {
    JSONObject reqJson;

    try {
      reqJson = new JSONObject(param);
      fchatUser.setEmail(reqJson.getString("email"));
    } catch (Exception e){
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
  }

  public void setExternalId (String param) {
    JSONObject reqJson;

    try {
      reqJson = new JSONObject(param);
      Freshchat.getInstance(appContext).identifyUser(reqJson.getString("id"),
        null);
    } catch (Exception e){
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
  }

  public void addMetaData (String param) {
    JSONObject reqJson;
    Map<String, String> userMeta = new HashMap<String, String>();

    try {
      reqJson = new JSONObject(param);
      userMeta.put(reqJson.getString("field_name"), reqJson.getString("value"));
    } catch (Exception e){
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
    Freshchat.getInstance(appContext).setUserProperties(userMeta);
  }

  public void clearUserData(String param) {
    try {
      Freshchat.resetUser(appContext);
    } catch (Exception ex) {
      logger.log(TAG + "{exception}", "" + ex.getMessage());
    }
  }

  public void showConversations(String params) {
    try {
      appActivity.runOnUiThread(new Runnable () {
        @Override
        public void run() {
          Freshchat.showConversations(appContext);
        }
      });
    } catch (Exception ex) {
      logger.log(TAG + "{exception}", "" + ex.getMessage());
    }
  }

  public void showFAQs(String params) {
    try {
      appActivity.runOnUiThread(new Runnable () {
        @Override
        public void run() {
          Freshchat.showFAQs(appContext);
        }
      });
 
    } catch (Exception e) {
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
  }

  public class UnreadCountEvent extends com.tealeaf.event.Event {
    int count;

    public UnreadCountEvent(String status, int count) {
      super("freshchatUnreadCount");
      this.count = count;
    }
  }

  public void getUnreadCountAsync (String params) {
    Freshchat.getInstance(appContext).getUnreadCountAsync(new UnreadCountCallback() {
      @Override
      public void onResult(FreshchatCallbackStatus freshchatCallbackStatus, int unreadCount) {
        EventQueue.pushEvent(new UnreadCountEvent("success", unreadCount));
      }
    });
  }
}
