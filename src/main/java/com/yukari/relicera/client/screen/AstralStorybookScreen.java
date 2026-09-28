package com.yukari.relicera.client.screen;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.item.AstralStorybookItem;
import com.yukari.relicera.client.toast.ReliceraToast;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class AstralStorybookScreen extends Screen {
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/gui/astral_storybook.png"
    );
    private static final Component EMPTY_STORY = Component.translatable("screen.relicera.astral_storybook.empty");
    private static final PageLine BLANK_LINE = new PageLine(Component.empty().getVisualOrderText(), 0, 0);
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    private static final int TEXT_X_OFFSET = 36;
    private static final int TEXT_Y_OFFSET = 34;
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 124;
    private static final int PAGE_INDICATOR_Y_OFFSET = 16;
    private static final int FORWARD_BUTTON_X_OFFSET = 116;
    private static final int BACK_BUTTON_X_OFFSET = 43;
    private static final int PAGE_BUTTON_Y_OFFSET = 157;
    private static final int DONE_BUTTON_Y_OFFSET = 194;
    private static final int DONE_BUTTON_HEIGHT = 20;
    private static final int GROUP_HEIGHT = DONE_BUTTON_Y_OFFSET + DONE_BUTTON_HEIGHT;
    private static final int VERTICAL_OFFSET = 6;
    private static final int FADE_IN_MILLIS = 600;
    private static final int FADE_OUT_MILLIS = 400;
    private static final int PARAGRAPH_DELAY_MILLIS = 150;
    private static final int MAX_PARAGRAPH_DELAY_MILLIS = 400;
    private static final float PAGE_SLIDE_DISTANCE = 3.0F;
    private static final int TEXT_COLOR = 0x5D3A72;

    private final AstralStorybookItem.StoryDefinition story;
    private final ItemStack storybook;
    private final long animationStartedAt = Util.getNanos();
    private List<List<PageLine>> pages = List.of();
    private int bookLeft;
    private int bookTop;
    private int currentPage;
    private int pendingPage = -1;
    private long transitionStartedAt;
    private int turnDirection;
    private Transition transition = Transition.FADING_IN;
    private StorybookPageButton forwardButton;
    private StorybookPageButton backButton;
    private boolean toastShown;

    private AstralStorybookScreen(ItemStack storybook) {
        super(storybook.isEmpty() ? GameNarrator.NO_TITLE : storybook.getHoverName());
        this.storybook = storybook.copy();
        this.story = AstralStorybookItem.getStoryDefinition(AstralStorybookItem.getStoryId(storybook)).orElse(null);
    }

    public static void open(ItemStack storybook) {
        Minecraft.getInstance().setScreen(new AstralStorybookScreen(storybook.copy()));
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.story != null && !this.toastShown) {
            this.toastShown = true;
            ReliceraToast.show(
                    Component.translatable("toast.relicera.astral_storybook_read.title"),
                    Component.translatable("toast.relicera.astral_storybook_read.description"),
                    this.storybook
            );
        }
    }

    @Override
    protected void init() {
        this.bookLeft = (this.width - IMAGE_WIDTH) / 2;
        int maximumTop = Math.max(2, this.height - GROUP_HEIGHT);
        int centeredTop = (this.height - GROUP_HEIGHT) / 2 + VERTICAL_OFFSET;
        this.bookTop = Mth.clamp(centeredTop, 2, maximumTop);
        rebuildPages();
        // Resizing may rebuild pagination while a turn is pending; restart from the clamped page.
        this.pendingPage = -1;
        this.transition = Transition.FADING_IN;
        this.transitionStartedAt = Util.getNanos();
        this.turnDirection = 0;

        this.forwardButton = addRenderableWidget(new StorybookPageButton(
                this.bookLeft + FORWARD_BUTTON_X_OFFSET,
                this.bookTop + PAGE_BUTTON_Y_OFFSET,
                true,
                button -> beginPageChange(this.currentPage + 1)
        ));
        this.backButton = addRenderableWidget(new StorybookPageButton(
                this.bookLeft + BACK_BUTTON_X_OFFSET,
                this.bookTop + PAGE_BUTTON_Y_OFFSET,
                false,
                button -> beginPageChange(this.currentPage - 1)
        ));

        int buttonWidth = Math.min(200, Math.max(20, this.width - 20));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds((this.width - buttonWidth) / 2, this.bookTop + DONE_BUTTON_Y_OFFSET, buttonWidth, DONE_BUTTON_HEIGHT)
                .build());
        updatePageButtonVisibility();
    }

    private void updateTransition(long now) {
        if (this.transition == Transition.STABLE) {
            return;
        }

        if (this.transition == Transition.FADING_OUT && getTransitionMillis(now) >= FADE_OUT_MILLIS) {
            this.currentPage = this.pendingPage;
            this.pendingPage = -1;
            this.transition = Transition.FADING_IN;
            // Carry the frame's overshoot into the next phase instead of restarting its clock.
            this.transitionStartedAt += FADE_OUT_MILLIS * 1_000_000L;
        }
        if (this.transition == Transition.FADING_IN && getTransitionMillis(now) >= getFadeInDuration()) {
            this.transition = Transition.STABLE;
        }
        updatePageButtonVisibility();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 266 && this.backButton.visible) {
            this.backButton.playDownSound(this.minecraft.getSoundManager());
            this.backButton.onPress();
            return true;
        }
        if (keyCode == 267 && this.forwardButton.visible) {
            this.forwardButton.playDownSound(this.minecraft.getSoundManager());
            this.forwardButton.onPress();
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // A paused singleplayer screen receives a frozen partialTick; use the render-time clock.
        long now = Util.getNanos();
        updateTransition(now);
        renderBackground(guiGraphics);
        guiGraphics.blit(BOOK_TEXTURE, this.bookLeft, this.bookTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        renderPage(guiGraphics, now);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPage(GuiGraphics guiGraphics, long now) {
        if (this.pages.isEmpty()) {
            return;
        }

        double elapsedMillis = getTransitionMillis(now);
        double timeSeconds = (now - this.animationStartedAt) / 1_000_000_000.0D;
        float wave = (float) Math.sin(timeSeconds * 1.5D);
        float transitionAlpha = getTransitionAlpha(elapsedMillis, 0);
        float breathingAlpha = 0.86F + 0.14F * (0.5F + 0.5F * wave);
        List<PageLine> lines = this.pages.get(this.currentPage);
        int baseY = this.bookTop + TEXT_Y_OFFSET;
        if (this.story == null) {
            baseY += (TEXT_HEIGHT - lines.size() * this.font.lineHeight) / 2;
        }
        float floatingOffset = wave * 1.5F;
        float slideOffset = this.turnDirection * PAGE_SLIDE_DISTANCE * (1.0F - transitionAlpha);
        if (this.transition == Transition.FADING_OUT) {
            slideOffset = -slideOffset;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(slideOffset, floatingOffset, 0.0F);
        for (int index = 0; index < lines.size(); index++) {
            PageLine line = lines.get(index);
            float alpha = getTransitionAlpha(elapsedMillis, line.paragraphIndex()) * breathingAlpha;
            if (line == BLANK_LINE || alpha < 0.02F) {
                continue;
            }
            int color = Mth.floor(alpha * 255.0F) << 24 | TEXT_COLOR;
            int x = this.bookLeft + TEXT_X_OFFSET + (TEXT_WIDTH - line.width()) / 2;
            guiGraphics.drawString(this.font, line.text(), x, baseY + index * this.font.lineHeight, color, false);
        }
        guiGraphics.pose().popPose();

        if (this.story != null && this.pages.size() > 1 && transitionAlpha >= 0.02F) {
            Component pageIndicator = Component.translatable(
                    "book.pageIndicator", this.currentPage + 1, this.pages.size()
            );
            int indicatorWidth = this.font.width(pageIndicator);
            guiGraphics.drawString(
                    this.font,
                    pageIndicator,
                    this.bookLeft + IMAGE_WIDTH - 44 - indicatorWidth,
                    this.bookTop + PAGE_INDICATOR_Y_OFFSET,
                    Mth.floor(transitionAlpha * 255.0F) << 24,
                    false
            );
        }
    }

    private void rebuildPages() {
        List<Component> paragraphs = this.story == null ? List.of(EMPTY_STORY) : this.story.paragraphs();
        int maximumLines = Math.max(1, TEXT_HEIGHT / this.font.lineHeight);
        List<List<PageLine>> builtPages = new ArrayList<>();
        List<PageLine> currentLines = new ArrayList<>();
        int paragraphIndex = 0;
        for (Component paragraph : paragraphs) {
            List<FormattedCharSequence> paragraphLines = this.font.split(paragraph, TEXT_WIDTH);
            if (paragraphLines.isEmpty()) {
                continue;
            }
            if (!currentLines.isEmpty()) {
                int minimumParagraphStart = 1 + Math.min(2, paragraphLines.size());
                if (maximumLines - currentLines.size() < minimumParagraphStart) {
                    builtPages.add(List.copyOf(currentLines));
                    currentLines.clear();
                    paragraphIndex = 0;
                } else {
                    currentLines.add(BLANK_LINE);
                }
            }

            for (FormattedCharSequence line : paragraphLines) {
                if (currentLines.size() >= maximumLines) {
                    builtPages.add(List.copyOf(currentLines));
                    currentLines.clear();
                    paragraphIndex = 0;
                }
                currentLines.add(new PageLine(line, this.font.width(line), paragraphIndex));
            }
            paragraphIndex++;
        }
        if (!currentLines.isEmpty()) {
            builtPages.add(List.copyOf(currentLines));
        }
        if (builtPages.isEmpty()) {
            builtPages.add(List.of(BLANK_LINE));
        }

        this.pages = List.copyOf(builtPages);
        this.currentPage = Mth.clamp(this.currentPage, 0, this.pages.size() - 1);
    }

    private void beginPageChange(int targetPage) {
        if (this.transition != Transition.STABLE
                || targetPage < 0
                || targetPage >= this.pages.size()
                || targetPage == this.currentPage) {
            return;
        }
        this.pendingPage = targetPage;
        this.turnDirection = Integer.compare(targetPage, this.currentPage);
        this.transition = Transition.FADING_OUT;
        this.transitionStartedAt = Util.getNanos();
        updatePageButtonVisibility();
    }

    private int getFadeInDuration() {
        List<PageLine> lines = this.pages.get(this.currentPage);
        return FADE_IN_MILLIS + getParagraphDelay(lines.get(lines.size() - 1).paragraphIndex());
    }

    private static int getParagraphDelay(int paragraphIndex) {
        return Math.min(paragraphIndex * PARAGRAPH_DELAY_MILLIS, MAX_PARAGRAPH_DELAY_MILLIS);
    }

    private double getTransitionMillis(long now) {
        return (now - this.transitionStartedAt) / 1_000_000.0D;
    }

    private float getTransitionAlpha(double elapsedMillis, int paragraphIndex) {
        if (this.transition == Transition.STABLE) {
            return 1.0F;
        }
        float duration = this.transition == Transition.FADING_OUT ? FADE_OUT_MILLIS : FADE_IN_MILLIS;
        int delay = this.transition == Transition.FADING_IN ? getParagraphDelay(paragraphIndex) : 0;
        float progress = Mth.clamp((float) ((elapsedMillis - delay) / duration), 0.0F, 1.0F);
        float easedProgress = progress * progress * (3.0F - 2.0F * progress);
        return this.transition == Transition.FADING_OUT ? 1.0F - easedProgress : easedProgress;
    }

    private void updatePageButtonVisibility() {
        if (this.forwardButton == null || this.backButton == null) {
            return;
        }
        boolean canTurnPage = this.transition == Transition.STABLE && this.pages.size() > 1;
        this.forwardButton.visible = canTurnPage && this.currentPage < this.pages.size() - 1;
        this.backButton.visible = canTurnPage && this.currentPage > 0;
    }

    private enum Transition {
        FADING_IN,
        STABLE,
        FADING_OUT
    }

    private record PageLine(FormattedCharSequence text, int width, int paragraphIndex) {
    }

    private static final class StorybookPageButton extends Button {
        private final boolean forward;

        private StorybookPageButton(int x, int y, boolean forward, OnPress onPress) {
            super(x, y, 23, 13, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.forward = forward;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int textureX = isHoveredOrFocused() ? 23 : 0;
            int textureY = this.forward ? 192 : 205;
            guiGraphics.blit(BOOK_TEXTURE, getX(), getY(), textureX, textureY, 23, 13);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }
}
