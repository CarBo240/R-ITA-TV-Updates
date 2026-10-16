package it.carmine.streamplayer;

/** No Android/network dependency; simulates reentrant player notifications. */
public final class EpisodeAdvanceGateCheck {
    private static void check(boolean value,String label) {
        if(!value)throw new AssertionError(label);
    }
    public static void main(String[] args) {
        EpisodeAdvanceGate gate=new EpisodeAdvanceGate();
        check(!gate.canAutoAdvance("4"),"initial end cannot advance");
        gate.resolving("4");gate.onReady("4");
        check(gate.canAutoAdvance("4"),"real episode end advances");
        check(gate.beginChange(),"manual or automatic first change accepted");
        int episode=5;
        // clearMediaItems can notify ENDED synchronously, before resolve runs.
        if(gate.canAutoAdvance("4")&&gate.beginChange())episode++;
        check(episode==5,"clear playlist does not skip episode five");
        check(!gate.beginChange(),"duplicate remote click ignored");
        gate.onReady("4");
        check(gate.isChanging(),"stale READY cannot release transition");
        gate.resolving("5");gate.onReady("4");
        check(gate.isChanging(),"previous media READY ignored during resolution");
        for(int i=0;i<100;i++) {
            check(!gate.canAutoAdvance("4"),"stale end ignored");
            check(!gate.beginChange(),"repeated click ignored while loading");
        }
        gate.onReady("5");
        check(gate.canAutoAdvance("5"),"auto-next restored for new episode");
        check(!gate.canAutoAdvance("4"),"old media end cannot advance new episode");
        check(gate.beginChange(),"next season starts once");
        check(!gate.beginChange(),"duplicate season request ignored");
        gate.failed();
        check(!gate.canAutoAdvance("5"),"failed load cannot auto-skip");
        check(gate.beginChange(),"manual change available after failure");
        gate.resolving("6");gate.failed();gate.resolving("6");gate.onReady("6");
        check(gate.canAutoAdvance("6"),"retry restores automatic advancement");
        System.out.println("PASS: manual/automatic advance, playlist reset, duplicate clicks, stale callbacks, season change and retry");
    }
}
