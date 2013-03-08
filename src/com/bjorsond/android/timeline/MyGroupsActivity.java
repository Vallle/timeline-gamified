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
package com.bjorsond.android.timeline;

import java.util.ArrayList;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import com.bjorsond.android.timeline.adapters.ExpandableGroupsListViewAdapter;
import com.bjorsond.android.timeline.adapters.UserListAdapter;
import com.bjorsond.android.timeline.database.TimelineDatabaseHelper;
import com.bjorsond.android.timeline.database.UserGroupDatabaseHelper;
import com.bjorsond.android.timeline.database.contentmanagers.ContentLoader;
import com.bjorsond.android.timeline.database.contentmanagers.ContentUpdater;
import com.bjorsond.android.timeline.database.contentmanagers.UserGroupManager;
import com.bjorsond.android.timeline.models.Experience;
import com.bjorsond.android.timeline.models.Group;
import com.bjorsond.android.timeline.models.User;
import com.bjorsond.android.timeline.sync.GoogleAppEngineHandler;
import com.bjorsond.android.timeline.sync.UserAndGroupServiceHandler;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.R;
import com.swarmconnect.SwarmActivity;


/**
 * 
 * The activity class for the user and group management.
 * 
 * @author andekr
 *
 */
public class MyGroupsActivity extends SwarmActivity implements ProgressDialogActivity {
	
	private Account userAccount;
	private ImageButton addNewGroupButton, homeButton;
	private User applicationUser;
	private UserListAdapter userlistAdapter;
	private Group selectedGroup;
	private ArrayList <Group> connectedGroups;
	private UserGroupManager uGManager;
	private ExpandableListView myGroupsList;
	private ExpandableGroupsListViewAdapter groupListAdapter;
	private UserGroupDatabaseHelper helper;
	private UserAndGroupServiceHandler userAndGroupServiceHandler;
	private TimelineDatabaseHelper timelineDatabaseHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groupmenuscreen);
		
		setupHelpers();
		
		userAndGroupServiceHandler = new UserAndGroupServiceHandler(this, this);
		userAndGroupServiceHandler.startDownloadUsersAndGroups();
	}
	
	/**
	 * Add a new group to the database
	 * @param groupName. The group name of the new group
	 */

	protected void addNewGroup(String groupName) {
		Group group = new Group(groupName);
		uGManager.addGroupToGroupDatabase(group);
		uGManager.addUserToAGroupInTheDatabase(group, applicationUser);
		group.addMembers(applicationUser);
		connectedGroups.add(group);
		groupListAdapter.notifyDataSetChanged();
		Toast.makeText(MyGroupsActivity.this.getApplicationContext(), "You have created the group: " +group.toString() , Toast.LENGTH_SHORT).show();
		GoogleAppEngineHandler.addGroupToServer(group);
		
	}
	
	/**
	 * Add new user to a group if they don't already are there
	 * @param selectedUsers
	 */
	protected void addUsersToGroup(ArrayList<User> selectedUsers) {
		for (User user : selectedUsers) {
				uGManager.addUserToAGroupInTheDatabase(selectedGroup, user);
				GoogleAppEngineHandler.addUserToGroupOnServer(selectedGroup, user);
				selectedGroup.addMembers(user);
		}
		Toast.makeText(this, "New users has been added to group "+selectedGroup+"!", Toast.LENGTH_SHORT).show();
		userlistAdapter.notifyDataSetChanged();
	}
	
