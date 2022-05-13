/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import com.formdev.flatlaf.FlatLightLaf;

import org.imgscalr.Scalr;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import static com.zoffcc.applications.trifa.AudioBar.audio_vu;
import static com.zoffcc.applications.trifa.AudioFrame.set_audio_out_bar_level;
import static com.zoffcc.applications.trifa.AudioSelectInBox.AUDIO_VU_MIN_VALUE;
import static com.zoffcc.applications.trifa.AudioSelectOutBox.semaphore_audio_out_convert;
import static com.zoffcc.applications.trifa.AudioSelectOutBox.semaphore_audio_out_convert_active_threads;
import static com.zoffcc.applications.trifa.AudioSelectOutBox.semaphore_audio_out_convert_max_active_threads;
import static com.zoffcc.applications.trifa.ConferenceMessageListFragmentJ.current_conf_id;
import static com.zoffcc.applications.trifa.HelperConference.get_last_conference_message_in_this_conference_within_n_seconds_from_sender_pubkey;
import static com.zoffcc.applications.trifa.HelperFiletransfer.check_auto_accept_incoming_filetransfer;
import static com.zoffcc.applications.trifa.HelperFiletransfer.get_incoming_filetransfer_local_filename;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_msgv3_capability;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.send_friend_msg_receipt_v2_wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.update_friend_in_db_capabilities;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.draw_main_top_icon;
import static com.zoffcc.applications.trifa.HelperGeneric.getImageFromClipboard;
import static com.zoffcc.applications.trifa.HelperGeneric.get_g_opts;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_msg_at_relay_from_id;
import static com.zoffcc.applications.trifa.HelperNotification.displayMessage;
import static com.zoffcc.applications.trifa.HelperNotification.init_system_tray;
import static com.zoffcc.applications.trifa.HelperRelay.get_own_relay_connection_status_real;
import static com.zoffcc.applications.trifa.HelperRelay.have_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.is_any_relay;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.add_outgoing_file;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.friendnum;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.get_current_friendnum;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.global_typing;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.setFriendName;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.typing_flag_thread;
import static com.zoffcc.applications.trifa.OrmaDatabase.create_db;
import static com.zoffcc.applications.trifa.OrmaDatabase.get_current_db_version;
import static com.zoffcc.applications.trifa.OrmaDatabase.update_db;
import static com.zoffcc.applications.trifa.Screenshot.getDisplayInfo;
import static com.zoffcc.applications.trifa.TRIFAGlobals.AVATAR_INCOMING_MAX_BYTE_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONTROL_PROXY_MESSAGE_TYPE.CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_AVATAR_FILENAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_IMAGE_THUMBNAIL_HEIGHT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_IMAGE_THUMBNAIL_WIDTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.IMAGE_THUMBNAIL_PLACEHOLDER;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_SYNC_DOUBLE_INTERVAL_SECS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_H264;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_VP8;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_IN_USE_H264;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_IN_USE_VP8;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_IN_USE_H264;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_IN_USE_VP8;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_NETWORK_ROUND_TRIP_MS;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_PLAY_BUFFER_ENTRIES;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_PLAY_DELAY;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ACCEPTING_A;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ACCEPTING_V;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ERROR;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_FINISHED;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_A;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_AV;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONFERENCE_TYPE.TOX_CONFERENCE_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static com.zoffcc.applications.trifa.TrifaToxService.resend_old_messages;
import static com.zoffcc.applications.trifa.TrifaToxService.resend_v3_messages;
import static com.zoffcc.applications.trifa.VideoInFrame.new_video_in_frame;
import static com.zoffcc.applications.trifa.VideoInFrame.on_call_ended_actions;
import static com.zoffcc.applications.trifa.VideoInFrame.on_call_started_actions;
import static com.zoffcc.applications.trifa.VideoInFrame.setup_video_in_resolution;
import static com.zoffcc.applications.trifa.VideoOutFrame.VideoInBitRate_text;
import static com.zoffcc.applications.trifa.VideoOutFrame.VideoOutBitRate_text;
import static java.awt.Font.PLAIN;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class MainActivity extends JFrame implements WindowListener, WindowFocusListener, WindowStateListener
{
    private static final String TAG = "trifa.MainActivity";
    static final String Version = "1.0.39";
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = false; // set "false" for release builds
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean DB_ENCRYPT = true; // set "true" always!
    final static boolean VFS_ENCRYPT = true; // set "true" always!
    final static boolean DEBUG_SCREENSHOT = false; // set "false" for release builds
    final static boolean DEBUG_EDT = false; // set "false" for release builds
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------

    static TrifaToxService tox_service_fg = null;
    static boolean native_lib_loaded = false;
    static long[] friends = null;
    static String app_files_directory = ".";
    static String password_hash = "pass";
    static Semaphore semaphore_tox_savedata = new Semaphore(1);

    static Connection sqldb = null;
    static int current_db_version = 0;

    static JFrame MainFrame = null;
    static VideoInFrame VideoInFrame1 = null;
    static VideoOutFrame VideoOutFrame1 = null;
    static AudioFrame AudioFrame1 = null;

    static EmojiSelectionTab EmojiFrame1 = null;
    static JSplitPane splitPane = null;
    static JSplitPane PeersplitPane = null;
    static FriendListFragmentJ FriendPanel;
    static PeerListFragmentJ PeerPanel;
    static JPanel leftPanel = null;
    static MessageListFragmentJ MessagePanel;
    static ConferenceMessageListFragmentJ MessagePanelConferences;
    static MessageListFragmentJInfo MessagePanel_Info;
    static JPanel MessagePanelContainer = null;
    static JPanel PeerPanelContainer = null;
    static JPanel MessageTextInputPanel;
    static JTextArea messageInputTextField;
    static JButton sendButton;
    static JButton attachmentButton;
    static JButton screengrabButton;
    static JButton FriendAddButton;
    static JTextField FriendAddToxID = null;
    static JTextField myToxID = null;
    static Style blueStyle;
    static Style blueSmallStyle;
    static Style redStyle;
    static Style mainStyle;
    static Style defaultStyle;
    static JTextArea ownProfileShort;
    static JPanel FriendAddPanel;
    static PopupToxIDQrcode QrcodeFrame = null;
    static BufferedImage PLACEHOLDER_IMG_RESIZED = null;

    // ---- lookup cache ----
    static Map<String, Long> cache_pubkey_fnum = new HashMap<String, Long>();
    static Map<Long, String> cache_fnum_pubkey = new HashMap<Long, String>();
    static Map<String, Long> cache_confid_confnum = new HashMap<String, Long>();
    // ---- lookup cache ----

    // ---- lookup cache for conference drawer ----
    static Map<String, Long> lookup_peer_listnum_pubkey = new HashMap<String, Long>();
    // ---- lookup cache for conference drawer ----

    static List<Long> selected_messages = new ArrayList<Long>();
    static List<Long> selected_messages_text_only = new ArrayList<Long>();
    static List<Long> selected_messages_incoming_file = new ArrayList<Long>();

    static boolean PREF__X_battery_saving_mode = false;
    static boolean PREF__auto_accept_image = true;
    static boolean PREF__auto_accept_video = true;
    static boolean PREF__auto_accept_all_upto = true;
    static boolean PREF__U_keep_nospam = false;
    static int PREF__audio_play_volume_percent = 100;
    static int PREF__udp_enabled = 1;
    static int PREF__orbot_enabled_to_int = 0;
    static int PREF__local_discovery_enabled = 1;
    static int PREF__ipv6_enabled = 1;
    static int PREF__force_udp_only = 0;
    static boolean PREF__show_image_thumbnails = true;

    static Random global_random = null;

    final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    static long update_all_messages_global_timestamp = -1;
    final static long UPDATE_MESSAGES_NORMAL_MILLIS = 250; // ~0.25 seconds
    final static SimpleDateFormat df_date_time_long = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final static SimpleDateFormat df_date_only = new SimpleDateFormat("yyyy-MM-dd");
    static ResourceBundle lo = null;
    static ByteBuffer video_buffer_1 = null;
    static ByteBuffer video_buffer_2 = null;
    static int buffer_size_in_bytes = 0;
    static ByteBuffer _recBuffer = null;
    static int message_panel_displayed = -1;
    final static String TTF_FONT_FILENAME = "TBitstreamVeraSans-Roman.ttf"; // "TDejaVuSans.ttf";
    final static String TTF_FONT_FAMILY_NAME = "TBitstream Vera Sans"; // "TDejaVuSans";
    final static int TTF_FONT_FAMILY_NAME_REGULAR_SIZE = 14;
    final static int TTF_FONT_FAMILY_NAME_SMALL_SIZE = 12;
    final static int TTF_FONT_FAMILY_NAME_SMALLER_SIZE = 9;
    final static int TTF_FONT_FAMILY_BUTTON_SIZE = 10;
    final static int TTF_FONT_FAMILY_MSG_DATE_SIZE = 10;
    final static int TTF_FONT_FAMILY_NAME_EMOJI_REGULAR_SIZE = 20;
    final static int TTF_FONT_FAMILY_FLIST_STATS_SIZE = 15;
    final static int TTF_FONT_FAMILY_BORDER_TITLE = 12;
    final static int TTF_FONT_FAMILY_MENU_SIZE = 12;
    final static int AVATAR_FRIENDLIST_W = 15;
    final static int AVATAR_FRIENDLIST_H = 15;
    final static int AVATAR_FRIENDINFO_W = 90;
    final static int AVATAR_FRIENDINFO_H = 90;

    static boolean mainwindow_has_focus = true;

    static class send_message_result
    {
        long msg_num;
        boolean msg_v2;
        String msg_hash_hex;
        String msg_hash_v3_hex;
        String raw_message_buf_hex;
        long error_num;
    }

    public MainActivity()
    {
        super("TRIfA - Desktop - " + Version + "   ");

        // Thread.currentThread().setName("t_main_act");

        load_prefs();

        try
        {
            this.addWindowListener(this);
            this.addWindowFocusListener(this);
            this.addWindowStateListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            // UIManager.getLookAndFeelDefaults().put("defaultFont", new Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));
            UIManager.put("Viewport.font",
                          new FontUIResource(new Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE)));
            UIManager.put("TextField.font",
                          new FontUIResource(new Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE)));
            UIManager.put("TextArea.font",
                          new FontUIResource(new Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE)));
            UIManager.put("TextPane.font",
                          new FontUIResource(new Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE)));
            UIManager.put("EditorPane.font",
                          new FontUIResource(new Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE)));
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            // HINT: show proper name in MacOS Menubar
            // https://alvinalexander.com/java/java-application-name-mac-menu-bar-menubar-class-name/
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TRIfA - Desktop");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TRIfA - Desktop");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            // com.apple.eawt.Application.setDockIconImage(icon); // Dock icon
            Image image = Toolkit.getDefaultToolkit().getImage("trifa_icon.png");

            //this is new since JDK 9
            final Taskbar taskbar = Taskbar.getTaskbar();

            try
            {
                // set icon for mac os (and other systems which do support this method)
                taskbar.setIconImage(image);
            }
            catch (final UnsupportedOperationException e)
            {
                Log.i(TAG, "The os does not support: 'taskbar.setIconImage'");
            }
            catch (final SecurityException e)
            {
                Log.i(TAG, "There was a security exception for: 'taskbar.setIconImage'");
            }

            //set icon for windows os (and other systems which do support this method)
            setIconImage(image);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        MainFrame = this;

        init_system_tray();

        VideoInFrame1 = new VideoInFrame();
        VideoOutFrame1 = new VideoOutFrame();
        AudioFrame1 = new AudioFrame();
        EmojiFrame1 = new EmojiSelectionTab();

        setLayout(new FlowLayout());

        // -------------- set previous size of main window --------------
        try
        {
            Preferences prefs = Preferences.userRoot().node("trifa_desktop/main_window");

            setLocation(getFrameLocation(prefs, 10, 10));
            setSize(getFrameSize(prefs, 600, 400));

            CoalescedEventUpdater updater = new CoalescedEventUpdater(400, () -> updatePref(this, prefs));

            addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    updater.update();
                }

                @Override
                public void componentMoved(ComponentEvent e)
                {
                    updater.update();
                }
            });
        }
        catch (Exception e)
        {
            setSize(600, 400);
        }
        // -------------- set previous size of main window --------------

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                want_exit();
            }
        });

        splitPane = new JSplitPane();
        PeersplitPane = new JSplitPane();

        FriendPanel = new FriendListFragmentJ();
        MessagePanelContainer = new JPanel(true);
        PeerPanelContainer = new JPanel(true);

        MessagePanelContainer.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                super.componentResized(e);
                // Log.i(TAG, "componentResized");
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                super.componentMoved(e);
                // Log.i(TAG, "componentMoved");
            }
        });

        // ------------------
        // ------------------
        // ------------------
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        // Create and add the main document style
        defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = sc.addStyle("MainStyle", defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "monospaced");
        StyleConstants.setFontSize(mainStyle, 12);

        // Create and add the constant width style
        redStyle = sc.addStyle("ConstantWidthRed", null);
        StyleConstants.setFontFamily(redStyle, "monospaced");
        StyleConstants.setFontSize(redStyle, 12);
        StyleConstants.setForeground(redStyle, Color.red);

        // Create and add the constant width style
        blueStyle = sc.addStyle("ConstantWidthBlue", null);
        StyleConstants.setFontFamily(blueStyle, "monospaced");
        StyleConstants.setFontSize(blueStyle, 12);
        StyleConstants.setForeground(blueStyle, Color.blue);

        blueSmallStyle = sc.addStyle("ConstantWidthBlue", null);
        StyleConstants.setFontFamily(blueSmallStyle, "monospaced");
        StyleConstants.setFontSize(blueSmallStyle, 9);
        StyleConstants.setForeground(blueSmallStyle, Color.blue);

        // Create and add the heading style
        /*
        final Style heading2Style = sc.addStyle("Heading2", null);
        StyleConstants.setForeground(heading2Style, Color.red);
        StyleConstants.setFontSize(heading2Style, 16);
        StyleConstants.setFontFamily(heading2Style, "serif");
        StyleConstants.setBold(heading2Style, true);
        StyleConstants.setLeftIndent(heading2Style, 8);
        StyleConstants.setFirstLineIndent(heading2Style, 0);
        */
        // ------------------
        // ------------------
        // ------------------

        MessagePanel_Info = new MessageListFragmentJInfo();
        MessagePanel = new MessageListFragmentJ();
        MessagePanel.setCurrentPK(null);

        MessagePanelConferences = new ConferenceMessageListFragmentJ();

        PeerPanel = new PeerListFragmentJ();

        MessageTextInputPanel = new JPanel(true);

        messageInputTextField = new JTextArea();
        messageInputTextField.setFont(
                new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));
        Action action = messageInputTextField.getActionMap().get("paste-from-clipboard");
        // messageInputTextField.setLineWrap(true);
        messageInputTextField.getActionMap().put("paste-from-clipboard", new ProxyPasteAction(action));
        //messageInputTextField.setTransferHandler(new ImageTransferHandler(messageInputTextField));
        //messageInputTextField.setDropMode(DropMode.INSERT);
        //messageInputTextField.setDragEnabled(true);
        //messageInputTextField.setDropTarget(new DropTarget(messageInputTextField, new FileDropTargetListener()));

        new FileDrop(null, messageInputTextField, /*dragBorder,*/ new FileDrop.Listener()
        {
            public void filesDropped(java.io.File[] files)
            {
                for (int i = 0; i < files.length; i++)
                {
                    try
                    {
                        if (message_panel_displayed == 1)
                        {
                            if (get_current_friendnum() != -1)
                            {
                                Log.i(TAG, "FFF:" + files[i].getCanonicalPath());
                                Log.i(TAG, "FFF:" + files[i].getAbsoluteFile().getParent() + " " +
                                           files[i].getAbsoluteFile().getName());
                                // send file
                                add_outgoing_file(files[i].getAbsoluteFile().getParent(),
                                                  files[i].getAbsoluteFile().getName());
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        sendButton = new JButton(lo.getString("send_button_text"));
        sendButton.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));

        attachmentButton = new JButton("\uD83D\uDCCE"); // paperclip utf-8 char
        attachmentButton.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));

        screengrabButton = new JButton("\uD83D\uDCBB"); // PERSONAL COMPUTER utf-8 char
        screengrabButton.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));

        leftPanel = new JPanel(true);
        ownProfileShort = new JTextArea();

        FriendAddPanel = new JPanel(true);
        FriendAddPanel.setLayout(new BoxLayout(FriendAddPanel, BoxLayout.Y_AXIS));
        FriendAddPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        FriendAddToxID = new JTextField("");
        FriendAddToxID.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));
        FriendAddPanel.add(FriendAddToxID);
        FriendAddButton = new JButton(lo.getString("add_friend_button_text"));
        FriendAddButton.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        FriendAddPanel.add(FriendAddButton);

        FriendAddButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "FriendAddButton pressed");
                String toxid = FriendAddToxID.getText().
                        replace(" ", "").
                        replace("\r", "").
                        replace("\n", "");

                String friend_tox_id = toxid.toUpperCase().replace(" ", "").replaceFirst("tox:", "").replaceFirst(
                        "TOX:", "").replaceFirst("Tox:", "");
                FriendAddToxID.setText("");
                HelperFriend.add_friend_real(friend_tox_id);
            }
        });

        this.setVisible(true);

        myToxID = new JTextField();
        myToxID.setVisible(true);
        myToxID.setEditable(false);

        getContentPane().setLayout(new GridLayout());
        getContentPane().add(splitPane);
        splitPane.setVisible(true);

        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(180);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(MessagePanelContainer);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(ownProfileShort);
        leftPanel.add(FriendPanel);
        leftPanel.add(FriendAddPanel);
        leftPanel.setVisible(true);

        ownProfileShort.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));
        ownProfileShort.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        ownProfileShort.setEditable(false);
        ownProfileShort.setVisible(true);

        MessagePanelContainer.setLayout(new BoxLayout(MessagePanelContainer, BoxLayout.Y_AXIS));
        PeerPanelContainer.setLayout(new BoxLayout(PeerPanelContainer, BoxLayout.Y_AXIS));
        myToxID.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));
        myToxID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        set_message_panel(0);

        MessageTextInputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        MessageTextInputPanel.setLayout(new BoxLayout(MessageTextInputPanel, BoxLayout.X_AXIS));

        MessageTextInputPanel.add(messageInputTextField);
        messageInputTextField.setEditable(true);

        messageInputTextField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if (e.isShiftDown())
                    {
                        // Log.i(TAG, "SHIFT Enter pressed");
                        messageInputTextField.append("\n");
                        return;
                    }
                    // Log.i(TAG, "Enter key pressed");
                    if (message_panel_displayed == 1)
                    {
                        messageInputTextField.setText(messageInputTextField.getText().replaceFirst("[\n\r]$", ""));
                        final String msg = messageInputTextField.getText();
                        final Thread t = new Thread(() -> {
                            try
                            {
                                MessageListFragmentJ.send_message_onclick(msg);
                            }
                            catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                        });
                        t.start();
                    }
                    else if (message_panel_displayed == 2)
                    {
                        messageInputTextField.setText(messageInputTextField.getText().replaceFirst("[\n\r]$", ""));
                        final String msg = messageInputTextField.getText();
                        final Thread t = new Thread(() -> {
                            try
                            {
                                ConferenceMessageListFragmentJ.send_message_onclick(msg);
                            }
                            catch (Exception e12)
                            {
                                e12.printStackTrace();
                            }
                        });
                        t.start();
                    }
                }
            }
        });


        MessageTextInputPanel.add(sendButton);
        MessageTextInputPanel.add(attachmentButton);
        MessageTextInputPanel.add(screengrabButton);

        sendButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "sendButton pressed");

                if (message_panel_displayed == 1)
                {
                    MessageListFragmentJ.send_message_onclick(messageInputTextField.getText());
                }
                else if (message_panel_displayed == 2)
                {
                    ConferenceMessageListFragmentJ.send_message_onclick(messageInputTextField.getText());
                }
            }
        });

        screengrabButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                take_screen_shot_with_selection();
            }
        });

        attachmentButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "attachmentButton pressed");

                try
                {
                    if (message_panel_displayed == 1)
                    {
                        if (friendnum != -1)
                        {
                            // show filepicker
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                            fileChooser.setDialogTitle(lo.getString("select_files_dialog_title"));
                            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            fileChooser.resetChoosableFileFilters();
                            fileChooser.setAcceptAllFileFilterUsed(true);
                            fileChooser.setMultiSelectionEnabled(true);
                            int result = fileChooser.showOpenDialog(attachmentButton);
                            if (result == JFileChooser.APPROVE_OPTION)
                            {
                                for (File f1 : fileChooser.getSelectedFiles())
                                {
                                    System.out.println("selected_file: " + f1.getAbsoluteFile().getParent() + " :: " +
                                                       f1.getAbsoluteFile().getName());
                                    // send file
                                    add_outgoing_file(f1.getAbsoluteFile().getParent(), f1.getAbsoluteFile().getName());
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        doc.setLogicalStyle(0, mainStyle);
        try
        {
            doc.insertString(0, "", null);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

        messageInputTextField.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updated();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updated();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                updated();
            }

            void updated()
            {
                if (global_typing == 0)
                {
                    global_typing = 1;  // typing = 1

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                tox_self_set_typing(friendnum, global_typing);
                                // Log.i(TAG, "typing:fn#" + friendnum + ":activated");
                            }
                            catch (Exception e)
                            {
                                // Log.i(TAG, "typing:fn#" + friendnum + ":EE1" + e.getMessage());
                            }
                        }
                    };

                    SwingUtilities.invokeLater(myRunnable);

                    try
                    {
                        typing_flag_thread.interrupt();
                    }
                    catch (Exception e)
                    {
                        // e.printStackTrace();
                    }

                    typing_flag_thread = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            boolean skip_flag_update = false;
                            try
                            {
                                Thread.sleep(TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS); // sleep for n seconds
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                                // ok, dont update typing flag
                                skip_flag_update = true;
                            }

                            if (global_typing == 1)
                            {
                                if (skip_flag_update == false)
                                {
                                    global_typing = 0;  // typing = 0
                                    Runnable myRunnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                tox_self_set_typing(friendnum, global_typing);
                                                // Log.i(TAG, "typing:fn#" + friendnum + ":DEactivated");
                                            }
                                            catch (Exception e)
                                            {
                                                // Log.i(TAG, "typing:fn#" + friendnum + ":EE2" + e.getMessage());
                                            }
                                        }
                                    };

                                    SwingUtilities.invokeLater(myRunnable);
                                }
                            }
                        }
                    };
                    typing_flag_thread.start();
                }
            }
        });


        final Thread set_focus_on_textinput = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    while (!messageInputTextField.isValid())
                    {
                        Thread.sleep(10);
                    }

                    while (!messageInputTextField.isShowing())
                    {
                        Thread.sleep(10);
                    }

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            messageInputTextField.requestFocus();
                            // sendTextField.revalidate();
                        }
                    };
                    SwingUtilities.invokeLater(myRunnable);
                }
                catch (Exception e)
                {
                }
            }
        };

        set_focus_on_textinput.start();

        // ------------ Main Menu ----------
        JMenuBar main_menu = new JMenuBar();
        JMenu m1 = new JMenu(lo.getString("menu_file"));
        m1.setFont(new java.awt.Font("SansSerif", PLAIN, TTF_FONT_FAMILY_MENU_SIZE));

        main_menu.add(m1);
        this.setJMenuBar(main_menu);

        // -------
        JMenuItem m11 = new JMenuItem(new AbstractAction(lo.getString("menu_settings"))
        {
            public void actionPerformed(ActionEvent e)
            {
                Log.i(TAG, "Settings selected");
                SettingsActivity SettingsFrame = new SettingsActivity();
                SettingsFrame.setVisible(true);
            }
        });
        m11.setFont(new java.awt.Font("SansSerif", PLAIN, TTF_FONT_FAMILY_MENU_SIZE));
        m1.add(m11);
        // -------
        // -------
        JMenuItem m12 = new JMenuItem(new AbstractAction(lo.getString("menu_exit"))
        {
            public void actionPerformed(ActionEvent e)
            {
                Log.i(TAG, "Exit selected");
                want_exit();
            }
        });
        m12.setFont(new java.awt.Font("SansSerif", PLAIN, TTF_FONT_FAMILY_MENU_SIZE));
        m1.add(m12);
        // -------

        // ------------ Main Menu ----------

        VideoInFrame1.setLocationRelativeTo(null);
        final Rectangle bounds = this.getBounds();
        VideoInFrame1.setLocation(bounds.x + bounds.width, bounds.y);

        VideoOutFrame1.setLocationRelativeTo(null);
        final Rectangle bounds2 = VideoInFrame1.getBounds();
        VideoOutFrame1.setLocation(bounds2.x, bounds2.y + bounds2.height);

        AudioFrame1.setLocationRelativeTo(null);
        final Rectangle bounds3 = VideoOutFrame1.getBounds();
        AudioFrame1.setLocation(bounds3.x, bounds3.y + bounds3.height);

        EmojiFrame1.setLocationRelativeTo(null);
        // final Rectangle bounds4 = EmojiFrame1.getBounds();
        EmojiFrame1.setLocation(bounds.x, bounds.y + bounds.height);

        EventQueue.invokeLater(() -> {
            this.toFront();
            // this.pack();
            this.revalidate();
        });

        addKeyBinding(getRootPane(), "F11", new FullscreenToggleAction(this));

        try
        {
            if (have_own_relay())
            {
                int relay_connection_status_real = get_own_relay_connection_status_real();

                if (relay_connection_status_real == 2)
                {
                    draw_main_top_icon(0x04b431, true);
                }
                else if (relay_connection_status_real == 1)
                {
                    draw_main_top_icon(0xffce00, true);
                }
                else
                {
                    draw_main_top_icon(0xff0000, true);
                }
            }
            else
            {
                draw_main_top_icon(Color.GRAY.getRGB(), true);
            }
        }
        catch (Exception e)
        {
        }
    }

    static void load_prefs()
    {
        if (get_g_opts("PREF__udp_enabled") != null)
        {
            if (get_g_opts("PREF__udp_enabled").equals("true"))
            {
                PREF__udp_enabled = 1;
            }
            else
            {
                PREF__udp_enabled = 0;
            }
        }
        else
        {
            PREF__udp_enabled = 1;
        }

        if (get_g_opts("PREF__show_image_thumbnails") != null)
        {
            if (get_g_opts("PREF__show_image_thumbnails").equals("true"))
            {
                PREF__show_image_thumbnails = true;
            }
            else
            {
                PREF__show_image_thumbnails = false;
            }
        }
        else
        {
            PREF__show_image_thumbnails = true;
        }
    }

    static void set_message_panel(int i)
    {
        EventQueue.invokeLater(() -> {

            // Log.i(TAG, "set_message_panel:001:" + i);

            //current_conf_id = "-1";
            //MessagePanel.setCurrentPK(null);
            //MessagePanel.friendnum = -1;

            //if (message_panel_displayed != i)
            {
                if (i == 0)
                {
                    current_conf_id = "-1";
                    MessagePanel.setCurrentPK(null);
                    MessagePanel.friendnum = -1;

                    PeerPanelContainer.removeAll();
                    MessagePanelContainer.removeAll();
                    MessagePanelContainer.add(MessagePanel_Info);
                    MessagePanelContainer.add(MessageTextInputPanel);
                    MessagePanelContainer.add(myToxID, BorderLayout.SOUTH);
                    MessagePanelContainer.revalidate();
                    MessagePanelContainer.repaint();
                    // Log.i(TAG, "set_message_panel:002:" + i);
                }
                else if (i == 1)
                {
                    current_conf_id = "-1";

                    PeerPanelContainer.removeAll();
                    MessagePanelContainer.removeAll();
                    MessagePanelContainer.add(MessagePanel);
                    MessagePanelContainer.add(MessageTextInputPanel);
                    MessagePanelContainer.add(myToxID, BorderLayout.SOUTH);
                    MessagePanelContainer.revalidate();
                    MessagePanelContainer.repaint();
                    // Log.i(TAG, "set_message_panel:002:" + i);
                }
                else
                {
                    MessagePanel.setCurrentPK(null);
                    MessagePanel.friendnum = -1;

                    PeerPanelContainer.removeAll();
                    MessagePanelContainer.removeAll();

                    // MessagePanelContainer.setLayout(new GridLayout());
                    MessagePanelContainer.add(PeersplitPane);
                    PeersplitPane.setVisible(true);

                    PeersplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                    PeersplitPane.setLeftComponent(PeerPanel);
                    PeersplitPane.setRightComponent(PeerPanelContainer);

                    PeerPanelContainer.add(MessagePanelConferences);
                    PeerPanelContainer.add(MessageTextInputPanel);
                    PeerPanelContainer.add(myToxID, BorderLayout.SOUTH);
                    MessagePanelContainer.revalidate();
                    MessagePanelContainer.repaint();

                    PeersplitPane.setDividerLocation(160);

                    ConferenceMessageListFragmentJ.update_group_all_users();
                    // Log.i(TAG, "set_message_panel:002:" + i);
                }

                myToxID.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mousePressed(final MouseEvent e)
                    {
                        if (SwingUtilities.isRightMouseButton(e))
                        {
                            Log.i(TAG, "PopupToxIDQrcode");
                            try
                            {
                                if (QrcodeFrame != null)
                                {
                                    QrcodeFrame.setVisible(false);
                                    QrcodeFrame.dispose();
                                    QrcodeFrame = null;
                                }
                            }
                            catch (Exception ew)
                            {
                                QrcodeFrame = null;
                            }
                            QrcodeFrame = new PopupToxIDQrcode();
                            QrcodeFrame.setVisible(true);
                        }
                    }
                });

                message_panel_displayed = i;
            }
        });
    }

    public static final void addKeyBinding(JComponent c, String key, final Action action)
    {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        c.getActionMap().put(key, action);
        c.setFocusable(true);
    }

    private static class MyEventQueue extends EventQueue
    {
        public void postEvent(AWTEvent theEvent)
        {
            final long rand_value = (long) global_random.nextInt() + (1L << 31);
            boolean need_print = false;
            if (DEBUG_EDT)
            {
                StackTraceElement[] ste = Thread.currentThread().getStackTrace();
                for (StackTraceElement a : ste)
                {
                    if (a.toString().contains("com.zoffcc.applications.trifa"))
                    {
                        if (!a.toString().contains("MyEventQueue.postEvent"))
                        {
                            System.out.println(a.toString());
                            need_print = true;
                        }
                    }
                }

                if (need_print)
                {
                    Log.i("-EVENT-", "start::" + rand_value + "::" + theEvent.getID() + " " + theEvent.paramString());
                }
            }
            super.postEvent(theEvent);
            if (DEBUG_EDT)
            {
                if (need_print)
                {
                    Log.i("-EVENT-", "end::" + rand_value);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        System.out.println("Version:" + Version);

        global_random = new Random();

        EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        eventQueue.push(new MyEventQueue());

        try
        {
            System.out.println("java.vm.name:" + System.getProperty("java.vm.name"));
            System.out.println("java.home:" + System.getProperty("java.home"));
            System.out.println("java.vendor:" + System.getProperty("java.vendor"));
            System.out.println("java.version:" + System.getProperty("java.version"));
            System.out.println("java.specification.vendor:" + System.getProperty("java.specification.vendor"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            // HINT: show proper name in MacOS Menubar
            // https://alvinalexander.com/java/java-application-name-mac-menu-bar-menubar-class-name/
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TRIfA - Desktop");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // // how to change locale ---------------
        // Locale.setDefault(Locale.GERMAN);
        // ResourceBundle.clearCache();
        // // how to change locale ---------------

        Locale locale = Locale.getDefault();
        Log.i(TAG, locale.getDisplayCountry());
        Log.i(TAG, locale.getDisplayLanguage());
        Log.i(TAG, locale.getDisplayName());
        Log.i(TAG, locale.getISO3Country());
        Log.i(TAG, locale.getISO3Language());
        Log.i(TAG, locale.getLanguage());
        Log.i(TAG, locale.getCountry());

        try
        {
            // set "StartupWMClass" for Java Swing applications
            //
            // https://stackoverflow.com/a/29218320
            //
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField = null;
            awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit,
                                     "normal_trifa"); // this needs to be exactly the same String as in "trifa.desktop" file
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Thread.currentThread().setName("t_main");
        }
        catch (Exception e)
        {
        }

        lo = ResourceBundle.getBundle("i18n.ResourceBundle", locale);
        Log.i(TAG, "locale_test:" + lo.getString("locale_test"));

        Log.i(TAG, "running_on:" + OperatingSystem.getCurrent().toString());

        try
        {
            for (int i = 0; i < UIManager.getInstalledLookAndFeels().length; i++)
            {
                Log.i(TAG, "look_and_feel:" + i + ":" + UIManager.getInstalledLookAndFeels()[i].getName());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            try
            {
                if (args[0].equalsIgnoreCase("sys"))
                {
                    // Set System L&F
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                else
                {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                }
            }
            catch (Exception ex1)
            {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
        }
        catch (Exception ex2)
        {
            System.err.println("Failed to initialize LaF");
        }

        try
        {
            final String asset_filename =
                    "." + File.separator + "assets" + File.separator + IMAGE_THUMBNAIL_PLACEHOLDER;
            final BufferedImage bi = ImageIO.read(new File(asset_filename));
            final Dimension newMaxSize = new Dimension(FT_IMAGE_THUMBNAIL_WIDTH, FT_IMAGE_THUMBNAIL_HEIGHT);
            PLACEHOLDER_IMG_RESIZED = Scalr.resize(bi, Scalr.Method.SPEED, newMaxSize.width, newMaxSize.height);
        }
        catch (Exception e)
        {
            PLACEHOLDER_IMG_RESIZED = null;
            e.printStackTrace();
        }

        orma = new OrmaDatabase();

        TrifaToxService.TOX_SERVICE_STARTED = false;
        bootstrapping = false;
        Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"));

        Log.i(TAG, "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch());
        Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version());

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            // --- DO NOT optimize this out of here ---
            public void run()
            {
                try
                {
                    System.out.println("Shutting down ...");
                    try
                    {
                        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    tox_service_fg.stop_me = true;

                    try
                    {
                        Thread.sleep(700);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    try
                    {
                        sqldb.close();
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                catch (Exception e)
                {
                    System.out.println("ERROR in Shutdown");
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            // --- DO NOT optimize this out of here ---
        });


        // create a database connection
        try
        {
            // Class.forName("org.sqlite.JDBC");
            sqldb = DriverManager.getConnection("jdbc:sqlite:main.db");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        current_db_version = get_current_db_version();
        Log.i(TAG, "trifa:current_db_version=" + current_db_version);
        create_db();
        current_db_version = update_db(current_db_version);
        Log.i(TAG, "trifa:new_db_version=" + current_db_version);
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------

        tox_service_fg = new TrifaToxService();

        if (!TrifaToxService.TOX_SERVICE_STARTED)
        {
            String ORBOT_PROXY_HOST = "";
            long ORBOT_PROXY_PORT = 0;

            app_files_directory = "." + File.separator;

            init(app_files_directory, PREF__udp_enabled, PREF__local_discovery_enabled, PREF__orbot_enabled_to_int,
                 ORBOT_PROXY_HOST, ORBOT_PROXY_PORT, password_hash, PREF__ipv6_enabled, PREF__force_udp_only);

            tox_service_fg.tox_thread_start_fg();
        }


        try
        {
            // File file = new File("assets/NotoEmoji-Regular.ttf");
            // File file = new File("assets/OpenMoji.ttf");
            // File file = new File("assets/OpenSansEmoji.ttf");
            // File file = new File("assets/TwitterColorEmoji-SVGinOT.ttf");
            //**// File file = new File("assets/TwitterColorEmoji-SVGinOT-OSX.ttf");
            File file = new File("." + File.separator + "assets" + File.separator + TTF_FONT_FILENAME);
            Font font = Font.createFont(Font.TRUETYPE_FONT, file);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (!ge.registerFont(font))
            {
                Log.i(TAG, "FONT:NotoEmoji-Regular could not be registered");
            }
            else
            {
                Log.i(TAG, "FONT:NotoEmoji-Regular *REGISTERED*");
            }

            //for (Font ft : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
            //{
            // Log.i(TAG, "FONT::" + ft.getFamily() + "::" + ft.getFontName() + "::" + ft.getName());
            //}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // ---------------------------------------------
        // java.awt.EventQueue.invokeLater(() -> new MainActivity().setVisible(true));
        new MainActivity();
        // final Thread main_t = new Thread(MainActivity::new);
        // main_t.start();
        // ---------------------------------------------

        String my_tox_id_temp = get_my_toxid();
        Log.i(TAG, "MyToxID:" + my_tox_id_temp);

        if (DEBUG_SCREENSHOT)
        {
            final Thread t_screengrab = new Thread(() -> {
                try
                {
                    Thread.sleep(20 * 1000);
                    take_screen_shot();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
            t_screengrab.start();
        }

        try
        {
            Thread.currentThread().setName("t_main");
        }
        catch (Exception e)
        {
        }
    }

    static
    {
        try
        {
            System.loadLibrary("jni-c-toxcore");
            native_lib_loaded = true;
            Log.i(TAG, "successfully loaded native library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_lib_loaded = false;
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
            e.printStackTrace();
            System.exit(4);
        }
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public static native void init(String data_dir, int udp_enabled, int local_discovery_enabled, int orbot_enabled, String orbot_host, long orbot_port, String tox_encrypt_passphrase_hash, int enable_ipv6, int force_udp_only_mode);

    public native String getNativeLibAPI();

    public static native String getNativeLibGITHASH();

    public static native String getNativeLibTOXGITHASH();

    public static native void update_savedata_file(String tox_encrypt_passphrase_hash);

    public static native String get_my_toxid();

    public static native int add_tcp_relay_single(String ip, String key_hex, long port);

    public static native int bootstrap_single(String ip, String key_hex, long port);

    public static native int tox_self_get_connection_status();

    public static native void init_tox_callbacks();

    public static native long tox_iteration_interval();

    public static native long tox_iterate();

    // ----------- TRIfA internal -----------
    public static native int jni_iterate_group_audio(int delta_new, int want_ms_output);

    public static native int jni_iterate_videocall_audio(int delta_new, int want_ms_output, int channels, int sample_rate, int send_emtpy_buffer);

    public static native void crgb2yuv(java.nio.ByteBuffer rgba_buf, java.nio.ByteBuffer yuv_buf, int w_yuv, int h_yuv, int w_rgba, int h_rgba);

    public static native void tox_set_do_not_sync_av(int do_not_sync_av);

    public static native void tox_set_onion_active(int active);
    // ----------- TRIfA internal -----------

    public static native long tox_kill();

    public static native void exit();

    public static native long tox_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, String message);

    public static native long tox_version_major();

    public static native long tox_version_minor();

    public static native long tox_version_patch();

    public static native String jnictoxcore_version();

    public static native long tox_max_filename_length();

    public static native long tox_file_id_length();

    public static native long tox_max_message_length();

    public static native long tox_friend_add(String toxid_str, String message);

    public static native long tox_friend_add_norequest(String public_key_str);

    public static native long tox_self_get_friend_list_size();

    public static native void tox_self_set_nospam(long nospam); // this actually needs an "uint32_t" which is an unsigned 32bit integer value

    public static native long tox_self_get_nospam(); // this actually returns an "uint32_t" which is an unsigned 32bit integer value

    public static native long tox_friend_by_public_key(String friend_public_key_string);

    public static native String tox_friend_get_public_key(long friend_number);

    public static native long tox_friend_get_capabilities(long friend_number);

    public static native long[] tox_self_get_friend_list();

    public static native int tox_self_set_name(String name);

    public static native int tox_self_set_status_message(String status_message);

    public static native void tox_self_set_status(int a_TOX_USER_STATUS);

    public static native int tox_self_set_typing(long friend_number, int typing);

    public static native int tox_friend_get_connection_status(long friend_number);

    public static native int tox_friend_delete(long friend_number);

    public static native String tox_self_get_name();

    public static native long tox_self_get_name_size();

    public static native long tox_self_get_status_message_size();

    public static native String tox_self_get_status_message();

    public static native int tox_friend_send_lossless_packet(long friend_number, byte[] data, int data_length);

    public static native int tox_file_control(long friend_number, long file_number, int a_TOX_FILE_CONTROL);

    public static native int tox_hash(ByteBuffer hash_buffer, ByteBuffer data_buffer, long data_length);

    public static native int tox_file_seek(long friend_number, long file_number, long position);

    public static native int tox_file_get_file_id(long friend_number, long file_number, ByteBuffer file_id_buffer);

    public static native long tox_file_send(long friend_number, long kind, long file_size, ByteBuffer file_id_buffer, String file_name, long filename_length);

    public static native int tox_file_send_chunk(long friend_number, long file_number, long position, ByteBuffer data_buffer, long data_length);

    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    public static native long tox_messagev2_size(long text_length, long type, long alter_type);

    public static native int tox_messagev2_wrap(long text_length, long type, long alter_type, ByteBuffer message_text_buffer, long ts_sec, long ts_ms, ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);

    public static native int tox_messagev2_get_message_id(ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);

    public static native long tox_messagev2_get_ts_sec(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_ts_ms(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_message_text(ByteBuffer raw_message_buffer, long raw_message_len, int is_alter_msg, long alter_type, ByteBuffer message_text_buffer);

    public static native String tox_messagev2_get_sync_message_pubkey(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_sync_message_type(ByteBuffer raw_message_buffer);

    public static native int tox_util_friend_send_msg_receipt_v2(long friend_number, long ts_sec, ByteBuffer msgid_buffer);

    public static native long tox_util_friend_send_message_v2(long friend_number, int type, long ts_sec, String message, long length, ByteBuffer raw_message_back_buffer, ByteBuffer raw_message_back_buffer_length, ByteBuffer msgid_back_buffer);

    public static native int tox_util_friend_resend_message_v2(long friend_number, ByteBuffer raw_message_buffer, long raw_msg_len);
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------

    // --------------- Message V3 -------------
    // --------------- Message V3 -------------
    // --------------- Message V3 -------------
    public static native int tox_messagev3_get_new_message_id(ByteBuffer hash_buffer);

    public static native long tox_messagev3_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, String message, ByteBuffer mag_hash, long timestamp);
    // --------------- Message V3 -------------
    // --------------- Message V3 -------------
    // --------------- Message V3 -------------

    // --------------- Conference -------------
    // --------------- Conference -------------
    // --------------- Conference -------------

    public static native long tox_conference_join(long friend_number, ByteBuffer cookie_buffer, long cookie_length);

    public static native long tox_conference_peer_count(long conference_number);

    public static native long tox_conference_peer_get_name_size(long conference_number, long peer_number);

    public static native String tox_conference_peer_get_name(long conference_number, long peer_number);

    public static native String tox_conference_peer_get_public_key(long conference_number, long peer_number);

    public static native long tox_conference_offline_peer_count(long conference_number);

    public static native long tox_conference_offline_peer_get_name_size(long conference_number, long offline_peer_number);

    public static native String tox_conference_offline_peer_get_name(long conference_number, long offline_peer_number);

    public static native String tox_conference_offline_peer_get_public_key(long conference_number, long offline_peer_number);

    public static native long tox_conference_offline_peer_get_last_active(long conference_number, long offline_peer_number);

    public static native int tox_conference_peer_number_is_ours(long conference_number, long peer_number);

    public static native long tox_conference_get_title_size(long conference_number);

    public static native String tox_conference_get_title(long conference_number);

    public static native int tox_conference_get_type(long conference_number);

    public static native int tox_conference_send_message(long conference_number, int a_TOX_MESSAGE_TYPE, String message);

    public static native int tox_conference_delete(long conference_number);

    public static native long tox_conference_get_chatlist_size();

    public static native long[] tox_conference_get_chatlist();

    public static native int tox_conference_get_id(long conference_number, ByteBuffer cookie_buffer);

    public static native int tox_conference_new();

    public static native int tox_conference_invite(long friend_number, long conference_number);

    public static native int tox_conference_set_title(long conference_number, String title);
    // --------------- Conference -------------
    // --------------- Conference -------------
    // --------------- Conference -------------

    // --------------- new Groups -------------
    // --------------- new Groups -------------
    // --------------- new Groups -------------

    /**
     * Creates a new group chat.
     * <p>
     * This function creates a new group chat object and adds it to the chats array.
     * <p>
     * The caller of this function has Founder role privileges.
     * <p>
     * The client should initiate its peer list with self info after calling this function, as
     * the peer_join callback will not be triggered.
     *
     * @param a_TOX_GROUP_PRIVACY_STATE The privacy state of the group. If this is set to TOX_GROUP_PRIVACY_STATE_PUBLIC,
     *                                  the group will attempt to announce itself to the DHT and anyone with the Chat ID may join.
     *                                  Otherwise a friend invite will be required to join the group.
     * @param group_name                The name of the group. The name must be non-NULL.
     * @param my_peer_name              The name of the peer creating the group.
     * @return group_number on success, UINT32_MAX on failure.
     */
    public static native long tox_group_new(int a_TOX_GROUP_PRIVACY_STATE, String group_name, String my_peer_name);

    /**
     * Joins a group chat with specified Chat ID.
     * <p>
     * This function creates a new group chat object, adds it to the chats array, and sends
     * a DHT announcement to find peers in the group associated with chat_id. Once a peer has been
     * found a join attempt will be initiated.
     *
     * @param chat_id_buffer The Chat ID of the group you wish to join. This must be TOX_GROUP_CHAT_ID_SIZE bytes.
     * @param password       The password required to join the group. Set to NULL if no password is required.
     * @param my_peer_name   The name of the peer joining the group.
     * @return group_number on success, UINT32_MAX on failure.
     */
    public static native long tox_group_join(ByteBuffer chat_id_buffer, long chat_id_length, String my_peer_name, String password);

    public static native int tox_group_leave(long group_number, String part_message);

    public static native long tox_group_self_get_peer_id(long group_number);

    public static native String tox_group_self_get_public_key(long group_number);

    public static native int tox_group_get_chat_id(long group_number, ByteBuffer chat_id_buffer);

    public static native long tox_group_get_number_groups();

    public static native long[] tox_group_get_grouplist();

    public static native long tox_group_peer_count(long group_number);

    public static native long[] tox_group_get_peerlist(long group_number);

    public static native long tox_group_by_chat_id(ByteBuffer chat_id_buffer);

    public static native int tox_group_get_privacy_state(long group_number);

    public static native String tox_group_peer_get_public_key(long group_number, long peer_id);

    public static native String tox_group_peer_get_name(long group_number, long peer_id);

    public static native String tox_group_get_name(long group_number);

    public static native String tox_group_get_topic(long group_number);

    public static native int tox_group_peer_get_connection_status(long group_number, long peer_id);

    public static native int tox_group_invite_friend(long group_number, long friend_number);

    public static native int tox_group_is_connected(long group_number);

    public static native int tox_group_reconnect(long group_number);

    /**
     * Send a text chat message to the group.
     * <p>
     * This function creates a group message packet and pushes it into the send
     * queue.
     * <p>
     * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
     * must be split by the client and sent as separate messages. Other clients can
     * then reassemble the fragments. Messages may not be empty.
     *
     * @param group_number       The group number of the group the message is intended for.
     * @param a_TOX_MESSAGE_TYPE Message type (normal, action, ...).
     * @param message            A non-NULL pointer to the first element of a byte array
     *                           containing the message text.
     * @return true on success.
     */
    public static native int tox_group_send_message(long group_number, int a_TOX_MESSAGE_TYPE, String message);

    /**
     * Send a text chat message to the specified peer in the specified group.
     * <p>
     * This function creates a group private message packet and pushes it into the send
     * queue.
     * <p>
     * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
     * must be split by the client and sent as separate messages. Other clients can
     * then reassemble the fragments. Messages may not be empty.
     *
     * @param group_number The group number of the group the message is intended for.
     * @param peer_id      The ID of the peer the message is intended for.
     * @param message      A non-NULL pointer to the first element of a byte array
     *                     containing the message text.
     * @return true on success.
     */
    public static native int tox_group_send_private_message(long group_number, long peer_id, int a_TOX_MESSAGE_TYPE, String message);

    /**
     * Accept an invite to a group chat that the client previously received from a friend. The invite
     * is only valid while the inviter is present in the group.
     *
     * @param invite_data_buffer The invite data received from the `group_invite` event.
     * @param my_peer_name       The name of the peer joining the group.
     * @param password           The password required to join the group. Set to NULL if no password is required.
     * @return the group_number on success, UINT32_MAX on failure.
     */
    public static native long tox_group_invite_accept(long friend_number, ByteBuffer invite_data_buffer, long invite_data_length, String my_peer_name, String password);
    // --------------- new Groups -------------
    // --------------- new Groups -------------
    // --------------- new Groups -------------


    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------
    public static native int toxav_answer(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native long toxav_iteration_interval();

    public static native int toxav_call(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_bit_rate_set(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_call_control(long friendnum, int a_TOXAV_CALL_CONTROL);

    public static native int toxav_video_send_frame_uv_reversed(long friendnum, int frame_width_px, int frame_height_px);

    public static native int toxav_video_send_frame(long friendnum, int frame_width_px, int frame_height_px);

    public static native int toxav_video_send_frame_age(long friendnum, int frame_width_px, int frame_height_px, int age_ms);

    public static native int toxav_video_send_frame_h264(long friendnum, int frame_width_px, int frame_height_px, long data_len);

    public static native int toxav_video_send_frame_h264_age(long friendnum, int frame_width_px, int frame_height_px, long data_len, int age_ms);

    public static native int toxav_option_set(long friendnum, long a_TOXAV_OPTIONS_OPTION, long value);

    public static native void set_av_call_status(int status);

    public static native void set_audio_play_volume_percent(int volume_percent);

    // buffer is for incoming video (call)
    public static native long set_JNI_video_buffer(java.nio.ByteBuffer buffer, int frame_width_px, int frame_height_px);

    // buffer2 is for sending video (call)
    public static native void set_JNI_video_buffer2(java.nio.ByteBuffer buffer2, int frame_width_px, int frame_height_px);

    // audio_buffer is for sending audio (group and call)
    public static native void set_JNI_audio_buffer(java.nio.ByteBuffer audio_buffer);

    // audio_buffer2 is for incoming audio (group and call)
    public static native void set_JNI_audio_buffer2(java.nio.ByteBuffer audio_buffer2);

    /**
     * Send an audio frame to a friend.
     * <p>
     * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
     * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
     * For mono audio, this has no meaning, every sample is subsequent. For stereo,
     * this means the expected format is LRLRLR... with samples for left and right
     * alternating.
     *
     * @param friend_number The friend number of the friend to which to send an
     *                      audio frame.
     * @param sample_count  Number of samples in this frame. Valid numbers here are
     *                      ((sample rate) * (audio length) / 1000), where audio length can be
     *                      2.5, 5, 10, 20, 40 or 60 millseconds.
     * @param channels      Number of audio channels. Supported values are 1 and 2.
     * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
     *                      rates are 8000, 12000, 16000, 24000, or 48000.
     */
    public static native int toxav_audio_send_frame(long friend_number, long sample_count, int channels, long sampling_rate);
    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------

    static void android_toxav_callback_call_cb_method(long friend_number, int audio_enabled, int video_enabled)
    {
        if (Callstate.state != 0)
        {
            // don't accept a new call if we already are in a call
            Log.i(TAG, "android_toxav_callback_call_cb_method:already in a call:fn calling=" + friend_number +
                       " fn in call=" + tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));
            return;
        }

        int res1 = toxav_answer(friend_number, GLOBAL_AUDIO_BITRATE, GLOBAL_VIDEO_BITRATE);
        Callstate.state = 1;
        Callstate.friend_pubkey = tox_friend_get_public_key__wrapper(friend_number);
        Callstate.accepted_call = 1;
        set_av_call_status(Callstate.state);

        on_call_started_actions();

        Log.i(TAG, "android_toxav_callback_call_cb_method:Callstate.state=" + Callstate.state);
    }

    static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride)
    {
        // Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method");
        int y_layer_size = (int) Math.max(frame_width_px, Math.abs(ystride)) * (int) frame_height_px;
        int u_layer_size = (int) Math.max((frame_width_px / 2), Math.abs(ustride)) * ((int) frame_height_px / 2);
        int v_layer_size = (int) Math.max((frame_width_px / 2), Math.abs(vstride)) * ((int) frame_height_px / 2);
        int frame_width_px1 = (int) Math.max(frame_width_px, Math.abs(ystride));
        int frame_height_px1 = (int) frame_height_px;
        buffer_size_in_bytes = y_layer_size + v_layer_size + u_layer_size;
        if (video_buffer_1 == null)
        {
            Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:11:1");
            video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes);
            set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1);
            setup_video_in_resolution(frame_width_px1, frame_height_px1, buffer_size_in_bytes);
            Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:11:2");
        }
        else
        {
            if ((VideoInFrame1.width != frame_width_px1) || (VideoInFrame1.height != frame_height_px1))
            {
                Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:22:1");
                video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes);
                set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1);
                setup_video_in_resolution(frame_width_px1, frame_height_px1, buffer_size_in_bytes);
                Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:22:2");
            }
        }

        new_video_in_frame(video_buffer_1, frame_width_px1, frame_height_px1);
        // Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:099");
    }

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        Log.i(TAG, "toxav_call_state:INCOMING_CALL:from=" + friend_number + " state=" + a_TOXAV_FRIEND_CALL_STATE);
        Log.i(TAG, "Callstate.tox_call_state:INCOMING_CALL=" + a_TOXAV_FRIEND_CALL_STATE + " old=" +
                   Callstate.tox_call_state);

        if (Callstate.state == 1)
        {
            int old_value = Callstate.tox_call_state;
            Callstate.tox_call_state = a_TOXAV_FRIEND_CALL_STATE;

            if ((a_TOXAV_FRIEND_CALL_STATE &
                 (TOXAV_FRIEND_CALL_STATE_SENDING_A.value + TOXAV_FRIEND_CALL_STATE_SENDING_V.value +
                  TOXAV_FRIEND_CALL_STATE_ACCEPTING_A.value + TOXAV_FRIEND_CALL_STATE_ACCEPTING_V.value)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call starting");
                on_call_started_actions();
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (TOXAV_FRIEND_CALL_STATE_FINISHED.value)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(1)");
                on_call_ended_actions();
            }
            else if ((old_value > TOXAV_FRIEND_CALL_STATE_NONE.value) &&
                     (a_TOXAV_FRIEND_CALL_STATE == TOXAV_FRIEND_CALL_STATE_NONE.value))
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(2)");
                on_call_ended_actions();
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (TOXAV_FRIEND_CALL_STATE_ERROR.value)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ERROR(3)");
                on_call_ended_actions();
            }
        }
    }

    static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate)
    {
    }

    static void android_toxav_callback_audio_receive_frame_cb_method(long friend_number, long sample_count, int channels, long sampling_rate)
    {
        // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:001:1");

        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:001a:ret01");
            return;
        }

        if ((sampling_rate != AudioSelectOutBox.SAMPLE_RATE) || (channels != AudioSelectOutBox.CHANNELS) ||
            (_recBuffer == null) || (sample_count == 0))
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:11:1");
            _recBuffer = ByteBuffer.allocateDirect((int) (10000 * 2 * channels));
            set_JNI_audio_buffer2(_recBuffer);
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:11:2");
        }

        if ((sampling_rate != AudioSelectOutBox.SAMPLE_RATE) || (channels != AudioSelectOutBox.CHANNELS))
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:22:1:" + sampling_rate + " " +
                       AudioSelectOutBox.SAMPLE_RATE);
            AudioSelectOutBox.change_audio_format((int) sampling_rate, channels);
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:22:2");
        }

        if (sample_count == 0)
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:77:setup");
            return;
        }

        try
        {
            _recBuffer.rewind();
            final int want_bytes = (int) (sample_count * 2 * channels);
            final byte[] audio_out_byte_buffer = new byte[want_bytes];
            _recBuffer.get(audio_out_byte_buffer, 0, want_bytes);

            if (1 == 1 + 1)
            {
                int can_write_bytes_without_blocking = AudioSelectOutBox.sourceDataLine.available();
                if (can_write_bytes_without_blocking < 2)
                {
                    Log.i(TAG,
                          "android_toxav_callback_audio_receive_frame_cb_method:1:audio play would block:want_bytes=" +
                          want_bytes + " can_write_bytes=" + can_write_bytes_without_blocking + " " + sampling_rate +
                          " " + AudioSelectOutBox.SAMPLE_RATE);

                    can_write_bytes_without_blocking = AudioSelectOutBox.sourceDataLine.available();
                    if (can_write_bytes_without_blocking < 2)
                    {
                        // HINT: for now we just abandon the audio data, since writing here would block all ToxAV
                        Log.i(TAG,
                              "android_toxav_callback_audio_receive_frame_cb_method:2:audio play would block:want_bytes=" +
                              want_bytes + " can_write_bytes=" + can_write_bytes_without_blocking + " " +
                              sampling_rate + " " + AudioSelectOutBox.SAMPLE_RATE);
                    }
                    return;
                }
            }

            try
            {
                semaphore_audio_out_convert.acquire();
                if (semaphore_audio_out_convert_active_threads >= semaphore_audio_out_convert_max_active_threads)
                {
                    Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:too many threads running");
                    semaphore_audio_out_convert.release();
                    return;
                }
                semaphore_audio_out_convert.release();
            }
            catch (Exception e)
            {
            }

            final Thread t_audio_bar_set_play = new Thread(() -> {

                try
                {
                    semaphore_audio_out_convert.acquire();
                    semaphore_audio_out_convert_active_threads++;
                    semaphore_audio_out_convert.release();
                }
                catch (Exception e)
                {
                }

                // HINT: this acutally plays incoming Audio
                // HINT: this may block!!
                try
                {
                    AudioSelectOutBox.sourceDataLine.write(audio_out_byte_buffer, 0, want_bytes);
                    // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:sourceDataLine.write:2:" +
                    //           actual_written_bytes);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:sourceDataLine.write:EE:" +
                               e.getMessage());
                    // e.printStackTrace();
                }

                try
                {
                    semaphore_audio_out_convert.acquire();
                    semaphore_audio_out_convert_active_threads--;
                    semaphore_audio_out_convert.release();
                }
                catch (Exception e)
                {
                }

                float global_audio_out_vu = AUDIO_VU_MIN_VALUE;
                if (sample_count > 0)
                {
                    float vu_value = audio_vu(audio_out_byte_buffer, (int) sample_count);
                    if (vu_value > AUDIO_VU_MIN_VALUE)
                    {
                        global_audio_out_vu = vu_value;
                    }
                    else
                    {
                        global_audio_out_vu = 0;
                    }
                }

                final float global_audio_out_vu_ = global_audio_out_vu;

                // Log.i(TAG, "set_audio_in_bar_level:" + global_audio_out_vu_);
                set_audio_out_bar_level((int) global_audio_out_vu_);

                /*
                if (sample_count > 1)
                {
                    for (int i = 0; i < sample_count; i = i + 2)
                    {
                        short s = (short) ((audio_out_byte_buffer[i] & 0xff) | (audio_out_byte_buffer[i + 1] << 8));
                        pcm_wave_play.add_pcm((int) s);
                    }
                }
                */
            });
            t_audio_bar_set_play.start();
        }
        catch (Exception e)
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:EE01:" + e.getMessage());
        }
    }

    static void android_toxav_callback_audio_receive_frame_pts_cb_method(long friend_number, long sample_count, int channels, long sampling_rate, long pts)
    {
        // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:001:0");
        android_toxav_callback_audio_receive_frame_cb_method(friend_number, sample_count, channels, sampling_rate);
    }

    static void android_toxav_callback_video_receive_frame_pts_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride, long pts)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            return;
        }
        // Log.i(TAG, "android_toxav_callback_video_receive_frame_pts_cb_method");
        android_toxav_callback_video_receive_frame_cb_method(friend_number, frame_width_px, frame_height_px, ystride,
                                                             ustride, vstride);
    }

    static void android_toxav_callback_video_receive_frame_h264_cb_method(long friend_number, long buf_size)
    {
        Log.i(TAG, "android_toxav_callback_video_receive_frame_h264_cb_method");
        // HINT: Disabled. this is now handled by c-toxcore. how nice.
    }

    static void android_toxav_callback_group_audio_receive_frame_cb_method(long conference_number, long peer_number, long sample_count, int channels, long sampling_rate)
    {
    }

    static void android_toxav_callback_call_comm_cb_method(long friend_number, long a_TOXAV_CALL_COMM_INFO, long comm_number)
    {
        // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_IN_USE_VP8.value)
        {
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:3:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
            Callstate.video_in_codec = VIDEO_CODEC_VP8;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_IN_USE_H264.value)
        {
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:4:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
            Callstate.video_in_codec = VIDEO_CODEC_H264;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_IN_USE_VP8.value)
        {
            Callstate.video_out_codec = VIDEO_CODEC_VP8;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:1:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_IN_USE_H264.value)
        {
            Callstate.video_out_codec = VIDEO_CODEC_H264;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:2:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE.value)
        {
            Callstate.video_in_bitrate = comm_number;
            EventQueue.invokeLater(() -> VideoInBitRate_text.setText("" + Callstate.video_bitrate));
            // Log.i(TAG,
            //      "android_toxav_callback_call_comm_cb_method:TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE:" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE.value)
        {
            Callstate.video_bitrate = comm_number;
            EventQueue.invokeLater(() -> VideoOutBitRate_text.setText("" + Callstate.video_bitrate));
            // Log.i(TAG,
            //      "android_toxav_callback_call_comm_cb_method:TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE:" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_PLAY_BUFFER_ENTRIES.value)
        {
            if (comm_number < 0)
            {
                Callstate.play_buffer_entries = 0;
            }
            else if (comm_number > 9900)
            {
                Callstate.play_buffer_entries = 99;
            }
            else
            {
                Callstate.play_buffer_entries = (int) comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:play_buffer_entries=:" + comm_number);
            }
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_NETWORK_ROUND_TRIP_MS.value)
        {
            if (comm_number < 0)
            {
                Callstate.round_trip_time = 0;
            }
            else if (comm_number > 9900)
            {
                Callstate.round_trip_time = 9900;
            }
            else
            {
                Callstate.round_trip_time = comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:round_trip_time=:" + Callstate.round_trip_time);
            }
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_PLAY_DELAY.value)
        {
            if (comm_number < 0)
            {
                Callstate.play_delay = 0;
            }
            else if (comm_number > 9900)
            {
                Callstate.play_delay = 9900;
            }
            else
            {
                Callstate.play_delay = comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:play_delay=:" + Callstate.play_delay);
            }
        }

        try
        {
            HelperGeneric.update_bitrates();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_toxav_callback_call_comm_cb_method:EE:" + e.getMessage());
        }
    }

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------


    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    static void android_tox_callback_self_connection_status_cb_method(int a_TOX_CONNECTION)
    {
        Log.i(TAG, "self_connection_status:status:" + a_TOX_CONNECTION);
        global_self_connection_status = a_TOX_CONNECTION;

        if (bootstrapping)
        {
            Log.i(TAG, "self_connection_status:bootstrapping=true");

            // we just went online
            if (a_TOX_CONNECTION != 0)
            {
                Log.i(TAG, "self_connection_status:bootstrapping set to false");
                bootstrapping = false;
            }
        }

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // ownProfileShort.setEditable(true);
                    if (a_TOX_CONNECTION == 2)
                    {
                        ownProfileShort.setBackground(Color.GREEN);
                    }
                    else if (a_TOX_CONNECTION == 1)
                    {
                        ownProfileShort.setBackground(Color.ORANGE);
                    }
                    else
                    {
                        ownProfileShort.setBackground(Color.LIGHT_GRAY);
                    }
                    // ownProfileShort.setEditable(false);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "android_tox_callback_self_connection_status_cb_method:005:EE:" + e.getMessage());
                }
            }
        };

        long loop = 0;
        while ((ownProfileShort == null) || (!ownProfileShort.isShowing()))
        {
            try
            {
                // Log.i(TAG, "myToxID.setText:sleep");
                Thread.sleep(10);
                loop++;
                if (loop > 20)
                {
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(myRunnable);
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        // Log.i(TAG, "friend_alias_name:friend:" + friend_number + " name:" + friend_alias_name);
        FriendList f = main_get_friend(friend_number);

        // Log.i(TAG, "friend_alias_name:002:" + f);
        if (f != null)
        {
            f.name = friend_name;
            HelperFriend.update_friend_in_db_name(f);
            HelperFriend.update_single_friend_in_friendlist_view(f);

            if (MessagePanel != null)
            {
                if (MessagePanel.friendnum == friend_number)
                {
                    setFriendName();
                }
            }
        }
    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
    }

    static void android_tox_callback_friend_lossless_packet_cb_method(long friend_number, byte[] data, long length)
    {
        // Log.i(TAG, "friend_lossless_packet_cb:fn=" + friend_number + " len=" + length + " data=" + bytes_to_hex(data));

        if (length > 0)
        {
            if (data[0] == (byte) CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND.value)
            {
                if (length == (TOX_PUBLIC_KEY_SIZE + 1))
                {
                    // Log.i(TAG, "friend_lossless_packet_cb:recevied CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND");
                    String relay_pubkey = HelperGeneric.bytes_to_hex(data).substring(2);
                    // Log.i(TAG, "friend_lossless_packet_cb:recevied pubkey:" + relay_pubkey);
                    HelperFriend.add_friend_to_system(relay_pubkey.toUpperCase(), true,
                                                      HelperFriend.tox_friend_get_public_key__wrapper(friend_number));
                }
            }
            else if (data[0] == (byte) CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND.value)
            {
                //Log.i(TAG,
                //      "android_tox_callback_friend_lossless_packet_cb_method:CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND:len=" +
                //      length);
                if (length > ("https://".length() + 1))
                {
                    final String pushurl = new String(Arrays.copyOfRange(data, 1, data.length), StandardCharsets.UTF_8);
                    //Log.i(TAG,
                    //      "android_tox_callback_friend_lossless_packet_cb_method:CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND:pushurl=" +
                    //      pushurl);
                    HelperFriend.add_pushurl_for_friend(pushurl,
                                                        HelperFriend.tox_friend_get_public_key__wrapper(friend_number));
                }
                else
                {
                    if (length == 0)
                    {
                        HelperFriend.remove_pushurl_for_friend(
                                HelperFriend.tox_friend_get_public_key__wrapper(friend_number));
                    }
                }
            }
        }
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        FriendList f = main_get_friend(friend_number);
        //Log.i(TAG,
        //      "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION + " f=" +
        //      f);

        if (f != null)
        {
            if (f.TOX_CONNECTION != a_TOX_CONNECTION)
            {
                if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
                {
                    if (f.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
                    {
                        // ******** friend just came online ********
                        // update and save this friends TOX CAPABILITIES
                        long friend_capabilities = tox_friend_get_capabilities(friend_number);
                        // Log.i(TAG, "" + get_friend_name_from_num(friend_number) + " friend_capabilities: " + friend_capabilities + " decoded:" + TOX_CAPABILITY_DECODE_TO_STRING(TOX_CAPABILITY_DECODE(friend_capabilities)) + " " + (1L << 63L));
                        f.capabilities = friend_capabilities;
                        update_friend_in_db_capabilities(f);
                    }
                }

                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {
                    // ******** friend just came online ********
                    if (HelperRelay.have_own_relay())
                    {
                        if (!HelperRelay.is_any_relay(f.tox_public_key_string))
                        {
                            HelperRelay.send_relay_pubkey_to_friend(HelperRelay.get_own_relay_pubkey(),
                                                                    f.tox_public_key_string);

                            HelperRelay.send_friend_pubkey_to_relay(HelperRelay.get_own_relay_pubkey(),
                                                                    f.tox_public_key_string);
                        }
                        else
                        {
                            if (HelperRelay.is_own_relay(f.tox_public_key_string))
                            {
                                HelperRelay.invite_to_all_conferences_own_relay(HelperRelay.get_own_relay_pubkey());
                            }
                        }
                    }
                }
            }

            if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
            {
                if (f.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
                {
                    // ******** friend just came online ********
                    // resend latest msgV3 message that was not "read"
                    try
                    {
                        if (get_friend_msgv3_capability(friend_number) == 1)
                        {
                            resend_v3_messages(f.tox_public_key_string);
                        }
                        else
                        {
                            // Log.i(TAG, "friend_connection_status:resend_old_messages" +
                            //            get_friend_name_from_pubkey(f.tox_public_key_string));
                            resend_old_messages(f.tox_public_key_string);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }

            if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
            {
                if (a_TOX_CONNECTION == 0)
                {
                    // Log.i(TAG, "friend_connection_status:friend:" + friend_number + ":went offline");
                    // TODO: stop any active calls to/from this friend
                    try
                    {
                        // Log.i(TAG, "friend_connection_status:friend:" + friend_number + ":stop any calls");
                        toxav_call_control(friend_number, ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);

                        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) == friend_number)
                        {
                            on_call_ended_actions();
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }

                f.TOX_CONNECTION_real = a_TOX_CONNECTION;
                f.TOX_CONNECTION_on_off_real = HelperGeneric.get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status_real(f);
            }

            if (HelperRelay.is_any_relay(f.tox_public_key_string))
            {
                if (!HelperRelay.is_own_relay(f.tox_public_key_string))
                {
                    FriendList f_real = HelperRelay.get_friend_for_relay(f.tox_public_key_string);

                    if (f_real != null)
                    {
                        HelperGeneric.update_friend_connection_status_helper(a_TOX_CONNECTION, f_real, true);
                    }
                }
                else // is own relay
                {
                    if (a_TOX_CONNECTION == 2)
                    {
                        draw_main_top_icon(0x04b431, false);
                    }
                    else if (a_TOX_CONNECTION == 1)
                    {
                        draw_main_top_icon(0xffce00, false);
                    }
                    else
                    {
                        draw_main_top_icon(0xff0000, false);
                    }
                }
            }

            HelperGeneric.update_friend_connection_status_helper(a_TOX_CONNECTION, f, false);

            if (friend_number == tox_friend_by_public_key__wrapper(Callstate.friend_pubkey))
            {
                try
                {
                    //**//update_calling_friend_connection_status(a_TOX_CONNECTION);
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, final int typing)
    {
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
        // Log.i(TAG, "friend_read_receipt:friend:" + friend_number + " message_id:" + message_id);
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:004:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

        try
        {
            if (get_friend_msgv3_capability(friend_number) == 1)
            {
                // HINT: friend has msgV3 capability, ignore normal read receipts
                // Log.i(TAG, "friend_read_receipt:msgV3:ignore low level ACK");
                return;
            }

            // there can be older messages with same message_id for this friend! so always take the latest one! -------
            final Message m = orma.selectFromMessage().
                    message_idEq(message_id).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    directionEq(1).
                    orderByIdDesc().
                    toList().get(0);
            // there can be older messages with same message_id for this friend! so always take the latest one! -------

            // Log.i(TAG, "friend_read_receipt:m=" + m);
            // Log.i(TAG, "friend_read_receipt:m:message_id=" + m.message_id + " text=" + m.text + " friendpubkey=" + m.tox_friendpubkey + " read=" + m.read + " direction=" + m.direction);

            try
            {
                m.rcvd_timestamp = System.currentTimeMillis();
                m.read = true;

                HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                // TODO this updates all messages. should be done nicer and faster!
                // update_message_view();
                HelperMessage.update_single_message(m, true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            // Log.i(TAG, "friend_read_receipt:EE:" + e.getMessage());
            // e.printStackTrace();
        }
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        // Log.i(TAG, "friend_request:friend:" + friend_public_key + " friend request message:" + friend_request_message);
        // Log.i(TAG, "friend_request:friend:" + friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2) +
        //            " friend request message:" + friend_request_message);
        String friend_public_key__ = friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2);
        HelperFriend.add_friend_to_system(friend_public_key__.toUpperCase(), false, null);
    }

    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length, byte[] msgV3hash_bin, long message_timestamp)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:007:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        HelperGeneric.receive_incoming_message(0, message_type, friend_number, friend_message, null, 0, null,
                                               msgV3hash_bin, message_timestamp);
    }

    static void android_tox_callback_friend_message_v2_cb_method(long friend_number, String friend_message, long length, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:005:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        HelperGeneric.receive_incoming_message(1, 0, friend_number, friend_message, raw_message, raw_message_length,
                                               null, null, 0);
    }

    static void android_tox_callback_friend_sync_message_v2_cb_method(long friend_number, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length, byte[] raw_data, long raw_data_length)
    {
        if (!HelperRelay.is_own_relay(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)))
        {
            // sync message only accepted from my own relay
            return;
        }

        // Log.i(TAG, "friend_sync_message_v2_cb::IN:fn=" + get_friend_name_from_num(friend_number));

        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:006:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        // Log.i(TAG, "friend_sync_message_v2_cb:fn=" + friend_number + " full rawmsg    =" + bytes_to_hex(raw_message));
        // Log.i(TAG, "friend_sync_message_v2_cb:fn=" + friend_number + " wrapped rawdata=" + bytes_to_hex(raw_data));
        final ByteBuffer raw_message_buf_wrapped = ByteBuffer.allocateDirect((int) raw_data_length);
        raw_message_buf_wrapped.put(raw_data, 0, (int) raw_data_length);
        ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) raw_message_length);
        raw_message_buf.put(raw_message, 0, (int) raw_message_length);
        long msg_sec = tox_messagev2_get_ts_sec(raw_message_buf);
        long msg_ms = tox_messagev2_get_ts_ms(raw_message_buf);
        // Log.i(TAG, "friend_sync_message_v2_cb:sec=" + msg_sec + " ms=" + msg_ms);
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
        ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);
        String msg_id_as_hex_string = HelperGeneric.bytesToHex(msg_id_buffer_compat.array(),
                                                               msg_id_buffer_compat.arrayOffset(),
                                                               msg_id_buffer_compat.limit());
        // Log.i(TAG, "friend_sync_message_v2_cb:MSGv2HASH=" + msg_id_as_hex_string);
        String real_sender_as_hex_string = tox_messagev2_get_sync_message_pubkey(raw_message_buf);
        // Log.i(TAG, "friend_sync_message_v2_cb:real sender pubkey=" + real_sender_as_hex_string);
        long msgv2_type = tox_messagev2_get_sync_message_type(raw_message_buf);
        // Log.i(TAG, "friend_sync_message_v2_cb:msg type=" + ToxVars.TOX_FILE_KIND.value_str((int) msgv2_type));
        ByteBuffer msg_id_buffer_wrapped = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        tox_messagev2_get_message_id(raw_message_buf_wrapped, msg_id_buffer_wrapped);

        ByteBufferCompat msg_id_buffer_wrapped_compat = new ByteBufferCompat(msg_id_buffer_wrapped);
        String msg_id_as_hex_string_wrapped = HelperGeneric.bytesToHex(msg_id_buffer_wrapped_compat.array(),
                                                                       msg_id_buffer_wrapped_compat.arrayOffset(),
                                                                       msg_id_buffer_wrapped_compat.limit());
        // Log.i(TAG, "friend_sync_message_v2_cb:MSGv2HASH=" + msg_id_as_hex_string_wrapped);

        // Log.i(TAG, "friend_sync_message_v2_cb::IN:msgv2_type=" + msgv2_type);

        if (msgv2_type == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_MESSAGEV2_SEND.value)
        {

            // Log.i(TAG, "friend_sync_message_v2_cb::0001");
            long msg_wrapped_sec = tox_messagev2_get_ts_sec(raw_message_buf_wrapped);
            long msg_wrapped_ms = tox_messagev2_get_ts_ms(raw_message_buf_wrapped);
            // Log.i(TAG, "friend_sync_message_v2_cb:sec=" + msg_wrapped_sec + " ms=" + msg_wrapped_ms);
            ByteBuffer msg_text_buffer_wrapped = ByteBuffer.allocateDirect((int) raw_data_length);
            long text_length = tox_messagev2_get_message_text(raw_message_buf_wrapped, raw_data_length, 0, 0,
                                                              msg_text_buffer_wrapped);
            String wrapped_msg_text_as_string = "";

            ByteBufferCompat msg_text_buffer_wrapped_compat = new ByteBufferCompat(msg_text_buffer_wrapped);

            try
            {
                wrapped_msg_text_as_string = new String(msg_text_buffer_wrapped_compat.array(),
                                                        msg_text_buffer_wrapped_compat.arrayOffset(), (int) text_length,
                                                        "UTF-8");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            String msg_text_as_hex_string_wrapped = HelperGeneric.bytesToHex(msg_text_buffer_wrapped_compat.array(),
                                                                             msg_text_buffer_wrapped_compat.arrayOffset(),
                                                                             msg_text_buffer_wrapped_compat.limit());
            // Log.i(TAG, "friend_sync_message_v2_cb:len=" + text_length + " wrapped msg text str=" +
            //            wrapped_msg_text_as_string);
            // Log.i(TAG, "friend_sync_message_v2_cb:wrapped msg text hex=" + msg_text_as_hex_string_wrapped);

            try
            {
                if (tox_friend_by_public_key__wrapper(real_sender_as_hex_string) == -1)
                {
                    // pubkey does NOT belong to a friend. it is probably a conference id
                    // check it here

                    // Log.i(TAG, "friend_sync_message_v2_cb::0002");

                    // Log.i(TAG, "friend_sync_message_v2_cb:LL:" + orma.selectFromConferenceDB().toList());
                    String real_conference_id = real_sender_as_hex_string;

                    long conference_num = HelperConference.tox_conference_by_confid__wrapper(real_conference_id);
                    // Log.i(TAG, "friend_sync_message_v2_cb:conference_num=" + conference_num);
                    if (conference_num > -1)
                    {
                        // Log.i(TAG, "friend_sync_message_v2_cb::0003");

                        String real_sender_peer_pubkey = wrapped_msg_text_as_string.substring(0, 64);
                        long real_text_length = (text_length - 64 - 9);
                        String real_sender_text_ = wrapped_msg_text_as_string.substring(64);

                        String real_sender_text = "";
                        String real_send_message_id = "";

                        // Log.i(TAG,
                        //      "xxxxxxxxxxxxx2:" + real_sender_text_.length() + " " + real_sender_text_.substring(8, 9) +
                        //      " " + real_sender_text_.substring(9) + " " + real_sender_text_.substring(0, 8));

                        if ((real_sender_text_.length() > 8) && (real_sender_text_.startsWith(":", 8)))
                        {
                            real_sender_text = real_sender_text_.substring(9);
                            real_send_message_id = real_sender_text_.substring(0, 8).toLowerCase();
                        }
                        else
                        {
                            real_sender_text = real_sender_text_;
                            real_send_message_id = "";
                        }


                        long sync_msg_received_timestamp = (msg_wrapped_sec * 1000) + msg_wrapped_ms;

                        // add text as conference message
                        long sender_peer_num = HelperConference.get_peernum_from_peer_pubkey(real_conference_id,
                                                                                             real_sender_peer_pubkey);
                        // Log.i(TAG, "friend_sync_message_v2_cb:sender_peer_num=" + sender_peer_num);

                        // now check if this is "potentially" a double message, we can not be sure a 100%
                        // since there is no uniqe key for each message
                        ConferenceMessage cm = get_last_conference_message_in_this_conference_within_n_seconds_from_sender_pubkey(
                                real_conference_id, real_sender_peer_pubkey, sync_msg_received_timestamp,
                                real_send_message_id, MESSAGE_SYNC_DOUBLE_INTERVAL_SECS, false);

                        if (cm != null)
                        {
                            if (cm.text.equals(real_sender_text))
                            {
                                // Log.i(TAG, "friend_sync_message_v2_cb:potentially double message");
                                // ok it's a "potentially" double message
                                // just ignore it, but still send "receipt" to proxy so it won't send this message again
                                send_friend_msg_receipt_v2_wrapper(friend_number, 3, msg_id_buffer,
                                                                   (System.currentTimeMillis() / 1000));
                                return;
                            }
                        }

                        HelperGeneric.conference_message_add_from_sync(
                                HelperConference.tox_conference_by_confid__wrapper(real_conference_id), sender_peer_num,
                                real_sender_peer_pubkey, TRIFA_MSG_TYPE_TEXT.value, real_sender_text, real_text_length,
                                sync_msg_received_timestamp, real_send_message_id);

                        send_friend_msg_receipt_v2_wrapper(friend_number, 3, msg_id_buffer,
                                                           (System.currentTimeMillis() / 1000));
                    }
                    else
                    {
                        // sync message from unkown original sender
                        // still send "receipt" to our relay, or else it will send us this message forever
                        // Log.i(TAG, "friend_sync_message_v2_cb::0004");

                        // Log.i(TAG, "friend_sync_message_v2_cb:send receipt for unknown message");
                        send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer,
                                                           (System.currentTimeMillis() / 1000));

                        return;
                    }
                }
                else
                {
                    // Log.i(TAG, "friend_sync_message_v2_cb::0005");

                    HelperGeneric.receive_incoming_message(2, 0,
                                                           tox_friend_by_public_key__wrapper(real_sender_as_hex_string),
                                                           wrapped_msg_text_as_string, raw_data, raw_data_length,
                                                           real_sender_as_hex_string, null, 0);
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            // Log.i(TAG, "friend_sync_message_v2_cb::0006");

            // send message receipt v2 to own relay
            send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer, (System.currentTimeMillis() / 1000));
        }
        else if (msgv2_type == ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_MESSAGEV2_ANSWER.value)
        {
            // we got an "msg receipt" from the relay
            // Log.i(TAG, "friend_sync_message_v2_cb:TOX_FILE_KIND_MESSAGEV2_ANSWER");
            final String message_id_hash_as_hex_string = msg_id_as_hex_string_wrapped;

            // Log.i(TAG, "friend_sync_message_v2_cb::0007");

            try
            {
                // Log.i(TAG, "friend_sync_message_v2_cb:message_id_hash_as_hex_string=" + message_id_hash_as_hex_string +
                //            " friendpubkey=" + real_sender_as_hex_string);

                final Message m = orma.selectFromMessage().
                        msg_id_hashEq(message_id_hash_as_hex_string).
                        tox_friendpubkeyEq(real_sender_as_hex_string).
                        directionEq(1).
                        readEq(false).
                        toList().get(0);

                // Log.i(TAG, "friend_sync_message_v2_cb::0008");

                if (m != null)
                {
                    try
                    {
                        // Log.i(TAG, "friend_sync_message_v2_cb::0009");

                        long msg_wrapped_sec = tox_messagev2_get_ts_sec(raw_message_buf_wrapped);
                        long msg_wrapped_ms = tox_messagev2_get_ts_ms(raw_message_buf_wrapped);
                        m.raw_msgv2_bytes = "";
                        m.rcvd_timestamp = (msg_wrapped_sec * 1000) + msg_wrapped_ms;
                        m.read = true;
                        HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                        m.resend_count = 2;
                        HelperMessage.update_message_in_db_resend_count(m);
                        HelperMessage.update_single_message(m, true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    // Log.i(TAG, "friend_sync_message_v2_cb::0010");
                    send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer,
                                                       (System.currentTimeMillis() / 1000));
                }
            }
            catch (Exception e)
            {
                // Log.i(TAG, "friend_sync_message_v2_cb::0011");

                // e.printStackTrace();
                send_friend_msg_receipt_v2_wrapper(friend_number, 4, msg_id_buffer,
                                                   (System.currentTimeMillis() / 1000));
            }
        }
        // Log.i(TAG, "friend_sync_message_v2_cb::0999");
    }

    static void android_tox_callback_friend_read_receipt_message_v2_cb_method(final long friend_number, long ts_sec, byte[] msg_id)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:003:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        msg_id_buffer.put(msg_id, 0, (int) TOX_HASH_LENGTH);

        // Log.i(TAG,"receipt_message_v2_cb:MSGv2HASH:2=" + msg_id.length + " fn=" + get_friend_name_from_num(friend_number));

        ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);

        final String message_id_hash_as_hex_string = HelperGeneric.bytesToHex(msg_id_buffer_compat.array(),
                                                                              msg_id_buffer_compat.arrayOffset(),
                                                                              msg_id_buffer_compat.limit());
        // Log.i(TAG, "receipt_message_v2_cb:MSGv2HASH:2=" + message_id_hash_as_hex_string);

        try
        {
            final List<Message> m_try = orma.selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    directionEq(1).
                    readEq(false).
                    toList();

            // Log.i(TAG, "receipt_message_v2_cb:list=" + m_try);

            if ((m_try == null) || (m_try.size() < 1))
            {
                // HINT: it must a an ACK send from a friends toxproxy to singal the receipt of the message on behalf of the friend

                // Log.i(TAG, "receipt_message_v2_cb:0001");

                if (is_any_relay(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)))
                {
                    // Log.i(TAG, "receipt_message_v2_cb:0002");
                    FriendList friend_of_relay = HelperRelay.get_friend_for_relay(
                            HelperFriend.tox_friend_get_public_key__wrapper(friend_number));

                    if (friend_of_relay != null)
                    {

                        // Log.i(TAG, "receipt_message_v2_cb:0003");

                        Message m = orma.selectFromMessage().
                                msg_id_hashEq(message_id_hash_as_hex_string).
                                tox_friendpubkeyEq(friend_of_relay.tox_public_key_string).
                                directionEq(1).
                                readEq(false).
                                toList().get(0);

                        if (m != null)
                        {
                            // Log.i(TAG, "receipt_message_v2_cb:msgid_via_relay found");

                            // Log.i(TAG, "receipt_message_v2_cb:0004");

                            try
                            {
                                // Log.i(TAG, "receipt_message_v2_cb:0005");

                                set_message_msg_at_relay_from_id(m.id, true);
                                m.msg_at_relay = true;
                                HelperMessage.update_single_message(m, true);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Log.i(TAG, "receipt_message_v2_cb:0006");

                return;
            }

            // Log.i(TAG, "receipt_message_v2_cb:0007");

            final Message m = orma.selectFromMessage().
                    msg_id_hashEq(message_id_hash_as_hex_string).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    directionEq(1).
                    readEq(false).
                    toList().get(0);

            if (m != null)
            {
                // Log.i(TAG, "receipt_message_v2_cb:0008");

                // Log.i(TAG, "receipt_message_v2_cb:m id=" + m.id);
                // Log.i(TAG, "receipt_message_v2_cb:msgid found");

                try
                {
                    // Log.i(TAG, "receipt_message_v2_cb:0009");

                    if (!HelperRelay.is_any_relay(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)))
                    {
                        // Log.i(TAG, "receipt_message_v2_cb:0010");

                        // only update if the "read receipt" comes from a friend, but not it's relay!
                        m.raw_msgv2_bytes = "";
                        m.rcvd_timestamp = System.currentTimeMillis();
                        m.read = true;
                        HelperMessage.update_message_in_db_read_rcvd_timestamp_rawmsgbytes(m);
                    }

                    // Log.i(TAG, "receipt_message_v2_cb:0011");

                    m.resend_count = 2;
                    HelperMessage.update_message_in_db_resend_count(m);
                    HelperMessage.update_single_message(m, true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // Log.i(TAG, "receipt_message_v2_cb:msgid *NOT* found");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Log.i(TAG, "receipt_message_v2_cb:0999");
    }

    static void android_tox_callback_file_recv_control_cb_method(long friend_number, long file_number, int a_TOX_FILE_CONTROL)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:008:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        // Log.i(TAG, "file_recv_control:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_CONTROL);

        if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_CANCEL.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_CANCEL");
            HelperFiletransfer.cancel_filetransfer(friend_number, file_number);
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_RESUME.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME");

            try
            {
                long ft_id = HelperFiletransfer.get_filetransfer_id_from_friendnum_and_filenum(friend_number,
                                                                                               file_number);
                Filetransfer ft_check = orma.selectFromFiletransfer().idEq(ft_id).toList().get(0);

                // -------- DEBUG --------
                //                List<Filetransfer> ft_res = orma.selectFromFiletransfer().
                //                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                //                        orderByIdDesc().
                //                        limit(30).toList();
                //                int ii;
                //                Log.i(TAG, "file_recv_control:SQL:===============================================");
                //                for (ii = 0; ii < ft_res.size(); ii++)
                //                {
                //                    Log.i(TAG, "file_recv_control:SQL:" + ft_res.get(ii));
                //                }
                //                Log.i(TAG, "file_recv_control:SQL:===============================================");
                // -------- DEBUG --------

                if (ft_check.kind == TOX_FILE_KIND_AVATAR.value)
                {
                    //Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME::+AVATAR+");
                    //Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:ft_id=" + ft_id);
                    HelperFiletransfer.set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);
                    // if outgoing FT set "ft_accepted" to true
                    HelperFiletransfer.set_filetransfer_accepted_from_id(ft_id);
                }
                else
                {
                    //Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME::*DATA*");
                    //Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:ft_id=" + ft_id);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    //Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:msg_id=" + msg_id);
                    HelperFiletransfer.set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_RESUME.value);
                    // if outgoing FT set "ft_accepted" to true
                    HelperFiletransfer.set_filetransfer_accepted_from_id(ft_id);
                    HelperGeneric.set_message_accepted_from_id(msg_id);

                    // update_all_messages_global(true);
                    try
                    {
                        if (ft_id != -1)
                        {
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_PAUSE.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_PAUSE");

            try
            {
                long ft_id = HelperFiletransfer.get_filetransfer_id_from_friendnum_and_filenum(friend_number,
                                                                                               file_number);
                long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                HelperFiletransfer.set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_PAUSE.value);
                HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_PAUSE.value);

                // update_all_messages_global(true);
                try
                {
                    if (ft_id != -1)
                    {
                        HelperMessage.update_single_message_from_messge_id(msg_id, true);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
    }

    static void android_tox_callback_file_chunk_request_cb_method(long friend_number, long file_number, long position, long length)
    {
        // final long ts01 = System.currentTimeMillis();

        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:009:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        global_last_activity_outgoung_ft_ts = System.currentTimeMillis();

        // Log.i(TAG, "file_chunk_request:" + friend_number + ":" + file_number + ":" + position + ":" + length);

        // @formatter:off
        Log.D(TAG,
              "DEBUG_FT:OUT:file_chunk_request:file_number=" +
              file_number +
              " fn=" + friend_number +
              " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
              " position="+position+
              " length=" + length
        );
        // @formatter:on

        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().
                    directionEq(TRIFA_FT_DIRECTION_OUTGOING.value).
                    stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0);

            if (ft == null)
            {
                Log.i(TAG, "file_chunk_request:ft=NULL");
                return;
            }

            // Log.i(TAG, "file_chunk_request:ft=" + ft.kind + ":" + ft);

            if (ft.kind == TOX_FILE_KIND_AVATAR.value)
            {
                // TODO: write me!!
            }
            else // TOX_FILE_KIND_DATA.value
            {
                if (length == 0)
                {
                    Log.i(TAG, "file_chunk_request:file fully sent");
                    // transfer finished -----------
                    long filedb_id = -1;

                    if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                    {
                        // put into "FileDB" table
                        FileDB file_ = new FileDB();
                        file_.kind = ft.kind;
                        file_.direction = ft.direction;
                        file_.tox_public_key_string = ft.tox_public_key_string;
                        file_.path_name = ft.path_name;
                        file_.file_name = ft.file_name;
                        file_.is_in_VFS = false;
                        file_.filesize = ft.filesize;
                        long row_id = orma.insertIntoFileDB(file_);
                        // Log.i(TAG, "file_chunk_request:FileDB:row_id=" + row_id);
                        filedb_id = orma.selectFromFileDB().
                                tox_public_key_stringEq(ft.tox_public_key_string).
                                file_nameEq(ft.file_name).
                                path_nameEq(ft.path_name).
                                directionEq(ft.direction).
                                filesizeEq(ft.filesize).
                                orderByIdDesc().toList().get(0).id;
                        // Log.i(TAG, "file_chunk_request:FileDB:filedb_id=" + filedb_id);
                    }

                    // Log.i(TAG, "file_chunk_request:file_READY:001:f.id=" + ft.id);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft.id, friend_number);
                    // Log.i(TAG, "file_chunk_request:file_READY:001a:msg_id=" + msg_id);
                    HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number,
                                                                                               file_number,
                                                                                               ft.path_name + "/" +
                                                                                               ft.file_name);
                    HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                                                                               TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number,
                                                                                               file_number, -1);

                    try
                    {
                        // Log.i(TAG, "file_chunk_request:file_READY:002");

                        if (ft.id != -1)
                        {
                            // Log.i(TAG, "file_chunk_request:file_READY:003:f.id=" + ft.id + " msg_id=" + msg_id);
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "file_chunk_request:file_READY:EE:" + e.getMessage());
                    }

                    // transfer finished -----------
                    ByteBuffer avatar_chunk = ByteBuffer.allocateDirect(1);
                    int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, 0);
                    // Log.i(TAG, "file_chunk_request:res(2)=" + res);
                    // remove FT from DB
                    HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
                else
                {
                    final String fname = new File(ft.path_name + "/" + ft.file_name).getAbsolutePath();
                    // Log.i(TAG, "file_chunk_request:fname=" + fname);

                    // @formatter:off
                    Log.D(TAG,
                          "DEBUG_FT:OUT:file_chunk_request:file_number=" +
                          file_number +
                          " fn=" + friend_number +
                          " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
                          " path_name="+ft.path_name+
                          " file_name=" + ft.file_name
                    );
                    // @formatter:on


                    long file_chunk_length = length;
                    final byte[] bytes_chunck = HelperGeneric.read_chunk_from_SD_file(fname, position,
                                                                                      file_chunk_length);
                    // byte[] bytes_chunck = new byte[(int) file_chunk_length];
                    // avatar_bytes.position((int) position);
                    // avatar_bytes.get(bytes_chunck, 0, (int) file_chunk_length);
                    final ByteBuffer file_chunk = ByteBuffer.allocateDirect((int) file_chunk_length);
                    file_chunk.put(bytes_chunck);
                    int res = tox_file_send_chunk(friend_number, file_number, position, file_chunk, file_chunk_length);
                    // Log.i(TAG, "file_chunk_request:res(1)=" + res);
                    // TODO: handle error codes from tox_file_send_chunk() here ----

                    // @formatter:off
                    Log.D(TAG,
                          "DEBUG_FT:OUT:file_chunk_request:tox_file_send_chunk:file_number=" +
                          file_number +
                          " fn=" + friend_number +
                          " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
                          " position="+position+
                          " file_chunk_length=" + file_chunk_length
                    );
                    // @formatter:on


                    if (ft.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                    {
                        if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                        {
                            ft.current_position = position;
                            HelperFiletransfer.update_filetransfer_db_current_position(ft);

                            if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                            {
                                // update_all_messages_global(false);
                                try
                                {
                                    if (ft.id != -1)
                                    {
                                        HelperMessage.update_single_message_from_ftid(ft.id, false);
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                    else
                    {
                        if ((ft.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES) < position)
                        {
                            ft.current_position = position;
                            HelperFiletransfer.update_filetransfer_db_current_position(ft);

                            if (ft.kind != TOX_FILE_KIND_AVATAR.value)
                            {
                                // update_all_messages_global(false);
                                try
                                {
                                    if (ft.id != -1)
                                    {
                                        HelperMessage.update_single_message_from_ftid(ft.id, false);
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                    // Log.i(TAG, "file_chunk_request:ft:099:" + (System.currentTimeMillis() - ts01));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "file_chunk_request:EE1:" + e.getMessage());
        }
    }

    static void android_tox_callback_file_recv_cb_method(long friend_number, long file_number, int a_TOX_FILE_KIND, long file_size, String filename, long filename_length)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:010:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        // Log.i(TAG,
        //       "file_recv:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_KIND + ":" + file_size + ":" +
        //       filename + ":" + filename_length);

        if (a_TOX_FILE_KIND == TOX_FILE_KIND_AVATAR.value)
        {
            // Log.i(TAG, "file_recv:TOX_FILE_KIND_AVATAR");

            if (file_size > AVATAR_INCOMING_MAX_BYTE_SIZE)
            {
                Log.i(TAG, "file_recv:avatar_too_large");
                try
                {
                    tox_file_control(friend_number, file_number, TOX_FILE_CONTROL_CANCEL.value);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }
            else if (file_size == 0)
            {
                Log.i(TAG, "file_recv:avatar_size_zero");

                // friend wants to unset avatar
                HelperFriend.del_friend_avatar(HelperFriend.tox_friend_get_public_key__wrapper(friend_number),
                                               VFS_PREFIX + VFS_FILE_DIR + "/" +
                                               HelperFriend.tox_friend_get_public_key__wrapper(friend_number) + "/",
                                               FRIEND_AVATAR_FILENAME);

                try
                {
                    tox_file_control(friend_number, file_number, TOX_FILE_CONTROL_CANCEL.value);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }

            String file_name_avatar = FRIEND_AVATAR_FILENAME;
            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.message_id = -1;
            f.state = TOX_FILE_CONTROL_RESUME.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = file_name_avatar;
            f.filesize = file_size;
            f.current_position = 0;
            long row_id = HelperFiletransfer.insert_into_filetransfer_db(f);
            f.id = row_id;
            // TODO: we just accept incoming avatar, maybe make some checks first?
            tox_file_control(friend_number, file_number, TOX_FILE_CONTROL_RESUME.value);
        }
        else // DATA file ft
        {
            String filename_corrected = get_incoming_filetransfer_local_filename(filename,
                                                                                 HelperFriend.tox_friend_get_public_key__wrapper(
                                                                                         friend_number));
            // @formatter:off
            Log.D(TAG,
                  "DEBUG_FT:IN:file_recv:file_number=" +
                  file_number +
                  " fn=" + friend_number +
                  " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
                  " fname=" + filename +
                  " fname2=" + filename_corrected);
            // @formatter:on

            Log.i(TAG, "file_recv:incoming regular file:file_number=" + file_number);
            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.state = TOX_FILE_CONTROL_PAUSE.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = filename_corrected;
            f.filesize = file_size;
            f.ft_accepted = false;
            f.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            f.current_position = 0;
            f.message_id = -1;
            long ft_id = HelperFiletransfer.insert_into_filetransfer_db(f);
            Log.D(TAG, "file_recv:ft_id=" + ft_id);
            // @formatter:off
            Log.D(TAG,
                  "DEBUG_FT:IN:file_recv:file_number=" +
                  file_number +
                  " fn=" + friend_number +
                  " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
                  " fname=" + filename +
                  " fname2=" + filename_corrected+
                  " ft_id=" + ft_id);
            // @formatter:on

            f.id = ft_id;
            // add FT message to UI
            Message m = new Message();
            m.tox_friendpubkey = HelperFriend.tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.filetransfer_id = ft_id;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_PAUSE.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            m.ft_outgoing_queued = false;
            m.rcvd_timestamp = System.currentTimeMillis();
            m.sent_timestamp = m.rcvd_timestamp;
            m.text = filename_corrected + "\n" + file_size + " bytes";
            long new_msg_id = -1;


            if (get_current_friendnum() == friend_number)
            {
                new_msg_id = HelperMessage.insert_into_message_db(m, true);
                m.id = new_msg_id;
            }
            else
            {
                new_msg_id = HelperMessage.insert_into_message_db(m, false);
                m.id = new_msg_id;
            }

            Log.D(TAG, "new_msg_id=" + new_msg_id);
            // @formatter:off
            Log.D(TAG,
                  "DEBUG_FT:IN:file_recv:file_number=" +
                  file_number +
                  " fn=" + friend_number +
                  " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
                  " fname=" + filename +
                  " fname2=" + filename_corrected+
                  " new_msg_id=" + new_msg_id);
            // @formatter:on

            f.message_id = new_msg_id;
            HelperFiletransfer.update_filetransfer_db_full(f);

            try
            {
                // update "new" status on friendlist fragment
                FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f2);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            final Message m2 = m;

            try
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            sleep(1 * 50);
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                        check_auto_accept_incoming_filetransfer(m2);
                    }
                };
                t.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static void android_tox_callback_file_recv_chunk_cb_method(long friend_number, long file_number, long position, byte[] data, long length)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:011:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        // Log.i(TAG, "file_recv_chunk:" + friend_number + ":fn==" + file_number + ":position=" + position + ":length=" + length + ":data len=" + data.length + ":data=" + data);
        // Log.i(TAG, "file_recv_chunk:--START--");
        // Log.i(TAG, "file_recv_chunk:" + friend_number + ":" + file_number + ":" + position + ":" + length);
        Filetransfer f = null;

        // @formatter:off
        //Log.D(TAG,
        //      "DEBUG_FT:IN:file_recv_chunk:file_number=" +
        //      file_number +
        //      " fn=" + friend_number +
        //      " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)
        //      );
        // @formatter:on

        try
        {
            f = orma.selectFromFiletransfer().
                    directionEq(TRIFA_FT_DIRECTION_INCOMING.value).
                    file_numberEq(file_number).
                    stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().toList().
                    get(0);

            // Log.i(TAG, "file_recv_chunk:filesize==" + f.filesize);

            if (f == null)
            {
                return;
            }

            // @formatter:off
            //Log.D(TAG,
            //      "DEBUG_FT:IN:file_recv_chunk:file_number=" +
            //      file_number +
            //      " fn=" + friend_number +
            //      " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
            //      " path_name="+f.path_name+
            //      " file_name=" + f.file_name
            //);
            // @formatter:on

            if (position == 0)
            {
                // Log.i(TAG, "file_recv_chunk:START-O-F:filesize==" + f.filesize);

                // file start. just to be sure, make directories
                File f1 = new File(f.path_name + "/" + f.file_name);
                File f2 = new File(f1.getParent());
                // Log.i(TAG, "file_recv_chunk:f1=" + f1.getAbsolutePath());
                // Log.i(TAG, "file_recv_chunk:f2=" + f2.getAbsolutePath());
                f2.mkdirs();
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            return;
        }

        if (length == 0)
        {
            // Log.i(TAG, "file_recv_chunk:END-O-F:filesize==" + f.filesize);

            try
            {
                // Log.i(TAG, "file_recv_chunk:file fully received");
                HelperGeneric.move_tmp_file_to_real_file(f.path_name, f.file_name,
                                                         VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string +
                                                         "/", f.file_name);
                long filedb_id = -1;

                if (f.kind != TOX_FILE_KIND_AVATAR.value)
                {
                    // put into "FileDB" table
                    FileDB file_ = new FileDB();
                    file_.kind = f.kind;
                    file_.direction = f.direction;
                    file_.tox_public_key_string = f.tox_public_key_string;
                    file_.path_name = VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/";
                    file_.file_name = f.file_name;
                    file_.filesize = f.filesize;
                    long row_id = orma.insertIntoFileDB(file_);
                    // Log.i(TAG, "file_recv_chunk:FileDB:row_id=" + row_id);
                    filedb_id = orma.selectFromFileDB().tox_public_key_stringEq(f.tox_public_key_string).file_nameEq(
                            f.file_name).orderByIdDesc().toList().get(0).id;
                    // Log.i(TAG, "file_recv_chunk:FileDB:filedb_id=" + filedb_id);
                }

                // Log.i(TAG, "file_recv_chunk:kind=" + f.kind);

                if (f.kind == TOX_FILE_KIND_AVATAR.value)
                {
                    // we have received an avatar image for a friend. and the filetransfer is complete here
                    HelperFriend.set_friend_avatar(HelperFriend.tox_friend_get_public_key__wrapper(friend_number),
                                                   VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/",
                                                   f.file_name);
                    // Log.i(TAG, "file_recv_chunk:kind=avatar:set_friend_avatar:" + VFS_PREFIX + VFS_FILE_DIR + "/" +
                    //           f.tox_public_key_string + "/" + f.file_name);
                }
                else
                {
                    // Log.i(TAG, "file_recv_chunk:file_READY:001:f.id=" + f.id);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(f.id, friend_number);
                    // Log.i(TAG, "file_recv_chunk:file_READY:001a:msg_id=" + msg_id);
                    HelperMessage.update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number,
                                                                                               file_number, VFS_PREFIX +
                                                                                                            VFS_FILE_DIR +
                                                                                                            "/" +
                                                                                                            f.tox_public_key_string +
                                                                                                            "/" +
                                                                                                            f.file_name);
                    HelperMessage.set_message_state_from_friendnum_and_filenum(friend_number, file_number,
                                                                               TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    HelperFiletransfer.set_filetransfer_for_message_from_friendnum_and_filenum(friend_number,
                                                                                               file_number, -1);

                    try
                    {
                        // Log.i(TAG, "file_recv_chunk:file_READY:002");

                        if (f.id != -1)
                        {
                            // Log.i(TAG, "file_recv_chunk:file_READY:003:f.id=" + f.id + " msg_id=" + msg_id);
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        // Log.i(TAG, "file_recv_chunk:file_READY:EE:" + e.getMessage());
                    }
                }

                // remove FT from DB
                HelperFiletransfer.delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
                // Log.i(TAG, "file_recv_chunk:EE2:" + e2.getMessage());
            }
        }
        else // normal chunck recevied ---------- (NOT start, and NOT end)
        {
            try
            {
                try
                {
                    RandomAccessFile fos = new RandomAccessFile(f.path_name + "/" + f.file_name, "rw");

                    // @formatter:off
                    // Log.D(TAG,
                    //       "DEBUG_FT:IN:file_recv_chunk:file_number=" +
                    //       file_number +
                    //       " fn=" + friend_number +
                    //       " pk="+HelperFriend.tox_friend_get_public_key__wrapper(friend_number)+
                    //       " path_name="+f.path_name+
                    //       " file_name=" + f.file_name+
                    //       " fos="+fos
                    // );
                    // @formatter:on

                    fos.seek(position);
                    fos.write(data);
                    fos.close();
                }
                catch (Exception ex)
                {
                    // ex.printStackTrace();
                }

                if (f.filesize < UPDATE_MESSAGE_PROGRESS_SMALL_FILE_IS_LESS_THAN_BYTES)
                {
                    if ((f.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES_SMALL_FILES) < position)
                    {
                        f.current_position = position;
                        // Log.i(TAG, "file_recv_chunk:filesize==:2:" + f.filesize);
                        HelperFiletransfer.update_filetransfer_db_current_position(f);

                        if (f.kind != TOX_FILE_KIND_AVATAR.value)
                        {
                            // update_all_messages_global(false);
                            try
                            {
                                if (f.id != -1)
                                {
                                    HelperMessage.update_single_message_from_ftid(f.id, false);
                                }
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                }
                else
                {
                    if ((f.current_position + UPDATE_MESSAGE_PROGRESS_AFTER_BYTES) < position)
                    {
                        f.current_position = position;
                        // Log.i(TAG, "file_recv_chunk:filesize==:2:" + f.filesize);
                        HelperFiletransfer.update_filetransfer_db_current_position(f);

                        if (f.kind != TOX_FILE_KIND_AVATAR.value)
                        {
                            // update_all_messages_global(false);
                            try
                            {
                                if (f.id != -1)
                                {
                                    HelperMessage.update_single_message_from_ftid(f.id, false);
                                }
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                // Log.i(TAG, "file_recv_chunk:EE1:" + e.getMessage());
            }
        }

        // Log.i(TAG, "file_recv_chunk:--END--");
    }

    static void android_tox_log_cb_method(int a_TOX_LOG_LEVEL, String file, long line, String function, String message)
    {
        if (CTOXCORE_NATIVE_LOGGING)
        {
            Log.i(TAG, "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" +
                       line + ":func=" + function + ":msg=" + message);
        }
    }

    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------

    static void android_tox_callback_conference_invite_cb_method(long friend_number, int a_TOX_CONFERENCE_TYPE, byte[] cookie_buffer, long cookie_length)
    {
        Log.i(TAG, "conference_invite_cb:fn=" + friend_number + " type=" + a_TOX_CONFERENCE_TYPE + " cookie_length=" +
                   cookie_length + " cookie=" + HelperGeneric.bytes_to_hex(cookie_buffer));
        //try
        //{
        //Thread t = new Thread()
        //{
        // @Override
        //public void run()
        //{
        ByteBuffer cookie_buf2 = ByteBuffer.allocateDirect((int) cookie_length);
        cookie_buf2.put(cookie_buffer);
        Log.i(TAG, "conference_invite_cb:bytebuffer offset=" + 0);

        long conference_num = -1;
        if (a_TOX_CONFERENCE_TYPE != TOX_CONFERENCE_TYPE_AV.value)
        {
            conference_num = tox_conference_join(friend_number, cookie_buf2, cookie_length);
        }
        else
        {
            /*
            conference_num = toxav_join_av_groupchat(friend_number, cookie_buf2, cookie_length);
            HelperGeneric.update_savedata_file_wrapper();
            long result = toxav_groupchat_disable_av(conference_num);
            Log.i(TAG, "conference_invite_cb:toxav_groupchat_disable_av result=" + result);
            */
        }

        cache_confid_confnum.clear();

        Log.i(TAG, "conference_invite_cb:tox_conference_join res=" + conference_num);
        // strip first 3 bytes of cookie to get the conference_id.
        // this is aweful and hardcoded
        String conference_identifier = HelperGeneric.bytes_to_hex(
                Arrays.copyOfRange(cookie_buffer, 3, (int) (3 + CONFERENCE_ID_LENGTH)));
        Log.i(TAG, "conference_invite_cb:conferenc ID=" + conference_identifier);

        // invite also my ToxProxy -------------
        if (a_TOX_CONFERENCE_TYPE == TOX_CONFERENCE_TYPE_TEXT.value)
        {
        }
        // invite also my ToxProxy -------------


        HelperConference.add_conference_wrapper(friend_number, conference_num, conference_identifier,
                                                a_TOX_CONFERENCE_TYPE, true);
        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
    }

    static void android_tox_callback_conference_connected_cb_method(long conference_number)
    {
        // invite also my ToxProxy -------------
        Log.i(TAG, "conference_connected_cb:cf_num=" + conference_number);
        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);

        // TODO: bad
        // MainActivity.cache_peername_pubkey2.clear();

        cache_confid_confnum.clear();
    }

    static void android_tox_callback_conference_message_cb_method(long conference_number, long peer_number, int a_TOX_MESSAGE_TYPE, String message_orig, long length)
    {
        if (tox_conference_get_type(conference_number) == TOX_CONFERENCE_TYPE_AV.value)
        {
            // we do not yet process messages from AV groups
            return;
        }

        // Log.i(TAG, "conference_message_cb:cf_num=" + conference_number + " pnum=" + peer_number + " msg=" + message);
        int res = tox_conference_peer_number_is_ours(conference_number, peer_number);

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
            return;
        }

        String message_ = "";
        String message_id_ = "";

        // Log.i(TAG, "xxxxxxxxxxxxx1:" + message_orig.length() + " " + message_orig.substring(8, 9) + " " +
        //           message_orig.substring(9) + " " + message_orig.substring(0, 8));

        if ((message_orig.length() > 8) && (message_orig.startsWith(":", 8)))
        {
            message_ = message_orig.substring(9);
            message_id_ = message_orig.substring(0, 8).toLowerCase();
        }
        else
        {
            message_ = message_orig;
            message_id_ = "";
        }

        boolean do_notification = true;
        boolean do_badge_update = true;
        String conf_id = "-1";
        ConferenceDB conf_temp = null;

        try
        {
            // TODO: cache me!!
            //conf_temp = orma.selectFromConferenceDB().
            //        tox_conference_numberEq(conference_number).
            //        conference_activeEq(true).toList().get(0);

            conf_temp = orma.selectFromConferenceDB().
                    tox_conference_numberEq(conference_number).
                    toList().get(0);

            conf_id = conf_temp.conference_identifier;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (conf_temp.notification_silent)
            {
                do_notification = false;
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            do_notification = false;
        }

        Log.i(TAG, "noti_and_badge:002conf:" + MessagePanelConferences.get_current_conf_id() + ":" + conf_id);

        if ((MessagePanelConferences.get_current_conf_id().equals(conf_id)) && (mainwindow_has_focus))
        {
            // Log.i(TAG, "noti_and_badge:003:");
            // no notifcation and no badge update
            do_notification = false;
            do_badge_update = false;
        }

        ConferenceMessage m = new ConferenceMessage();
        m.is_new = do_badge_update;
        // m.tox_friendnum = friend_number;
        m.tox_peerpubkey = HelperConference.tox_conference_peer_get_public_key__wrapper(conference_number, peer_number);
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_peername = null;
        m.conference_identifier = conf_id;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.sent_timestamp = System.currentTimeMillis();
        m.text = message_;
        m.message_id_tox = message_id_;
        m.was_synced = false;

        // now check if this is "potentially" a double message, we can not be sure a 100% since there is no uniqe key for each message
        ConferenceMessage cm = get_last_conference_message_in_this_conference_within_n_seconds_from_sender_pubkey(
                conf_id, m.tox_peerpubkey, m.sent_timestamp, m.message_id_tox, MESSAGE_SYNC_DOUBLE_INTERVAL_SECS, true);
        if (cm != null)
        {
            if (cm.text.equals(message_))
            {
                Log.i(TAG, "conference_message_cb:potentially double message");
                // ok it's a "potentially" double message
                return;
            }
        }

        try
        {
            m.tox_peername = HelperConference.tox_conference_peer_get_name__wrapper(m.conference_identifier,
                                                                                    m.tox_peerpubkey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (MessagePanelConferences.get_current_conf_id().equals(conf_id))
        {
            HelperConference.insert_into_conference_message_db(m, true);
        }
        else
        {
            HelperConference.insert_into_conference_message_db(m, false);
        }

        if (do_notification)
        {
            // change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.conference_identifier);
            displayMessage("new Group Message from: " + conf_temp.name);
        }
    }

    static void android_tox_callback_conference_title_cb_method(long conference_number, long peer_number, String title, long title_length)
    {
        // Log.i(TAG, "conference_title_cb:" + "confnum=" + conference_number + " peernum=" + peer_number + " new_title=" +
        //           title + " title_length=" + title_length);

        try
        {
            ConferenceDB conf_temp2 = null;

            try
            {
                try
                {
                    // TODO: cache me!!
                    conf_temp2 = orma.selectFromConferenceDB().tox_conference_numberEq(conference_number).
                            conference_activeEq(true).
                            get(0);

                    if (conf_temp2 != null)
                    {
                        // update it in the Database
                        orma.updateConferenceDB().
                                conference_identifierEq(conf_temp2.conference_identifier).
                                name(title).execute();
                    }
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "get_conference_title_from_confid:EE:3:" + e2.getMessage());
            }

            HelperConference.update_single_conference_in_friendlist_view(conf_temp2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_tox_callback_conference_title_cb_method:EE1:" + e.getMessage());
        }

        try
        {
        }
        catch (Exception e)
        {
        }
    }

    static void android_tox_callback_conference_peer_name_cb_method(long conference_number, long peer_number, String name, long name_length)
    {
        // TODO: bad
        // MainActivity.cache_peername_pubkey2.clear();
        ConferenceMessageListFragmentJ.update_group_all_users();
    }

    static void android_tox_callback_conference_peer_list_changed_cb_method(long conference_number)
    {
        // TODO: bad
        // MainActivity.cache_peername_pubkey2.clear();
        ConferenceMessageListFragmentJ.update_group_all_users();
    }

    static void android_tox_callback_conference_namelist_change_cb_method(long conference_number, long peer_number, int a_TOX_CONFERENCE_STATE_CHANGE)
    {
        // TODO: bad
        // MainActivity.cache_peername_pubkey2.clear();
        ConferenceMessageListFragmentJ.update_group_all_users();
    }

    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------

    // -------- called by native new Group methods --------
    // -------- called by native new Group methods --------
    // -------- called by native new Group methods --------

    static void android_tox_callback_group_message_cb_method(long group_number, long peer_id, int a_TOX_MESSAGE_TYPE, String message_orig, long length)
    {
        Log.i(TAG, "group_message_cb:gn=" + group_number + " peerid=" + peer_id + " message=" + message_orig);
    }

    static void android_tox_callback_group_private_message_cb_method(long group_number, long peer_id, int a_TOX_MESSAGE_TYPE, String message_orig, long length)
    {
        Log.i(TAG, "group_private_message_cb:gn=" + group_number + " peerid=" + peer_id + " message=" + message_orig);
    }

    static void android_tox_callback_group_invite_cb_method(long friend_number, final byte[] invite_data, final long invite_data_length, String group_name)
    {
        Log.i(TAG,
              "group_invite_cb:fn=" + friend_number + " invite_data_length=" + invite_data_length + " invite_data=" +
              bytes_to_hex(invite_data) + " groupname=" + group_name);
    }

    static void android_tox_callback_group_peer_join_cb_method(long group_number, long peer_id)
    {
        Log.i(TAG, "group_peer_join_cb:group_number=" + group_number + " peer_id=" + peer_id);
    }

    static void android_tox_callback_group_peer_exit_cb_method(long group_number, long peer_id, int a_Tox_Group_Exit_Type)
    {
        Log.i(TAG, "group_peer_exit_cb:group_number=" + group_number + " peer_id=" + peer_id + " exit_type=" +
                   a_Tox_Group_Exit_Type);
    }

    static void android_tox_callback_group_join_fail_cb_method(long group_number, int a_Tox_Group_Join_Fail)
    {
        Log.i(TAG, "group_join_fail_cb:group_number=" + group_number + " fail=" + a_Tox_Group_Join_Fail);
    }

    static void android_tox_callback_group_self_join_cb_method(long group_number)
    {
        Log.i(TAG, "group_self_join_cb:group_number=" + group_number);
    }

    static void android_tox_callback_group_topic_cb_method(long group_number, long peer_id, String topic, long topic_length)
    {
        Log.i(TAG, "group_topic_cb: groupnum=" + group_number + " peer=" + peer_id + " topic=" + topic);
    }

    // -------- called by native new Group methods --------
    // -------- called by native new Group methods --------
    // -------- called by native new Group methods --------

    static int add_tcp_relay_single_wrapper(String ip, long port, String key_hex)
    {
        return add_tcp_relay_single(ip, key_hex, port);
    }

    static int bootstrap_single_wrapper(String ip, long port, String key_hex)
    {
        return bootstrap_single(ip, key_hex, port);
    }

    static void switch_ui_look_and_feel(LookAndFeel lnfName, Component c)
    {
        try
        {
            UIManager.setLookAndFeel(lnfName);
            SwingUtilities.updateComponentTreeUI(c);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void want_exit()
    {
        int selected_answer = JOptionPane.showConfirmDialog(null, lo.getString("exit_app_msg"),
                                                            lo.getString("exit_app_title"), YES_NO_OPTION);
        if (selected_answer == YES_OPTION)
        {
            try
            {
                HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
            }
            catch (Exception e3)
            {
                e3.printStackTrace();
            }

            tox_service_fg.stop_me = true;

            try
            {
                Thread.sleep(700);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            try
            {
                sqldb.close();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            System.exit(0);
        }
    }

    public static void take_screen_shot_with_selection()
    {
        try
        {
            Log.i(TAG, "CaptureOccured...SelectionRectangle start");
            new SelectionRectangle();


            final Thread t = new Thread(() -> {
                try
                {
                    while (SelectionRectangle.showing)
                    {
                        Thread.sleep(20);
                    }

                    Thread.sleep(200);
                    Log.i(TAG, "CaptureOccured...SelectionRectangle done");

                    try
                    {
                        if (!SelectionRectangle.cancel)
                        {
                            Log.i(TAG, "CaptureOccured...Screenshot capture");
                            BufferedImage img = (BufferedImage) Screenshot.capture(SelectionRectangle.capture_x,
                                                                                   SelectionRectangle.capture_y,
                                                                                   SelectionRectangle.capture_width,
                                                                                   SelectionRectangle.capture_height).getImage();

                            Log.i(TAG, "CaptureOccured...Screenshot capture DONE");

                            if (img != null)
                            {
                                Log.i(TAG, "CaptureOccured...Image");
                                try
                                {
                                    if (message_panel_displayed == 1)
                                    {
                                        Log.i(TAG, "CaptureOccured...Image:002");
                                        if (get_current_friendnum() != -1)
                                        {
                                            Log.i(TAG, "CaptureOccured...Image:003:" + get_current_friendnum());

                                            final String friend_pubkey_str = HelperFriend.tox_friend_get_public_key__wrapper(
                                                    get_current_friendnum());

                                            String wanted_full_filename_path =
                                                    VFS_PREFIX + VFS_FILE_DIR + "/" + friend_pubkey_str;
                                            new File(wanted_full_filename_path).mkdirs();

                                            String filename_local_corrected = get_incoming_filetransfer_local_filename(
                                                    "clip.png", friend_pubkey_str);

                                            filename_local_corrected =
                                                    wanted_full_filename_path + "/" + filename_local_corrected;

                                            Log.i(TAG, "CaptureOccured...Image:004:" + filename_local_corrected);
                                            final File f_send = new File(filename_local_corrected);
                                            boolean res = ImageIO.write(img, "png", f_send);
                                            Log.i(TAG,
                                                  "CaptureOccured...Image:004:" + filename_local_corrected + " res=" +
                                                  res);

                                            // send file
                                            add_outgoing_file(f_send.getAbsoluteFile().getParent(),
                                                              f_send.getAbsoluteFile().getName());
                                        }
                                    }
                                }
                                catch (Exception e2)
                                {
                                    e2.printStackTrace();
                                    Log.i(TAG, "CaptureOccured...EE2:" + e2.getMessage());
                                }
                            }
                        }
                        else
                        {
                            Log.i(TAG, "CaptureOccured...SelectionRectangle CANCEL");
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                catch (Exception e2)
                {
                }
            });
            t.start();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.i(TAG, "CaptureOccured...EE1:" + e.getMessage());
        }
    }

    public static void take_screen_shot()
    {
        try
        {
            System.out.println("taking screenshot...");
            Log.i(TAG, "taking screenshot...");

            DisplayMode dm = getDisplayInfo().get(0);
            Screenshot.capture(0, 0, dm.getWidth(), dm.getHeight()).
                    store("png", new File("screen001.png"));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

    }

    public class ProxyPasteAction extends AbstractAction
    {
        private Action action;

        public ProxyPasteAction(Action action)
        {
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Log.i(TAG, "PasteOccured...");

            BufferedImage img = (BufferedImage) getImageFromClipboard();
            if (img != null)
            {
                Log.i(TAG, "PasteOccured...Image");
                try
                {
                    if (message_panel_displayed == 1)
                    {
                        Log.i(TAG, "PasteOccured...Image:002");
                        if (get_current_friendnum() != -1)
                        {
                            Log.i(TAG, "PasteOccured...Image:003:" + get_current_friendnum());

                            final String friend_pubkey_str = HelperFriend.tox_friend_get_public_key__wrapper(
                                    get_current_friendnum());

                            String wanted_full_filename_path = VFS_PREFIX + VFS_FILE_DIR + "/" + friend_pubkey_str;
                            new File(wanted_full_filename_path).mkdirs();

                            String filename_local_corrected = get_incoming_filetransfer_local_filename("clip.png",
                                                                                                       friend_pubkey_str);

                            filename_local_corrected = wanted_full_filename_path + "/" + filename_local_corrected;

                            Log.i(TAG, "PasteOccured...Image:004:" + filename_local_corrected);
                            final File f_send = new File(filename_local_corrected);
                            boolean res = ImageIO.write(img, "png", f_send);
                            Log.i(TAG, "PasteOccured...Image:004:" + filename_local_corrected + " res=" + res);

                            // send file
                            add_outgoing_file(f_send.getAbsoluteFile().getParent(), f_send.getAbsoluteFile().getName());
                        }
                    }
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "PasteOccured...EE:" + e2.getMessage());
                }
            }
            else
            {
                Log.i(TAG, "PasteOccured...Text");
                action.actionPerformed(e);
            }
            Log.i(TAG, "PasteOccured...END");
        }
    }

    public class ImageTransferHandler extends TransferHandler
    {
        private final DataFlavor FILE_FLAVOR = DataFlavor.javaFileListFlavor;
        private JComponent drop_component = null;

        public ImageTransferHandler(JComponent component)
        {
            drop_component = component;
            Log.i(TAG, "drag_n_drop:001");
        }

        public boolean importData(JComponent c, Transferable t)
        {
            Log.i(TAG, "drag_n_drop:002");
            if (transferFlavor(t.getTransferDataFlavors(), FILE_FLAVOR))
            {
                Log.i(TAG, "drag_n_drop:003");
                try
                {
                    Log.i(TAG, "drag_n_drop:004");
                    List<File> fileList = (List<File>) t.getTransferData(FILE_FLAVOR);
                    if (fileList != null && fileList.toArray() instanceof File[])
                    {
                        File[] files = (File[]) fileList.toArray();
                        // mainPanel.addFiles(files);
                        for (File f : files)
                        {
                            Log.i(TAG, "drag_n_drop:" + f.getAbsoluteFile() + " " + f.getName());
                        }
                    }
                    return true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return false;
        }

        /**
         * Returns the type of transfer actions to be supported.
         */
        public int getSourceActions(JComponent c)
        {
            return COPY_OR_MOVE;
        }

        /**
         * Specifies the actions to be performed after the data has been exported.
         */
        protected void exportDone(JComponent c, Transferable data, int action)
        {
            // c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        /**
         * Returns true if the specified flavor is contained in the flavors array,
         * false otherwise.
         */
        private boolean transferFlavor(DataFlavor[] flavors, DataFlavor flavor)
        {
            boolean found = false;
            for (int i = 0; i < flavors.length && !found; i++)
            {
                found = flavors[i].equals(flavor);
            }
            return found;
        }

        /**
         * Returns true if the component can import the specified flavours, false
         * otherwise.
         */
        public boolean canImport(JComponent c, DataFlavor[] flavors)
        {
            for (int i = 0; i < flavors.length; i++)
            {
                if (FILE_FLAVOR.equals(flavors[i]))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public class FileDropTargetListener implements DropTargetListener
    {

        @Override
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
        {
            Log.i(TAG, "FileDropTargetListener:dragEnter");
        }

        @Override
        public void dragOver(DropTargetDragEvent dropTargetDragEvent)
        {
            Log.i(TAG, "FileDropTargetListener:dragOver");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent)
        {
            Log.i(TAG, "FileDropTargetListener:dropActionChanged");
        }

        @Override
        public void dragExit(DropTargetEvent dropTargetEvent)
        {
            Log.i(TAG, "FileDropTargetListener:dragExit");
        }

        @Override
        public void drop(DropTargetDropEvent dropTargetDropEvent)
        {
            Log.i(TAG, "FileDropTargetListener:drop");
        }

    }

    @Override
    public void windowGainedFocus(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowGainedFocus");
        mainwindow_has_focus = true;
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowLostFocus");
        mainwindow_has_focus = false;
    }

    @Override
    public void windowOpened(WindowEvent windowEvent)
    {
        Log.i(TAG, "windowOpened");
    }

    @Override
    public void windowClosing(WindowEvent windowEvent)
    {
        Log.i(TAG, "windowClosing");
    }

    @Override
    public void windowClosed(WindowEvent windowEvent)
    {
        Log.i(TAG, "windowClosed");
    }

    @Override
    public void windowIconified(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowIconified");
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowDeiconified");
    }

    @Override
    public void windowActivated(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowActivated");
    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowDeactivated");
    }

    @Override
    public void windowStateChanged(WindowEvent windowEvent)
    {
        // Log.i(TAG, "windowStateChanged");
    }

    static class delete_selected_messages_asynchtask extends SwingWorker<Integer, Integer>
    {
        boolean update_message_list = false;
        boolean update_friend_list = false;

        public delete_selected_messages_asynchtask(boolean update_message_list, boolean update_friend_list)
        {
            this.update_message_list = update_message_list;
            this.update_friend_list = update_friend_list;
        }

        protected Integer doInBackground()
        {
            // sort ascending (lowest ID on top)
            Collections.sort(selected_messages, new Comparator<Long>()
            {
                public int compare(Long o1, Long o2)
                {
                    return o1.compareTo(o2);
                }
            });

            Iterator i = selected_messages.iterator();

            while (i.hasNext())
            {
                try
                {
                    long mid = (Long) i.next();
                    final Message m_to_delete = orma.selectFromMessage().idEq(mid).get(0);

                    // ---------- delete fileDB if this message is an outgoing file ----------
                    if (m_to_delete.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (m_to_delete.direction == 1)
                        {
                            try
                            {
                                // TODO: cleanup duplicated outgoing files from provider here ************
                                // FileDB file_ = orma.selectFromFileDB().idEq(m_to_delete.filedb_id).get(0);
                                orma.deleteFromFileDB().idEq(m_to_delete.filedb_id).execute();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "delete_selected_messages_asynchtask:EE4:" + e.getMessage());
                            }
                        }
                    }

                    // ---------- delete fileDB if this message is an outgoing file ----------

                    // ---------- delete fileDB and VFS file if this message is an incoming file ----------
                    if (m_to_delete.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                    {
                        if (m_to_delete.direction == 0)
                        {
                            try
                            {
                                FileDB file_ = orma.selectFromFileDB().idEq(m_to_delete.filedb_id).get(0);

                                try
                                {
                                    File f_vfs = new File(file_.path_name + "/" + file_.file_name);

                                    if (f_vfs.exists())
                                    {
                                        f_vfs.delete();
                                    }
                                }
                                catch (Exception e6)
                                {
                                    e6.printStackTrace();
                                    Log.i(TAG, "delete_selected_messages_asynchtask:EE5:" + e6.getMessage());
                                }

                                orma.deleteFromFileDB().idEq(m_to_delete.filedb_id).execute();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "delete_selected_messages_asynchtask:EE4:" + e.getMessage());
                            }
                        }
                    }

                    // ---------- delete fileDB and VFS file if this message is an incoming file ----------

                    // ---------- delete the message itself ----------
                    try
                    {
                        long message_id_to_delete = m_to_delete.id;

                        try
                        {
                            // let message delete animation finish (maybe use yet another asynctask here?) ------------
                            try
                            {
                                if (update_message_list)
                                {
                                    Thread.sleep(50);
                                }
                            }
                            catch (Exception sleep_ex)
                            {
                                sleep_ex.printStackTrace();
                            }

                            // let message delete animation finish (maybe use yet another asynctask here?) ------------
                            orma.deleteFromMessage().idEq(message_id_to_delete).execute();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.i(TAG, "delete_selected_messages_asynchtask:EE1:" + e.getMessage());
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        Log.i(TAG, "delete_selected_messages_asynchtask:EE2:" + e2.getMessage());
                    }

                    // ---------- delete the message itself ----------
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "delete_selected_messages_asynchtask:EE3:" + e2.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void done()
        {
            selected_messages.clear();
            selected_messages_incoming_file.clear();
            selected_messages_text_only.clear();

        }
    }

    static class CoalescedEventUpdater
    {
        private Timer timer;

        public CoalescedEventUpdater(int delay, Runnable callback)
        {
            timer = new Timer(delay, e -> {
                timer.stop();
                callback.run();
            });
        }

        public void update()
        {
            if (!SwingUtilities.isEventDispatchThread())
            {
                SwingUtilities.invokeLater(() -> {
                    timer.restart();
                });
            }
            else
            {
                timer.restart();
            }
        }
    }

    private static void updatePref(JFrame frame, Preferences prefs)
    {
        // System.out.println("Updating preferences");
        Point location = frame.getLocation();
        prefs.putInt("x", location.x);
        prefs.putInt("y", location.y);
        Dimension size = frame.getSize();
        prefs.putInt("w", size.width);
        prefs.putInt("h", size.height);
    }

    private static Dimension getFrameSize(Preferences pref, int defaultW, int defaultH)
    {
        int w = pref.getInt("w", defaultW);
        int h = pref.getInt("h", defaultH);
        return new Dimension(w, h);
    }

    private static Point getFrameLocation(Preferences pref, int defaultX, int defaultY)
    {
        int x = pref.getInt("x", defaultX);
        int y = pref.getInt("y", defaultY);
        return new Point(x, y);
    }
}

