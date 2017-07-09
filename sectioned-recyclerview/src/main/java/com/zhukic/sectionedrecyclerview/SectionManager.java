package com.zhukic.sectionedrecyclerview;

import android.support.annotation.IntRange;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladislav Zhukov (https://github.com/zhukic)
 */

class SectionManager {

    private final List<Section> sections = new ArrayList<>();

    void addSection(Section section) {
        sections.add(section);
    }

    boolean isSectionSubheaderOnPosition(@IntRange(from = 0, to = Integer.MAX_VALUE) int position) {
        for (Section section : sections) {
            if (section.getSubheaderPosition() == position) {
                return true;
            }
        }
        return false;
    }

    boolean isSectionExpanded(@IntRange(from = 0, to = Integer.MAX_VALUE) int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSections().size()) {
            throw new IllegalArgumentException("sectionIndex: " + sectionIndex + ", size: " + getSections().size());
        }
        return sections.get(sectionIndex).isExpanded();
    }

    int expandSection(@IntRange(from = 0, to = Integer.MAX_VALUE) int sectionIndex) {

        final Section sectionToExpand = sections.get(sectionIndex);

        if (sectionToExpand.isExpanded()) {
            return 0;
        }

        sectionToExpand.setExpanded(true);

        for (int i = sectionIndex + 1; i < sections.size(); i++) {
            final Section section = sections.get(i);
            section.setSubheaderPosition(section.getSubheaderPosition() + sectionToExpand.getItemsCount());
        }

        return sectionToExpand.getItemsCount();

    }

    void expandAllSections() {
        for (Section section : sections) {
            expandSection(sections.indexOf(section));
        }
    }

    int collapseSection(@IntRange(from = 0, to = Integer.MAX_VALUE) int sectionIndex) {

        if (sectionIndex < 0 || sectionIndex >= getSections().size()) {
            throw new IllegalArgumentException("sectionIndex: " + sectionIndex + ", size: " + getSections().size());
        }

        final Section sectionToCollapse = sections.get(sectionIndex);

        if (!sectionToCollapse.isExpanded()) {
            return 0;
        }

        sectionToCollapse.setExpanded(false);

        for (int i = sectionIndex + 1; i < sections.size(); i++) {
            final Section section = sections.get(i);
            section.setSubheaderPosition(section.getSubheaderPosition() - sectionToCollapse.getItemsCount());
        }

        return sectionToCollapse.getItemsCount();

    }

    void collapseAllSections() {
        for (Section section : sections) {
            collapseSection(sections.indexOf(section));
        }
    }

    int getItemPositionForSubheaderViewHolder(@IntRange(from = 0, to = Integer.MAX_VALUE) int subheaderPosition) {

        if (subheaderPosition < 0 || subheaderPosition >= getItemCount()) {
            throw new IllegalArgumentException("subheaderPosition: " + subheaderPosition + ", itemCount: " + getItemCount());
        }

        int itemPosition = 0;

        int sectionIndex = sectionIndex(subheaderPosition);

        for (int i = 0; i < sectionIndex; i++) {
            itemPosition += sections.get(i).getItemsCount();
        }

        return itemPosition;

    }

    int getItemPositionForItemViewHolder(@IntRange(from = 0, to = Integer.MAX_VALUE) int itemHolderPosition) {

        if (itemHolderPosition < 0 || itemHolderPosition >= getItemCount()) {
            throw new IllegalArgumentException("itemHolderPosition: " + itemHolderPosition + ", itemCount: " + getItemCount());
        }
        if (isSectionSubheaderOnPosition(itemHolderPosition)) {
            throw new IllegalArgumentException("section subheader is placed at " + itemHolderPosition + " position");
        }

        int sectionIndex = sectionIndex(itemHolderPosition);

        itemHolderPosition -= sectionIndex + 1;

        for (int i = 0; i < sectionIndex; i++) {

            final Section section = sections.get(i);

            if (!section.isExpanded()) {
                itemHolderPosition += section.getItemsCount();
            }

        }

        return itemHolderPosition;

    }

    int getAdapterPositionForItem(int itemPosition) {

        if (itemPosition < 0 || itemPosition >= getDataItemCount()) {
            throw new IllegalArgumentException("itemPosition: " + itemPosition + ", itemCount: " + getDataItemCount());
        }

        int adapterPosition = 0;

        int itemCount = 0;

        for (Section section : sections) {

            adapterPosition += 1;

            if (!section.isExpanded()) {
                adapterPosition -= section.getItemsCount();
            }

            if (itemCount + section.getItemsCount() <= itemPosition) {
                itemCount += section.getItemsCount();
            } else {
                break;
            }

        }

        return adapterPosition + itemPosition;

    }

    boolean removeItem(int itemAdapterPosition) {

        if (itemAdapterPosition < 0 || itemAdapterPosition >= getItemCount()) {
            throw new IllegalArgumentException("itemAdapterPosition: " + itemAdapterPosition + ", itemCount: " + getItemCount());
        }
        if (isSectionSubheaderOnPosition(itemAdapterPosition)) {
            throw new IllegalArgumentException("section subheader is placed at " + itemAdapterPosition + " position");
        }

        final int sectionIndex = sectionIndex(itemAdapterPosition);
        final Section section = sections.get(sectionIndex);

        boolean isSectionRemoved;

        int positionsToDecrease;

        if (section.getItemsCount() == 1) {
            isSectionRemoved = true;
            positionsToDecrease = 2;
        } else {
            isSectionRemoved = false;
            section.setItemsCount(section.getItemsCount() - 1);
            positionsToDecrease = 1;
        }

        if (!section.isExpanded()) {
            if (isSectionRemoved) {
                positionsToDecrease = 1;
            }
        }

        for (int i = sectionIndex + 1; i < sections.size(); i++) {
            final Section section1 = sections.get(i);
            section1.setSubheaderPosition(section1.getSubheaderPosition() - positionsToDecrease);
        }

        if (isSectionRemoved) {
            sections.remove(section);
        }

        return isSectionRemoved;

    }

    void insertItem(int adapterPosition, boolean shouldInsertSection) {

        if (shouldInsertSection) {

            Section newSection = new Section(adapterPosition);
            newSection.setItemsCount(1);

            if (adapterPosition == getItemCount()) {
                sections.add(newSection);
                return;
            }

            int sectionIndex = sectionIndex(adapterPosition);

            for (int i = sectionIndex; i < sections.size(); i++) {
                Section section = getSection(i);
                section.setSubheaderPosition(section.getSubheaderPosition() + 2);
            }

            sections.add(sectionIndex, newSection);

        } else {

            if ((isSectionSubheaderOnPosition(adapterPosition) && adapterPosition != 0) || adapterPosition == getItemCount()) {
                adapterPosition--;
            }

            int currentSectionIndex = sectionIndex(adapterPosition);
            Section currentSection = getSection(currentSectionIndex);
            currentSection.setItemsCount(currentSection.getItemsCount() + 1);

            if (!currentSection.isExpanded()) {
                return;
            }

            for (int i = currentSectionIndex + 1; i < sections.size(); i++) {
                Section section = getSection(i);
                section.setSubheaderPosition(section.getSubheaderPosition() + 1);
            }

        }

    }

    Section getSection(@IntRange(from = 0, to = Integer.MAX_VALUE) int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSections().size()) {
            throw new IllegalArgumentException("sectionIndex: " + sectionIndex + ", size: " + getSections().size());
        }
        return sections.get(sectionIndex);
    }

    Section getLastSection() {
        return sections.get(sections.size() - 1);
    }

    int sectionIndex(@IntRange(from = 0, to = Integer.MAX_VALUE) int adapterPosition) {

        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IllegalArgumentException("adapterPosition: " + adapterPosition + ", itemCount: " + getItemCount());
        }

        int sectionIndex = 0;

        for (Section section : sections) {
            if (adapterPosition == section.getSubheaderPosition()) {
                return sections.indexOf(section);
            } else if (adapterPosition > section.getSubheaderPosition()) {
                sectionIndex = sections.indexOf(section);
            }
        }

        return sectionIndex;

    }

    int getItemCount() {

        int itemCount = 0;

        for (Section section : sections) {
            itemCount += 1;
            if (section.isExpanded()) {
                itemCount += section.getItemsCount();
            }
        }

        return itemCount;

    }

    int getDataItemCount() {
        int dataItemCount = 0;
        for (Section section : sections) {
            dataItemCount += section.getItemsCount();
        }
        return dataItemCount;
    }

    int positionInSection(@IntRange(from = 0, to = Integer.MAX_VALUE) int itemAdapterPosition) {

        if (itemAdapterPosition < 0 || itemAdapterPosition >= getItemCount()) {
            throw new IllegalArgumentException("itemAdapterPosition: " + itemAdapterPosition + ", itemCount: " + getItemCount());
        }
        if (isSectionSubheaderOnPosition(itemAdapterPosition)) {
            throw new IllegalArgumentException("section subheader is placed at " + itemAdapterPosition + " position");
        }

        final Section section = getSection(sectionIndex(itemAdapterPosition));

        return itemAdapterPosition - section.getSubheaderPosition() - 1;

    }

    int sectionSize(int sectionIndex) {
        return getSection(sectionIndex).getItemsCount();
    }

    int getSectionCount() {
        return sections.size();
    }

    List<Section> getSections() {
        return sections;
    }

    void clear() {
        getSections().clear();
    }

}
