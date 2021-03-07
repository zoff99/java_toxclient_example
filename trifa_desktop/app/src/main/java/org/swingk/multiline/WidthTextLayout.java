package org.swingk.multiline;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import static javax.swing.plaf.basic.BasicGraphicsUtils.getStringWidth;
import static org.swingk.multiline.MultilineUtils.toDimension;

/**
 * Dynamically calculates line breaks based on value of {@link MultilineLabel#getPreferredScrollableViewportSize()}
 * or the current label width. Ignores line breaks in text by replacing them with spaces.
 */
final class WidthTextLayout extends AbstractTextLayout {

    static void paintText(JComponent c, Graphics g, String text, Insets insets, int wLimit, boolean enabled,
                          Color background, float lineSpacing) {
        paintText2(c, g, toRenderedText(text), insets, wLimit, enabled, background, lineSpacing);
    }

    private static void paintText2(JComponent c, Graphics g, String text, Insets insets, int wLimit, boolean enabled,
                                   Color background, float lineSpacing) {
        if (text.isEmpty()) {
            return;
        }
        final int wLimitText = wLimit - insets.right - insets.left;
        if (wLimitText < 1) {
            return;
        }
        final var fm = g.getFontMetrics();
        final int x = insets.left;
        int y = insets.top + fm.getAscent();
        final int yIncrement = MultilineUtils.getHeightIncrement(fm, lineSpacing);
        NextLine nextLine;
        int index = 0;
        do {
            nextLine = getNextLine(c, text, index, fm, wLimitText);
            String lineStr = text.substring(nextLine.lineStartIndex, nextLine.lineEndIndex + 1);
            if (enabled) {
                drawString(c, g, lineStr, x, y);
            } else {
                drawStringInDisabledStyle(c, lineStr, g, background, x, y);
            }
            y += yIncrement;
            index = nextLine.nextLineStartIndex;
        } while (!nextLine.lastLine);
    }

    static Dimension calcPreferredSize(JComponent c, Insets insets, FontMetrics fm, String text, int wLimit,
                                       float lineSpacing) {
        return calcPreferredSize2(c, insets, fm, toRenderedText(text), wLimit, lineSpacing);
    }

    private static Dimension calcPreferredSize2(JComponent c, Insets insets, FontMetrics fm, String text, int wLimit,
                                                float lineSpacing) {
        assert insets != null;
        assert fm != null;
        assert text != null;
        assert wLimit > 0;

        int textPrefWidth;
        int textPrefHeight;
        if (!text.isEmpty()) {
            NextLine nextLine;
            int startIndex = 0;
            final int textWidthLimit = Math.max(1, wLimit - insets.right - insets.left);
            int lineCount = 0;
            int maxLineWidth = 0; // pixels
            do {
                nextLine = getNextLine(c, text, startIndex, fm, textWidthLimit);
                String nextLineStr = text.substring(nextLine.lineStartIndex, nextLine.lineEndIndex + 1);
                int nextLineWidth = Math.round(getStringWidth(c, fm, nextLineStr));
                maxLineWidth = Math.max(maxLineWidth, nextLineWidth);
                lineCount++;
                startIndex = nextLine.nextLineStartIndex;
            } while (!nextLine.lastLine);
            textPrefWidth = maxLineWidth;
            textPrefHeight = getTextPreferredHeight(lineCount, fm, lineSpacing);
        } else {
            textPrefWidth = textPrefHeight = 0;
        }
        return toDimension(textPrefWidth, textPrefHeight, insets);
    }

