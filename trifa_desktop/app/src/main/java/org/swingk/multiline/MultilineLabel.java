package org.swingk.multiline;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Scrollable;

import static org.swingk.multiline.MultilineUtils.toDimension;

/**
 * Component to display a plain text (possibly on multiple lines).
 * <p>
 * Operates in 2 modes:
 * <ol>
 *     <li>When the label text doesn't contain line separators, then the label dynamically decides how to break it
 *     into multiple lines according to available space.</li>
 *     <li>When the label text contains line separators, it is broken into lines according to them.</li>
 * </ol>
 * </p>
 */
public class MultilineLabel extends JComponent implements Scrollable
{

    /**
     * Default preferred width limit in pixels.
     */
    public static final int DEFAULT_WIDTH_LIMIT = 500;

    public static final float DEFAULT_LINE_SPACING = 1.1f;

    public static final int DEFAULT_PREFERRED_VIEWPORT_LINE_COUNT = 20;

    /**
     * @param insets      Insets to include in the calculation. Not null.
     * @param fm          {@link FontMetrics} to calculate text size. Not null.
     * @param text        Text to calculate preferred size for. Not null.
     * @param wLimit      Positive width limit in pixels (incl. insets). Applicable only if the text doesn't
     *                    contain line separators.
     * @param lineSpacing Distance between two adjacent baselines will be the font height (as returned
     *                    by {@link FontMetrics#getHeight()}) multiplied by this value.
     * @return Preferred size of text bounds.
     */
    public static Dimension calculatePreferredSize(JComponent c, Insets insets, FontMetrics fm, String text, int wLimit, float lineSpacing)
    {
        return ProvidedTextLayout.hasLines(text) ? ProvidedTextLayout.calcPreferredSize(c, text, fm, insets,
                                                                                        lineSpacing) : WidthTextLayout.calcPreferredSize(
                c, insets, fm, text, wLimit, lineSpacing);
    }

    /**
     * Paints the specified text on the provided {@link Graphics} object.
     *
     * @param c           Component. If not null, its AA settings will be used for the painting.
     * @param g           Graphics to paint the text. Must be preconfigured with font, color and AA hints. Not null.
     * @param text        Text to paint. Not null.
     * @param insets      Insets to leave around the painted text. Not null.
     * @param wLimit      Positive width limit in pixels (incl. insets). Applicable only if the text doesn't
     *                    contain line separators as defined in {@link String#lines()} method.
     * @param enabled     If false - paint disabled text.
     * @param background  Background color of the target component. Used to paint disabled text. Not null.
     * @param lineSpacing Distance between two adjacent baselines will be the font height (as returned
     *                    by {@link FontMetrics#getHeight()}) multiplied by this value.
     */
    public static void paintText(JComponent c, Graphics g, String text, Insets insets, int wLimit, boolean enabled, Color background, float lineSpacing)
    {
        assert g != null;
        assert text != null;
        assert insets != null;
        assert background != null;
        if (ProvidedTextLayout.hasLines(text))
        {
            ProvidedTextLayout.paintText(c, g, text, insets, enabled, background, lineSpacing);
        }
        else
        {
            WidthTextLayout.paintText(c, g, text, insets, wLimit, enabled, background, lineSpacing);
        }
    }

    private String text = "";
    private TextLayout textLayout; // not null after constructor
    private int prefWidthLimit = DEFAULT_WIDTH_LIMIT;
    private boolean useCurrentWidthForPreferredSize = true;
    private float lineSpacing = DEFAULT_LINE_SPACING;
    private int preferredViewportLineCount = DEFAULT_PREFERRED_VIEWPORT_LINE_COUNT;

    /**
     * Default constructor.
     */
    public MultilineLabel()
    {
        this("");
    }

    /**
     * Constructor.
     *
     * @param text The text to be displayed by the label. Not null.
     */
    public MultilineLabel(String text)
    {
        super();
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(true);
        updateUI();
        setTextAndTextLayout(text);
    }

