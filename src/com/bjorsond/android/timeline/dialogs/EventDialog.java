/*******************************************************************************
 * Copyright (c) 2011 Andreas Storlien and Anders Kristiansen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Andreas Storlien and Anders Kristiansen - initial API and implementation
 ******************************************************************************/
package com.bjorsond.android.timeline.dialogs;

import java.util.ArrayList;
import java.util.List;

import net.sondbjor.android.ActionItem;
import net.sondbjor.android.QuickAction;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.LinearLayout.LayoutParams;

import com.bjorsond.android.timeline.MyTagsActivity;
import com.bjorsond.android.timeline.NoteActivity;
import com.bjorsond.android.timeline.ReflectionActivity;
import com.bjorsond.android.timeline.TimelineActivity;
import com.bjorsond.android.timeline.adapters.TagListAdapter;
import com.bjorsond.android.timeline.database.TimelineDatabaseHelper;
import com.bjorsond.android.timeline.database.contentmanagers.ContentUpdater;
import com.bjorsond.android.timeline.database.contentmanagers.TagManager;
import com.bjorsond.android.timeline.models.Emotion;
import com.bjorsond.android.timeline.models.Event;
import com.bjorsond.android.timeline.models.EventItem;
import com.bjorsond.android.timeline.models.ReflectionNote;
import com.bjorsond.android.timeline.models.SimpleNote;
import com.bjorsond.android.timeline.models.SimplePicture;
import com.bjorsond.android.timeline.models.Emotion.EmotionEnum;
import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceHandler;
import com.bjorsond.android.timeline.sync.GoogleAppEngineHandler;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.utilities.MyLocation;
import com.bjorsond.android.timeline.utilities.Utilities;
import com.bjorsond.android.timeline.R;

/**
 * 
 * The {@link Dialog} that shows the {@link Event}, it's items and actions on the {@link Event}.
 * 
 * @author andekr
 *
 */
public class EventDialog extends Dialog {
	
	private final Event mEvent;
	private final Activity mActivity;
	private Context mContext;
	private LinearLayout mainLayout, emotionLayout;
	private QuickAction qa, shareQa;
	private List<EventItem> items;
	private ImageView dialogIcon;
	private Runnable shareEventThread;
	private String addressString="";
	private Address address;
	private boolean fromMap;
	private ImageButton tagButton;
	private TagListAdapter taglistAdapter;
	private Dialog tagDialog;
	private TagManager tagManager;

	public EventDialog(Context context, Event event, Activity activity, boolean fromMap){
		super(context);
		this.mContext = context;
		this.mEvent = event;
		this.mActivity = activity;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.popupcontentdialog);
		this.setCancelable(true);
		this.fromMap = fromMap;
		
		setupHelpers();
		
		 dialogIcon = (ImageView)findViewById(R.id.PopupDialogTypeIconImageView);
         dialogIcon.setImageResource(Utilities.getImageIcon(this.mEvent));
         
         TextView dialogDateTime = (TextView)findViewById(R.id.PopupDialogDateAndTimeTextView);
         TextView dialogLocation = (TextView)findViewById(R.id.PopupDialogLocationTextView);
         TextView dialogCreator = (TextView)findViewById(R.id.popupDialogCreatorTextView);
         
         //Set header (time and location)
         dialogDateTime.setText(DateFormat.format
        		 ("dd MMMM yyyy "+DateFormat.HOUR_OF_DAY+":mm:ss",this.mEvent.getDatetime()));
         if(this.mEvent.getLocation()!=null){
        try {
        	 address = MyLocation.getAddressForLocation(mContext, this.mEvent.getLocation());
	         addressString += address.getAddressLine(0)+", "; //Address
	         addressString += address.getAddressLine(1)+" "; // Zipcode and area
		} catch (Exception e) {
			addressString = "Unknown location";
		}
	        
         }
         dialogLocation.setText(addressString);
         dialogCreator.append("\n"+this.mEvent.getUser().name);
	
         mainLayout = (LinearLayout)findViewById(R.id.PopupContentLinearLayout);
         emotionLayout = (LinearLayout)findViewById(R.id.PopupMenuDockLinearLayout);
         updateMainview(false);
         
        ((TimelineActivity)mActivity).setSelectedEvent(this.mEvent);
         
