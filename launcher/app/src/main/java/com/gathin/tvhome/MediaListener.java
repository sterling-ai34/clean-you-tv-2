package com.gathin.tvhome;

import android.service.notification.NotificationListenerService;

/**
 * now_playing: Android lists the active media sessions (MediaSessionManager.getActiveSessions) only to an app with
 * an enabled notification listener. This one exists for that grant alone; it reads no notifications.
 */
public class MediaListener extends NotificationListenerService {
}
