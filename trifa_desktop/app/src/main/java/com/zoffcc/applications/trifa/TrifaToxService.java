/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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

import java.nio.ByteBuffer;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_FRIEND;
import static com.zoffcc.applications.trifa.HelperConference.new_or_updated_conference;
import static com.zoffcc.applications.trifa.HelperConference.set_all_conferences_inactive;
import static com.zoffcc.applications.trifa.HelperFiletransfer.set_all_filetransfers_inactive;
import static com.zoffcc.applications.trifa.HelperFiletransfer.start_outgoing_ft;
import static com.zoffcc.applications.trifa.HelperFriend.add_friend_real;
import static com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_msgv3_capability;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online_real;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.set_all_friends_offline;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.get_combined_connection_status;
import static com.zoffcc.applications.trifa.HelperGeneric.get_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.get_toxconnection_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.hex_to_bytes;
import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_resend_msgv3_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.new_or_updated_group;
import static com.zoffcc.applications.trifa.HelperGroup.update_group_in_db_name;
import static com.zoffcc.applications.trifa.HelperGroup.update_group_in_db_privacy_state;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_no_read_recvedts;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.MainActivity.MainFrame;
import static com.zoffcc.applications.trifa.MainActivity.PREF__faster_filetransfers;
import static com.zoffcc.applications.trifa.MainActivity.PREF__force_gc;
import static com.zoffcc.applications.trifa.MainActivity.PREF__udp_enabled;
import static com.zoffcc.applications.trifa.MainActivity.cache_confid_confnum;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.get_my_toxid;
import static com.zoffcc.applications.trifa.MainActivity.myToxID;
import static com.zoffcc.applications.trifa.MainActivity.ownProfileShort;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_type;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_chat_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_grouplist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_number_groups;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_privacy_state;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_is_connected;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_set_onion_active;
import static com.zoffcc.applications.trifa.MainActivity.tox_util_friend_resend_message_v2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ADD_BOTS_ON_STARTUP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_INIT_NAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_INIT_STATUSMSG;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_TOXID;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GROUP_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_ITERATE_MS_MIN_FILETRANSFER;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_ITERATE_MS_MIN_NORMAL;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_outgoung_ft_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_status_message;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_last_went_offline_timestamp;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;

public class TrifaToxService
{
    static final String TAG = "trifa.ToxService";
    static Thread ToxServiceThread = null;
    static boolean stop_me = false;
    static boolean is_tox_started = false;
    static boolean global_toxid_text_set = false;
    static boolean TOX_SERVICE_STARTED = false;
    static OrmaDatabase orma = null;
    static long last_resend_pending_messages0_ms = -1;
    static long last_resend_pending_messages1_ms = -1;
    static long last_resend_pending_messages2_ms = -1;
    static long last_resend_pending_messages3_ms = -1;
    static long last_resend_pending_messages4_ms = -1;
    static long last_start_queued_fts_ms = -1;

