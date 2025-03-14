package fi.dy.masa.tweakeroo.gui.widgets;

import java.util.Collection;

import com.mojang.blaze3d.systems.RenderSystem;

import javax.annotation.Nullable;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntrySortable;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.gui.Icons;
import fi.dy.masa.tweakeroo.items.ItemList;
import fi.dy.masa.tweakeroo.items.ItemList.SortCriteria;
import fi.dy.masa.tweakeroo.items.ItemListEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class WidgetItemListEntry extends WidgetListEntrySortable<ItemListEntry>
{
    private static final String[] HEADERS = new String[] {
            "tweakeroo.gui.label.item_list.title.item",
            "tweakeroo.gui.label.item_list.title.total",
            "tweakeroo.gui.label.item_list.title.boxes",
            "tweakeroo.gui.label.item_list.title.containers" };
    private static int maxNameLength;
    private static int maxCountLength1;
    private static int maxCountLength2;
    private static int maxCountLength3;

    private final ItemList itemList;
    private final WidgetListItemList listWidget;
    @Nullable private final ItemListEntry entry;
    @Nullable private final String header1;
    @Nullable private final String header2;
    @Nullable private final String header3;
    @Nullable private final String header4;
    private final String shulkerBoxAbbr;
    private final boolean isOdd;

    public WidgetItemListEntry(int x, int y, int width, int height, boolean isOdd,
            ItemList itemList, @Nullable ItemListEntry entry, int listIndex, WidgetListItemList listWidget)
    {
        super(x, y, width, height, entry, listIndex);

        this.columnCount = 4;
        this.entry = entry;
        this.isOdd = isOdd;
        this.listWidget = listWidget;
        this.itemList = itemList;
     
        this.shulkerBoxAbbr = StringUtils.translate("tweakeroo.gui.label.item_list.abbr.shulker_box");

        if (this.entry != null)
        {
            this.header1 = null;
            this.header2 = null;
            this.header3 = null;
            this.header4 = null;
        }
        else
        {
            this.header1 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[0]) + GuiBase.TXT_RST;
            this.header2 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[1]) + GuiBase.TXT_RST;
            this.header3 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[2]) + GuiBase.TXT_RST;
            this.header4 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[3]) + GuiBase.TXT_RST;
        }
    }

    public static void setMaxNameLength(Collection<ItemListEntry> items)
    {
        maxNameLength   = StringUtils.getStringWidth(GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[0]) + GuiBase.TXT_RST);
        maxCountLength1 = StringUtils.getStringWidth(GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[1]) + GuiBase.TXT_RST);
        maxCountLength2 = StringUtils.getStringWidth(GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[2]) + GuiBase.TXT_RST);
        maxCountLength3 = StringUtils.getStringWidth(GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[3]) + GuiBase.TXT_RST);

        for (ItemListEntry entry : items)
        {
            int countTotal = entry.getCountTotal();
            int countBoxes = entry.getCountBoxes();
            int countContainer = entry.getCountContainers();

            maxNameLength   = Math.max(maxNameLength,   StringUtils.getStringWidth(entry.getItemName()));
            maxCountLength1 = Math.max(maxCountLength1, StringUtils.getStringWidth(String.valueOf(countTotal)));
            maxCountLength2 = Math.max(maxCountLength2, StringUtils.getStringWidth(String.valueOf(countBoxes)));
            maxCountLength3 = Math.max(maxCountLength3, StringUtils.getStringWidth(String.valueOf(countContainer)));
        }
    }

    @Override
    public boolean canSelectAt(int mouseX, int mouseY, int mouseButton)
    {
        return true;
        //return mouseX < this.ignoreButton.getX();
    }

    @Override
    protected int getCurrentSortColumn()
    {
        return this.itemList.getSortCriteria().ordinal();
    }

    @Override
    protected boolean getSortInReverse()
    {
        return this.itemList.getSortInReverse();
    }

    @Override
    protected int getColumnPosX(int column)
    {
        int x1 = this.x + 4;
        int x2 = x1 + maxNameLength + 40; // item icon plus offset
        int x3 = x2 + maxCountLength1 + 20;
        int x4 = x3 + maxCountLength2 + 20;

        switch (column)
        {
            case 0: return x1;
            case 1: return x2;
            case 2: return x3;
            case 3: return x4;
            case 4: return x4 + maxCountLength3 + 20;
            default: return x1;
        }
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
    {
        if (super.onMouseClickedImpl(mouseX, mouseY, mouseButton))
        {
            return true;
        }

        if (this.entry != null)
        {
            if (canSelectAt(mouseX, mouseY, mouseButton)) {
                this.itemList.toggleEntrySelection(this.entry);
                return true;
            }
            return false;
        }

        int column = this.getMouseOverColumn(mouseX, mouseY);

        switch (column)
        {
            case 0:
                this.itemList.setSortCriteria(SortCriteria.NAME);
                break;
            case 1:
                this.itemList.setSortCriteria(SortCriteria.COUNT_TOTAL);
                break;
            case 2:
                this.itemList.setSortCriteria(SortCriteria.COUNT_BOXES);
                break;
            case 3:
                this.itemList.setSortCriteria(SortCriteria.COUNT_CONTAINERS);
                break;
            default:
                return false;
        }

        // Re-create the widgets
        this.listWidget.refreshEntries();

        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, DrawContext drawContext)
    {
        if (this.entry != null) selected = this.itemList.isEntrySelected(this.entry);
        // Draw a lighter background for the hovered and the selected entry
        if (this.header1 == null && (selected || this.isMouseOver(mouseX, mouseY)))
        {
            RenderUtils.drawRect(this.x, this.y, this.width, this.height, 0xA0707070);
        }
        else if (this.isOdd)
        {
            RenderUtils.drawRect(this.x, this.y, this.width, this.height, 0xA0101010);
        }
        // Draw a slightly lighter background for even entries
        else
        {
            RenderUtils.drawRect(this.x, this.y, this.width, this.height, 0xA0303030);
        }

        int x1 = this.getColumnPosX(0);
        int x2 = this.getColumnPosX(1);
        int x3 = this.getColumnPosX(2);
        int x4 = this.getColumnPosX(3);
        int y = this.y + 7;
        int color = 0xFFFFFFFF;

        if (this.header1 != null)
        {
            if (this.listWidget.getSearchBarWidget().isSearchOpen() == false)
            {
                this.drawString(x1, y, color, this.header1, drawContext);
                this.drawString(x2, y, color, this.header2, drawContext);
                this.drawString(x3, y, color, this.header3, drawContext);
                this.drawString(x4, y, color, this.header4, drawContext);

                this.renderColumnHeader(mouseX, mouseY, Icons.ARROW_DOWN, Icons.ARROW_UP);
            }
        }
        else if (this.entry != null)
        {
            int countTotal = this.entry.getCountTotal();
            int countBoxes = this.entry.getCountBoxes();
            int countContainers = this.entry.getCountContainers();
            this.drawString(x1 + 20, y, color, this.entry.getItemName(), drawContext);

            this.drawString(x2, y, color, String.valueOf(countTotal), drawContext);

            this.drawString(x3, y, color, String.valueOf(countBoxes), drawContext);

            this.drawString(x4, y, color, String.valueOf(countContainers), drawContext);

            drawContext.getMatrices().push();
            //RenderSystem.disableLighting();
            RenderUtils.enableDiffuseLightingGui3D();

            //mc.getRenderItem().zLevel -= 110;
            y = this.y + 3;
            RenderUtils.drawRect(x1, y, 16, 16, 0x20FFFFFF); // light background for the item
            drawContext.drawItem(this.entry.getStack(), x1, y);
            //mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, this.entry.getStack(), x1, y, null);
            //mc.getRenderItem().zLevel += 110;

            RenderSystem.disableBlend();
            RenderUtils.disableDiffuseLighting();
            drawContext.getMatrices().pop();

            super.render(mouseX, mouseY, selected, drawContext);
        }
    }

    @Override
    public void postRenderHovered(int mouseX, int mouseY, boolean selected, DrawContext drawContext)
    {
        if (this.entry != null)
        {
            MatrixStack matrixStack = drawContext.getMatrices();
            matrixStack.push();
            matrixStack.translate(0, 0, 200);

            String header1 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[0]);
            String header2 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[1]);
            String header3 = GuiBase.TXT_BOLD + StringUtils.translate(HEADERS[2]);

            ItemStack stack = this.entry.getStack();
            String stackName = stack.getName().getString();
            int total = this.entry.getCountTotal();
            int boxes = this.entry.getCountBoxes();
            String strCountTotal = this.getFormattedCountString(total, stack.getMaxCount());
            String strCountBoxes = boxes + " boxes containing " + entry.getCountItemsInBoxes() + " items";

            int w1 = Math.max(this.getStringWidth(header1)       , Math.max(this.getStringWidth(header2)      , this.getStringWidth(header3)));
            int w2 = Math.max(this.getStringWidth(stackName) + 20, Math.max(this.getStringWidth(strCountTotal), this.getStringWidth(strCountBoxes)));
            int totalWidth = w1 + w2 + 60;

            int x = mouseX + 10;
            int y = mouseY - 10;

            if (x + totalWidth - 20 >= this.width)
            {
                x -= totalWidth + 20;
            }

            int x1 = x + 10;
            int x2 = x1 + w1 + 20;

            RenderUtils.drawOutlinedBox(x, y, totalWidth, 60, 0xFF000000, GuiBase.COLOR_HORIZONTAL_BAR);
            y += 6;
            int y1 = y;
            y += 4;

            this.drawString(x1     , y, 0xFFFFFFFF, header1  , drawContext);
            this.drawString(x2 + 20, y, 0xFFFFFFFF, stackName, drawContext);
            y += 16;

            this.drawString(x1, y, 0xFFFFFFFF, header2      , drawContext);
            this.drawString(x2, y, 0xFFFFFFFF, strCountTotal, drawContext);
            y += 16;

            this.drawString(x1, y, 0xFFFFFFFF, header3        , drawContext);
            this.drawString(x2, y, 0xFFFFFFFF, strCountBoxes, drawContext);

            RenderUtils.drawRect(x2, y1, 16, 16, 0x20FFFFFF); // light background for the item

            //TODO: RenderSystem.disableLighting();
            RenderUtils.enableDiffuseLightingGui3D();

            //mc.getRenderItem().zLevel += 100;
            drawContext.drawItem(stack, x2, y1);
            //mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, stack, x1, y, null);
            //mc.getRenderItem().zLevel -= 100;
            //RenderSystem.disableBlend();

            RenderUtils.disableDiffuseLighting();
            matrixStack.pop();
        }
    }

    private String getFormattedCountString(int total, int maxStackSize)
    {
        int stacks = total / maxStackSize;
        int remainder = total % maxStackSize;
        double boxCount = (double) total / (27D * maxStackSize);
        String strCount;

        if (total > maxStackSize)
        {
            if (maxStackSize > 1)
            {
                if (remainder > 0)
                {
                    strCount = String.format("%d = %d x %d + %d = %.2f %s", total, stacks, maxStackSize, remainder, boxCount, this.shulkerBoxAbbr);
                }
                else
                {
                    strCount = String.format("%d = %d x %d = %.2f %s", total, stacks, maxStackSize, boxCount, this.shulkerBoxAbbr);
                }
            }
            else
            {
                strCount = String.format("%d = %.2f %s", total, boxCount, this.shulkerBoxAbbr);
            }
        }
        else
        {
            strCount = String.format("%d", total);
        }

        return strCount;
    }
}