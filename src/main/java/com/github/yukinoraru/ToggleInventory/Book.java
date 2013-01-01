package com.github.yukinoraru.ToggleInventory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Book {
	private String encoded_author;
	private String encoded_title;
	private String[] encoded_pages;

	public Book(ItemStack bookItem) throws UnsupportedEncodingException {
		BookMeta bookData = (BookMeta) bookItem.getItemMeta();

		this.encoded_author = bookData.getAuthor();
		this.encoded_title = bookData.getTitle();

		List<String> nPages = bookData.getPages();

		String[] sPages = new String[nPages.size()];
		for (int i = 0; i < nPages.size(); i++) {
			sPages[i] = nPages.get(i).toString();
		}
		this.encoded_pages = sPages;

		this.encode(); //
	}

	private void encode() throws UnsupportedEncodingException {
		this.encoded_title = URLEncoder.encode(encoded_title, "UTF-8");
		this.encoded_author = URLEncoder.encode(encoded_author, "UTF-8");

		for (int i = 0; i < this.encoded_pages.length; i++) {
			this.encoded_pages[i] = URLEncoder.encode(this.encoded_pages[i],
					"UTF-8");
		}
	}

	public Book(String title, String author, String[] pages,
			Boolean isNeedToEncode) throws UnsupportedEncodingException {
		this.encoded_title = title;
		this.encoded_author = author;
		this.encoded_pages = pages;

		if (isNeedToEncode) {
			this.encode(); //
		}
	}

	public String getAuthor() {
		return this.encoded_author;
	}

	public void setAuthor(String sAuthor) {
		this.encoded_author = sAuthor;
	}

	public String getTitle() {
		return this.encoded_title;
	}

	public void setTitle(String title) {
		this.encoded_title = title;
	}

	public String[] getPages() {
		return this.encoded_pages;
	}

	public void setPage(int page, String text) {
		this.encoded_pages[page] = text;
	}

	public ItemStack generateItemStack() throws UnsupportedEncodingException {
		ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK);

		// needs for decode. all members are encoded
		String decoded_author = URLDecoder.decode(this.encoded_author, "UTF-8");
		String decoded_title = URLDecoder.decode(this.encoded_title, "UTF-8");

		BookMeta bm = (BookMeta) newbook.getItemMeta();
		bm.setAuthor(decoded_author);
		bm.setTitle(decoded_title);

		for (int i = 0; i < this.encoded_pages.length; i++) {
			String decoded_page = URLDecoder.decode(this.encoded_pages[i],
					"UTF-8");
			bm.addPage(decoded_page);
		}

		newbook.setItemMeta(bm);

		return newbook;
	}
}