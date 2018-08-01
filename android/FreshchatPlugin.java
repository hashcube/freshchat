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
import android.net.Uri;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.Runnable;
import java.util.Iterator;

import com.freshchat.consumer.sdk.*;

public class FreshchatPlugin implements IPlugin {

  private Context appContext;
  private Activity appActivity;
  private FreshchatUser fchatUser;
  private String fTag = "";

  private boolean emailSupport = false;
  private String emailId = "";
  private Map<String, String> emailProps = new HashMap<String, String>();

  private final String TAG = "{freshchat}";

  public void onCreateApplication(Context applicationContext) {
    appContext = applicationContext;
  }

  private String getApplicationName() {
    return appContext.getApplicationInfo().loadLabel(appContext.getPackageManager()).toString();
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    String appId = "";
    String appKey = "";

    PackageManager manager = activity.getBaseContext().getPackageManager();

    appActivity = activity;

    emailSupport = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP;

    logger.log(TAG + "inside onCReate");
    try {
      Bundle meta = manager.getApplicationInfo(activity.getApplicationContext().getPackageName(),
        PackageManager.GET_META_DATA).metaData;
      appId = meta.get("FRESHCHAT_APP_ID").toString();
      appKey = meta.get("FRESHCHAT_APP_KEY").toString();
      fTag = meta.get("FRESHCHAT_TAG").toString();
      emailId = meta.get("FRESHCHAT_EMAIL").toString();
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

  private String getMailContent() {
    StringBuilder sb = new StringBuilder();

    sb.append("============= User Details ============\n");
    for (Map.Entry<String, String> curr: emailProps.entrySet()) {
      sb.append(curr.getKey() + " : " + curr.getValue() + "\n");
    }
    sb.append("=======================================\n\n\n\n");
    sb.append("PLEASE WRITE YOUR FEEDBACK BELOW:\n\n");

    return sb.toString();
  }

  private void sendEmail() {
    logger.log(TAG + "{support email}", "" + emailId);
    Intent emailIntent = new Intent(Intent.ACTION_VIEW);

    emailIntent.setData(Uri.parse("mailto:"));
    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailId, ""});
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, fTag + " - Support");
    emailIntent.putExtra(Intent.EXTRA_TEXT, getMailContent());

    try {
      appActivity.startActivity(Intent.createChooser(emailIntent, "Contact " + getApplicationName() + " Using:"));
    } catch (android.content.ActivityNotFoundException ex) {
      logger.log(TAG + "{exception}", "" + ex.getMessage());
    }
  }

  public void setName (String param) {
    JSONObject reqJson;

    try {
      fchatUser = Freshchat.getInstance(appContext).getUser();
      reqJson = new JSONObject(param);
      fchatUser.setFirstName(reqJson.getString("first_name"));
      fchatUser.setLastName(reqJson.getString("last_name"));
      Freshchat.getInstance(appContext).setUser(fchatUser);

      if (emailSupport) {
        emailProps.put("first_name", reqJson.getString("first_name"));
        emailProps.put("last_name", reqJson.getString("last_name"));
      }
    } catch (Exception e) {
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
  }

  public void setEmail (String param) {
    JSONObject reqJson;

    try {
      fchatUser = Freshchat.getInstance(appContext).getUser();
      reqJson = new JSONObject(param);
      fchatUser.setEmail(reqJson.getString("email"));
      Freshchat.getInstance(appContext).setUser(fchatUser);

      if (emailSupport) {
        emailProps.put("email", reqJson.getString("email"));
      }
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

      if (emailSupport) {
        emailProps.put("user_id", reqJson.getString("id"));
      }
    } catch (Exception e){
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
  }

  public void addMetaData (String params) {
    JSONObject reqJson;
    String key;
    Map<String, String> userMeta = new HashMap<String, String>();

    try {
      reqJson = new JSONObject(params);
      Iterator<String> iter = reqJson.keys();
      while (iter.hasNext()) {
        key = iter.next();
        userMeta.put(key, reqJson.getString(key));

        if (emailSupport) {
          emailProps.put(key, String.valueOf(reqJson.getString(key)));
        }
      }
    } catch (Exception e){
      logger.log(TAG + "{exception}", "" + e.getMessage());
    }
    Freshchat.getInstance(appContext).setUserProperties(userMeta);
  }

  public void clearUserData(String param) {
    try {
      Freshchat.resetUser(appContext);

      emailProps.clear();
    } catch (Exception ex) {
      logger.log(TAG + "{exception}", "" + ex.getMessage());
    }
  }

  public void showConversations(String params) {
    if (emailSupport) {
      this.sendEmail();
      return;
    }

    try {
      appActivity.runOnUiThread(new Runnable () {
        @Override
        public void run() {
          ConversationOptions convOptions = null;

          if (!fTag.isEmpty()) {
            List<String> tags = new ArrayList<String>();
            tags.add(fTag);

            convOptions = new ConversationOptions()
              .filterByTags(tags, "Messages");
          }

          Freshchat.showConversations(appActivity, convOptions);
        }
      });
    } catch (Exception ex) {
      logger.log(TAG + "{exception}", "" + ex.getMessage());
    }
  }

  public void showFAQs(String params) {
    if (emailSupport) {
      this.sendEmail();
      return;
    }

    try {
      appActivity.runOnUiThread(new Runnable () {
        @Override
        public void run() {
          FaqOptions faqOptions = null;

          if (!fTag.isEmpty()) {
            List<String> tags = new ArrayList<String>();
            tags.add(fTag);

            faqOptions = new FaqOptions()
              .filterByTags(tags, "FAQs", FaqOptions.FilterType.CATEGORY)
              .filterContactUsByTags(tags, "Message Us");
          }

          Freshchat.showFAQs(appActivity, faqOptions);
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