 		setupAddButtonQuickAction();
 		setupEmotionButtonQuickAction();
 		
// 		final ToggleButton shareButton = (ToggleButton)findViewById(R.id.PopupShareButton);
// 		shareButton.setTag(this.mEvent);
// 		shareButton.setChecked(mEvent.isShared());
// 		
// 		if(((TimelineActivity)activity).getTimeline().isShared() && Utilities.isConnectedToInternet(mContext)) {
// 			shareButton.setEnabled(!mEvent.isShared());
// 		}
// 		else {
// 			shareButton.setEnabled(false);
// 		}
//
// 		shareButton.setOnClickListener(new View.OnClickListener() {
//			
//			public void onClick(View v) {
//				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//				builder.setMessage("Do you really want to share this event to all group members? You won't be able to undo this later.")
//				.setPositiveButton(R.string.yes_label, new DialogInterface.OnClickListener() {
//					
//					public void onClick(DialogInterface dialog, int which) {
//						mEvent.setShared(true);
//						shareButton.setEnabled(!mEvent.isShared());
//						Thread shareThread = new Thread(shareEventThread, "shareThread");
//		 				shareThread.start();
//		 				dialog.dismiss();
//					}
//				})
//				.setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					shareButton.setChecked(mEvent.isShared());
//					dialog.dismiss();
//				}
//			})
//				.setOnCancelListener(new OnCancelListener() {
//				public void onCancel(DialogInterface dialog) {
//					shareButton.setChecked(mEvent.isShared());
//					dialog.dismiss();					
//				}
//			});
//				AlertDialog confirmation = builder.create();
//				confirmation.show();	
//			}
//		});
			
 		
 		final Button shareButton = (Button) findViewById(R.id.PopupShareButton);
 		if(mEvent.getEventItems().size()==1){
			if(mEvent.getEventItems().get(0) instanceof SimpleNote || mEvent.getEventItems().get(0) instanceof ReflectionNote){
				shareButton.setVisibility(View.VISIBLE);
				if(mEvent.getEventItems().get(0) instanceof SimpleNote){
					shareButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Intent shareIntent = new Intent(Intent.ACTION_SEND);
							shareIntent.setType("text/plain");
					        shareIntent.putExtra(Intent.EXTRA_SUBJECT, ((SimpleNote)mEvent.getEventItems().get(0)).getNoteTitle()); 
					        shareIntent.putExtra(Intent.EXTRA_TEXT, ((SimpleNote)mEvent.getEventItems().get(0)).getNoteText()); 
							mContext.startActivity(Intent.createChooser(shareIntent, mContext.getString(R.string.Share_note_label)));
						}
					});
				}
				
				else if(mEvent.getEventItems().get(0) instanceof ReflectionNote){
					shareButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Intent shareIntent = new Intent(Intent.ACTION_SEND);
							shareIntent.setType("text/plain");
					        shareIntent.putExtra(Intent.EXTRA_SUBJECT, ((ReflectionNote)mEvent.getEventItems().get(0)).getReflectionTitle()); 
					        shareIntent.putExtra(Intent.EXTRA_TEXT, ((ReflectionNote)mEvent.getEventItems().get(0)).getReflectionText()); 
							mContext.startActivity(Intent.createChooser(shareIntent, mContext.getString(R.string.Share_reflection_label)));
						}
					});
				}
			}
 		}
 		
 		