    protected Clipboard getClipboard()
    {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Transfers the text of this label to the system clipboard.
     */
    public void copy()
    {
        getClipboard().setContents(new StringSelection(getText()), null);
    }

    @Override
    public void updateUI()
    {
        setUI(new MultilineLabelUI());
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        textLayout.preSetBounds(x, y, width, height);
        super.setBounds(x, y, width, height);
    }

    @Override
    public Dimension getPreferredSize()
    {
        if (isPreferredSizeSet())
        {
            return super.getPreferredSize();
        }
        return textLayout.calculatePreferredSize();
    }

    @Override
    public Dimension getMinimumSize()
    {
        if (isMinimumSizeSet())
        {
            return super.getMinimumSize();
        }
        return toDimension(0, 0, getInsets());
    }

    @Override
    public Dimension getMaximumSize()
    {
        if (isMaximumSizeSet())
        {
            return super.getMaximumSize();
        }
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * @return Text of this label. The actually displayed text may differ from this value - multiple adjacent spaces
     * may collapsed into one space, text may trimmed, EOL may be inserted, etc.
     */
    public String getText()
    {
        return text;
    }

    /**
     * @param text Text of this label. Not null. The actually displayed text may differ from this value - multiple
     *             adjacent spaces may collapsed into one space, text may trimmed, EOL may be inserted, etc.
     *             Fires change event for property "text".
     */
    public void setText(String text)
    {
        setTextAndTextLayout(text);
        revalidate();
        repaint();
    }

    private void setTextAndTextLayout(String text)
    {
        this.text = Objects.requireNonNull(text);
        this.textLayout = createTextLayout();
    }

    /**
     * @return True is the label displays its text according to the preferred/current width, false if the line breaks
     * are predefined by line separators in the text and the label width is ignored.
     * @see #setPreferredWidthLimit(int)
     */
    public boolean isWidthBasedLayout()
    {
        assert textLayout != null;
        return textLayout instanceof WidthTextLayout;
    }

    protected TextLayout createTextLayout()
    {
        return ProvidedTextLayout.hasLines(getText()) ? new ProvidedTextLayout(this) : new WidthTextLayout(this);
    }

    protected TextLayout getTextLayout()
    {
        return textLayout;
    }

    /**
     * Note: This property is ignored if the text contains line separators.
     *
     * @return A limit on a preferred width value to use for preferred size calculation.
     * @see #DEFAULT_WIDTH_LIMIT
     */
    public int getPreferredWidthLimit()
    {
        return prefWidthLimit;
    }

    /**
     * @param prefWidthLimit A limit (in pixels) on a preferred width value to use for preferred size calculation.
     *                       Default: {@link #DEFAULT_WIDTH_LIMIT}.
     * @implSpec This property is ignored if the label text contains line separators.
     * @see #getPreferredSize()
     */
    public void setPreferredWidthLimit(int prefWidthLimit)
    {
        if (prefWidthLimit < 1)
        {
            throw new IllegalArgumentException();
        }
        this.prefWidthLimit = prefWidthLimit;
        revalidate();
        repaint();
    }

    public boolean isUseCurrentWidthForPreferredSize()
    {
        return useCurrentWidthForPreferredSize;
    }

    /**
     * In some cases using current width to calculate the preferred size may produce undesired results.
     * This parameter allows to disable this behavior.
     * See https://stackoverflow.com/questions/39455573/how-to-set-fixed-width-but-dynamic-height-on-jtextpane/39466255#39466255
     *
     * @param useCurrentWidthForPreferredSize If true, the label will calculate its preferred size according
     *                                        to its current width, if false - the current width will be ignored.
     */
    public void setUseCurrentWidthForPreferredSize(boolean useCurrentWidthForPreferredSize)
    {
        this.useCurrentWidthForPreferredSize = useCurrentWidthForPreferredSize;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        Dimension prefSize = getPreferredSize();
        FontMetrics fm = getFontMetrics(getFont());
        int lineCount = getPreferredViewportLineCount();
        int lineHeight = AbstractTextLayout.getTextPreferredHeight(lineCount, fm, getLineSpacing());
        Insets insets = getInsets();
        return new Dimension(prefSize.width, Math.min(prefSize.height, lineHeight + insets.top + insets.bottom));
    }

    /**
     * @return Preferred number of lines to calculate height of {@link Dimension} returned by
     * {@link #getPreferredScrollableViewportSize()}.
     */
    public int getPreferredViewportLineCount()
    {
        return preferredViewportLineCount;
    }

    /**
     * @param preferredViewportLineCount Preferred number of lines to calculate the height of
     *                                   {@link Dimension} returned by {@link #getPreferredScrollableViewportSize()}.
     * @see #DEFAULT_PREFERRED_VIEWPORT_LINE_COUNT
     */
    public void setPreferredViewportLineCount(int preferredViewportLineCount)
    {
        if (preferredViewportLineCount < 1)
        {
            throw new IllegalArgumentException();
        }
        this.preferredViewportLineCount = preferredViewportLineCount;
        revalidate();
        repaint();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 20;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return true; // no horizontal scroll bar
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return false; // vertical scroll bar is OK
    }

    /**
     * @return Distance between two adjacent baselines is the font height (as returned
     * by {@link FontMetrics#getHeight()}) multiplied by this value.
     */
    public float getLineSpacing()
    {
        return lineSpacing;
    }

    /**
     * @param lineSpacing Distance between two adjacent baselines will be the font height (as returned
     *                    by {@link FontMetrics#getHeight()}) multiplied by this value.
     *                    <p>
     *                    Fires {@link java.beans.PropertyChangeEvent} for {@code "lineSpacing"} property.
     */
    public void setLineSpacing(float lineSpacing)
    {
        if (lineSpacing <= 0.0f)
        {
            throw new IllegalArgumentException("Line spacing must be positive.");
        }
        this.lineSpacing = lineSpacing;
        revalidate();
        repaint();
    }

    @Override
    protected String paramString()
    {
        return super.paramString() + ",prefWidthLimit=" + prefWidthLimit + ",useCurrentWidthForPreferredSize=" +
               useCurrentWidthForPreferredSize + ",textLayout=" + textLayout + ",lineSpacing=" + lineSpacing +
               ",textLength=" + text.length();
    }
}
