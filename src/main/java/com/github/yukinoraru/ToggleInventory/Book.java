package com.github.yukinoraru.ToggleInventory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Book {
    private String encoded_author;
    private String encoded_title;
    private String[] encoded_pages;

    public Book(ItemStack bookItem) throws UnsupportedEncodingException {
        NBTTagCompound bookData = ((CraftItemStack) bookItem).getHandle().tag;

        this.encoded_author = bookData.getString("author");
        this.encoded_title = bookData.getString("title");

        NBTTagList nPages = bookData.getList("pages");

        String[] sPages = new String[nPages.size()];
        for (int i = 0; i < nPages.size(); i++) {
            sPages[i] = nPages.get(i).toString();
        }
        this.encoded_pages = sPages;

        this.encode(); //
    }

    private void encode() throws UnsupportedEncodingException{
        this.encoded_title = URLEncoder.encode(encoded_title, "UTF-8");
        this.encoded_author = URLEncoder.encode(encoded_author, "UTF-8");

        for (int i = 0; i < this.encoded_pages.length; i++) {
            this.encoded_pages[i] = URLEncoder.encode(this.encoded_pages[i], "UTF-8");
        }
    }

    public Book(String title, String author, String[] pages, Boolean isNeedToEncode) throws UnsupportedEncodingException {
        this.encoded_title = title;
        this.encoded_author = author;
        this.encoded_pages = pages;

        if(isNeedToEncode){
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
        CraftItemStack newbook = new CraftItemStack(Material.WRITTEN_BOOK);

        NBTTagCompound newBookData = new NBTTagCompound();

        // needs for decode. all members are encoded
        String decoded_author = URLDecoder.decode(this.encoded_author, "UTF-8");
        String decoded_title  = URLDecoder.decode(this.encoded_title, "UTF-8");

        newBookData.setString("author", decoded_author);
        newBookData.setString("title", decoded_title);

        NBTTagList nPages = new NBTTagList();
        for (int i = 0; i < this.encoded_pages.length; i++) {
            String decoded_page = URLDecoder.decode(this.encoded_pages[i], "UTF-8");
            nPages.add(new NBTTagString(decoded_page, decoded_page));
        }

        newBookData.set("pages", nPages);
        newbook.getHandle().tag = newBookData;

        return newbook;
    }
}