    void tox_thread_start_fg()
    {
        Log.i(TAG, "tox_thread_start_fg");

        ToxServiceThread = new Thread()
        {
            @Override
            public void run()
            {

                // ------ correct startup order ------
                boolean old_is_tox_started = is_tox_started;
                Log.i(TAG, "is_tox_started:==============================");
                Log.i(TAG, "is_tox_started=" + is_tox_started);
                Log.i(TAG, "is_tox_started:==============================");

                is_tox_started = true;

                // --------------------------------------------------
                // --- wait for UI to finish layouting
                // --------------------------------------------------
                long count_msgs = 0;
                final long count_msgs_max = 20;
                while ((MainFrame == null) || (!MainFrame.isShowing()))
                {
                    try
                    {
                        if (count_msgs < count_msgs_max)
                        {
                            Log.i(TAG, "waiting for UI to finish layout ...");
                            count_msgs++;
                        }
                        Thread.sleep(30);
                    }
                    catch (Exception e)
                    {
                    }
                }

                Log.i(TAG, "waiting for UI to finish layout ... DONE");
                // --------------------------------------------------
                // --- wait for UI to finish layouting
                // --------------------------------------------------


                if (!old_is_tox_started)
                {
                    set_all_friends_offline();
                    set_all_conferences_inactive();
                    set_all_filetransfers_inactive();
                    MainActivity.init_tox_callbacks();
                    HelperGeneric.update_savedata_file_wrapper();
                }
                // ------ correct startup order ------

                cache_pubkey_fnum.clear();
                cache_fnum_pubkey.clear();
                cache_confid_confnum.clear();

                // ----- convert old NULL's into 0 -----
                try
                {
                    Statement statement = sqldb.createStatement();
                    final String sql_str = "update Message set filetransfer_kind='0' where filetransfer_kind is NULL";
                    statement.execute(sql_str);
                    try
                    {
                        statement.close();
                    }
                    catch (Exception ignored)
                    {
                    }
                    Log.i(TAG, "onCreate:filetransfer_kind");
                }
                catch (Exception e)
                {
                    Log.i(TAG, "onCreate:filetransfer_kind:EE01");
                }
                // ----- convert old NULL's into 0 -----

                // TODO --------
                String my_tox_id_local = get_my_toxid();
                global_my_toxid = my_tox_id_local;
                if (tox_self_get_name_size() > 0)
                {
                    global_my_name = tox_self_get_name().substring(0, (int) tox_self_get_name_size());
                    Log.i(TAG, "AAA:003:" + global_my_name + " size=" + tox_self_get_name_size());
                }
                else
                {
                    tox_self_set_name("TRIfA " + my_tox_id_local.substring(my_tox_id_local.length() - 5,
                                                                           my_tox_id_local.length()));
                    global_my_name = ("TRIfA " + my_tox_id_local.substring(my_tox_id_local.length() - 5,
                                                                           my_tox_id_local.length()));
                    Log.i(TAG, "AAA:005");
                }

                if (tox_self_get_status_message_size() > 0)
                {
                    global_my_status_message = tox_self_get_status_message().substring(0,
                                                                                       (int) tox_self_get_status_message_size());
                    Log.i(TAG, "AAA:008:" + global_my_status_message + " size=" + tox_self_get_status_message_size());
                }
                else
                {
                    tox_self_set_status_message("this is TRIfA");
                    global_my_status_message = "this is TRIfA";
                    Log.i(TAG, "AAA:010");
                }
                Log.i(TAG, "AAA:011");

                HelperGeneric.update_savedata_file_wrapper();

                load_and_add_all_friends();

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                if (!old_is_tox_started)
                {
                    bootstrapping = true;
                    Log.i(TAG, "bootrapping:set to true");
                    global_self_last_went_offline_timestamp = System.currentTimeMillis();
                    bootstrap_me();
                }

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------

                long tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                MainActivity.tox_iterate();

                Log.i(TAG, "myToxID.setText:001");
                long loop = 0;
                while ((myToxID == null) || (!myToxID.isShowing()))
                {
                    try
                    {
                        // Log.i(TAG, "myToxID.setText:sleep");
                        Thread.sleep(10);
                        loop++;
                        if (loop > 1000)
                        {
                            break;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Log.i(TAG, "myToxID.setText:003");
                            myToxID.setEditable(true);
                            myToxID.setText(my_tox_id_local);
                            myToxID.setEditable(false);
                            ownProfileShort.setEditable(true);
                            ownProfileShort.setText(global_my_name);
                            ownProfileShort.setEditable(false);
                            Log.i(TAG, "myToxID.setText:004");
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, "myToxID.setText:005:EE:" + e.getMessage());
                        }
                    }
                };

                SwingUtilities.invokeLater(myRunnable);
                Log.i(TAG, "myToxID.setText:002");

                if (ADD_BOTS_ON_STARTUP)
                {
                    boolean need_add_bots = true;

                    try
                    {
                        Log.i(TAG, "need_add_bots read:" + get_g_opts("ADD_BOTS_ON_STARTUP_done"));

                        if (get_g_opts("ADD_BOTS_ON_STARTUP_done") != null)
                        {
                            if (get_g_opts("ADD_BOTS_ON_STARTUP_done").equals("true"))
                            {
                                need_add_bots = false;
                                Log.i(TAG, "need_add_bots=false");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (need_add_bots)
                    {
                        Log.i(TAG, "need_add_bots:start");
                        add_friend_real(ECHOBOT_TOXID);
                        set_g_opts("ADD_BOTS_ON_STARTUP_done", "true");

                        FriendList f_echobot = main_get_friend(ECHOBOT_TOXID.substring(0, 32 * 2).toUpperCase());
                        if (f_echobot != null)
                        {
                            f_echobot.status_message = ECHOBOT_INIT_STATUSMSG;
                            f_echobot.name = ECHOBOT_INIT_NAME;
                            HelperFriend.update_friend_in_db_name(f_echobot);
                            HelperFriend.update_friend_in_db_status_message(f_echobot);
                            HelperFriend.update_single_friend_in_friendlist_view(f_echobot);
                        }

                        Log.i(TAG, "need_add_bots=true (INSERT)");
                    }
                }

                try
                {
                    load_and_add_all_conferences();
                }
                catch (Exception e)
                {
                }

                try
                {
                    load_and_add_all_groups();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "Priority of thread is CUR: " + Thread.currentThread().getPriority());
                //Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                Thread.currentThread().setName("t_iterate");
                Log.i(TAG, "Priority of thread is NEW: " + Thread.currentThread().getPriority());

                // ---------------- force GC after first startup has happend ----------------
                // ---------------- force GC after first startup has happend ----------------
                // ---------------- force GC after first startup has happend ----------------
                //
                Log.i(TAG, "*** force GC:001 ***");
                System.gc();
                // run GC after 120 seconds
                new java.util.Timer().schedule(new java.util.TimerTask()
                {
                    @Override
                    public void run()
                    {
                        Log.i(TAG, "*** force GC:002 ***");
                        System.gc();
                    }
                }, 120 * 1000);

                if (PREF__force_gc)
                {
                    final Thread t_heap_check_and_gc_thread = new Thread(() -> {
                        long heapSize = 2;
                        long heapFreeSize = 1;
                        long heapUsedSize = 1;
                        while (1 == (2 - 1))
                        {
                            try
                            {
                                Thread.sleep(5 * 1000);
                                heapSize = Runtime.getRuntime().totalMemory() / 1024 / 1024;
                                heapFreeSize = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                                if (heapSize > heapFreeSize)
                                {
                                    heapUsedSize = heapSize - heapFreeSize;
                                    // Log.i(TAG, "*** HEAP:" + (heapSize) + " / " + (heapUsedSize) + " ***");
                                    if ((heapSize > 300) && (heapSize < 1000000))
                                    {
                                        // Log.i(TAG, "*** force GC:003 ***");
                                        System.gc();

                                        heapSize = Runtime.getRuntime().totalMemory() / 1024 / 1024;
                                        heapFreeSize = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                                        if (heapSize > heapFreeSize)
                                        {
                                            heapUsedSize = heapSize - heapFreeSize;
                                            // Log.i(TAG, "*** HEAP:" + (heapSize) + " / " + (heapUsedSize) + " ***");
                                        }
                                    }
                                }
                            }
                            catch (Exception ignored)
                            {
                            }
                        }
                    });
                    t_heap_check_and_gc_thread.start();
                }

                // ---------------- force GC after first startup has happend ----------------
                // ---------------- force GC after first startup has happend ----------------
                // ---------------- force GC after first startup has happend ----------------

                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                global_self_last_went_offline_timestamp = System.currentTimeMillis();
                while (!stop_me)
                {
                    try
                    {
                        // Log.i(TAG, "(tox_iteration_interval_ms):" + tox_iteration_interval_ms + "ms");
                        if (tox_iteration_interval_ms < TOX_ITERATE_MS_MIN_NORMAL)
                        {
                            tox_iteration_interval_ms = TOX_ITERATE_MS_MIN_NORMAL;
                        }

                        if (PREF__faster_filetransfers)
                        {
                            tox_set_onion_active(1);
                        }

                        if (global_last_activity_outgoung_ft_ts > -1)
                        {
                            if ((global_last_activity_outgoung_ft_ts + 200) > System.currentTimeMillis())
                            {
                                // iterate faster if outgoing filetransfers are active
                                if (PREF__faster_filetransfers)
                                {
                                    tox_iteration_interval_ms = 0;
                                }
                                else
                                {
                                    tox_iteration_interval_ms = TOX_ITERATE_MS_MIN_FILETRANSFER;
                                }

                                if (PREF__faster_filetransfers)
                                {
                                    tox_set_onion_active(0);
                                }
                            }
                        }

                        if (PREF__faster_filetransfers)
                        {
                            if (tox_iteration_interval_ms == 0)
                            {
                                Thread.sleep(0, 40);
                            }
                            else
                            {
                                Thread.sleep(tox_iteration_interval_ms);
                            }
                        }
                        else
                        {
                            Thread.sleep(tox_iteration_interval_ms);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // --- send pending 1-on-1 text messages here --------------
                    if (global_self_connection_status != TOX_CONNECTION_NONE.value)
                    {
                        if ((last_resend_pending_messages4_ms + (5 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages4_ms = System.currentTimeMillis();
                            resend_push_for_v3_messages();
                        }

                        if ((last_resend_pending_messages0_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages0_ms = System.currentTimeMillis();
                            resend_old_messages(null);
                        }

                        if ((last_resend_pending_messages1_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages1_ms = System.currentTimeMillis();
                            resend_v3_messages(null);
                        }

                        if ((last_resend_pending_messages2_ms + (30 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages2_ms = System.currentTimeMillis();
                            resend_v2_messages(false);
                        }

                        if ((last_resend_pending_messages3_ms + (120 * 1000)) < System.currentTimeMillis())
                        {
                            last_resend_pending_messages3_ms = System.currentTimeMillis();
                            resend_v2_messages(true);
                        }
                    }
                    // --- send pending 1-on-1 text messages here --------------

                    // --- start queued outgoing FTs here --------------
                    if (global_self_connection_status != TOX_CONNECTION_NONE.value)
                    {
                        if ((last_start_queued_fts_ms + (4 * 1000)) < System.currentTimeMillis())
                        {
                            // Log.i(TAG, "start_queued_outgoing_FTs ============================================");
                            last_start_queued_fts_ms = System.currentTimeMillis();

                            try
                            {
                                List<Message> m_v1 = orma.selectFromMessage().
                                        directionEq(1).
                                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                                        ft_outgoing_queuedEq(true).
                                        stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                                        orderBySent_timestampAsc().
                                        toList();

                                if ((m_v1 != null) && (m_v1.size() > 0))
                                {
                                    Iterator<Message> ii = m_v1.iterator();
                                    while (ii.hasNext())
                                    {
                                        Message m_resend_ft = ii.next();

                                        if (is_friend_online_real(
                                                tox_friend_by_public_key__wrapper(m_resend_ft.tox_friendpubkey)) != 0)
                                        {
                                            start_outgoing_ft(m_resend_ft);
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                    // --- start queued outgoing FTs here --------------

                    check_if_need_bootstrap_again();
                    MainActivity.tox_iterate();
                    tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                }
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------

                try
                {
                    Thread.sleep(100); // wait a bit, for "something" to finish up in the native code
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    MainActivity.tox_kill();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(100); // wait a bit, for "something" to finish up in the native code
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        };

        ToxServiceThread.start();
    }

    // ------------------------------

    static void resend_push_for_v3_messages()
    {
        try
        {
            // HINT: if we have not received a "read receipt" for msgV3 within 10 seconds, then we trigger a push again
            final long cutoff_sent_time = System.currentTimeMillis() - (10 * 1000);

            List<Message> m_push = orma.selectFromMessage().
                    directionEq(1).
                    msg_versionEq(0).
                    TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                    sent_pushEq(0).
                    readEq(false).
                    orderBySent_timestampAsc().
                    sent_timestampLt(cutoff_sent_time).
                    toList();

            if ((m_push != null) && (m_push.size() > 0))
            {
                Iterator<Message> ii = m_push.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_push = ii.next();
                    if ((m_resend_push.msg_idv3_hash != null) && (m_resend_push.msg_idv3_hash.length() > 3))
                    {
                        friend_call_push_url(m_resend_push.tox_friendpubkey, m_resend_push.sent_timestamp);
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "resend_push_for_v3_messages:EE:" + e.getMessage());
        }
    }

    static void resend_v3_messages(String friend_pubkey)
    {
        // loop through "old msg version" msgV3 1-on-1 text messages that have "resend_count < MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION" --------------
        try
        {
            int max_resend_count_per_iteration = 20;

            if (friend_pubkey != null)
            {
                max_resend_count_per_iteration = 20;
            }

            int cur_resend_count_per_iteration = 0;

            List<Message> m_v1 = null;
            if (friend_pubkey != null)
            {
                m_v1 = orma.selectFromMessage().
                        directionEq(1).
                        msg_versionEq(0).
                        tox_friendpubkeyEq(friend_pubkey).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).
                        readEq(false).
                        orderBySent_timestampAsc().
                        toList();
            }
            else
            {
                m_v1 = orma.selectFromMessage().
                        directionEq(1).
                        msg_versionEq(0).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        resend_countLt(MAX_TEXTMSG_RESEND_COUNT_OLDMSG_VERSION).
                        readEq(false).
                        orderBySent_timestampAsc().
                        toList();
            }

            if ((m_v1 != null) && (m_v1.size() > 0))
            {
                Iterator<Message> ii = m_v1.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_v1 = ii.next();
                    if (friend_pubkey == null)
                    {
                        if (is_friend_online_real(tox_friend_by_public_key__wrapper(m_resend_v1.tox_friendpubkey)) == 0)
                        {
                            continue;
                        }
                    }

                    if (get_friend_msgv3_capability(m_resend_v1.tox_friendpubkey) != 1)
                    {
                        continue;
                    }

                    tox_friend_resend_msgv3_wrapper(m_resend_v1);
                    cur_resend_count_per_iteration++;
                    if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                    {
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "resend_v3_messages:EE:" + e.getMessage());
        }
        // loop through all pending outgoing 1-on-1 text messages --------------
    }

    static void resend_old_messages(final String friend_pubkey)
    {
        try
        {
            int max_resend_count_per_iteration = 10;

            if (friend_pubkey != null)
            {
                max_resend_count_per_iteration = 20;
            }

            int cur_resend_count_per_iteration = 0;

            // HINT: cutoff time "now" minus 25 seconds
            final long cutoff_sent_time = System.currentTimeMillis() - (25 * 1000);
            List<Message> m_v0 = null;

            if (friend_pubkey != null)
            {
                // HINT: this is the generic resend for all friends, that happens in regular intervals
                //       only resend if the original sent timestamp is at least 25 seconds in the past
                //       to try to avoid resending when the read receipt is very late.
                m_v0 = orma.selectFromMessage().
                        directionEq(1).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        msg_versionEq(0).
                        tox_friendpubkeyEq(friend_pubkey).
                        readEq(false).
                        resend_countLt(2).
                        orderBySent_timestampAsc().
                        sent_timestampLt(cutoff_sent_time).
                        toList();
            }
            else
            {
                // HINT: this is the specific resend for 1 friend only, when that friend comes online
                m_v0 = orma.selectFromMessage().
                        directionEq(1).
                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                        msg_versionEq(0).
                        readEq(false).
                        resend_countLt(2).
                        orderBySent_timestampAsc().
                        toList();
            }

            if ((m_v0 != null) && (m_v0.size() > 0))
            {
                Iterator<Message> ii = m_v0.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_v0 = ii.next();

                    if (friend_pubkey == null)
                    {
                        if (is_friend_online_real(tox_friend_by_public_key__wrapper(m_resend_v0.tox_friendpubkey)) == 0)
                        {
                            // Log.i(TAG, "resend_old_messages:RET:01:" +
                            //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                            continue;
                        }
                    }

                    if (get_friend_msgv3_capability(m_resend_v0.tox_friendpubkey) == 1)
                    {
                        // Log.i(TAG, "resend_old_messages:RET:02:" +
                        //            get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                        continue;
                    }

                    // Log.i(TAG, "resend_old_messages:tox_friend_resend_msgv3_wrapper:" + m_resend_v0.text + " : m=" +
                    //            m_resend_v0 + " : " + get_friend_name_from_pubkey(m_resend_v0.tox_friendpubkey));
                    tox_friend_resend_msgv3_wrapper(m_resend_v0);

                    cur_resend_count_per_iteration++;

                    if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                    {
                        break;
                    }

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void resend_v2_messages(boolean at_relay)
    {
        // loop through all pending outgoing 1-on-1 text messages V2 (resend) --------------
        try
        {
            final int max_resend_count_per_iteration = 10;
            int cur_resend_count_per_iteration = 0;

            List<Message> m_v1 = orma.selectFromMessage().
                    directionEq(1).
                    TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                    msg_versionEq(1).
                    readEq(false).
                    msg_at_relayEq(at_relay).
                    orderBySent_timestampAsc().
                    toList();


            if ((m_v1 != null) && (m_v1.size() > 0))
            {
                Iterator<Message> ii = m_v1.iterator();
                while (ii.hasNext())
                {
                    Message m_resend_v2 = ii.next();

                    if (is_friend_online(tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey)) == 0)
                    {
                        continue;
                    }

                    if ((m_resend_v2.msg_id_hash == null) ||
                        (m_resend_v2.msg_id_hash.equalsIgnoreCase(""))) // resend msgV2 WITHOUT hash
                    {
                        // Log.i(TAG, "resend_msgV2_WITHOUT_hash:f=" +
                        //           get_friend_name_from_pubkey(m_resend_v2.tox_friendpubkey) + " m=" + m_resend_v2);
                        MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                                m_resend_v2.tox_friendpubkey, 0, m_resend_v2.text, (m_resend_v2.sent_timestamp / 1000));

                        if (result != null)
                        {
                            long res = result.msg_num;

                            if (res > -1)
                            {
                                m_resend_v2.resend_count = 1; // we sent the message successfully
                                m_resend_v2.message_id = res;
                            }
                            else
                            {
                                m_resend_v2.resend_count = 0; // sending was NOT successfull
                                m_resend_v2.message_id = -1;
                            }

                            if (result.msg_v2)
                            {
                                m_resend_v2.msg_version = 1;
                            }
                            else
                            {
                                m_resend_v2.msg_version = 0;
                            }

                            if ((result.msg_hash_hex != null) && (!result.msg_hash_hex.equalsIgnoreCase("")))
                            {
                                // msgV2 message -----------
                                m_resend_v2.msg_id_hash = result.msg_hash_hex;
                                // msgV2 message -----------
                            }

                            if ((result.msg_hash_v3_hex != null) && (!result.msg_hash_v3_hex.equalsIgnoreCase("")))
                            {
                                // msgV3 message -----------
                                m_resend_v2.msg_idv3_hash = result.msg_hash_v3_hex;
                                // msgV3 message -----------
                            }

                            if ((result.raw_message_buf_hex != null) &&
                                (!result.raw_message_buf_hex.equalsIgnoreCase("")))
                            {
                                // save raw message bytes of this v2 msg into the database
                                // we need it if we want to resend it later
                                m_resend_v2.raw_msgv2_bytes = result.raw_message_buf_hex;
                            }

                            update_message_in_db_messageid(m_resend_v2);
                            update_message_in_db_resend_count(m_resend_v2);
                            update_message_in_db_no_read_recvedts(m_resend_v2);
                        }
                    }
                    else // resend msgV2 with hash
                    {
                        final int raw_data_length = (m_resend_v2.raw_msgv2_bytes.length() / 2);
                        byte[] raw_msg_resend_data = hex_to_bytes(m_resend_v2.raw_msgv2_bytes);

                        ByteBuffer msg_text_buffer_resend_v2 = ByteBuffer.allocateDirect(raw_data_length);
                        msg_text_buffer_resend_v2.put(raw_msg_resend_data, 0, raw_data_length);

                        int res = tox_util_friend_resend_message_v2(
                                tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey),
                                msg_text_buffer_resend_v2, raw_data_length);


                        String relay = get_relay_for_friend(m_resend_v2.tox_friendpubkey);
                        if (relay != null)
                        {
                            int res_relay = tox_util_friend_resend_message_v2(tox_friend_by_public_key__wrapper(relay),
                                                                              msg_text_buffer_resend_v2,
                                                                              raw_data_length);

                        }
                    }

                    cur_resend_count_per_iteration++;

                    if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                    {
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------
    }

    private void check_if_need_bootstrap_again()
    {
        if (global_self_connection_status == TOX_CONNECTION_NONE.value)
        {
            if (global_self_last_went_offline_timestamp != -1)
            {
                if (global_self_last_went_offline_timestamp + TOX_BOOTSTRAP_AGAIN_AFTER_OFFLINE_MILLIS <
                    System.currentTimeMillis())
                {
                    Log.i(TAG, "offline for too long --> bootstrap again ...");
                    global_self_last_went_offline_timestamp = System.currentTimeMillis();

                    bootstrapping = true;
                    Log.i(TAG, "bootrapping:set to true[2]");

                    try
                    {
                        bootstrap_me();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "bootstrap_me:001:EE:" + e.getMessage());
                    }
                }
            }
        }
    }

    static void bootstrap_me()
    {
        Log.i(TAG, "bootstrap_me");

        // ----- UDP ------
        if (PREF__udp_enabled == 1)
        {
            MainActivity.bootstrap_single_wrapper("85.143.221.42", 33445,
                                                  "DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43");
            MainActivity.bootstrap_single_wrapper("tox.verdict.gg", 33445,
                                                  "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");
            MainActivity.bootstrap_single_wrapper("78.46.73.141", 33445,
                                                  "02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46");
            MainActivity.bootstrap_single_wrapper("tox.initramfs.io", 33445,
                                                  "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");
            MainActivity.bootstrap_single_wrapper("46.229.52.198", 33445,
                                                  "813C8F4187833EF0655B10F7752141A352248462A567529A38B6BBF73E979307");
            MainActivity.bootstrap_single_wrapper("144.217.167.73", 33445,
                                                  "7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");
            MainActivity.bootstrap_single_wrapper("tox.abilinski.com", 33445,
                                                  "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");
            MainActivity.bootstrap_single_wrapper("tox.novg.net", 33445,
                                                  "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");
            MainActivity.bootstrap_single_wrapper("95.31.18.227", 33445,
                                                  "257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E");
            MainActivity.bootstrap_single_wrapper("198.199.98.108", 33445,
                                                  "BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");
            MainActivity.bootstrap_single_wrapper("tox.kurnevsky.net", 33445,
                                                  "82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");
            MainActivity.bootstrap_single_wrapper("81.169.136.229", 33445,
                                                  "E0DB78116AC6500398DDBA2AEEF3220BB116384CAB714C5D1FCD61EA2B69D75E");
            MainActivity.bootstrap_single_wrapper("205.185.115.131", 53,
                                                  "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");
            MainActivity.bootstrap_single_wrapper("tox2.abilinski.com", 33445,
                                                  "7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");
            MainActivity.bootstrap_single_wrapper("46.101.197.175", 33445,
                                                  "CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");
            MainActivity.bootstrap_single_wrapper("tox1.mf-net.eu", 33445,
                                                  "B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");
            MainActivity.bootstrap_single_wrapper("tox2.mf-net.eu", 33445,
                                                  "70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");
            MainActivity.bootstrap_single_wrapper("195.201.7.101", 33445,
                                                  "B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");
            MainActivity.bootstrap_single_wrapper("168.138.203.178", 33445,
                                                  "6D04D8248E553F6F0BFDDB66FBFB03977E3EE54C432D416BC2444986EF02CC17");
            MainActivity.bootstrap_single_wrapper("209.59.144.175", 33445,
                                                  "214B7FEA63227CAEC5BCBA87F7ABEEDB1A2FF6D18377DD86BF551B8E094D5F1E");
            MainActivity.bootstrap_single_wrapper("188.225.9.167", 33445,
                                                  "1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");
            MainActivity.bootstrap_single_wrapper("122.116.39.151", 33445,
                                                  "5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");
            MainActivity.bootstrap_single_wrapper("195.123.208.139", 33445,
                                                  "534A589BA7427C631773D13083570F529238211893640C99D1507300F055FE73");
            MainActivity.bootstrap_single_wrapper("208.38.228.104", 33445,
                                                  "3634666A51CA5BE1579C031BD31B20059280EB7C05406ED466BD9DFA53373271");
            MainActivity.bootstrap_single_wrapper("104.225.141.59", 43334,
                                                  "933BA20B2E258B4C0D475B6DECE90C7E827FE83EFA9655414E7841251B19A72C");
            MainActivity.bootstrap_single_wrapper("137.74.42.224", 33445,
                                                  "A95177FA018066CF044E811178D26B844CBF7E1E76F140095B3A1807E081A204");
            MainActivity.bootstrap_single_wrapper("198.98.49.206", 33445,
                                                  "28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");
        }
        else
        {
            MainActivity.bootstrap_single_wrapper("127.0.0.1", 7766,
                                                  "2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1");
        }
        // ----- UDP ------
        //
        // ----- TCP ------
        MainActivity.add_tcp_relay_single_wrapper("85.143.221.42", 3389,
                                                  "DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43");
        MainActivity.add_tcp_relay_single_wrapper("tox.verdict.gg", 33445,
                                                  "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");
        MainActivity.add_tcp_relay_single_wrapper("78.46.73.141", 33445,
                                                  "02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46");
        MainActivity.add_tcp_relay_single_wrapper("tox.initramfs.io", 3389,
                                                  "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");
        MainActivity.add_tcp_relay_single_wrapper("144.217.167.73", 3389,
                                                  "7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");
        MainActivity.add_tcp_relay_single_wrapper("tox.abilinski.com", 33445,
                                                  "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");
        MainActivity.add_tcp_relay_single_wrapper("tox.novg.net", 33445,
                                                  "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");
        MainActivity.add_tcp_relay_single_wrapper("95.31.18.227", 33445,
                                                  "257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E");
        MainActivity.add_tcp_relay_single_wrapper("198.199.98.108", 3389,
                                                  "BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");
        MainActivity.add_tcp_relay_single_wrapper("tox.kurnevsky.net", 33445,
                                                  "82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");
        MainActivity.add_tcp_relay_single_wrapper("81.169.136.229", 33445,
                                                  "E0DB78116AC6500398DDBA2AEEF3220BB116384CAB714C5D1FCD61EA2B69D75E");
        MainActivity.add_tcp_relay_single_wrapper("205.185.115.131", 3389,
                                                  "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");
        MainActivity.add_tcp_relay_single_wrapper("tox2.abilinski.com", 33445,
                                                  "7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");
        MainActivity.add_tcp_relay_single_wrapper("46.101.197.175", 33445,
                                                  "CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");
        MainActivity.add_tcp_relay_single_wrapper("tox1.mf-net.eu", 3389,
                                                  "B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");
        MainActivity.add_tcp_relay_single_wrapper("tox2.mf-net.eu", 3389,
                                                  "70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");
        MainActivity.add_tcp_relay_single_wrapper("195.201.7.101", 33445,
                                                  "B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");
        MainActivity.add_tcp_relay_single_wrapper("168.138.203.178", 33445,
                                                  "6D04D8248E553F6F0BFDDB66FBFB03977E3EE54C432D416BC2444986EF02CC17");
        MainActivity.add_tcp_relay_single_wrapper("209.59.144.175", 33445,
                                                  "214B7FEA63227CAEC5BCBA87F7ABEEDB1A2FF6D18377DD86BF551B8E094D5F1E");
        MainActivity.add_tcp_relay_single_wrapper("188.225.9.167", 33445,
                                                  "1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");
        MainActivity.add_tcp_relay_single_wrapper("122.116.39.151", 33445,
                                                  "5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");
        MainActivity.add_tcp_relay_single_wrapper("195.123.208.139", 3389,
                                                  "534A589BA7427C631773D13083570F529238211893640C99D1507300F055FE73");
        MainActivity.add_tcp_relay_single_wrapper("208.38.228.104", 33445,
                                                  "3634666A51CA5BE1579C031BD31B20059280EB7C05406ED466BD9DFA53373271");
        MainActivity.add_tcp_relay_single_wrapper("137.74.42.224", 33445,
                                                  "A95177FA018066CF044E811178D26B844CBF7E1E76F140095B3A1807E081A204");
        MainActivity.add_tcp_relay_single_wrapper("198.98.49.206", 33445,
                                                  "28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");
        MainActivity.add_tcp_relay_single_wrapper("5.19.249.240", 3389,
                                                  "DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238");
        // ----- TCP ------
    }

    // --------------- JNI ---------------
    // --------------- JNI ---------------
    // --------------- JNI ---------------
    static void logger(int level, String text)
    {
        Log.i(TAG, text);
    }

    static String safe_string(byte[] in)
    {
        // Log.i(TAG, "safe_string:in=" + in);
        String out = "";

        try
        {
            out = new String(in, "UTF-8");  // Best way to decode using "UTF-8"
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "safe_string:EE:" + e.getMessage());
            try
            {
                out = new String(in);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "safe_string:EE2:" + e2.getMessage());
            }
        }

        // Log.i(TAG, "safe_string:out=" + out);
        return out;
    }
    // --------------- JNI ---------------
    // --------------- JNI ---------------
    // --------------- JNI ---------------

    void load_and_add_all_friends()
    {
        // --- load and update all friends ---
        MainActivity.friends = MainActivity.tox_self_get_friend_list();
        Log.i(TAG, "loading_friend:number_of_friends=" + MainActivity.friends.length);

        int fc = 0;
        boolean exists_in_db = false;
        //                try
        //                {
        //                    MainActivity.friend_list_fragment.clear_friends();
        //                }
        //                catch (Exception e)
        //                {
        //                }

        for (fc = 0; fc < MainActivity.friends.length; fc++)
        {
            // Log.i(TAG, "loading_friend:" + fc + " friendnum=" + MainActivity.friends[fc]);
            // Log.i(TAG, "loading_friend:" + fc + " pubkey=" + tox_friend_get_public_key__wrapper(MainActivity.friends[fc]));

            FriendList f;
            List<FriendList> fl = orma.selectFromFriendList().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).toList();

            // Log.i(TAG, "loading_friend:" + fc + " db entry size=" + fl);

            if (fl.size() > 0)
            {
                f = fl.get(0);
                // Log.i(TAG, "loading_friend:" + fc + " db entry=" + f);
            }
            else
            {
                f = null;
            }

            if (f == null)
            {
                Log.i(TAG, "loading_friend:c is null");

                f = new FriendList();
                f.tox_public_key_string = "" + (long) ((Math.random() * 10000000d));
                try
                {
                    f.tox_public_key_string = tox_friend_get_public_key__wrapper(MainActivity.friends[fc]);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                f.name = "friend #" + fc;
                exists_in_db = false;
                // Log.i(TAG, "loading_friend:c is null fnew=" + f);
            }
            else
            {
                // Log.i(TAG, "loading_friend:found friend in DB " + f.tox_public_key_string + " f=" + f);
                exists_in_db = true;
            }

            try
            {
                // get the real "live" connection status of this friend
                // the value in the database may be old (and wrong)
                int status_new = tox_friend_get_connection_status(MainActivity.friends[fc]);
                int combined_connection_status_ = get_combined_connection_status(f.tox_public_key_string, status_new);
                f.TOX_CONNECTION = combined_connection_status_;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                f.added_timestamp = System.currentTimeMillis();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // ----- would be double in list -----
            // ----- would be double in list -----
            // ----- would be double in list -----
            //                    if (MainActivity.friend_list_fragment != null)
            //                    {
            //                        try
            //                        {
            //                            MainActivity.friend_list_fragment.add_friends(f);
            //                        }
            //                        catch (Exception e)
            //                        {
            //                        }
            //                    }
            // ----- would be double in list -----
            // ----- would be double in list -----
            // ----- would be double in list -----

            if (exists_in_db == false)
            {
                // Log.i(TAG, "loading_friend:1:insertIntoFriendList:" + " f=" + f);
                orma.insertIntoFriendList(f);
                // Log.i(TAG, "loading_friend:2:insertIntoFriendList:" + " f=" + f);
            }
            else
            {
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
                orma.updateFriendList().tox_public_key_stringEq(
                        tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).name(f.name).status_message(
                        f.status_message).TOX_CONNECTION(f.TOX_CONNECTION).TOX_CONNECTION_on_off(
                        get_toxconnection_wrapper(f.TOX_CONNECTION)).TOX_USER_STATUS(f.TOX_USER_STATUS).execute();
                // Log.i(TAG, "loading_friend:1:updateFriendList:" + " f=" + f);
            }

            FriendList f_check;
            List<FriendList> fl_check = orma.selectFromFriendList().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(MainActivity.friends[fc])).toList();
            // Log.i(TAG, "loading_friend:check:" + " db entry=" + fl_check);
            try
            {
                // Log.i(TAG, "loading_friend:check:" + " db entry=" + fl_check.get(0));

                try
                {
                    if (MainActivity.FriendPanel != null)
                    {
                        CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                        cc.is_friend = COMBINED_IS_FRIEND;
                        cc.friend_item = fl_check.get(0);
                        MainActivity.FriendPanel.modify_friend(cc, cc.is_friend);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "loading_friend:check:EE:" + e.getMessage());
            }
        }
        // --- load and update all friends ---
    }

    void load_and_add_all_conferences()
    {
        long num_conferences = tox_conference_get_chatlist_size();
        Log.i(TAG, "load conferences at startup: num=" + num_conferences);

        long[] conference_numbers = tox_conference_get_chatlist();
        ByteBuffer cookie_buf3 = ByteBuffer.allocateDirect(CONFERENCE_ID_LENGTH * 2);

        int conf_ = 0;
        for (conf_ = 0; conf_ < num_conferences; conf_++)

        {
            cookie_buf3.clear();
            if (tox_conference_get_id(conference_numbers[conf_], cookie_buf3) == 0)
            {
                byte[] cookie_buffer = new byte[CONFERENCE_ID_LENGTH];
                cookie_buf3.get(cookie_buffer, 0, CONFERENCE_ID_LENGTH);
                String conference_identifier = bytes_to_hex(cookie_buffer);
                // Log.i(TAG, "load conference num=" + conference_numbers[conf_] + " cookie=" + conference_identifier +
                //           " offset=" + cookie_buf3.arrayOffset());

                final ConferenceDB conf2 = orma.selectFromConferenceDB().toList().get(0);
                //Log.i(TAG,
                //      "conference 0 in db:" + conf2.conference_identifier + " " + conf2.tox_conference_number + " " +
                //      conf2.name);

                new_or_updated_conference(conference_numbers[conf_], tox_friend_get_public_key__wrapper(0),
                                          conference_identifier, tox_conference_get_type(
                                conference_numbers[conf_])); // rejoin a saved conference

                //if (tox_conference_get_type(conference_numbers[conf_]) == TOX_CONFERENCE_TYPE_AV.value)
                //{
                //    // TODO: this returns error. check it
                //    long result = toxav_groupchat_disable_av(conference_numbers[conf_]);
                //    Log.i(TAG, "load conference num=" + conference_numbers[conf_] + " toxav_groupchat_disable_av res=" +
                //               result);
                //}

            }
        }
    }

    void load_and_add_all_groups()
    {
        long num_groups = tox_group_get_number_groups();
        Log.i(TAG, "load groups at startup: num=" + num_groups);

        long[] group_numbers = tox_group_get_grouplist();
        ByteBuffer groupid_buf3 = ByteBuffer.allocateDirect(GROUP_ID_LENGTH * 2);

        int conf_ = 0;
        for (conf_ = 0; conf_ < num_groups; conf_++)
        {
            groupid_buf3.clear();

            Log.i(TAG, "load group:1:" + conf_ + " " + group_numbers[conf_]);

            if (tox_group_get_chat_id(group_numbers[conf_], groupid_buf3) == 0)
            {
                Log.i(TAG, "load group:2:" + conf_ + " " + group_numbers[conf_]);

                byte[] groupid_buffer = new byte[GROUP_ID_LENGTH];
                groupid_buf3.get(groupid_buffer, 0, GROUP_ID_LENGTH);
                String group_identifier = bytes_to_hex(groupid_buffer);
                int is_connected = tox_group_is_connected(conf_);

                new_or_updated_group(group_numbers[conf_], tox_friend_get_public_key__wrapper(0), group_identifier,
                                     tox_group_get_privacy_state(group_numbers[conf_]));

                String group_name = tox_group_get_name(group_numbers[conf_]);

                if (group_name == null)
                {
                    group_name = "";
                }
                update_group_in_db_name(group_identifier, group_name);

                final int new_privacy_state = tox_group_get_privacy_state(group_numbers[conf_]);
                update_group_in_db_privacy_state(group_identifier, new_privacy_state);
            }
        }
    }
}
