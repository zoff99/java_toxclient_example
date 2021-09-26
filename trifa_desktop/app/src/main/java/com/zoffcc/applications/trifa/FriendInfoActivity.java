/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SizeRequirements;
import javax.swing.border.Border;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;

import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.is_valid_pushurl_for_friend_with_whitelist;
import static com.zoffcc.applications.trifa.MainActivity.AVATAR_FRIENDINFO_H;
import static com.zoffcc.applications.trifa.MainActivity.AVATAR_FRIENDINFO_W;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SEE_THRU;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class FriendInfoActivity extends JFrame
{
    public static int width = 640;
    public static int height = 800;

    private JLabel label_name = new JLabel("Name");
    private JTextArea text_name = new JTextArea();

    private JLabel label_toxpubkey = new JLabel("Publickey");
    private JTextArea text_toxpubkey = new JTextArea();

    private JLabel label_pushurl = new JLabel("Push URL");
    private JEditorPane text_pushurl = new JEditorPane();

    private JLabel label_relay = new JLabel("Relay");
    private JTextArea text_relay = new JTextArea("");

    private JPanel main_panel = null;

    final JPanel avatar_container = new JPanel(new SingleComponentAspectRatioKeeperLayout(), true);
    final JPictureBox avatar = new JPictureBox();

    FriendInfoActivity(String pubkey)
    {
        super("Friend Info");

        FriendList fl = main_get_friend(pubkey);

        setSize(width, height / 2);
        setPreferredSize(new Dimension(width, height / 2));

        main_panel = new JPanel(true);
        main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.PAGE_AXIS));

        text_pushurl.setEditorKit(new HTMLEditorKit()
        {
            @Override
            public ViewFactory getViewFactory()
            {

                return new HTMLFactory()
                {

                    public View create(Element e)
                    {
                        View v = super.create(e);
                        if (v instanceof InlineView)
                        {
                            return new InlineView(e)
                            {
                                public int getBreakWeight(int axis, float pos, float len)
                                {
                                    return GoodBreakWeight;
                                }

                                public View breakView(int axis, int p0, float pos, float len)
                                {
                                    if (axis == View.X_AXIS)
                                    {
                                        checkPainter();
                                        int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                                        if (p0 == getStartOffset() && p1 == getEndOffset())
                                        {
                                            return this;
                                        }
                                        return createFragment(p0, p1);
                                    }
                                    return this;
                                }
                            };
                        }
                        else if (v instanceof ParagraphView)
                        {
                            return new ParagraphView(e)
                            {
                                protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r)
                                {
                                    if (r == null)
                                    {
                                        r = new SizeRequirements();
                                    }
                                    float pref = layoutPool.getPreferredSpan(axis);
                                    float min = layoutPool.getMinimumSpan(axis);
                                    // Don't include insets, Box.getXXXSpan will include them.
                                    r.minimum = (int) min;
                                    r.preferred = Math.max(r.minimum, (int) pref);
                                    r.maximum = Integer.MAX_VALUE;
                                    r.alignment = 0.5f;
                                    return r;
                                }

                            };
                        }
                        return v;
                    }
                };
            }
        });

        text_pushurl.setContentType("text/html");

        avatar.setSize(AVATAR_FRIENDINFO_W, AVATAR_FRIENDINFO_H);

        avatar_container.add(avatar);
        avatar_container.setPreferredSize(new Dimension(AVATAR_FRIENDINFO_W, AVATAR_FRIENDINFO_H));
        avatar_container.setMaximumSize(new Dimension(AVATAR_FRIENDINFO_W, AVATAR_FRIENDINFO_H));
        avatar_container.setMinimumSize(new Dimension(AVATAR_FRIENDINFO_W, AVATAR_FRIENDINFO_H));

        // ----------- avatar -----------
        JPanel panel_avatar = new JPanel(new GridBagLayout());

        GridBagConstraints constraints_avatar = new GridBagConstraints();
        constraints_avatar.anchor = GridBagConstraints.CENTER;
        constraints_avatar.insets = new Insets(0, 0, 0, 0);

        constraints_avatar.gridx = 0;
        constraints_avatar.gridy = 0;
        panel_avatar.add(avatar_container, constraints_avatar);

        panel_avatar.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                lo.getString("settings_category_002")));

        panel_avatar.setPreferredSize(new Dimension(AVATAR_FRIENDINFO_W, AVATAR_FRIENDINFO_H + 40));
        // ----------- avatar -----------


        // ----------- Tox -----------
        final Border line_border = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        JPanel panel_tox = new JPanel(new GridBagLayout());

        GridBagConstraints constraints_tox = new GridBagConstraints();
        constraints_tox.anchor = GridBagConstraints.WEST;
        constraints_tox.insets = new Insets(2, 0, 2, 5);

        constraints_tox.gridx = 0;
        constraints_tox.gridy = 0;
        panel_tox.add(label_name, constraints_tox);
        constraints_tox.gridx = 1;
        panel_tox.add(text_name, constraints_tox);

        constraints_tox.gridx = 0;
        constraints_tox.gridy++;
        panel_tox.add(label_toxpubkey, constraints_tox);
        constraints_tox.gridx = 1;
        panel_tox.add(text_toxpubkey, constraints_tox);


        text_toxpubkey.setText(fl.tox_public_key_string);
        text_toxpubkey.setLineWrap(true);
        text_toxpubkey.setColumns(35);
        text_toxpubkey.setEditable(false);
        text_toxpubkey.setBorder(
                BorderFactory.createCompoundBorder(line_border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));


        constraints_tox.gridx = 0;
        constraints_tox.gridy++;
        panel_tox.add(label_pushurl, constraints_tox);
        constraints_tox.gridx = 1;
        panel_tox.add(text_pushurl, constraints_tox);

        constraints_tox.gridx = 0;
        constraints_tox.gridy++;
        panel_tox.add(label_relay, constraints_tox);
        constraints_tox.gridx = 1;
        panel_tox.add(text_relay, constraints_tox);

        String friend_relay_pubkey = get_relay_for_friend(pubkey);
        String pushurl_for_friend = get_pushurl_for_friend(pubkey);

        text_name.setText(fl.name);
        text_name.setLineWrap(true);
        text_name.setColumns(35);
        text_name.setEditable(false);
        text_name.setBorder(
                BorderFactory.createCompoundBorder(line_border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        boolean is_valid = false;
        try
        {
            if (pushurl_for_friend.length() > "https://".length())
            {
                if (is_valid_pushurl_for_friend_with_whitelist(pushurl_for_friend))
                {
                    is_valid = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        if ((pushurl_for_friend != null) && (pushurl_for_friend.length() > "https://".length()))
        {
            String to_set = "<font color = \"000000\">" + pushurl_for_friend + "</font>";
            String nl = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            if (is_valid)
            {
                final String color_green = "008000";
                to_set = to_set + "<br>" + nl + nl + nl + "<B><font color = \"" + color_green + "\">" + "OK" +
                         "</font></B>";
            }
            else
            {
                to_set =
                        to_set + "<br>" + nl + nl + nl + "<B><font color = \"FF0000\">" + "**invalid**" + "</font></B>";
            }

            text_pushurl.setText(to_set);
        }
        else
        {
            text_pushurl.setText("");
        }

        text_pushurl.setEditable(false);
        text_pushurl.setBorder(
                BorderFactory.createCompoundBorder(line_border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        text_relay.setText(friend_relay_pubkey);
        text_relay.setLineWrap(true);
        text_relay.setColumns(35);
        text_relay.setEditable(false);
        text_relay.setBorder(
                BorderFactory.createCompoundBorder(line_border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panel_tox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                             lo.getString("settings_category_001")));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        try
        {
            final BufferedImage img = ImageIO.read(new File(fl.avatar_pathname + File.separator + fl.avatar_filename));
            final ImageIcon icon = new ImageIcon(img);
            avatar.setIcon(icon);
        }
        catch (Exception e)
        {
            final BufferedImage img = new BufferedImage(1, 1, TYPE_INT_ARGB);
            img.setRGB(0, 0, SEE_THRU.getRGB());
            final ImageIcon icon = new ImageIcon(img);
            avatar.setIcon(icon);
        }

        avatar.setBackground(Color.ORANGE);
        avatar.repaint();

        main_panel.add(panel_avatar);
        main_panel.add(panel_tox);

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.add(main_panel);
        scrollPane1.setViewportView(main_panel);
        add(scrollPane1);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.isVisible();

        text_pushurl.setSize(new Dimension(new Dimension(text_name.getWidth(), 100)));
        text_pushurl.setPreferredSize(
                new Dimension(new Dimension(text_name.getWidth(), text_pushurl.getPreferredSize().height)));
        text_pushurl.revalidate();
    }
}
