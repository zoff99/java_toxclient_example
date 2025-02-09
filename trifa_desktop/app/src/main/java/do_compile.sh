#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_

# -Xlint:unchecked \

# (find com/ -name '*.java';find org/ -name '*.java';find io/ -name '*.java')|sort|awk '{ print "./" $1 " \\" }'

javac -encoding UTF-8 \
-classpath ".:json-20210307.jar:emoji-java-5.1.1.jar:sqlite-jdbc-3.34.0.jar:webcam-capture-0.3.12.jar:bridj-0.7.0.jar:slf4j-api-1.7.2.jar:flatlaf-1.0.jar" \
./com/github/sarxos/webcam/ds/ffmpegcli/FFmpegScreenDevice.java \
./com/github/sarxos/webcam/ds/ffmpegcli/FFmpegScreenDriver.java \
./com/github/sarxos/webcam/ds/ffmpegcli/impl/VideoDeviceFilenameFilter.java \
./com/kevinnovate/jemojitable/EmojiTable.java \
./com/zoffcc/applications/trifa/AboutActivity.java \
./com/zoffcc/applications/trifa/AudioBar.java \
./com/zoffcc/applications/trifa/AudioFrame.java \
./com/zoffcc/applications/trifa/AudioSelectInBox.java \
./com/zoffcc/applications/trifa/AudioSelectionRenderer.java \
./com/zoffcc/applications/trifa/AudioSelectOutBox.java \
./com/zoffcc/applications/trifa/BootstrapNodeEntryDB.java \
./com/zoffcc/applications/trifa/ByteBufferCompat.java \
./com/zoffcc/applications/trifa/Callstate.java \
./com/zoffcc/applications/trifa/ChatColors.java \
./com/zoffcc/applications/trifa/Column.java \
./com/zoffcc/applications/trifa/CombinedFriendsAndConferences.java \
./com/zoffcc/applications/trifa/ConferenceDB.java \
./com/zoffcc/applications/trifa/ConferenceMessage.java \
./com/zoffcc/applications/trifa/ConferenceMessageListFragmentJ.java \
./com/zoffcc/applications/trifa/ConferenceMessageTableModel.java \
./com/zoffcc/applications/trifa/ConferencePeerCacheDB.java \
./com/zoffcc/applications/trifa/EmojiFrame.java \
./com/zoffcc/applications/trifa/EmojiSelectionTab.java \
./com/zoffcc/applications/trifa/FileDB.java \
./com/zoffcc/applications/trifa/FileDrop.java \
./com/zoffcc/applications/trifa/Filetransfer.java \
./com/zoffcc/applications/trifa/FriendInfoActivity.java \
./com/zoffcc/applications/trifa/FriendListFragmentJ.java \
./com/zoffcc/applications/trifa/FriendList.java \
./com/zoffcc/applications/trifa/FullscreenToggleAction.java \
./com/zoffcc/applications/trifa/GroupDB.java \
./com/zoffcc/applications/trifa/GroupMessage.java \
./com/zoffcc/applications/trifa/GroupMessageListFragmentJ.java \
./com/zoffcc/applications/trifa/GroupMessageTableModel.java \
./com/zoffcc/applications/trifa/HelperConference.java \
./com/zoffcc/applications/trifa/HelperFiletransfer.java \
./com/zoffcc/applications/trifa/HelperFriend.java \
./com/zoffcc/applications/trifa/HelperGeneric.java \
./com/zoffcc/applications/trifa/HelperGroup.java \
./com/zoffcc/applications/trifa/HelperMessage.java \
./com/zoffcc/applications/trifa/HelperNotification.java \
./com/zoffcc/applications/trifa/HelperOSFile.java \
./com/zoffcc/applications/trifa/HelperRelay.java \
./com/zoffcc/applications/trifa/Identicon.java \
./com/zoffcc/applications/trifa/Index.java \
./com/zoffcc/applications/trifa/JPictureBox.java \
./com/zoffcc/applications/trifa/Log.java \
./com/zoffcc/applications/trifa/MainActivity.java \
./com/zoffcc/applications/trifa/Message.java \
./com/zoffcc/applications/trifa/MessageListFragmentJInfo.java \
./com/zoffcc/applications/trifa/MessageListFragmentJ.java \
./com/zoffcc/applications/trifa/MessageTableModel.java \
./com/zoffcc/applications/trifa/Nullable.java \
./com/zoffcc/applications/trifa/OnConflict.java \
./com/zoffcc/applications/trifa/OperatingSystem.java \
./com/zoffcc/applications/trifa/OrmaDatabase.java \
./com/zoffcc/applications/trifa/PanelCellEditorRenderer.java \
./com/zoffcc/applications/trifa/PCMWaveFormDisplay.java \
./com/zoffcc/applications/trifa/PeerListFragmentJ.java \
./com/zoffcc/applications/trifa/PeerModel.java \
./com/zoffcc/applications/trifa/PopupToxIDQrcode.java \
./com/zoffcc/applications/trifa/PrimaryKey.java \
./com/zoffcc/applications/trifa/RelayListDB.java \
./com/zoffcc/applications/trifa/Renderer_ConfMessageList.java \
./com/zoffcc/applications/trifa/Renderer_ConfMessageListTable.java \
./com/zoffcc/applications/trifa/Renderer_ConfPeerList.java \
./com/zoffcc/applications/trifa/Renderer_FriendsAndConfsList.java \
./com/zoffcc/applications/trifa/Renderer_GroupMessageListTable.java \
./com/zoffcc/applications/trifa/Renderer_MessageList.java \
./com/zoffcc/applications/trifa/Renderer_MessageListTable.java \
./com/zoffcc/applications/trifa/Screenshot.java \
./com/zoffcc/applications/trifa/SelectionRectangle.java \
./com/zoffcc/applications/trifa/SettingsActivity.java \
./com/zoffcc/applications/trifa/SingleComponentAspectRatioKeeperLayout.java \
./com/zoffcc/applications/trifa/Table.java \
./com/zoffcc/applications/trifa/Toast.java \
./com/zoffcc/applications/trifa/ToxVars.java \
./com/zoffcc/applications/trifa/TRIFADatabaseGlobalsNew.java \
./com/zoffcc/applications/trifa/TRIFAGlobals.java \
./com/zoffcc/applications/trifa/TrifaSetPatternActivity.java \
./com/zoffcc/applications/trifa/TrifaToxService.java \
./com/zoffcc/applications/trifa/VideoInFrame.java \
./com/zoffcc/applications/trifa/VideoOutFrame.java \
./io/nayuki/qrcodegen/BitBuffer.java \
./io/nayuki/qrcodegen/DataTooLongException.java \
./io/nayuki/qrcodegen/package-info.java \
./io/nayuki/qrcodegen/QrCodeGeneratorDemo.java \
./io/nayuki/qrcodegen/QrCodeGeneratorWorker.java \
./io/nayuki/qrcodegen/QrCode.java \
./io/nayuki/qrcodegen/QrSegmentAdvanced.java \
./io/nayuki/qrcodegen/QrSegment.java \
./org/imgscalr/Scalr.java