    /**
     * @param text Text to display in {@link MultilineLabel}.
     * @param startIndex Index of 1st character in the new line.
     * @param fm Current {@link FontMetrics}.
     * @param widthLimit Limit on the width of the line.
     * @return Object with details of the next line.
     */
    static NextLine getNextLine(JComponent c, String text, int startIndex, FontMetrics fm, int widthLimit) {
        assert text != null;
        assert text.length() > 0;
        assert startIndex > -1;
        assert fm != null;
        assert widthLimit > 0;

        int spaceIndex = startIndex;
        while (true) {
            int nextSpaceIndex = text.indexOf(' ', spaceIndex + 1);
            if (nextSpaceIndex == -1) { // there is no next space after spaceIndex
                if (spaceIndex > startIndex && getStringWidth(c, fm, text.substring(startIndex)) > widthLimit) {
                    // next line will be single word last line
                    return new NextLine(false, startIndex, spaceIndex - 1, spaceIndex + 1);
                } else {
                    // last line
                    return new NextLine(true, startIndex, text.length() - 1, -1);
                }
            } else { // there is next space after spaceIndex
                if (getStringWidth(c, fm, text.substring(startIndex, nextSpaceIndex)) > widthLimit) {
                    if (spaceIndex > startIndex) {
                        // regular next line
                        return new NextLine(false, startIndex, spaceIndex - 1, spaceIndex + 1);
                    } else {
                        // single word line
                        return new NextLine(false, startIndex, nextSpaceIndex - 1, nextSpaceIndex + 1);
                    }
                } else {
                    spaceIndex = nextSpaceIndex; // continue with current line
                }
            }
        }
    }

    static class NextLine {
        /**
         * True if this is the last line of the text (end of text).
         */
        public final boolean lastLine;

        /**
         * Index of first character in the line. Inclusive.
         */
        public final int lineStartIndex;

        /**
         * Index of last character in the line. Inclusive.
         */
        public final int lineEndIndex;

        /**
         * Index of first character in the line after that line. Inclusive.
         */
        public final int nextLineStartIndex;

        NextLine(boolean lastLine,
                 int lineStartIndex,
                 int lineEndIndex,
                 int nextLineStartIndex) {
            this.lastLine = lastLine;
            this.lineStartIndex = lineStartIndex;
            this.lineEndIndex = lineEndIndex;
            this.nextLineStartIndex = nextLineStartIndex;
        }

        @Override
        public String toString() {
            return "NextLine{" +
                    "lastLine=" + lastLine +
                    ", lineStartIndex=" + lineStartIndex +
                    ", lineEndIndex=" + lineEndIndex +
                    ", nextLineStartIndex=" + nextLineStartIndex +
                    '}';
        }
    }

    static String toRenderedText(String text) {
        var sb = new StringBuilder(text.replace('\n', ' ').trim());
        int doubleSpaceIndex;
        while ((doubleSpaceIndex = sb.indexOf("  ")) > -1) {
            sb.delete(doubleSpaceIndex + 1, doubleSpaceIndex + 2); // delete second space
        }
        return sb.toString();
    }

    private final String textToRender;

    WidthTextLayout(MultilineLabel label) {
        super(label);
        this.textToRender = toRenderedText(label.getText());
    }

    @Override
    public Dimension calculatePreferredSize() {
        return calcPreferredSize(0);
    }

    protected void requestLayout() {
        SwingUtilities.invokeLater(() -> {
            label.revalidate();
            label.repaint();
        });
    }

    @Override
    public void preSetBounds(int x, int y, int width, int height) {
        if (label.isUseCurrentWidthForPreferredSize()
                && !textToRender.isEmpty()
                && width > 0 && height > 0
                && width != label.getWidth()
                && calcPreferredSize(width).height != height) {
            requestLayout();
        }
    }

    private Dimension calcPreferredSize(int expectedLabelWidth) {
        final int wLimit;
        if (expectedLabelWidth > 0) {
            wLimit = expectedLabelWidth;
        } else if (label.isUseCurrentWidthForPreferredSize() && label.getWidth() > 0) {
            // https://stackoverflow.com/questions/39455573/how-to-set-fixed-width-but-dynamic-height-on-jtextpane/39466255#39466255
            wLimit = label.getWidth();
        } else {
            wLimit = label.getPreferredWidthLimit();
        }
        final var fm = label.getFontMetrics(label.getFont());
        final var insets = label.getInsets();
        return calcPreferredSize2(label, insets, fm, textToRender, wLimit, label.getLineSpacing());
    }

    @Override
    public void paintText(Graphics g) {
        paintText2(label, g, textToRender, label.getInsets(), label.getWidth(), label.isEnabled(),
                label.getBackground(), label.getLineSpacing());
    }
}