// 		final ActionItem other = new ActionItem();
//		other.setIcon(mContext.getResources().getDrawable(R.drawable.share_to_other));
//		final ActionItem reflectionSpace = new ActionItem();
//		reflectionSpace.setIcon(mContext.getResources().getDrawable(R.drawable.share_to_spaces));
//		
// 		final Button shareButton = (Button) findViewById(R.id.PopupShareButton);
// 		if(mEvent.getEventItems().size()==1){
//			if(mEvent.getEventItems().get(0) instanceof SimpleNote || mEvent.getEventItems().get(0) instanceof ReflectionNote){
//				shareButton.setVisibility(View.VISIBLE);
//				if(mEvent.getEventItems().get(0) instanceof SimpleNote){
//					other.setOnClickListener(new View.OnClickListener() {
//						public void onClick(View v) {
//							Intent shareIntent = new Intent(Intent.ACTION_SEND);
//							shareIntent.setType("text/plain");
//					        shareIntent.putExtra(Intent.EXTRA_SUBJECT, ((SimpleNote)mEvent.getEventItems().get(0)).getNoteTitle()); 
//					        shareIntent.putExtra(Intent.EXTRA_TEXT, ((SimpleNote)mEvent.getEventItems().get(0)).getNoteText()); 
//							mContext.startActivity(Intent.createChooser(shareIntent, mContext.getString(R.string.Share_note_label)));
//						}
//					});
//					
//					reflectionSpace.setOnClickListener(new View.OnClickListener() {
//						public void onClick(View v) {
//							ReflectionSpaceHandler.insertToReflectionSpace(mContext, ((SimpleNote)mEvent.getEventItems().get(0)).getNoteText());
//						}
//					});
//				}
//				
//				else if(mEvent.getEventItems().get(0) instanceof ReflectionNote){
//					other.setOnClickListener(new View.OnClickListener() {
//						public void onClick(View v) {
//							Intent shareIntent = new Intent(Intent.ACTION_SEND);
//							shareIntent.setType("text/plain");
//					        shareIntent.putExtra(Intent.EXTRA_SUBJECT, ((ReflectionNote)mEvent.getEventItems().get(0)).getReflectionTitle()); 
//					        shareIntent.putExtra(Intent.EXTRA_TEXT, ((ReflectionNote)mEvent.getEventItems().get(0)).getReflectionText()); 
//							mContext.startActivity(Intent.createChooser(shareIntent, mContext.getString(R.string.Share_reflection_label)));
//						}
//					});
//					
//					reflectionSpace.setOnClickListener(new View.OnClickListener() {
//						public void onClick(View v) {
//							ReflectionSpaceHandler.insertToReflectionSpace(mContext, ((ReflectionNote)mEvent.getEventItems().get(0)).getReflectionTitle());
//						}
//					});
//				}
//			}
// 		}
// 		shareButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				shareQa = new QuickAction(v);
//		 		shareQa.addActionItem(other);
//		 		shareQa.addActionItem(reflectionSpace);
//		 		shareQa.setAnimStyle(QuickAction.ANIM_AUTO);
//		 		shareQa.show();
//			}
//		});
 		
 		
 		
 		
 		
 		
 		ImageButton deleteButton = (ImageButton)findViewById(R.id.popupDeleteButton);
 		deleteButton.setTag(event);
 		deleteButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
 				builder.setMessage(R.string.Delete_event_confirmation)
 				.setPositiveButton(R.string.yes_label, deleteEventListener)
 				.setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							dialog.cancel();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				})
 				.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						try {
							dialog.cancel()	;	
						} catch (Exception e) {
							// TODO: handle exception
						}
										
					}
				});
 				
 				AlertDialog confirmation = builder.create();
 				confirmation.show();
 			}
 		});
 		
 		
 		tagButton = (ImageButton)findViewById(R.id.PopuptagButton);
 		tagButton.setOnClickListener(tagClickListener);
 		
 		shareEventThread = new Runnable() {
			
			public void run() {
				GoogleAppEngineHandler.persistTimelineObject(mEvent);
				ContentUpdater updater = new ContentUpdater(mContext);
				updater.setEventShared(mEvent);
			}
		};
 		
 		
	}
	
	public OnClickListener deleteEventListener = new OnClickListener() {	
		public void onClick(DialogInterface dialog, int which) {
			deleteEvent(mEvent);
		}
	};

	/**
	 * Update method to run after adding items
	 * 
	 */
	public void updateMainview(boolean sendToServer) {
		items = this.mEvent.getEventItems();
        mainLayout.removeAllViews();
        emotionLayout.removeAllViews();
        dialogIcon.setImageResource(Utilities.getImageIcon(this.mEvent));
        ((TimelineActivity)mActivity).getEventAdapter().notifyDataSetChanged();
        try {
        	 qa.dismiss();
		} catch (NullPointerException e) {
			//Nothing. qa isn't created yet.
		}
       int i = 0;
		for (EventItem exItem : items) {
         	View contentView = exItem.getView(mContext);
         	Intent openIntent = exItem.getIntent();
			if(openIntent!=null){
				contentView.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						Intent i = ((EventItem)v.getTag()).getIntent();
						mActivity.startActivity(i);
					}
				});
			}
         
         	contentView.setId(i);
         	contentView.setTag(exItem);
         	contentView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenuInfo menuInfo) {
					menu.add(R.id.MENU_EXTRACT_ITEM, v.getId(), 0, R.string.extract_item_label);
					if(v.getTag() instanceof SimpleNote){
						menu.add(R.id.MENU_EDIT_NOTE, v.getId(), 0, R.string.Edit_label);
					}
					else if(v.getTag() instanceof ReflectionNote){
						menu.add(R.id.MENU_EDIT_REFLECTION, v.getId(), 0, R.string.Edit_label);
					}
					menu.add(R.id.MENU_DELETE_ITEM, v.getId(), 0, R.string.Delete_item_label);
					menu.add(R.id.MENU_SHOW_CREATOR, v.getId(), 0, R.string.Show_creator_label);
				}
			});
         	
         	mainLayout.addView(contentView);
         	
        	i++;
			}
		
		for (Emotion emotion : mEvent.getEmotionList()) {
			ImageView emotionIcon  = getEmotionIcon(mContext.getResources().getDrawable(emotion.getEmotionType().getIcon()));
			emotionLayout.addView(emotionIcon);
		}
		if(sendToServer){
			Runnable SendEventRunnable = new Runnable() {
				
				public void run() {
					GoogleAppEngineHandler.persistTimelineObject(mEvent);
				}
			};	
	    	//A little overkill to send the whole event. TODO: Make similar for EventItem
	    	Thread sendEventThread = new Thread(SendEventRunnable, "shareThread");
			sendEventThread.start();
		}
		
		
		
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getGroupId()) {
		case R.id.MENU_EXTRACT_ITEM:
			Log.v("LONG-CLICK", "Extract: "+items.get(item.getItemId()).getId());
			extractEventItem(items.remove(item.getItemId()));
			return true;
		
		case R.id.MENU_EDIT_NOTE:
			Log.v("LONG-CLICK", "Edit: "+item.getItemId());
			Intent noteIntent = new Intent(mActivity.getBaseContext(), NoteActivity.class);
			noteIntent.putExtra("NOTE_ID", item.getItemId());//TODO: Ikke bra � bruke denne ID'en. M� tenke p� bedre l�sninger her...
			noteIntent.putExtra(Intent.EXTRA_SUBJECT, ((SimpleNote)items.get(item.getItemId())).getNoteTitle()); 
			noteIntent.putExtra(Intent.EXTRA_TEXT, ((SimpleNote)items.get(item.getItemId())).getNoteText());
			noteIntent.putExtra(Constants.REQUEST_CODE, Constants.EDIT_NOTE);
			noteIntent.putExtra(Intent.EXTRA_TEXT, ((SimpleNote)items.get(item.getItemId())).getNoteText());
			mActivity.startActivityForResult(noteIntent, Constants.EDIT_NOTE); 
			return true;
			
		case R.id.MENU_EDIT_REFLECTION:
			Log.v("LONG-CLICK", "Edit: "+item.getItemId());
			Intent reflectionIntent = new Intent(mActivity.getBaseContext(), ReflectionActivity.class);
			reflectionIntent.putExtra("REFLECTION_ID", item.getItemId());//TODO: Ikke bra � bruke denne ID'en. M� tenke p� bedre l�sninger her...
			reflectionIntent.putExtra(Intent.EXTRA_SUBJECT, ((ReflectionNote)items.get(item.getItemId())).getReflectionTitle()); 
			reflectionIntent.putExtra(Intent.EXTRA_TEXT, ((ReflectionNote)items.get(item.getItemId())).getReflectionText());
			reflectionIntent.putExtra(Constants.REQUEST_CODE, Constants.EDIT_REFLECTION);
			
			mActivity.startActivityForResult(reflectionIntent, Constants.EDIT_REFLECTION); 
			return true;
			
		case R.id.MENU_DELETE_ITEM:
			Log.v("LONG-CLICK", "Delete: "+items.get(item.getItemId()).getId());
			deleteEventItem(items.remove(item.getItemId()));
			
			return true;
		case R.id.MENU_SHOW_CREATOR:
			Log.v("LONG-CLICK", "Show creator: "+items.get(item.getItemId()).getId());
			Toast.makeText(EventDialog.this.mContext, "Creator of this item: "+items.get(item.getItemId()).getCreator(), Toast.LENGTH_LONG).show();
			
			return true;
	}
		
		return false;
	}
	
	private ImageView getEmotionIcon(Drawable icon) {
		ImageView img 	= new ImageView(mContext);
		
		if (icon != null) {
			img.setImageDrawable(icon);
			img.setPadding(5, 0, 5, 0);
			LayoutParams lp = new LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
			img.setLayoutParams(lp);
		} else {
			img.setVisibility(View.GONE);
		}
		
		return img;
	}
	
	private void setupAddButtonQuickAction() {
		final ActionItem camera = new ActionItem();
		
		camera.setIcon(mContext.getResources().getDrawable(R.drawable.ic_menu_camera));
		camera.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				((TimelineActivity)mActivity).startCamera();						
			}
		});
				
				
		final ActionItem video = new ActionItem();
		
		video.setIcon(mContext.getResources().getDrawable(R.drawable.ic_menu_video));
		video.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				((TimelineActivity)mActivity).startVideoCamera();
			}
		});
		
		final ActionItem audio = new ActionItem();
		
		audio.setIcon(mContext.getResources().getDrawable(R.drawable.ic_menu_audio));
		audio.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				((TimelineActivity)mActivity).startAudioRecording();
			}
		});
		
		final ActionItem note = new ActionItem();
		
		note.setIcon(mContext.getResources().getDrawable(R.drawable.ic_menu_note));
		note.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				((TimelineActivity)mActivity).createNote();
			}

		});
		
		final ActionItem reflection = new ActionItem();
		
		reflection.setIcon(mContext.getResources().getDrawable(R.drawable.ic_menu_sumday));
		reflection.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				((TimelineActivity)mActivity).createReflection();
			}

		});
		
		final ActionItem attachment = new ActionItem();
		
		attachment.setIcon(mContext.getResources().getDrawable(R.drawable.ic_menu_attachment));
		attachment.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				((TimelineActivity)mActivity).startAttachmentDialog();
			}
		});
        
        
		ImageButton addButton = (ImageButton)findViewById(R.id.PopupAddButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				qa = new QuickAction(v);
				
				qa.addActionItem(camera);
				qa.addActionItem(video);
				qa.addActionItem(audio);
				qa.addActionItem(note);
				qa.addActionItem(reflection);
				qa.addActionItem(attachment);
				qa.setAnimStyle(QuickAction.ANIM_AUTO);
				qa.show();
				
			}
		});
		
	}
	
	private void setupEmotionButtonQuickAction() {
		final ActionItem like = new ActionItem();
		
		like.setIcon(mContext.getResources().getDrawable(EmotionEnum.LIKE.getIcon()));
		like.setTag(mEvent);
		like.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Emotion emo = new Emotion(EmotionEnum.LIKE);
				((Event)v.getTag()).addEmotion(emo);
				((TimelineActivity)mActivity).getContentAdder().addEmotionToDatabase((Event) v.getTag(), emo);
				Toast.makeText(mContext, R.string.like_toast , Toast.LENGTH_SHORT).show();
				Log.i(this.toString(), "LIKE event set");
				updateMainview(true);
			}
		});
				
				
		final ActionItem cool = new ActionItem();
		
		cool.setIcon(mContext.getResources().getDrawable(EmotionEnum.COOL.getIcon()));
		cool.setTag(mEvent);
		cool.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Emotion emo = new Emotion(EmotionEnum.COOL);
				((Event)v.getTag()).addEmotion(emo);
				((TimelineActivity)mActivity).getContentAdder().addEmotionToDatabase((Event) v.getTag(), emo);
				Toast.makeText(mContext, R.string.cool_toast , Toast.LENGTH_SHORT).show();
				Log.i(this.toString(), "COOL event set");
				updateMainview(true);
			}
		});
		
		final ActionItem dislike = new ActionItem();
		
		dislike.setIcon(mContext.getResources().getDrawable(EmotionEnum.DISLIKE.getIcon()));
		dislike.setTag(mEvent);
		dislike.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Emotion emo = new Emotion(EmotionEnum.DISLIKE);
				((Event)v.getTag()).addEmotion(emo);
				((TimelineActivity)mActivity).getContentAdder().addEmotionToDatabase((Event) v.getTag(), emo);
				Toast.makeText(mContext, R.string.dislike_toast , Toast.LENGTH_SHORT).show();
				Log.i(this.toString(), "DISLIKE event set");
				updateMainview(true);
			}
		});
		
		final ActionItem sad = new ActionItem();
		
		sad.setIcon(mContext.getResources().getDrawable(EmotionEnum.SAD.getIcon()));
		sad.setTag(mEvent);
		sad.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Emotion emo = new Emotion(EmotionEnum.SAD);
				((Event)v.getTag()).addEmotion(emo);
				((TimelineActivity)mActivity).getContentAdder().addEmotionToDatabase((Event) v.getTag(), emo);
				Toast.makeText(mContext, R.string.sad_toast , Toast.LENGTH_SHORT).show();
				Log.i(this.toString(), "SAD event set");
				updateMainview(true);
			}

		});
		
        
		ImageButton emotionButton = (ImageButton)findViewById(R.id.PopupEmotionButton);
		emotionButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				qa = new QuickAction(v);
				
				qa.addActionItem(like);
				qa.addActionItem(cool);
				qa.addActionItem(dislike);
				qa.addActionItem(sad);
				qa.setAnimStyle(QuickAction.ANIM_AUTO);
				qa.show();
				
			}
		});
		
	}
	
	public void extractEventItem(EventItem exItem){
		Event newEvent = new Event(mEvent.getExperienceid(), mEvent.getLocation(), Utilities.getUserAccount(mContext));
		newEvent.addEventItem(exItem);
		((TimelineActivity)mActivity).addEvent(newEvent);
        if(items.size()==0){
        	deleteEvent(mEvent);
        }else{
        	updateMainview(true);
        }
		
	}
	
	public void deleteEventItem(EventItem exItem){
		((TimelineActivity)mActivity).delete(exItem);
        if(items.size()==0){
        	deleteEvent(mEvent);
        }else{
        	updateMainview(true);
        }
	}
	
	public void deleteEvent(Event event){
		this.dismiss();
    	((TimelineActivity)mActivity).setSelectedEvent(null);
    	((TimelineActivity)mActivity).removeEvent(event);
	}
	
	public android.view.View.OnClickListener tagClickListener = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			//Open tagging dialog
			openSelectTagsToAddDialog();
		}
	};

	/**
	 * Dialog for selecting which tags to add to the event
	 */
	public void openSelectTagsToAddDialog() {
		if(tagDialog!=null){
			tagDialog.dismiss();
		}
		tagDialog = new Dialog(mContext);
		tagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		ArrayList<String> allTags = tagManager.getAllTags();
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		RelativeLayout tagDialogRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.taggingdialog, null);
		ListView tagList = (ListView)tagDialogRelativeLayout.findViewById(R.id.tagListlistView);
		taglistAdapter = new TagListAdapter(mActivity, R.layout.list_tags_view, allTags, mEvent.getTags());
		tagList.setAdapter(taglistAdapter);
		tagDialog.addContentView(tagDialogRelativeLayout, new android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.FILL_PARENT, android.widget.AbsListView.LayoutParams.FILL_PARENT));
		
		Button okButton = (Button)tagDialogRelativeLayout.findViewById(R.id.tagsOKButton);
		okButton.setOnClickListener(addTagDialogListener);
		
		Button manageButton = (Button)tagDialogRelativeLayout.findViewById(R.id.tagManageButton);
		manageButton.setOnClickListener(manageTagsListener);
		
		Button cancelButton = (Button)tagDialogRelativeLayout.findViewById(R.id.tagCancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				taglistAdapter.setCheckedTags(mEvent.getTags());
				tagDialog.dismiss();		
			}
		});
		
		tagDialog.show();
	}
	
	/**
	 * Add tag dialog listener
	 */
	private View.OnClickListener addTagDialogListener = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			mEvent.setTags(taglistAdapter.getCheckedTags());
			
			for (String tag : mEvent.getTags()) {
				tagManager.addTagToEventInDatabase(tag, mEvent);
			}
			tagDialog.dismiss();
			
		}
	};
	
	private View.OnClickListener manageTagsListener = new View.OnClickListener() {
		
		public void onClick(View arg0) {
			Intent tagIntent = new Intent(mActivity.getBaseContext(), MyTagsActivity.class);
			tagIntent.setAction(Constants.INTENT_ACTION_NEW_TAG);
			
			mActivity.startActivityForResult(tagIntent, Constants.NEW_TAG_REQUESTCODE); 
			
		}
	};
	
	public void updateTagDialog(){
		openSelectTagsToAddDialog();
	}
	
	private void setupHelpers() {
		new TimelineDatabaseHelper(mContext, Constants.ALL_TIMELINES_DATABASE_NAME);
		tagManager = new TagManager(mContext);
	}
	
	@Override
	public void onBackPressed() {
		TimelineDatabaseHelper.getCurrentTimeLineDatabase().close();
		if(fromMap) {
			((TimelineActivity) mActivity).openMapView();
		}
		super.onBackPressed();
	}
}
