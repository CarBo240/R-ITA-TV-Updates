package it.carmine.streamplayer;
import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.*;
/** Modal choice list with explicit remote handling, instead of a TV-unfriendly popup window. */
final class EventFilterSpinner extends Spinner {
 EventFilterSpinner(Context context){super(context,Spinner.MODE_DIALOG);setFocusable(true);setFocusableInTouchMode(true);}
 @Override public boolean performClick(){if(getAdapter()==null||getAdapter().getCount()==0)return false;String[] labels=new String[getAdapter().getCount()];for(int i=0;i<labels.length;i++)labels[i]=String.valueOf(getAdapter().getItem(i));int[] selected={Math.max(0,getSelectedItemPosition())};boolean[] enterPressed={false};AlertDialog dialog=new AlertDialog.Builder(getContext()).setTitle(getContentDescription()).setSingleChoiceItems(labels,selected[0],(d,n)->{setSelection(n);d.dismiss();requestFocus();}).setNegativeButton("Annulla",null).create();dialog.setOnShowListener(d->{ListView list=dialog.getListView();list.setItemsCanFocus(false);list.setFocusable(true);list.setFocusableInTouchMode(true);list.setSelection(selected[0]);list.setItemChecked(selected[0],true);list.requestFocus();});dialog.setOnKeyListener((d,key,event)->{if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN){if(event.getAction()==KeyEvent.ACTION_DOWN){selected[0]=Math.max(0,Math.min(labels.length-1,selected[0]+(key==KeyEvent.KEYCODE_DPAD_DOWN?1:-1)));dialog.getListView().setSelection(selected[0]);dialog.getListView().setItemChecked(selected[0],true);}return true;}if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_NUMPAD_ENTER){if(event.getAction()==KeyEvent.ACTION_DOWN){enterPressed[0]=true;}else if(event.getAction()==KeyEvent.ACTION_UP&&enterPressed[0]){setSelection(selected[0]);dialog.dismiss();requestFocus();}return true;}return false;});dialog.setOnDismissListener(d->post(this::requestFocus));dialog.show();return true;}
}
