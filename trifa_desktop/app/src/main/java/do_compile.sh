#! /bin/bash

#javac com/zoffcc/applications/trifa/ToxVars.java
#javac com/zoffcc/applications/trifa/TRIFAGlobals.java
#javac com/zoffcc/applications/trifa/MainActivity.java
#javac com/zoffcc/applications/trifa/TrifaToxService.java

# -Xlint:unchecked \

javac \
-classpath ".:sqlite-jdbc-3.32.3.2.jar:webcam-capture-0.3.12.jar:bridj-0.7.0.jar:slf4j-api-1.7.2.jar:flatlaf-1.0.jar" \
./com/zoffcc/applications/trifa/TRIFAGlobals.java ./com/zoffcc/applications/trifa/Table.java ./com/zoffcc/applications/trifa/OnConflict.java ./com/zoffcc/applications/trifa/CombinedFriendsAndConferences.java ./com/zoffcc/applications/trifa/OrmaDatabase.java ./com/zoffcc/applications/trifa/HelperGeneric.java ./com/zoffcc/applications/trifa/ConferencePeerCacheDB.java ./com/zoffcc/applications/trifa/Index.java ./com/zoffcc/applications/trifa/AudioFrame.java ./com/zoffcc/applications/trifa/FullscreenToggleAction.java ./com/zoffcc/applications/trifa/Nullable.java ./com/zoffcc/applications/trifa/Column.java ./com/zoffcc/applications/trifa/FriendList.java ./com/zoffcc/applications/trifa/SingleComponentAspectRatioKeeperLayout.java ./com/zoffcc/applications/trifa/HelperFriend.java ./com/zoffcc/applications/trifa/JPictureBox.java ./com/zoffcc/applications/trifa/MainActivity.java ./com/zoffcc/applications/trifa/TrifaToxService.java ./com/zoffcc/applications/trifa/MessageListFragmentJ.java ./com/zoffcc/applications/trifa/AudioSelectOutBox.java ./com/zoffcc/applications/trifa/ByteBufferCompat.java ./com/zoffcc/applications/trifa/AudioBar.java ./com/zoffcc/applications/trifa/HelperMessage.java ./com/zoffcc/applications/trifa/VideoOutFrame.java ./com/zoffcc/applications/trifa/ConferenceDB.java ./com/zoffcc/applications/trifa/FriendListFragmentJ.java ./com/zoffcc/applications/trifa/Callstate.java ./com/zoffcc/applications/trifa/Log.java ./com/zoffcc/applications/trifa/PrimaryKey.java ./com/zoffcc/applications/trifa/RelayListDB.java ./com/zoffcc/applications/trifa/Message.java ./com/zoffcc/applications/trifa/ToxVars.java ./com/zoffcc/applications/trifa/VideoInFrame.java ./com/zoffcc/applications/trifa/HelperConference.java ./com/zoffcc/applications/trifa/AudioSelectInBox.java ./com/zoffcc/applications/trifa/TRIFADatabaseGlobalsNew.java ./com/zoffcc/applications/trifa/BootstrapNodeEntryDB.java ./com/zoffcc/applications/trifa/HelperRelay.java ./com/zoffcc/applications/trifa/SettingsActivity.java