//	set the selected group
	private void setSelectedGroup(Group group) {
		this.selectedGroup = group;
	}
	
	public Group getSelectedGroup() {
		return selectedGroup;
	}
	
	/**
	 * Leaves the selected group
	 */
	protected void leaveGroup() {
		
		ContentLoader experienceLoader = new ContentLoader(this);
		ContentUpdater experienceUpdater = new ContentUpdater(this);
		ArrayList<Experience> experiencesConnectedToSelectedGroup = experienceLoader.LoadAllSharedExperiencesOnGroupFromDatabase(selectedGroup);
		
		for (Experience experience : experiencesConnectedToSelectedGroup) {
			experience.setShared(false);
			experienceUpdater.updateExperience(experience);
		}

		GoogleAppEngineHandler.removeUserFromGroupOnServer(selectedGroup, applicationUser);
		uGManager.removeUserFromAGroupInTheDatabase(selectedGroup, applicationUser);
		connectedGroups.remove(selectedGroup);
		selectedGroup.removeMember(applicationUser);
		
		if(selectedGroup.getMembers().isEmpty()) {
			deleteGroupFromDatabase(selectedGroup);
			GoogleAppEngineHandler.removeGroupFromDatabase(selectedGroup);
		}
		
		groupListAdapter.notifyDataSetChanged();
	}

	
	private void deleteGroupFromDatabase(Group group) {
		uGManager.deleteGroupFromDatabase(group);
	}
	
	/**
	 * Get all groups from the database connected to the user using the application
	 * @param user. The user using the application
	 * @return a list of all the groups the user are a part of
	 */
	private ArrayList <Group> getAllGroupsConnectedToUser(Account user) {
		
		//TODO move this to appropriate place
		uGManager.addUsersToUserDatabase(GoogleAppEngineHandler.getUsers());
		ArrayList <Group> allGroups = uGManager.getAllGroupsConnectedToAUser(applicationUser);
		return allGroups;
	}
	
	/**
	 * Get all users from the database
	 * @return ArrayList with all the users
	 */
	private ArrayList<User> getAllUsers() {
		ArrayList<User> users = uGManager.getAllUsersFromDatabase();
		return users;
	}
	
	/**
	 * Checks what item type that has been selected in a long press context menu
	 */

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getGroupId()) {
		
		case R.id.MENU_DELETE_ITEM:
			leaveGroupConfirmationDialog();
			break;
		case R.id.MENU_ADD_USER:
			openSelectUserToAddDialog();
			break;
		}
		return false;
	}
	/**
	 * Closes the database when the back-button is pressed
	 */
	@Override
	public void onBackPressed() {
		helper.close();
		timelineDatabaseHelper.close();
		super.onBackPressed();
	}
	/**
	 * Checks if a username already is part of a group (the user name is unique by default)
	 * @param user the user to be checked
	 * @param group the group to be checked
	 * @return true if the user already is a part of the group, false otherwise
	 */
	private boolean isAlreadyPartOfGroup(User user, Group group) {
		
		for (User u : group.getMembers()) {
			if(user.getUserName().equals(u.getUserName())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Dialog for selecting which user to add to the group
	 */
	private void openSelectUserToAddDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		ListView userList = new ListView(this);
		userList.setBackgroundColor(this.getResources().getColor(R.color.White));
		userList.setCacheColorHint(this.getResources().getColor(android.R.color.transparent));
		userlistAdapter = new UserListAdapter(this, getAllUsersNotInGroupAlready(selectedGroup) , selectedGroup);
		userList.setAdapter(userlistAdapter);
				
		builder.setView(userList);
		
		builder.setMessage("Select users to add:")
		.setPositiveButton("Add users", addUserDialogListener)
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	})
		.setOnCancelListener(new OnCancelListener() {
		public void onCancel(DialogInterface dialog) {
			dialog.dismiss();					
		}
	});
		
		AlertDialog confirmation = builder.create();
		if(userlistAdapter.getCount()>0)
			confirmation.show();
		else
			Toast.makeText(this, "No more users to add", Toast.LENGTH_SHORT).show();
	}
	
	private ArrayList<User> getAllUsersNotInGroupAlready(Group selectedGroup) {
		ArrayList<User> usersNotInGroup = new ArrayList<User>();
		
		for (User user : getAllUsers()) {
			if(!isAlreadyPartOfGroup(user, selectedGroup))
				usersNotInGroup.add(user);
		}
	return usersNotInGroup;
}


	/**
	 * Input dialog for the writing the name of a new group
	 */
	private void openNewGroupNameInputDialog() {
		
		final AlertDialog.Builder groupNameInputDialog = new AlertDialog.Builder(
				this);
		
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.newgroupdialog, (ViewGroup) findViewById(R.id.newgroupdialogroot));
		groupNameInputDialog.setView(layout);
		
		final EditText inputTextField = (EditText)layout.findViewById(R.id.NewGroupeditText);

		groupNameInputDialog.setTitle("Enter a name for your group!");
		groupNameInputDialog.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String inputName = inputTextField.getText().toString().trim();
				addNewGroup(inputName);
				dialog.dismiss();
			}
		});

		groupNameInputDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		}).setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				
			}
		});
		
		groupNameInputDialog.show();
	}
	
	/**
	 * Confirmation dialog that pops when you tries to leave a group
	 */
	private void leaveGroupConfirmationDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you really want to leave group " +selectedGroup.toString()+"?")
		.setPositiveButton(R.string.yes_label, leaveGroupConfirmationListener)
		.setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			setSelectedGroup(null);
			dialog.dismiss();
		}
	})
		.setOnCancelListener(new OnCancelListener() {
		public void onCancel(DialogInterface dialog) {
			setSelectedGroup(null);
			dialog.dismiss();					
		}
	});
		AlertDialog confirmation = builder.create();
		confirmation.show();
	}
	
	//listeners
	/**
	 * Add user dialog listener
	 */
	private OnClickListener addUserDialogListener = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			addUsersToGroup(userlistAdapter.getSelectedUsers());
			setSelectedGroup(null);
			dialog.dismiss();
		}
	};
	
	/**
	 * Confirmation dialog listener
	 */
	private android.content.DialogInterface.OnClickListener leaveGroupConfirmationListener = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			
			leaveGroup();
			Toast.makeText(MyGroupsActivity.this.getApplicationContext(), "You have left group: "+selectedGroup.toString() , Toast.LENGTH_SHORT).show();
			setSelectedGroup(null);
			dialog.dismiss();
		}
	};

	/**
	 * Listener for a long click on an Item in the group list view
	 */
	private OnItemLongClickListener openItemLongClickMenuListener = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> view, View arg1,
				int position, long arg3) {
					
			view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenuInfo menuInfo) {
					
					
					//make sure that the selectedGroup is not out of bounds:
					ExpandableListView.ExpandableListContextMenuInfo info =
						(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
					MyGroupsActivity.this.setSelectedGroup(groupListAdapter.getGroup(ExpandableListView.getPackedPositionGroup(info.packedPosition)));
					
					menu.setHeaderTitle(MyGroupsActivity.this.selectedGroup.toString());
					menu.add(R.id.MENU_ADD_USER,0,0, R.string.Add_user_label);
					menu.add(R.id.MENU_DELETE_ITEM, 0,0, R.string.Leave_group_label);
				}
			});
			return false;
		}
	};
	
	private android.view.View.OnClickListener newGroupButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openNewGroupNameInputDialog();
		}
	};
	
	//SETUP HELPERS
	
	private void setupHelpers() {
		uGManager = new UserGroupManager(this);
		helper = new UserGroupDatabaseHelper(this, Constants.USER_GROUP_DATABASE_NAME);
		timelineDatabaseHelper = new TimelineDatabaseHelper(this, Constants.ALL_TIMELINES_DATABASE_NAME);
		userAccount = (Account) getIntent().getParcelableExtra("ACCOUNT");
		applicationUser = new User(userAccount.name);
	}
	
	
	/**
	 * Setup views and instantiate objects the activity is going to use
	 */
	private void setupViews() {
		myGroupsList = (ExpandableListView) findViewById(R.id.groupsList);
		
		addNewGroupButton = (ImageButton) findViewById(R.id.my_groups);
		addNewGroupButton.setOnClickListener(newGroupButtonListener);
		
		connectedGroups = getAllGroupsConnectedToUser(userAccount);
		groupListAdapter = new ExpandableGroupsListViewAdapter(this, connectedGroups);
		
		myGroupsList.setAdapter(groupListAdapter);
		this.registerForContextMenu(myGroupsList);
		myGroupsList.setOnItemLongClickListener(openItemLongClickMenuListener);

		
		homeButton = (ImageButton)findViewById(R.id.GroupHomeButto);
		homeButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				helper.close();
				finish();
			}
		});
		
	}

	public void callBack() {
		setupViews();
		
	}

}
