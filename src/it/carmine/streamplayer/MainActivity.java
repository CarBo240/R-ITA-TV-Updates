package it.carmine.streamplayer;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.*;
import androidx.media3.common.*;
import androidx.media3.exoplayer.*;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.AspectRatioFrameLayout;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final ExecutorService playbackWorker=Executors.newSingleThreadExecutor(),guideWorker=Executors.newSingleThreadExecutor();
    private final Handler uiHandler=new Handler(Looper.getMainLooper());
    private Future<?> resolveJob,catalogJob;
    private final ExecutorService sessionWorker=Executors.newSingleThreadExecutor();
    private final ExecutorService gomWorker=Executors.newSingleThreadExecutor();
    private ChannelSource channelSource=ChannelSource.VAVOO;
    private int catalogGeneration,gomRetries,vavooRetries;
    private final Runnable vavooRenew=new Runnable(){public void run(){if(destroyed||!resumed)return;if(channelSource==ChannelSource.VAVOO){final VavooClient activeClient=client;sessionWorker.submit(()->{try{activeClient.renewSession();}catch(Exception ignored){}});}uiHandler.postDelayed(this,VavooClient.RENEW_INTERVAL_MS);}};
    private List<VavooClient.Channel> gomCatalog=new ArrayList<>();
    private String catalogDaddyHome="";private boolean homePaused;
    private final Runnable gomRefresh=new Runnable(){public void run(){if(destroyed||!resumed||channelSource!=ChannelSource.GOMSTREAM)return;loadCatalog(false);scheduleGomRefresh();}};
    private LinearLayout playerHeader,playerControls,channelPanel;
    private ListView playerList;
    private TextView playerTitle,playerEpg;
    private Button pauseButton,seekBack,seekForward;
    private PlayerView video;
    private Player engine;private AudioDelay audioDelay;
    private Button selectChannels;
    private Spinner playerCategories;
    private CheckBox playerFavorites;
    private String playerCategory="Tutti";
    private List<VavooClient.Channel> playerVisible=new ArrayList<>();
    private ArtworkStore artwork;
    private ImageView guideImage;
    private LinearLayout navigation;
    private Button navDirect,navCategories,navFavorites,navGuide,navEpg,navEvents,navDaddy,navVod;
    private boolean resumePlayback;

    private VavooClient.Channel currentChannel,requestedChannel;
    private List<VavooClient.Channel> zapChannels=new ArrayList<>();
    private EpgStore epg;
    private boolean guideLoading=false;
    private String epgMessage="EPG in caricamento…";
    private final Runnable hideControls=()->hidePlayerControls();
    private final Runnable epgTick=new Runnable(){public void run(){if(destroyed)return;updatePlayerInfo();updateHomeGuide();if(adapter!=null)adapter.notifyDataSetChanged();refreshPlayerList();uiHandler.postDelayed(this,60000);}};
    private VavooClient client;
    private CatalogStore catalogStore;
    private android.content.SharedPreferences prefs;
    private Set<String> favorites;
    private List<VavooClient.Channel> channels=new ArrayList<>(),visible=new ArrayList<>();
    private LinearLayout home;
    private FrameLayout root,player;
    private ListView list;
    private TextView status;
    private EditText search;private long seenBackup;
    
    private Button reload,favs;
    private Spinner categories;
    private VavooClient.Channel guideChannel;
    private TextView guideChannelName,guideTitle,guideTime,guideDescription,guideNext;
    private ProgressBar guideProgress;
    private Button watch;
    private ScrollView guideScroll;
    private String selectedCategory="Tutti";
    private final Runnable hideChannelList=()->hideChannelPanel();
    private static final long LIST_TIMEOUT=8000;

    private ProgressBar buffering;
    private boolean favoritesOnly=false,loading=false,destroyed=false,resumed=false;
    private int playGeneration=0;
    private ArrayAdapter<String> adapter;
    
    private boolean homeBackArmed;
    private final int bg=Color.rgb(21,22,25),fg=Color.rgb(244,243,239),muted=Color.rgb(166,166,170),accent=Color.rgb(242,178,69);
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(fg);return t;}
    private android.graphics.drawable.GradientDrawable surface(int color,int radius,int border){
        android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(border!=0)d.setStroke(dp(1),border);return d;
    }
    private android.graphics.drawable.StateListDrawable selectable(int color){
        android.graphics.drawable.StateListDrawable d=new android.graphics.drawable.StateListDrawable();
        d.addState(new int[]{android.R.attr.state_focused},surface(Color.rgb(64,51,30),5,accent));
        d.addState(new int[]{android.R.attr.state_selected},surface(Color.rgb(64,51,30),5,accent));
        d.addState(new int[]{android.R.attr.state_pressed},surface(Color.rgb(75,57,29),5,accent));
        d.addState(new int[]{},surface(color,5,Color.rgb(56,57,61)));return d;
    }
    private Button button(String title){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(fg);b.setTextSize(14);
        b.setTypeface(UiTheme.medium(this));b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);
        b.setPadding(dp(8),dp(4),dp(8),dp(4));b.setBackground(selectable(Color.rgb(32,33,37)));b.setElevation(0);return b;}
    private android.graphics.drawable.StateListDrawable homeSelection(){return selectable(bg);}
    private android.graphics.drawable.StateListDrawable channelSelection(){android.graphics.drawable.StateListDrawable d=new android.graphics.drawable.StateListDrawable();d.addState(new int[]{android.R.attr.state_focused},surface(Color.rgb(64,51,30),5,accent));d.addState(new int[]{android.R.attr.state_pressed},surface(Color.rgb(64,51,30),5,accent));d.addState(new int[]{},surface(bg,0,0));return d;}
    private TextView homeText(String title,int size){return text(title,size);}
    private Button homeButton(String title){return button(title);}
    protected VavooClient createClient(String id){return new VavooClient(id,this);}
    protected boolean imagesEnabled(){return true;}
    private final class ResponsiveHome extends LinearLayout {
        LinearLayout content;Boolean wideMode;
        ResponsiveHome(){super(MainActivity.this);setOrientation(VERTICAL);}
        @Override protected void onMeasure(int width,int height){
            boolean wide=MeasureSpec.getSize(width)>=dp(700);
            if(navigation!=null&&content!=null&&(wideMode==null||wideMode!=wide)){wideMode=wide;
                navigation.setOrientation(HORIZONTAL);navigation.setLayoutParams(lp(-1,dp(wide?50:44)));
                content.setLayoutParams(new LinearLayout.LayoutParams(-1,0,1));
                Button[] buttons={navDirect,navCategories,navEvents,navDaddy,navFavorites,navGuide,navVod,reload};
                String[] large={"Canali ▾","Categorie ▾","Eventi","Daddy Live","Preferiti","Guida TV","VOD","↻"},small={"TV","▦","●","Daddy","★","Guida","VOD","↻"};
                for(int i=0;i<buttons.length;i++){LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(i==7?dp(wide?48:36):0,-1,i==7?0:1);params.setMarginEnd(dp(4));buttons[i].setLayoutParams(params);buttons[i].setText(wide?large[i]:small[i]);buttons[i].setContentDescription(i==7?"Aggiorna":large[i]);buttons[i].setTextSize(wide?14:11);buttons[i].setSingleLine(true);}
            }
            super.onMeasure(width,height);
        }
    }
    private void resetListTimer(){uiHandler.removeCallbacks(hideChannelList);if(channelPanel!=null&&channelPanel.getVisibility()==View.VISIBLE)uiHandler.postDelayed(hideChannelList,LIST_TIMEOUT);}
    private static class ChannelRow {
        String channelKey;LinearLayout card;TextView badge,name,programme,next;ProgressBar progress;ImageView logo;
    }
    private View channelCard(VavooClient.Channel c,int number,View recycled,boolean main){
        ChannelRow holder;
        if(recycled!=null&&recycled.getTag() instanceof ChannelRow)holder=(ChannelRow)recycled.getTag();
        else {
            holder=new ChannelRow();LinearLayout outer=new LinearLayout(this);outer.setPadding(0,0,0,0);
            holder.card=new LinearLayout(this);holder.card.setGravity(Gravity.CENTER_VERTICAL);holder.card.setPadding(dp(12),dp(10),dp(12),dp(10));
            holder.card.setBackground(channelSelection());holder.card.setDuplicateParentStateEnabled(true);outer.addView(holder.card,lp(-1,-2));
            holder.badge=homeText("",14);holder.badge.setGravity(Gravity.CENTER);holder.badge.setTypeface(UiTheme.medium(this));
            holder.badge.setTextColor(accent);holder.card.addView(holder.badge,lp(dp(28),dp(34)));
            holder.logo=new ImageView(this);holder.logo.setScaleType(ImageView.ScaleType.FIT_CENTER);holder.card.addView(holder.logo,lp(dp(44),dp(34)));
            LinearLayout details=new LinearLayout(this);details.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams detailParams=new LinearLayout.LayoutParams(0,-2,1);detailParams.leftMargin=dp(10);holder.card.addView(details,detailParams);
            holder.name=homeText("",17);holder.name.setTypeface(UiTheme.medium(this));holder.name.setMaxLines(2);details.addView(holder.name,lp(-1,-2));
            holder.programme=homeText("",13);holder.programme.setTextColor(muted);holder.programme.setMaxLines(2);holder.programme.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams programmeParams=lp(-1,-2);programmeParams.topMargin=dp(3);details.addView(holder.programme,programmeParams);
            holder.progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);holder.progress.setMax(1000);
            holder.progress.setProgressTintList(ColorStateList.valueOf(accent));holder.progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(57,58,62)));
            LinearLayout.LayoutParams progressParams=lp(-1,dp(3));progressParams.topMargin=dp(5);details.addView(holder.progress,progressParams);
            holder.next=homeText("",11);holder.next.setTextColor(muted);holder.next.setMaxLines(1);holder.next.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams nextParams=lp(-1,-2);nextParams.topMargin=dp(3);details.addView(holder.next,nextParams);
            outer.setTag(holder);recycled=outer;
        }
        holder.channelKey=c.key();
        String clean=DisplayNames.channel(c.name);
        holder.badge.setText(String.format(Locale.US,"%02d",number+1));holder.badge.setVisibility(main&&home.getWidth()>=dp(700)?View.VISIBLE:View.GONE);
        boolean compact=main&&home.getWidth()<dp(700);holder.logo.setLayoutParams(lp(dp(compact?32:48),dp(34)));holder.name.setTextSize(compact?14:19);holder.next.setVisibility(main?View.GONE:View.VISIBLE);holder.progress.setVisibility(main?View.GONE:View.VISIBLE);
        artwork.bind(holder.logo,artwork.logo(c,epg));
        holder.name.setText((favorites.contains(c.key())?"★  ":"")+clean);
        boolean selected=main&&guideChannel!=null&&guideChannel.key().equals(c.key());
        holder.card.setActivated(selected);holder.card.setBackground(selected?surface(Color.rgb(64,51,30),5,accent):channelSelection());
        long now=System.currentTimeMillis();EpgStore.Programme live=epg==null?null:epg.now(c,now),following=epg==null?null:epg.next(c,now);
        holder.programme.setText(live==null?(epg==null?(guideLoading?"Guida TV in caricamento…":"Guida TV non disponibile"):"Programmazione non disponibile"):(time(live.start)+"–"+time(live.end)+"  "+live.title));
        holder.progress.setVisibility(live==null?View.GONE:View.VISIBLE);
        if(live!=null)holder.progress.setProgress((int)Math.max(0,Math.min(1000,(now-live.start)*1000/(live.end-live.start))));
        holder.next.setVisibility(following==null?View.GONE:View.VISIBLE);holder.next.setText(following==null?"":"DOPO  "+time(following.start)+"  "+following.title);
        if(main){holder.programme.setMaxLines(2);if(live!=null)holder.programme.setText(live.title);holder.progress.setVisibility(View.GONE);holder.next.setVisibility(View.GONE);}
        return recycled;
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        if(saved==null)VodSession.reset();
        AppUpdater.check(this,false);
        try { initialize();if(saved==null&&VodSettings.startsWithVod(this))startActivity(new Intent(this,VodActivity.class)); } catch (Throwable failure) { startupFailure(failure); }
    }
    private void startupFailure(Throwable failure) {
        android.util.Log.e("TVSatPlayer", "Errore iniziale", failure);
        android.widget.ScrollView scroll=new android.widget.ScrollView(this);
        android.widget.TextView details=new android.widget.TextView(this);
        details.setPadding(dp(20),dp(32),dp(20),dp(20));
        details.setTextSize(16);details.setTextColor(android.graphics.Color.WHITE);
        scroll.setBackgroundColor(android.graphics.Color.rgb(27,28,32));
        details.setText("R. ITA TV — errore di avvio\n\n"+android.util.Log.getStackTraceString(failure));
        details.setTextIsSelectable(true);scroll.addView(details);setContentView(scroll);
    }
    private void initialize() {
        prefs=getSharedPreferences("player",MODE_PRIVATE);channelSource=ChannelSource.saved(this);
        String id=prefs.getString("device_id",null);
        if(id==null){id=UUID.randomUUID().toString();prefs.edit().putString("device_id",id).apply();}
        client=createClient(id);artwork=new ArtworkStore(this,imagesEnabled());catalogStore=new CatalogStore(this);favorites=new HashSet<>(prefs.getStringSet("favorites",Collections.emptySet()));
        prefs.edit().remove("country").apply();
        root=new FrameLayout(this);root.setBackgroundColor(bg);root.setFitsSystemWindows(true);setContentView(root);
        ResponsiveHome shell=new ResponsiveHome();home=shell;home.setPadding(dp(12),dp(12),dp(12),dp(8));root.addView(home,new FrameLayout.LayoutParams(-1,-1));
        navigation=new LinearLayout(this);navigation.setTag("homeNavigation");navigation.setOrientation(LinearLayout.HORIZONTAL);navigation.setPadding(0,0,0,dp(6));home.addView(navigation,lp(-1,dp(50)));
        navDirect=homeButton("◉ Canali ▾");navDirect.setTag("navChannels");navigation.addView(navDirect,lp(-1,dp(48)));
        navCategories=homeButton("▦ Categorie");navigation.addView(navCategories,lp(-1,dp(48)));
        navFavorites=homeButton("★ Preferiti");navigation.addView(navFavorites,lp(-1,dp(48)));
        navGuide=homeButton("≡ Guida TV");navigation.addView(navGuide,lp(-1,dp(48)));
        navEvents=homeButton("● Eventi");navEvents.setTag("navEvents");navigation.addView(navEvents,lp(-1,dp(48)));navEvents.setOnClickListener(v->startActivity(new Intent(this,EventsActivity.class)));navEvents.setOnLongClickListener(v->{EventSettings.edit(this);return true;});
        navDaddy=homeButton("Daddy Live");navDaddy.setTag("navDaddyLive");navigation.addView(navDaddy,lp(-1,dp(48)));navDaddy.setOnClickListener(v->startActivity(new Intent(this,DaddyLiveActivity.class)));navDaddy.setOnLongClickListener(v->{DaddyLiveSettings.edit(this,null);return true;});
        navVod=homeButton("VOD");navVod.setTag("navVod");navVod.setOnClickListener(v->startActivity(new Intent(this,VodActivity.class)));navVod.setOnLongClickListener(v->{VodSession.videoMenu(this,()->{});return true;});
        reload=homeButton("↻ Aggiorna");navigation.addView(reload,lp(-1,dp(48)));
        navEpg=homeButton("⚙ EPG");navEpg.setOnClickListener(v->epgSettings());navGuide.setOnLongClickListener(v->{epgSettings();return true;});
        navigation.removeAllViews();for(Button item:new Button[]{navDirect,navCategories,navEvents,navDaddy,navFavorites,navGuide,navVod,reload}){navigation.addView(item);UiTheme.navigation(item);}
        navDirect.setOnClickListener(v->chooseChannelSource());
        navDirect.setOnLongClickListener(v->{editChannelUrls();return true;});
        navCategories.setOnClickListener(v->{categories.requestFocus();categories.performClick();});navFavorites.setOnClickListener(v->favs.performClick());navGuide.setOnClickListener(v->showSchedule(guideChannel));
        LinearLayout content=new LinearLayout(this);content.setTag("homeContent");content.setOrientation(LinearLayout.VERTICAL);home.addView(content,new LinearLayout.LayoutParams(0,-1,1));shell.content=content;
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(fg);search.setHintTextColor(muted);
        search.setHint("Cerca canali o eventi");search.setTextSize(15);search.setPadding(dp(12),0,dp(12),0);search.setBackground(homeSelection());
        search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);content.addView(search,lp(-1,dp(36)));
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);LinearLayout.LayoutParams rowParams=lp(-1,dp(36));rowParams.topMargin=dp(6);content.addView(row,rowParams);
        categories=new Spinner(this,Spinner.MODE_DROPDOWN);categories.setTag("categories");categories.setContentDescription("Scegli categoria canali");categories.setFocusable(true);categories.setBackground(homeSelection());row.addView(categories,new LinearLayout.LayoutParams(0,-1,1));
        ArrayAdapter<String> cats=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ChannelCategories.ALL){
            @Override public View getView(int pos,View old,ViewGroup parent){TextView t=(TextView)super.getView(pos,old,parent);t.setText(ChannelCategories.ALL[pos]+"  ▾");t.setTextColor(fg);t.setTextSize(15);return t;}
        
            @Override public View getDropDownView(int pos,View old,ViewGroup parent){TextView t=(TextView)super.getDropDownView(pos,old,parent);t.setTextColor(fg);t.setBackground(homeSelection());t.setPadding(dp(16),dp(12),dp(16),dp(12));return t;}
        };cats.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);categories.setAdapter(cats);
        categories.setOnFocusChangeListener((v,focus)->{categories.setBackground(focus?surface(Color.rgb(64,51,30),5,accent):homeSelection());});
        selectedCategory=prefs.getString("category","Tutti");int selected=Arrays.asList(ChannelCategories.ALL).indexOf(selectedCategory);if(selected<0){selected=0;selectedCategory="Tutti";}categories.setSelection(selected);
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){if(selectedCategory.equals(ChannelCategories.ALL[pos]))return;selectedCategory=ChannelCategories.ALL[pos];prefs.edit().putString("category",selectedCategory).apply();filter();}
            public void onNothingSelected(AdapterView<?> parent){}
        });
        favs=navFavorites;
        LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER_VERTICAL);content.addView(tools,lp(-1,dp(24)));
        status=homeText("Caricamento…",11);status.setTextColor(muted);tools.addView(status,new LinearLayout.LayoutParams(0,-1,1));status.setGravity(Gravity.CENTER_VERTICAL);
        list=new ListView(this);list.setDivider(new android.graphics.drawable.ColorDrawable(Color.rgb(49,50,54)));list.setDividerHeight(dp(1));list.setCacheColorHint(Color.TRANSPARENT);list.setItemsCanFocus(false);
        list.setSelector(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));list.setDrawSelectorOnTop(false);list.setTag("homeChannels");list.setId(View.generateViewId());
        LinearLayout browser=new LinearLayout(this);browser.setTag("homeBrowser");browser.setOrientation(LinearLayout.HORIZONTAL);content.addView(browser,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout channelColumn=new LinearLayout(this);channelColumn.setTag("homeChannelColumn");channelColumn.setOrientation(LinearLayout.VERTICAL);browser.addView(channelColumn,new LinearLayout.LayoutParams(0,-1,0.58f));
        content.removeView(search);content.removeView(row);content.removeView(tools);
        channelColumn.addView(search,lp(-1,dp(36)));channelColumn.addView(row,rowParams);channelColumn.addView(tools,lp(-1,dp(24)));
        channelColumn.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        guideScroll=new ScrollView(this);guideScroll.setTag("homeGuide");guideScroll.setFillViewport(true);guideScroll.setFocusable(true);guideScroll.setFocusableInTouchMode(true);guideScroll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);guideScroll.setId(View.generateViewId());guideScroll.setContentDescription("Guida del canale: su e giù per leggere, sinistra per tornare ai canali, OK per Guarda");guideScroll.setOnFocusChangeListener((v,focus)->guideScroll.setBackground(surface(Color.rgb(27,28,32),12,focus?accent:Color.rgb(49,50,54))));
        guideScroll.setBackground(surface(Color.rgb(27,28,32),12,Color.rgb(49,50,54)));
        LinearLayout.LayoutParams detailsParams=new LinearLayout.LayoutParams(0,-1,0.42f);detailsParams.leftMargin=dp(10);browser.addView(guideScroll,detailsParams);
        LinearLayout details=new LinearLayout(this);details.setOrientation(LinearLayout.VERTICAL);guideScroll.addView(details,new ScrollView.LayoutParams(-1,-2));
        FrameLayout hero=new FrameLayout(this){@Override protected void onMeasure(int w,int h){super.onMeasure(w,View.MeasureSpec.makeMeasureSpec(Math.max(dp(180),View.MeasureSpec.getSize(w)*9/16),View.MeasureSpec.EXACTLY));}};hero.setTag("programmeArtwork");details.addView(hero,lp(-1,dp(250)));
        guideImage=new ProgrammeArtwork(this);guideImage.setTag("guideImage");guideImage.setScaleType(ImageView.ScaleType.FIT_CENTER);guideImage.setContentDescription("Immagine del programma");hero.addView(guideImage,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout heroInfo=new LinearLayout(this);heroInfo.setOrientation(LinearLayout.VERTICAL);heroInfo.setPadding(dp(16),dp(12),dp(16),dp(16));details.addView(heroInfo,lp(-1,-2));
        guideChannelName=text("Scegli un canale",16);guideChannelName.setTag("guideChannelName");heroInfo.addView(guideChannelName,lp(-1,-2));
        guideTime=text("",12);guideTime.setTextColor(accent);LinearLayout.LayoutParams timeParams=lp(-1,-2);timeParams.topMargin=dp(12);heroInfo.addView(guideTime,timeParams);
        guideTitle=text("",24);guideTitle.setTag("guideTitle");guideTitle.setMaxLines(3);guideTitle.setTypeface(UiTheme.medium(this));heroInfo.addView(guideTitle,lp(-1,-2));
        guideProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);guideProgress.setMax(1000);guideProgress.setProgressTintList(ColorStateList.valueOf(accent));guideProgress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(57,58,62)));LinearLayout.LayoutParams progressParams=lp(-1,dp(3));progressParams.topMargin=dp(10);heroInfo.addView(guideProgress,progressParams);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(16),0,dp(16),dp(16));details.addView(body,lp(-1,-2));
        watch=homeButton("▶ Guarda");watch.setTag("watchChannel");watch.setTextColor(bg);android.graphics.drawable.StateListDrawable watchColors=new android.graphics.drawable.StateListDrawable();watchColors.addState(new int[]{android.R.attr.state_focused},surface(Color.rgb(255,205,111),5,fg));watchColors.addState(new int[]{},surface(accent,5,0));watch.setBackground(watchColors);watch.setId(View.generateViewId());list.setNextFocusRightId(guideScroll.getId());watch.setNextFocusLeftId(list.getId());guideScroll.setNextFocusLeftId(list.getId());body.addView(watch,lp(-1,dp(42)));
        watch.setOnClickListener(v->{if(guideChannel!=null)play(guideChannel);});
        guideDescription=text("",15);guideDescription.setTag("guideDescription");guideDescription.setLineSpacing(dp(3),1);LinearLayout.LayoutParams descriptionParams=lp(-1,-2);descriptionParams.topMargin=dp(16);body.addView(guideDescription,descriptionParams);
        guideNext=text("",14);guideNext.setTextColor(muted);LinearLayout.LayoutParams nextParams=lp(-1,-2);nextParams.topMargin=dp(22);body.addView(guideNext,nextParams);
        Button schedule=homeButton("Oggi e domani  ›");LinearLayout.LayoutParams scheduleParams=lp(-1,dp(44));scheduleParams.topMargin=dp(16);body.addView(schedule,scheduleParams);schedule.setOnClickListener(v->showSchedule(guideChannel));
        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){if(pos>=0&&pos<visible.size())selectHomeChannel(visible.get(pos));}
            public void onNothingSelected(AdapterView<?> parent){}
        });
        updateHomeGuide();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>()){
            @Override public View getView(int pos,View old,android.view.ViewGroup parent){
                return channelCard(visible.get(pos),pos,old,true);
            }
        };adapter.setNotifyOnChange(false);list.setAdapter(adapter);
        TextView hint=homeText("OK: guarda · →: EPG · ↑↓: scorri EPG · ←: canali · Tieni premuto Guida TV: impostazioni EPG",10);hint.setTextColor(muted);hint.setGravity(Gravity.CENTER);content.addView(hint,lp(-1,dp(28)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}
            public void onTextChanged(CharSequence s,int st,int before,int count){filter();}public void afterTextChanged(Editable e){}});
        search.setOnEditorActionListener((t,action,event)->{if(!search.getText().toString().trim().isEmpty())openTvSearch();else list.requestFocus();
            ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);return true;});
        list.setOnItemClickListener((p,v,pos,id2)->{if(pos>=0&&pos<visible.size()){VavooClient.Channel c=visible.get(pos);selectHomeChannel(c);if(!list.isInTouchMode())play(c);}});
        list.setOnItemLongClickListener((p,v,pos,id2)->{
            if(pos>=visible.size())return true;String k=visible.get(pos).key();
            boolean add=!favorites.remove(k);if(add)favorites.add(k);
            prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();filter();
            Toast.makeText(this,add?"Aggiunto ai preferiti":"Rimosso dai preferiti",Toast.LENGTH_SHORT).show();return true;
        });
        favs.setOnClickListener(v->{favoritesOnly=!favoritesOnly;favs.setText(favoritesOnly?"Tutti i canali":"★ Preferiti");favs.setTextColor(favoritesOnly?accent:fg);filter();});
        reload.setOnLongClickListener(v->{startActivity(new Intent(this,BackupActivity.class));return true;});reload.setOnClickListener(v->{VodSession.reset();loadCatalog(true);});
        load();loadEpg(false);uiHandler.postDelayed(epgTick,60000);
    }
    private void filter(){
        if(adapter==null)return;String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        visible.clear();ArrayList<String> names=new ArrayList<>();
        for(VavooClient.Channel c:channels){
            if(!c.isItalian())continue;
            if(!"Tutti".equals(selectedCategory)&&!selectedCategory.equals(ChannelCategories.of(c)))continue;
            if(favoritesOnly&&!favorites.contains(c.key()))continue;
            if(!c.name.toLowerCase(Locale.ROOT).contains(q))continue;
            visible.add(c);names.add((favorites.contains(c.key())?"★  ":"")+DisplayNames.channel(c.name));
        }
        adapter.setNotifyOnChange(false);adapter.clear();adapter.addAll(names);adapter.notifyDataSetChanged();
        VavooClient.Channel match=null;if(guideChannel!=null)for(VavooClient.Channel c:visible)if(c.key().equals(guideChannel.key())){match=c;break;}
        guideChannel=match!=null?match:(visible.isEmpty()?null:visible.get(0));if(guideChannel!=null)list.setSelection(visible.indexOf(guideChannel));updateHomeGuide();
        if(!loading)status.setText(channelSource.label+" · "+selectedCategory+" · "+visible.size()+" canali"+(visible.isEmpty()?" · prova una ricerca diversa":""));
    }
    private void selectHomeChannel(VavooClient.Channel c){
        if(guideChannel!=null&&guideChannel.key().equals(c.key()))return;
        guideChannel=c;if(guideScroll!=null)guideScroll.scrollTo(0,0);updateHomeGuide();
        // Selection changes no data: repaint only the rows currently on screen.
        for(int i=0;i<list.getChildCount();i++){View row=list.getChildAt(i);
            if(row.getTag() instanceof ChannelRow){ChannelRow holder=(ChannelRow)row.getTag();boolean selected=guideChannel.key().equals(holder.channelKey);holder.card.setActivated(selected);holder.card.setBackground(selected?surface(Color.rgb(64,51,30),5,accent):channelSelection());}}

    }
    private void updateHomeGuide(){
        if(guideChannelName==null)return;
        guideChannelName.setText(guideChannel==null?"Nessun canale":DisplayNames.channel(guideChannel.name));watch.setEnabled(guideChannel!=null);
        long now=System.currentTimeMillis();EpgStore.Programme live=epg==null||guideChannel==null?null:epg.now(guideChannel,now),next=epg==null||guideChannel==null?null:epg.next(guideChannel,now);
        if(guideImage!=null){String image=live==null?"":live.image;guideImage.setScaleType(ImageView.ScaleType.FIT_CENTER);artwork.bind(guideImage,image.isEmpty()?(guideChannel==null?"":artwork.logo(guideChannel,epg)):image);}
        navFavorites.setSelected(false);navDirect.setSelected(false);navGuide.setSelected(false);
        guideTime.setText(live==null?"":("IN ONDA · "+time(live.start)+"–"+time(live.end)));
        guideTitle.setText(live==null?(guideChannel==null?"Scegli un'altra categoria":(guideLoading?"Guida in caricamento…":"Programmazione non disponibile")):live.title);
        guideDescription.setText(live==null?(guideChannel==null?"Non ci sono canali per i filtri selezionati.":"La fonte EPG non fornisce un programma in onda per questo canale."):(live.description.trim().isEmpty()?"Trama non disponibile nella guida TV.":live.description));
        guideProgress.setVisibility(live==null?View.GONE:View.VISIBLE);if(live!=null)guideProgress.setProgress((int)Math.max(0,Math.min(1000,(now-live.start)*1000/(live.end-live.start))));
        guideNext.setText(next==null?"":("A SEGUIRE · "+time(next.start)+"–"+time(next.end)+"\n"+next.title+(next.description.trim().isEmpty()?"":"\n\n"+next.description)));
        guideNext.setVisibility(next==null?View.GONE:View.VISIBLE);
    }
    private void applyCatalog(List<VavooClient.Channel> result){
        channels=new ArrayList<>();for(VavooClient.Channel c:result)if(c.isItalian())channels.add(c);
        Iterator<String> saved=favorites.iterator();while(saved.hasNext()){String key=saved.next();if(key.startsWith("gomstream|"))continue;int split=key.indexOf("|");if(split>0&&!new VavooClient.Channel("",key.substring(0,split),"").isItalian())saved.remove();}
        prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();filter();if(player!=null)rebuildPlayerChoices();
    }
    protected void load(){loadCatalog(false);}
    private void chooseChannelSource(){
        new AlertDialog.Builder(this).setTitle("Canali · scegli fonte").setSingleChoiceItems(new String[]{"Vavoo","Gomstream"},channelSource.ordinal(),(d,n)->{d.dismiss();selectSource(ChannelSource.values()[n]);list.requestFocus();}).setNegativeButton("Annulla",null).show();
    }
    private void editChannelUrls(){
        new AlertDialog.Builder(this).setTitle("Canali · cambio URL").setItems(new String[]{"Vavoo","Gomstream","DNS Live"},(d,n)->{if(n==0)VavooSettings.edit(this,()->sourceSettingsChanged(ChannelSource.VAVOO));else if(n==1)GomstreamSettings.edit(this,()->sourceSettingsChanged(ChannelSource.GOMSTREAM));else LiveNetwork.settings(this,()->{});}).setNegativeButton("Annulla",null).show();
    }
    private void sourceSettingsChanged(ChannelSource source){
        if(source==ChannelSource.VAVOO){client.cancelResolve();client=createClient(prefs.getString("device_id",""));catalogStore=new CatalogStore(this);}
        if(source!=channelSource)return;
        if(player!=null)closePlayer();cancelCatalog();channels.clear();guideChannel=null;filter();loadCatalog(true);
    }
    private void selectSource(ChannelSource source){
        if(source==channelSource){filter();loadCatalog(false);return;}
        if(player!=null)closePlayer();cancelCatalog();channelSource=source;prefs.edit().putString("channel_source",source.name()).apply();
        channels.clear();visible.clear();guideChannel=null;filter();scheduleGomRefresh();loadCatalog(false);
    }
    private void cancelCatalog(){++catalogGeneration;if(catalogJob!=null)catalogJob.cancel(true);loading=false;reload.setEnabled(true);}
    private boolean catalogCurrent(int request,ChannelSource source,String daddy){return !destroyed&&request==catalogGeneration&&source==channelSource&&(source!=ChannelSource.GOMSTREAM||daddy.equals(DaddyLiveSettings.home(this)));}
    private void scheduleGomRefresh(){uiHandler.removeCallbacks(gomRefresh);if(resumed&&channelSource==ChannelSource.GOMSTREAM)uiHandler.postDelayed(gomRefresh,600000);}
    protected List<VavooClient.Channel> fetchGomCatalog(String daddy)throws Exception {
        List<VavooClient.Channel> result=new ArrayList<>();for(GomstreamSource.Channel c:GomstreamSource.catalog(daddy,LiveNetwork.gom(this)))result.add(ChannelSource.gomstream(c));
        Collections.sort(result,(a,b)->a.name.compareToIgnoreCase(b.name));return result;
    }
    private void loadCatalog(boolean force){
        if(loading)return;loading=true;reload.setEnabled(false);status.setText(channelSource.label+" · caricamento dei canali italiani…");
        final int request=++catalogGeneration;final ChannelSource source=channelSource;final String daddy=DaddyLiveSettings.home(this);
        final VavooClient requestClient=client;final CatalogStore store=catalogStore;
        if(source==ChannelSource.GOMSTREAM){
            if(!catalogDaddyHome.equals(daddy)){gomCatalog.clear();catalogDaddyHome=daddy;}
            if(!gomCatalog.isEmpty()){applyCatalog(gomCatalog);status.setText("Gomstream · aggiornamento in corso…");}
            catalogJob=gomWorker.submit(()->{try{List<VavooClient.Channel> result=fetchGomCatalog(daddy);runOnUiThread(()->{
                if(!catalogCurrent(request,source,daddy))return;gomCatalog=new ArrayList<>(result);loading=false;reload.setEnabled(true);applyCatalog(result);
            });}catch(Exception e){runOnUiThread(()->{if(!catalogCurrent(request,source,daddy))return;loading=false;reload.setEnabled(true);status.setText(channels.isEmpty()?"Gomstream · catalogo non disponibile. Premi Aggiorna.":"Gomstream · canali disponibili, aggiornamento non riuscito. Premi Aggiorna.");});}});return;
        }
        catalogJob=worker.submit(()->{
            CatalogStore.Snapshot cached=store.read();final boolean haveCache=!cached.channels.isEmpty();
            if(!force&&cached.fresh(System.currentTimeMillis())){
                runOnUiThread(()->{if(!catalogCurrent(request,source,daddy))return;loading=false;reload.setEnabled(true);applyCatalog(cached.channels);});return;
            }
            if(haveCache)runOnUiThread(()->{if(!catalogCurrent(request,source,daddy))return;applyCatalog(cached.channels);status.setText("Vavoo · canali disponibili, aggiornamento in corso…");});
            try{
                List<VavooClient.Channel> result=requestClient.catalog(new VavooClient.Progress(){
                    public void update(int count){}
                    public void updateChannels(List<VavooClient.Channel> partial){if(haveCache)return;
                        runOnUiThread(()->{if(!catalogCurrent(request,source,daddy)||!loading)return;applyCatalog(partial);status.setText("Vavoo · "+partial.size()+" canali disponibili, caricamento in corso…");});}
                });
                if(!Thread.currentThread().isInterrupted())try{store.save(result,System.currentTimeMillis());}catch(Exception e){android.util.Log.w("TVSatPlayer","Salvataggio catalogo non riuscito",e);}
                runOnUiThread(()->{if(!catalogCurrent(request,source,daddy))return;loading=false;reload.setEnabled(true);applyCatalog(result);});
            }catch(Exception e){runOnUiThread(()->{if(!catalogCurrent(request,source,daddy))return;loading=false;reload.setEnabled(true);
                if(!channels.isEmpty())status.setText("Vavoo · canali disponibili, aggiornamento non riuscito. Premi Aggiorna.");
                else {status.setText("Vavoo · catalogo non disponibile. Premi Aggiorna.");error("Caricamento non riuscito",e);}});}
        });
    }
    private void error(String title,Exception e){
        String msg=e instanceof javax.net.ssl.SSLException?"Il certificato HTTPS del servizio non è valido.":e.getMessage();
        if(msg==null)msg="Errore di connessione";
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK",null).show();
    }
    protected void play(VavooClient.Channel channel){
        if(player==null){zapChannels=new ArrayList<>(visible);if(zapChannels.isEmpty())zapChannels.add(channel);showPlayer();}
        if(currentChannel!=null&&currentChannel.key().equals(channel.key())&&requestedChannel==null){hideChannelPanel();showPlayerControls();return;}
        gomRetries=0;vavooRetries=0;hideChannelPanel();requestedChannel=channel;final int generation=++playGeneration;
        ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);
        buffering.setVisibility(View.VISIBLE);updatePlayerInfo();showPlayerControls();
        if(resolveJob!=null){client.cancelResolve();resolveJob.cancel(true);}
        if(channel.source==ChannelSource.GOMSTREAM&&engine!=null)engine.stop();
        resolveChannel(channel,generation);
    }
    protected void resolveChannel(VavooClient.Channel channel,int generation){
        resolveJob=playbackWorker.submit(()->{
            try{String url=resolveStream(channel);runOnUiThread(()->{
                if(destroyed||generation!=playGeneration||video==null)return;
                currentChannel=channel;requestedChannel=null;updatePlayerInfo();
                engine.setMediaItem(mediaItem(channel,url));engine.prepare();engine.setPlayWhenReady(resumed);resumePlayback=!resumed;if(channelPanel.getVisibility()!=View.VISIBLE)video.requestFocus();
            });}catch(Exception e){runOnUiThread(()->{if(destroyed||generation!=playGeneration)return;
                requestedChannel=null;if(buffering!=null)buffering.setVisibility(View.GONE);updatePlayerInfo();
                error("Canale non disponibile",e);
            });}
        });
    }
    protected String resolveStream(VavooClient.Channel channel)throws Exception {
        if(channel.source==ChannelSource.GOMSTREAM)return GomstreamSource.resolve(new GomstreamSource.Channel(channel.url,channel.name),DaddyLiveSettings.home(this),GomstreamSettings.home(this),LiveNetwork.gom(this));
        return client.resolve(channel);
    }
    static MediaItem mediaItem(VavooClient.Channel channel,String url){
        MediaItem.Builder b=new MediaItem.Builder().setUri(url).setMediaId(channel.key());
        if(channel.source==ChannelSource.GOMSTREAM)b.setMimeType(MimeTypes.APPLICATION_M3U8).setLiveConfiguration(new MediaItem.LiveConfiguration.Builder().setTargetOffsetMs(18000).setMinPlaybackSpeed(1f).setMaxPlaybackSpeed(1f).build());
        return b.build();
    }
    private void showPlayer(){
        home.setVisibility(View.GONE);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        player=new FrameLayout(this);player.setTag("player");player.setBackgroundColor(Color.BLACK);root.addView(player,new FrameLayout.LayoutParams(-1,-1));
        engine=createPlayer();video=new PlayerView(this);video.setUseController(false);video.setKeepContentOnPlayerReset(true);video.setPlayer(engine);video.setResizeMode(prefs.getInt("resize",AspectRatioFrameLayout.RESIZE_MODE_FIT));video.setTag("video");video.setFocusable(true);player.addView(video,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));
        buffering=new ProgressBar(this);player.addView(buffering,new FrameLayout.LayoutParams(dp(56),dp(56),Gravity.CENTER));
        playerHeader=new LinearLayout(this);playerHeader.setTag("playerHeader");playerHeader.setOrientation(LinearLayout.VERTICAL);playerHeader.setPadding(dp(20),dp(12),dp(20),dp(12));
        playerHeader.setBackgroundColor(0xAA151619);player.addView(playerHeader,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
        playerTitle=text("",22);playerTitle.setMaxLines(2);playerHeader.addView(playerTitle,lp(-1,-2));
        playerEpg=text("",15);playerEpg.setMaxLines(3);playerHeader.addView(playerEpg,lp(-1,-2));
        playerControls=new LinearLayout(this);playerControls.setTag("playerControls");playerControls.setOrientation(LinearLayout.VERTICAL);playerControls.setPadding(dp(10),dp(6),dp(10),dp(6));playerControls.setBackgroundColor(0xDD151619);
        player.addView(playerControls,new FrameLayout.LayoutParams(-1,dp(112),Gravity.BOTTOM));
        LinearLayout controls=new LinearLayout(this);playerControls.addView(controls,lp(-1,dp(48)));
        Button previous=button("CH−");previous.setContentDescription("Canale precedente");controls.addView(previous,new LinearLayout.LayoutParams(0,-1,1));previous.setOnClickListener(v->zap(-1));
        selectChannels=button("Canali");controls.addView(selectChannels,new LinearLayout.LayoutParams(0,-1,2));selectChannels.setOnClickListener(v->showChannelPanel());
        pauseButton=button("Pausa");controls.addView(pauseButton,new LinearLayout.LayoutParams(0,-1,2));pauseButton.setOnClickListener(v->togglePause());
        Button next=button("CH+");next.setContentDescription("Canale successivo");controls.addView(next,new LinearLayout.LayoutParams(0,-1,1));next.setOnClickListener(v->zap(1));
        Button guide=button("EPG");controls.addView(guide,new LinearLayout.LayoutParams(0,-1,1));guide.setOnClickListener(v->showGuide());
        LinearLayout extras=new LinearLayout(this);playerControls.addView(extras,lp(-1,dp(46)));
        seekBack=button("−10 s");seekBack.setTag("seekBack");extras.addView(seekBack,new LinearLayout.LayoutParams(0,-1,1));seekBack.setOnClickListener(v->seekBy(-10000));
        seekForward=button("+10 s");seekForward.setTag("seekForward");extras.addView(seekForward,new LinearLayout.LayoutParams(0,-1,1));seekForward.setOnClickListener(v->seekBy(10000));
        Button live=button("Diretta");extras.addView(live,new LinearLayout.LayoutParams(0,-1,1));live.setOnClickListener(v->{if(engine!=null&&engine.isCommandAvailable(Player.COMMAND_SEEK_TO_DEFAULT_POSITION))engine.seekToDefaultPosition();showPlayerControls();});
        Button audio=button("Audio");extras.addView(audio,new LinearLayout.LayoutParams(0,-1,1));audio.setOnClickListener(v->audioOptions());
        Button format=button("Formato");extras.addView(format,new LinearLayout.LayoutParams(0,-1,1));format.setOnClickListener(v->formatOptions());
        for(int i=0;i<controls.getChildCount();i++)controls.getChildAt(i).setOnFocusChangeListener((v,focus)->{if(focus)scheduleHide();});
        for(int i=0;i<extras.getChildCount();i++)extras.getChildAt(i).setOnFocusChangeListener((v,focus)->{if(focus)scheduleHide();});
        channelPanel=new LinearLayout(this);channelPanel.setTag("channelPanel");channelPanel.setOrientation(LinearLayout.VERTICAL);channelPanel.setPadding(dp(10),dp(10),dp(10),dp(10));
        channelPanel.setBackgroundColor(0xF2151619);int width=Math.min(dp(380),(int)(getResources().getDisplayMetrics().widthPixels*.78));
        player.addView(channelPanel,new FrameLayout.LayoutParams(width,-1,Gravity.RIGHT));
        playerCategories=new Spinner(this,Spinner.MODE_DROPDOWN);playerCategories.setTag("playerCategories");playerCategories.setBackground(homeSelection());channelPanel.addView(playerCategories,lp(-1,dp(44)));
        ArrayAdapter<String> catAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ChannelCategories.ALL);catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);playerCategories.setAdapter(catAdapter);playerCategory=selectedCategory;playerCategories.setSelection(Math.max(0,Arrays.asList(ChannelCategories.ALL).indexOf(playerCategory)));
        playerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){playerCategory=ChannelCategories.ALL[pos];rebuildPlayerChoices();resetListTimer();}public void onNothingSelected(AdapterView<?> p){}});
        playerFavorites=new CheckBox(this);playerFavorites.setTag("playerFavorites");playerFavorites.setText("Solo preferiti");playerFavorites.setTextColor(fg);playerFavorites.setChecked(favoritesOnly);channelPanel.addView(playerFavorites,lp(-1,dp(40)));playerFavorites.setOnCheckedChangeListener((b,checked)->{rebuildPlayerChoices();resetListTimer();});
        Button returnToVideo=homeButton("Torna al video");channelPanel.addView(returnToVideo,lp(-1,dp(50)));returnToVideo.setOnClickListener(v->{hideChannelPanel();showPlayerControls();});
        playerList=new ListView(this);playerList.setTag("playerChannels");playerList.setDivider(new android.graphics.drawable.ColorDrawable(Color.rgb(49,50,54)));playerList.setDividerHeight(dp(1));android.graphics.drawable.StateListDrawable playerFocus=new android.graphics.drawable.StateListDrawable();playerFocus.addState(new int[]{android.R.attr.state_focused},surface(0x22F2B245,5,accent));playerFocus.addState(new int[]{android.R.attr.state_pressed},surface(0x22F2B245,5,accent));playerFocus.addState(new int[]{},new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));playerList.setSelector(playerFocus);playerList.setDrawSelectorOnTop(true);
        channelPanel.addView(playerList,new LinearLayout.LayoutParams(-1,0,1));channelPanel.setVisibility(View.GONE);
        playerList.setOnScrollListener(new AbsListView.OnScrollListener(){
            public void onScrollStateChanged(AbsListView v,int state){if(state!=SCROLL_STATE_IDLE)uiHandler.removeCallbacks(hideChannelList);else resetListTimer();}
            public void onScroll(AbsListView v,int first,int count,int total){}
        });
        playerList.setOnItemClickListener((p,v,pos,id)->{if(pos>=0&&pos<zapChannels.size())play(zapChannels.get(pos));});
        playerList.setOnItemLongClickListener((p,v,pos,id)->{if(pos<0||pos>=zapChannels.size())return true;String key=zapChannels.get(pos).key();if(!favorites.remove(key))favorites.add(key);prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();filter();rebuildPlayerChoices();resetListTimer();return true;});
        engine.addListener(new Player.Listener(){
            @Override public void onEvents(Player p,Player.Events events){if(p!=engine)return;if(buffering!=null)buffering.setVisibility(p.getPlaybackState()==Player.STATE_BUFFERING||requestedChannel!=null?View.VISIBLE:View.GONE);updatePauseLabel();updateSeekButtons();}
            @Override public void onPlayerError(PlaybackException failure){
                if(requestedChannel!=null||destroyed||player==null)return;
                if(currentChannel!=null&&currentChannel.source==ChannelSource.GOMSTREAM&&resumed&&gomRetries<2){
                    final VavooClient.Channel retry=currentChannel;final int generation=playGeneration;++gomRetries;if(buffering!=null)buffering.setVisibility(View.VISIBLE);
                    uiHandler.postDelayed(()->{if(destroyed||!resumed||engine==null||generation!=playGeneration)return;requestedChannel=retry;engine.stop();updatePlayerInfo();resolveChannel(retry,++playGeneration);},gomRetries*2000L);return;
                }
                if(currentChannel!=null&&currentChannel.source==ChannelSource.VAVOO&&resumed&&vavooRetries<1&&!accessDenied(failure)){final VavooClient.Channel retry=currentChannel;final int generation=playGeneration;++vavooRetries;uiHandler.postDelayed(()->{if(destroyed||!resumed||engine==null||generation!=playGeneration)return;requestedChannel=retry;engine.stop();resolveChannel(retry,++playGeneration);},2000);return;}
                if(buffering!=null)buffering.setVisibility(View.GONE);android.util.Log.w("RITATV","Riproduzione non riuscita",failure);
                VavooClient.Channel failed=currentChannel;new AlertDialog.Builder(MainActivity.this).setTitle("Video non disponibile").setMessage("Il flusso è momentaneamente indisponibile o non supportato. Puoi scegliere un altro canale o riprovare.").setPositiveButton("Riprova",(d,w)->{if(failed!=null){currentChannel=null;play(failed);}}).setNegativeButton("Chiudi",null).show();}
        });
        updateSeekButtons();rebuildPlayerChoices();
        video.setOnTouchListener((v,event)->{if(event.getAction()==android.view.MotionEvent.ACTION_UP){if(playerControls.getVisibility()==View.VISIBLE)hidePlayerControls();else showPlayerControls();}return true;});
        video.requestFocus();if(epg==null)loadEpg(false);
    }
    private void showPlayerControls(){
        if(playerControls==null||channelPanel.getVisibility()==View.VISIBLE)return;
        playerControls.setVisibility(View.VISIBLE);playerHeader.setVisibility(View.VISIBLE);updatePlayerInfo();scheduleHide();
    }
    private void scheduleHide(){uiHandler.removeCallbacks(hideControls);uiHandler.postDelayed(hideControls,5000);}
    private void hidePlayerControls(){
        if(playerControls==null)return;playerControls.setVisibility(View.GONE);playerHeader.setVisibility(View.GONE);
        if(channelPanel.getVisibility()!=View.VISIBLE&&video!=null)video.requestFocus();
    }
    private void showChannelPanel(){
        if(player==null)return;uiHandler.removeCallbacks(hideControls);hidePlayerControls();refreshPlayerList();
        channelPanel.setVisibility(View.VISIBLE);int index=indexOfCurrent();playerList.setSelection(Math.max(0,index));playerList.requestFocus();resetListTimer();
    }
    private void hideChannelPanel(){uiHandler.removeCallbacks(hideChannelList);if(channelPanel!=null)channelPanel.setVisibility(View.GONE);if(video!=null)video.requestFocus();}
    private void refreshPlayerList(){
        if(playerList==null)return;List<String> names=new ArrayList<>();long time=System.currentTimeMillis();
        for(VavooClient.Channel c:zapChannels){String name=(currentChannel!=null&&c.key().equals(currentChannel.key())?"▶  ":"")+DisplayNames.channel(c.name);
            EpgStore.Programme p=epg==null?null:epg.now(c,time);if(p!=null)name+="\n"+p.title;names.add(name);}
        ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names){
            @Override public View getView(int pos,View old,ViewGroup parent){return channelCard(zapChannels.get(pos),pos,old,false);}
        };int selected=playerList.getSelectedItemPosition(),first=playerList.getFirstVisiblePosition();View top=playerList.getChildAt(0);int offset=top==null?0:top.getTop();playerList.setAdapter(a);if(selected>=0&&playerList.isFocused())playerList.setSelection(selected);else playerList.setSelectionFromTop(first,offset);
    }
    private int indexOfCurrent(){VavooClient.Channel c=requestedChannel!=null?requestedChannel:currentChannel;if(c==null)return 0;
        for(int i=0;i<zapChannels.size();i++)if(zapChannels.get(i).key().equals(c.key()))return i;return -1;}
    private void zap(int direction){if(zapChannels.isEmpty())return;int current=indexOfCurrent();int index=current<0?(direction>0?0:zapChannels.size()-1):(current+direction+zapChannels.size())%zapChannels.size();play(zapChannels.get(index));}
    private static boolean accessDenied(Throwable e){for(Throwable t=e;t!=null;t=t.getCause())if(t instanceof androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException){int code=((androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException)t).responseCode;if(code==401||code==403||code==429)return true;}return false;}
    protected Player createPlayer(){
        androidx.media3.datasource.okhttp.OkHttpDataSource.Factory http=new androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(LiveNetwork.client(this)).setUserAgent("VAVOO/2.6");
        audioDelay=AudioDelay.create(this,"live");ExoPlayer.Builder builder=new ExoPlayer.Builder(this,AudioDelay.renderers(this,audioDelay));
        if(channelSource==ChannelSource.GOMSTREAM){androidx.media3.datasource.okhttp.OkHttpDataSource.Factory gomHttp=new androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(LiveNetwork.client(this)).setUserAgent("R-ITA-TV/1.25");
            builder.setMediaSourceFactory(new HlsMediaSource.Factory(type->{DataSource upstream=gomHttp.createDataSource();return type==C.DATA_TYPE_MEDIA?new GomstreamDataSource(upstream):type==C.DATA_TYPE_MANIFEST?new GomstreamManifestDataSource(upstream):upstream;}));
        }else builder.setMediaSourceFactory(new DefaultMediaSourceFactory(http).setLoadErrorHandlingPolicy(new StreamErrorPolicy(client)));
        ExoPlayer p=builder.build();
        p.setAudioAttributes(new androidx.media3.common.AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),true);p.setHandleAudioBecomingNoisy(true);p.setTrackSelectionParameters(p.getTrackSelectionParameters().buildUpon().setPreferredAudioLanguage("it").build());return p;
    }
    private void rebuildPlayerChoices(){
        if(playerList==null)return;List<VavooClient.Channel> source=channels.isEmpty()?new ArrayList<>(zapChannels):channels;playerVisible=new ArrayList<>();
        for(VavooClient.Channel c:source){if(!c.isItalian())continue;if(!"Tutti".equals(playerCategory)&&!playerCategory.equals(ChannelCategories.of(c)))continue;if(playerFavorites!=null&&playerFavorites.isChecked()&&!favorites.contains(c.key()))continue;playerVisible.add(c);}
        zapChannels=new ArrayList<>(playerVisible);refreshPlayerList();
    }
    private void togglePause(){if(engine==null)return;if(engine.getPlayWhenReady())engine.pause();else if(resumed)engine.play();updatePauseLabel();showPlayerControls();}
    private void updatePauseLabel(){if(pauseButton!=null&&engine!=null)pauseButton.setText(engine.getPlayWhenReady()?"Pausa":"Riprendi");}
    private boolean canSeek(){return engine!=null&&requestedChannel==null&&engine.getPlaybackState()==Player.STATE_READY&&engine.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)&&engine.isCurrentMediaItemSeekable();}
    private void updateSeekButtons(){boolean enabled=canSeek();for(Button b:new Button[]{seekBack,seekForward})if(b!=null){b.setEnabled(enabled);b.setAlpha(enabled?1f:.35f);b.setContentDescription(enabled?b.getText():"Spostamento non disponibile per questo flusso");}}
    private void seekBy(long amount){if(!canSeek())return;long position=Math.max(0,engine.getCurrentPosition()+amount),duration=engine.getDuration();if(duration!=C.TIME_UNSET&&duration>=0)position=Math.min(position,duration);engine.seekTo(position);showPlayerControls();}
    private void audioOptions(){if(engine==null)return;if(audioDelay==null)audioDelay=AudioDelay.create(this,"live");List<TrackSelectionOverride> overrides=new ArrayList<>();List<String> names=new ArrayList<>();names.add("Automatico · italiano preferito");names.add("Sincronizzazione audio · "+audioDelay.get()+" ms");
        for(Tracks.Group group:engine.getCurrentTracks().getGroups())if(group.getType()==C.TRACK_TYPE_AUDIO)for(int i=0;i<group.length;i++)if(group.isTrackSupported(i)){Format f=group.getTrackFormat(i);names.add((f.label==null?"Traccia "+(overrides.size()+1):f.label)+(f.language==null?"":" · "+f.language));overrides.add(new TrackSelectionOverride(group.getMediaTrackGroup(),i));}
        uiHandler.removeCallbacks(hideControls);new AlertDialog.Builder(this).setTitle("Audio").setItems(names.toArray(new String[0]),(d,n)->{if(engine==null)return;if(n==1){AudioDelay.menu(this,"live",audioDelay,engine);return;}TrackSelectionParameters.Builder b=engine.getTrackSelectionParameters().buildUpon().clearOverridesOfType(C.TRACK_TYPE_AUDIO).setPreferredAudioLanguage("it");if(n>1)b.setOverrideForType(overrides.get(n-2));engine.setTrackSelectionParameters(b.build());}).setOnDismissListener(d->showPlayerControls()).show();}
    private void formatOptions(){uiHandler.removeCallbacks(hideControls);new AlertDialog.Builder(this).setTitle("Formato video").setItems(new String[]{"Adatta · proporzioni originali","Zoom · ritaglia i bordi","Riempi lo schermo"},(d,n)->{int mode=new int[]{AspectRatioFrameLayout.RESIZE_MODE_FIT,AspectRatioFrameLayout.RESIZE_MODE_ZOOM,AspectRatioFrameLayout.RESIZE_MODE_FILL}[n];prefs.edit().putInt("resize",mode).apply();if(video!=null)video.setResizeMode(mode);}).setOnDismissListener(d->showPlayerControls()).show();}
    private String time(long value){return new java.text.SimpleDateFormat("HH:mm",Locale.ITALIAN).format(new Date(value));}
    private void updatePlayerInfo(){
        if(playerTitle==null)return;VavooClient.Channel c=requestedChannel!=null?requestedChannel:currentChannel;
        playerTitle.setText(c==null?"R. ITA TV":DisplayNames.channel(c.name)+(requestedChannel!=null?" · apertura…":""));
        updateSeekButtons();
        if(c==null){playerEpg.setText(epgMessage);return;}
        if(epg==null){playerEpg.setText(epgMessage);return;}
        long now=System.currentTimeMillis();EpgStore.Programme live=epg.now(c,now),next=epg.next(c,now);
        String info=live==null?"Ora: informazioni non disponibili":"Ora  "+time(live.start)+"–"+time(live.end)+"  "+live.title;
        if(next!=null)info+="\nDopo  "+time(next.start)+"  "+next.title;
        if(epg.latestEnd<now)info="La fonte EPG non è aggiornata";
        playerEpg.setText(info);
    }
    protected void loadEpg(boolean force){
        if(guideLoading)return;guideLoading=true;epgMessage="EPG in caricamento…";updatePlayerInfo();updateHomeGuide();if(adapter!=null)adapter.notifyDataSetChanged();
        final String source=prefs.getString("epg_url",EpgStore.DEFAULT_URL);
        guideWorker.execute(()->{try{EpgStore result=new EpgStore(this);result.load(source,force);
            runOnUiThread(()->{if(destroyed)return;guideLoading=false;epg=result;epgMessage=result.latestEnd<System.currentTimeMillis()?"Fonte EPG non aggiornata":"EPG aggiornata";
                updatePlayerInfo();updateHomeGuide();refreshPlayerList();if(adapter!=null)adapter.notifyDataSetChanged();if(force)Toast.makeText(this,epgMessage,Toast.LENGTH_SHORT).show();});
        }catch(Exception e){runOnUiThread(()->{if(destroyed)return;guideLoading=false;epgMessage="EPG non disponibile";updatePlayerInfo();updateHomeGuide();if(adapter!=null)adapter.notifyDataSetChanged();if(force)error("Guida TV",e);});}});
    }
    private void epgSettings(){
        if(guideLoading){Toast.makeText(this,"Attendi il caricamento della guida",Toast.LENGTH_SHORT).show();return;}
        new AlertDialog.Builder(this).setTitle("Guida TV italiana").setItems(new String[]{"Aggiorna guida","Modifica fonte XMLTV","Ripristina fonte predefinita"},(dialog,which)->{
            if(which==0)loadEpg(true);
            else if(which==2){prefs.edit().remove("epg_url").apply();loadEpg(true);}
            else {EditText input=new EditText(this);input.setSingleLine(true);input.setText(prefs.getString("epg_url",EpgStore.DEFAULT_URL));
                new AlertDialog.Builder(this).setTitle("Fonte XMLTV HTTPS").setView(input).setPositiveButton("Salva",(d,w)->{
                    String url=input.getText().toString().trim();try{java.net.URI u=new java.net.URI(url);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null)throw new Exception();
                        prefs.edit().putString("epg_url",url).apply();epg=null;loadEpg(true);
                    }catch(Exception e){Toast.makeText(this,"Inserisci un URL HTTPS valido",Toast.LENGTH_LONG).show();}
                }).setNegativeButton("Annulla",null).show();}
        }).show();
    }
    private void showGuide(){showSchedule(currentChannel!=null?currentChannel:requestedChannel);}
    private void showSchedule(VavooClient.Channel c){
        if(c==null)return;List<EpgStore.Programme> programmes=new ArrayList<>();List<String> labels=new ArrayList<>();long now=System.currentTimeMillis();
        if(epg!=null)for(EpgStore.Programme p:epg.guide(c))if(p.end>now){programmes.add(p);labels.add(new java.text.SimpleDateFormat("EEE dd/MM",Locale.ITALIAN).format(new Date(p.start))+"  "+time(p.start)+"–"+time(p.end)+"\n"+p.title);}
        uiHandler.removeCallbacks(hideControls);
        if(programmes.isEmpty()){new AlertDialog.Builder(this).setTitle(DisplayNames.channel(c.name)+" · Guida TV").setMessage(epg==null?epgMessage:"Programmazione non disponibile").setPositiveButton("OK",null).setOnDismissListener(d->showPlayerControls()).show();return;}
        new AlertDialog.Builder(this).setTitle(DisplayNames.channel(c.name)+" · Oggi e domani").setItems(labels.toArray(new String[0]),(dialog,n)->showProgramme(c,programmes.get(n))).setNegativeButton("Chiudi",null).setOnDismissListener(d->showPlayerControls()).show();
    }
    private void showProgramme(VavooClient.Channel c,EpgStore.Programme p){
        ScrollView scroll=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(12),dp(20),dp(16));scroll.addView(body);
        ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);body.addView(image,lp(-1,dp(200)));artwork.bind(image,p.image.isEmpty()?artwork.logo(c,epg):p.image);
        TextView description=text(time(p.start)+"–"+time(p.end)+"\n\n"+(p.description.isEmpty()?"Trama non disponibile.":p.description),16);body.addView(description,lp(-1,-2));
        uiHandler.removeCallbacks(hideControls);new AlertDialog.Builder(this).setTitle(p.title).setView(scroll).setPositiveButton("Chiudi",null).setOnDismissListener(d->showPlayerControls()).show();
    }
    private void closePlayerViews(){
        uiHandler.removeCallbacks(hideControls);uiHandler.removeCallbacks(hideChannelList);if(video!=null){video.setPlayer(null);video=null;}if(engine!=null){engine.release();engine=null;}seekBack=null;seekForward=null;selectChannels=null;playerCategories=null;playerFavorites=null;buffering=null;
        if(player!=null&&root!=null)root.removeView(player);player=null;playerHeader=null;playerControls=null;channelPanel=null;playerList=null;playerTitle=null;playerEpg=null;pauseButton=null;
    }
    private void closePlayer(){playGeneration++;if(resolveJob!=null){client.cancelResolve();resolveJob.cancel(true);}currentChannel=null;requestedChannel=null;closePlayerViews();home.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(0);list.requestFocus();}
    @Override public void onBackPressed(){if(player!=null){if(channelPanel.getVisibility()==View.VISIBLE)closePlayer();else showChannelPanel();}else if(!homeBackArmed){homeBackArmed=true;navDirect.requestFocus();}else finish();}
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent e){if(e.getAction()==android.view.MotionEvent.ACTION_DOWN)homeBackArmed=false;if(player!=null&&channelPanel!=null&&channelPanel.getVisibility()==View.VISIBLE){if(e.getAction()==android.view.MotionEvent.ACTION_DOWN)uiHandler.removeCallbacks(hideChannelList);else if(e.getAction()==android.view.MotionEvent.ACTION_UP||e.getAction()==android.view.MotionEvent.ACTION_CANCEL)resetListTimer();}return super.dispatchTouchEvent(e);}
    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getKeyCode()!=KeyEvent.KEYCODE_BACK)homeBackArmed=false;
        if(player==null&&list!=null&&guideScroll!=null){int key=e.getKeyCode();boolean down=e.getAction()==KeyEvent.ACTION_DOWN;
            if(list.isFocused()&&key==KeyEvent.KEYCODE_DPAD_RIGHT){if(down)guideScroll.requestFocus();return true;}
            if(guideScroll.hasFocus()&&key==KeyEvent.KEYCODE_DPAD_LEFT){if(down)list.requestFocus();return true;}
            if(guideScroll.isFocused()){
                if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN){if(down)guideScroll.scrollBy(0,dp(key==KeyEvent.KEYCODE_DPAD_DOWN?80:-80));return true;}
                if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_DPAD_RIGHT){if(down&&e.getRepeatCount()==0)watch.requestFocus();return true;}
            }
        }

        if(player!=null&&e.getAction()==KeyEvent.ACTION_DOWN){if(channelPanel!=null&&channelPanel.getVisibility()==View.VISIBLE)resetListTimer();int key=e.getKeyCode();
            if(key==KeyEvent.KEYCODE_CHANNEL_UP||key==KeyEvent.KEYCODE_CHANNEL_DOWN){if(e.getRepeatCount()==0)zap(key==KeyEvent.KEYCODE_CHANNEL_UP?1:-1);return true;}
            if(key==KeyEvent.KEYCODE_MEDIA_REWIND||key==KeyEvent.KEYCODE_MEDIA_FAST_FORWARD){if(e.getRepeatCount()==0)seekBy(key==KeyEvent.KEYCODE_MEDIA_REWIND?-10000:10000);return true;}
            if(key==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){if(e.getRepeatCount()==0)togglePause();return true;}
            if(channelPanel.getVisibility()!=View.VISIBLE){boolean buttons=playerControls.getVisibility()==View.VISIBLE&&getCurrentFocus()!=video;
                if(!buttons&&(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN)){
                    if(e.getRepeatCount()==0)zap(key==KeyEvent.KEYCODE_DPAD_UP?1:-1);return true;}
                if(!buttons&&(key==KeyEvent.KEYCODE_DPAD_RIGHT||key==KeyEvent.KEYCODE_DPAD_LEFT)){
                    if(e.getRepeatCount()==0)seekBy(key==KeyEvent.KEYCODE_DPAD_RIGHT?10000:-10000);return true;}
                if(!buttons&&(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)){showPlayerControls();selectChannels.requestFocus();return true;}
            }
        }
        return super.dispatchKeyEvent(e);
    }
    private void openTvSearch(){org.json.JSONArray data=new org.json.JSONArray();for(VavooClient.Channel c:channels)try{data.put(new org.json.JSONObject().put("name",c.name).put("key",c.key()));}catch(Exception ignored){}startActivityForResult(new Intent(this,TvSearchActivity.class).putExtra("channels",data.toString()).putExtra("query",search.getText().toString().trim()),203);}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==203&&result==RESULT_OK&&data!=null){String key=data.getStringExtra("channelKey");for(VavooClient.Channel c:channels)if(c.key().equals(key)){play(c);break;}}}
    @Override protected void onResume(){super.onResume();homeBackArmed=false;resumed=true;uiHandler.removeCallbacks(vavooRenew);uiHandler.postDelayed(vavooRenew,VavooClient.RENEW_INTERVAL_MS);long restored=getSharedPreferences("backup",0).getLong("restoredAt",0);if(restored!=seenBackup){seenBackup=restored;favorites=new HashSet<>(prefs.getStringSet("favorites",new HashSet<>()));selectedCategory=prefs.getString("category","Tutti");int categoryIndex=Arrays.asList(ChannelCategories.ALL).indexOf(selectedCategory);categories.setSelection(Math.max(0,categoryIndex));client.cancelResolve();client=createClient(prefs.getString("device_id",""));catalogStore=new CatalogStore(this);gomCatalog.clear();cancelCatalog();ChannelSource updated=ChannelSource.saved(this);if(updated!=channelSource)selectSource(updated);else{filter();loadCatalog(true);}epg=new EpgStore(this);loadEpg(true);}if(engine!=null&&resumePlayback)engine.play();
        if(channelSource==ChannelSource.GOMSTREAM&&homePaused){if(!catalogDaddyHome.equals(DaddyLiveSettings.home(this))){cancelCatalog();gomCatalog.clear();channels.clear();guideChannel=null;filter();}loadCatalog(false);}homePaused=false;scheduleGomRefresh();}
    @Override protected void onPause(){resumed=false;homePaused=true;uiHandler.removeCallbacks(vavooRenew);uiHandler.removeCallbacks(gomRefresh);if(engine!=null){resumePlayback=engine.getPlayWhenReady();engine.pause();}super.onPause();}
    @Override protected void onDestroy(){destroyed=true;playGeneration++;uiHandler.removeCallbacksAndMessages(null);worker.shutdownNow();gomWorker.shutdownNow();playbackWorker.shutdownNow();guideWorker.shutdownNow();sessionWorker.shutdownNow();if(artwork!=null)artwork.close();closePlayerViews();super.onDestroy();}
}
