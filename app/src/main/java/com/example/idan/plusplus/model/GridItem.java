package com.example.idan.plusplus.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GridItem implements Parcelable {

    public long id;
    public String type;
    public final String channelTag;
    public String category;
    public String title;
    public int sortOrder;
    public int level;
    public String description;
    public String linkUrl;
    public String videoUrl;
    public String imageUrl;
    public String studio;
    public boolean isPlayable;
    public String tag;


    private GridItem (
            final long id,
            final String type,
            final String channelTag,
            final String category,
            final String title,
            final int sortOrder,
            final int level,
            final String description,
            final String linkUrl,
            final String videoUrl,
            final String imageUrl,
            final String studio,
            final boolean isPlayable,
            final String tag
    ) {
        this.id = id;
        this.type = type;
        this.channelTag = channelTag;
        this.category = category;
        this.title = title;
        this.sortOrder = sortOrder;
        this.level = level;
        this.description =description;
        this.linkUrl = linkUrl;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.studio = studio;
        this.isPlayable = isPlayable;
        this.tag = tag;
    }


    protected GridItem(Parcel in) {
        this.id = in.readLong();
        this.type = in.readString();
        this.channelTag = in.readString();
        this.category = in.readString();
        this.title = in.readString();
        this.sortOrder = in.readInt();
        this.level = in.readInt();
        this.description =in.readString();
        this.linkUrl = in.readString();
        this.videoUrl = in.readString();
        this.imageUrl = in.readString();
        this.studio = in.readString();
        this.isPlayable = in.readByte() == 1 ? true : false;
        this.tag = in.readString();
    }

    public static final Creator<GridItem> CREATOR = new Creator<GridItem>() {
        @Override
        public GridItem createFromParcel(Parcel in) {
            return new GridItem(in);
        }

        @Override
        public GridItem[] newArray(int size) {
            return new GridItem[size];
        }
    };


    @Override
    public boolean equals(Object m) {
        return m instanceof GridItem && id == ((GridItem) m).id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(type);
            dest.writeString(channelTag);
            dest.writeString(category);
            dest.writeString(title);
            dest.writeInt(sortOrder);
            dest.writeInt(level);
            dest.writeString(description);
            dest.writeString(linkUrl);
            dest.writeString(videoUrl);
            dest.writeString(imageUrl);
            dest.writeString(studio);
            dest.writeByte(isPlayable ? (byte)1 : (byte)0);
            dest.writeString(tag);
    }

    @Override
    public String toString() {
        return new com.google.gson.Gson().toJson(this);
    }

    public static class GridItemBuilder {
        private long id;
        private String type;
        private String channelTag;
        private String category;
        private String title;
        private int sortOrder;
        private int level;
        private String description;
        private String linkUrl;
        private String videoUrl;
        private String imageUrl;
        private String studio;
        private boolean isPlayable;
        private String tag;


        public GridItemBuilder Id(long id) {
            this.id = id;
            return  this;
        }
        public GridItemBuilder Type(String type) {
            this.type = type;
            return  this;
        }
        public GridItemBuilder ChannelTag(String channelTag) {
            this.channelTag = channelTag;
            return  this;
        }
        public GridItemBuilder Category(String category) {
            this.category = category;
            return  this;
        }
        public GridItemBuilder Title(String title) {
            this.title = title;
            return  this;
        }
        public GridItemBuilder SortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
            return  this;
        }
        public GridItemBuilder Level(int level) {
            this.level = level;
            return  this;
        }
        public GridItemBuilder Description(String description) {
            this.description = description;
            return  this;
        }
        public GridItemBuilder LinkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
            return  this;
        }
        public GridItemBuilder VideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return  this;
        }
        public GridItemBuilder ImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return  this;
        }
        public GridItemBuilder Studio(String studio) {
            this.studio = studio;
            return  this;
        }
        public GridItemBuilder IsPlayable(boolean isPlayable) {
            this.isPlayable = isPlayable;
            return  this;
        }
        public GridItemBuilder Tag(String tag) {
            this.tag = tag;
            return  this;
        }

        public GridItem build() {
            return new GridItem(
                    id,
                    type,
                    channelTag,
                    category,
                    title,
                    sortOrder,
                    level,
                    description,
                    linkUrl,
                    videoUrl,
                    imageUrl,
                    studio,
                    isPlayable,
                    tag
            );
        }
    }

}
