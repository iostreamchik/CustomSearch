package com.example.customsearch.app.entitys;

import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class  MyImage implements Comparable {
	private transient int id;
	private transient boolean isChecked;
	private transient byte[] thumbnailBLOB;
	private transient String kind;
	private transient String title;
	private transient String htmlTitle;
	private String link;
	private transient String displayLink;
	private String snippet;
	private transient String htmlSnippet;
	private transient String mime;
	@SerializedName("image")
	private Thumbnail thumbnail;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHtmlTitle() {
		return htmlTitle;
	}

	public void setHtmlTitle(String htmlTitle) {
		this.htmlTitle = htmlTitle;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDisplayLink() {
		return displayLink;
	}

	public void setDisplayLink(String displayLink) {
		this.displayLink = displayLink;
	}

	/**
	 * Image name
	 * @return String: image name
	 */
	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getHtmlSnippet() {
		return htmlSnippet;
	}

	public void setHtmlSnippet(String htmlSnippet) {
		this.htmlSnippet = htmlSnippet;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public Thumbnail getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Thumbnail thumbnail) {
		this.thumbnail = thumbnail;
	}

	public byte[] getThumbnailBLOB() {
		return thumbnailBLOB;
	}

	public void setThumbnailBLOB(byte[] thumbnailBLOB) {
		this.thumbnailBLOB = thumbnailBLOB;
	}

	@Override
	public int hashCode() {
		int result;
		result = link.hashCode();
		return result;
	}

	@Override
	public int compareTo(@NonNull Object another) {
		if (this == another) {
			return 0;
		}
		if (another.hashCode() > this.hashCode()) {
			return 1;
		}
		return -1;
	}
}
