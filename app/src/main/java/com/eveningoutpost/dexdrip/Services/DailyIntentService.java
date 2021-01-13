package com.eveningoutpost.dexdrip.Services;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.eveningoutpost.dexdrip.Home;
import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.Models.DesertSync;
import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.RollCall;
import com.eveningoutpost.dexdrip.Models.StepCounter;
import com.eveningoutpost.dexdrip.Models.usererror.UserErrorLog;
import com.eveningoutpost.dexdrip.Models.usererror.UserErrorStore;
import com.eveningoutpost.dexdrip.UtilityModels.BgSendQueue;
import com.eveningoutpost.dexdrip.UtilityModels.CalibrationSendQueue;
import com.eveningoutpost.dexdrip.UtilityModels.IncompatibleApps;
import com.eveningoutpost.dexdrip.UtilityModels.Pref;
import com.eveningoutpost.dexdrip.UtilityModels.UploaderQueue;
import com.eveningoutpost.dexdrip.utils.DatabaseUtil;
import com.eveningoutpost.dexdrip.utils.Telemetry;
import com.eveningoutpost.dexdrip.wearintegration.WatchUpdaterService;
import com.eveningoutpost.dexdrip.xdrip;

import static com.eveningoutpost.dexdrip.Home.startWatchUpdaterService;
import static com.eveningoutpost.dexdrip.UtilityModels.UpdateActivity.checkForAnUpdate;

public class DailyIntentService extends IntentService {
    private final static String TAG = DailyIntentService.class.getSimpleName();

    public DailyIntentService() {
        super("DailyIntentService");
    }

    // TODO this used to be an IntentService but that is being depreciated

    @Override
    protected void onHandleIntent(Intent intent) {
        UserErrorLog.wtf(TAG, "CALLED VIA INTENT - cancelling");
        cancelSelf();
    }

    // if we have alarm manager hangovers from previous scheduling methodology then cancel it
    private void cancelSelf() {
        try {
            final PendingIntent pi = PendingIntent.getService(xdrip.getAppContext(), 0, new Intent(this, DailyIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
            final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
        } catch (Exception e) {
            UserErrorLog.wtf(TAG, "Crash in cancelSelf() " + e);
        }
    }

    public static synchronized void work() {
        final PowerManager.WakeLock wl = JoH.getWakeLock("DailyIntentService", 120000);
        try {
            UserErrorLog.ueh(TAG, "DailyIntent Service work called");
            if (JoH.pratelimit("daily-intent-service", 60000)) {
                UserErrorLog.i(TAG, "DailyIntentService onHandleIntent Starting");
                Long start = JoH.tsl();

                // @TecMunky -- save database before pruning - allows daily capture of database
                if (Pref.getBooleanDefaultFalse("save_db_ondemand")) {
                    try {
                        String export = DatabaseUtil.saveSql(xdrip.getAppContext(), "daily");
                    } catch (Exception e) {
                        UserErrorLog.e(TAG, "DailyIntentService exception on Daily Save Database - ", e);
                    }
                }

                // prune old database records

                try {
                    startWatchUpdaterService(xdrip.getAppContext(), WatchUpdaterService.ACTION_SYNC_DB, TAG);
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on watch clear DB ", e);
                }
                try {
                    UserErrorStore.get().cleanup();
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on UserError ", e);
                }
                try {
                    BgSendQueue.cleanQueue(); // no longer used

                } catch (Exception e) {
                    UserErrorLog.d(TAG, "DailyIntentService exception on BgSendQueue " + e);
                }
                try {
                    CalibrationSendQueue.cleanQueue();
                } catch (Exception e) {
                    UserErrorLog.d(TAG, "DailyIntentService exception on CalibrationSendQueue " + e);
                }
                try {
                    UploaderQueue.cleanQueue();
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on UploaderQueue ", e);
                }
                try {
                    StepCounter.cleanup(Pref.getInt("retention_pebble_movement", 180));
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on PebbleMovement ", e);
                }

                try {
                    final int bg_retention_days = Pref.getStringToInt("retention_days_bg_reading", 0);
                    if (bg_retention_days > 0) {
                        BgReading.cleanup(bg_retention_days);
                    }
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on BgReadings cleanup ", e);
                }

                try {
                    BluetoothGlucoseMeter.startIfNoRecentData();
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on BluetoothGlucoseMeter");
                }
                try {
                    checkForAnUpdate(xdrip.getAppContext());
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "DailyIntentService exception on checkForAnUpdate ", e);
                }
                try {
                    if (Home.get_master_or_follower()) RollCall.pruneOld(0);
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "exception on RollCall prune " + e);
                }
                try {
                    DesertSync.cleanup();
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "Exception cleaning up DesertSync");
                }
                try {
                    Telemetry.sendFirmwareReport();
                    Telemetry.sendCaptureReport();
                } catch (Exception e) {
                    UserErrorLog.e(TAG, "Exception in Telemetry: " + e);
                }

                try {
                    IncompatibleApps.notifyAboutIncompatibleApps();
                } catch (Exception e) {
                    //
                }

                UserErrorLog.i(TAG, "DailyIntentService onHandleIntent exiting after " + ((JoH.tsl() - start) / 1000) + " seconds");
                //} else {
                // UserErrorLog.e(TAG, "DailyIntentService exceeding rate limit");
            }
        } finally {
            JoH.releaseWakeLock(wl);
        }
    }

}
