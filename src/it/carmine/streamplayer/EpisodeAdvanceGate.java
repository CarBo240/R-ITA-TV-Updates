package it.carmine.streamplayer;

/** Serializes manual and automatic episode changes on the player UI thread. */
final class EpisodeAdvanceGate {
    private boolean changing;
    private boolean ready;
    private String mediaId;

    boolean beginChange() {
        if (changing) return false;
        changing = true;
        ready = false;
        mediaId = null;
        return true;
    }

    void resolving(String id) {
        changing = true;
        ready = false;
        mediaId = id;
    }

    void onReady(String id) {
        if (mediaId == null || !mediaId.equals(id)) return;
        changing = false;
        ready = true;
    }

    boolean canAutoAdvance(String id) {
        return !changing && ready && mediaId != null && mediaId.equals(id);
    }

    boolean isChanging() { return changing; }

    void failed() {
        changing = false;
        ready = false;
        mediaId = null;
    }
}
