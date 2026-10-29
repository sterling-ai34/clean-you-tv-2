package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * now_playing: the top bar's music pill. Follows the media session that is playing (or the last one paused) in any
 * app, shows its artwork, title and artist, and sends previous / play-pause / next to it. Listens only while Home
 * is started, and only when the notification listener (MediaListener) has been granted; without the grant the
 * pill simply stays hidden.
 */
final class NowPlaying implements MediaSessionManager.OnActiveSessionsChangedListener {
    /** Artwork is shown at 42dp: keep a small copy instead of the app's full-size picture. */
    private static final int ART_PX = 128;

    private final Handler handler;
    private final MediaSessionManager sessions;
    private final ComponentName listener;
    private final View pill;
    private final ImageView art;
    private final TextView kicker, title, artist;
    private final EqualizerView bars;
    private final ImageView play;
    /** Called after the pill hid while one of its buttons had focus, so the activity can put focus somewhere. */
    private final Runnable onHidden;
    private MediaController controller;
    private boolean listening;
    /** The metadata artwork last scaled down, and the copy, so a state change does not rescale it. */
    private Bitmap artSource, artScaled;

    private final MediaController.Callback callback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            render();
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            render();
        }

        @Override
        public void onSessionDestroyed() {
            choose();
        }
    };

    NowPlaying(Context ctx, Handler handler, View pill, boolean reducedMotion, Runnable onHidden) {
        this.handler = handler;
        this.pill = pill;
        this.onHidden = onHidden;
        sessions = ctx.getSystemService(MediaSessionManager.class);
        listener = new ComponentName(ctx, MediaListener.class);
        art = pill.findViewById(R.id.npArt);
        art.setClipToOutline(true);
        kicker = pill.findViewById(R.id.npKicker);
        title = pill.findViewById(R.id.npTitle);
        artist = pill.findViewById(R.id.npArtist);
        bars = pill.findViewById(R.id.npBars);
        bars.setStill(reducedMotion);
        play = pill.findViewById(R.id.npPlay);
        pill.findViewById(R.id.npPrev).setOnClickListener(v -> {
            if (controller != null) controller.getTransportControls().skipToPrevious();
        });
        pill.findViewById(R.id.npNext).setOnClickListener(v -> {
            if (controller != null) controller.getTransportControls().skipToNext();
        });
        play.setOnClickListener(v -> {
            if (controller == null) return;
            if (isPlaying(controller)) controller.getTransportControls().pause();
            else controller.getTransportControls().play();
        });
    }

    /** Home is on screen: start following the sessions. */
    void start() {
        if (listening || sessions == null) return;
        try {
            sessions.addOnActiveSessionsChangedListener(this, listener, handler);
            listening = true;
            choose();
        } catch (SecurityException notGranted) {
            // Notification access was never granted (NOW_PLAYING=1 at install): no pill, nothing else changes.
            hide();
        }
    }

    /** Home is hidden: stop listening and let go of the session. */
    void stop() {
        if (!listening) return;
        listening = false;
        sessions.removeOnActiveSessionsChangedListener(this);
        follow(null);
        bars.setPlaying(false);
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        choose();
    }

    /** The session that plays, else the first one paused with a song loaded, else none. */
    private void choose() {
        if (!listening) return;
        List<MediaController> active;
        try {
            active = sessions.getActiveSessions(listener);
        } catch (SecurityException revoked) {
            stop();
            hide();
            return;
        }
        MediaController pick = null;
        for (MediaController c : active) {
            if (isPlaying(c)) { pick = c; break; }
            if (pick == null && isPaused(c) && hasSong(c)) pick = c;
        }
        follow(pick);
        render();
    }

    private void follow(MediaController next) {
        if (controller != null && next != null && controller.getSessionToken().equals(next.getSessionToken())) return;
        if (controller != null) controller.unregisterCallback(callback);
        controller = next;
        if (controller != null) controller.registerCallback(callback, handler);
    }

    private void render() {
        if (controller == null || !hasSong(controller) || !(isPlaying(controller) || isPaused(controller))) {
            hide();
            return;
        }
        MediaMetadata m = controller.getMetadata();
        boolean playing = isPlaying(controller);
        title.setText(text(m, MediaMetadata.METADATA_KEY_TITLE, MediaMetadata.METADATA_KEY_DISPLAY_TITLE));
        CharSequence by = text(m, MediaMetadata.METADATA_KEY_ARTIST, MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        if (TextUtils.isEmpty(by)) by = text(m, MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, MediaMetadata.METADATA_KEY_ALBUM);
        artist.setText(by);
        artist.setVisibility(TextUtils.isEmpty(by) ? View.GONE : View.VISIBLE);
        kicker.setText(playing ? R.string.now_playing : R.string.now_paused);
        play.setImageResource(playing ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
        art.setImageBitmap(artwork(m));
        bars.setPlaying(playing);
        pill.setVisibility(View.VISIBLE);
    }

    private void hide() {
        if (pill.getVisibility() == View.GONE) return;
        boolean hadFocus = pill.hasFocus();
        pill.setVisibility(View.GONE);
        art.setImageDrawable(null);
        artSource = artScaled = null;
        bars.setPlaying(false);
        if (hadFocus && onHidden != null) onHidden.run();
    }

    /** The song's artwork scaled down once, or null for the plain rounded square. */
    private Bitmap artwork(MediaMetadata m) {
        Bitmap source = m.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if (source == null) source = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (source == null) source = m.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        if (source == null) {
            artSource = artScaled = null;
            return null;
        }
        if (source != artSource) {
            artSource = source;
            int w = source.getWidth(), h = source.getHeight();
            if (w <= 0 || h <= 0) return artScaled = null;
            float scale = Math.min(1f, (float) ART_PX / Math.max(w, h));
            artScaled = scale >= 1f ? source
                    : Bitmap.createScaledBitmap(source, Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)), true);
        }
        return artScaled;
    }

    private static CharSequence text(MediaMetadata m, String key, String fallback) {
        CharSequence t = m.getText(key);
        return TextUtils.isEmpty(t) ? m.getText(fallback) : t;
    }

    private static boolean hasSong(MediaController c) {
        MediaMetadata m = c.getMetadata();
        return m != null && !TextUtils.isEmpty(text(m, MediaMetadata.METADATA_KEY_TITLE, MediaMetadata.METADATA_KEY_DISPLAY_TITLE));
    }

    private static boolean isPlaying(MediaController c) {
        PlaybackState s = c.getPlaybackState();
        if (s == null) return false;
        int st = s.getState();
        return st == PlaybackState.STATE_PLAYING || st == PlaybackState.STATE_BUFFERING
                || st == PlaybackState.STATE_FAST_FORWARDING || st == PlaybackState.STATE_REWINDING
                || st == PlaybackState.STATE_SKIPPING_TO_NEXT || st == PlaybackState.STATE_SKIPPING_TO_PREVIOUS;
    }

    private static boolean isPaused(MediaController c) {
        PlaybackState s = c.getPlaybackState();
        return s != null && s.getState() == PlaybackState.STATE_PAUSED;
    }
}
