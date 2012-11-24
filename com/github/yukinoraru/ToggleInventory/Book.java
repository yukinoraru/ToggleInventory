package com.github.yukinoraru.ToggleInventory;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Book {
    private String author;
    private String title;
    private String[] pages;

    public Book(ItemStack bookItem) {
        NBTTagCompound bookData = ((CraftItemStack) bookItem).getHandle().tag;

        this.author = bookData.getString("author");
        this.title = bookData.getString("title");

        NBTTagList nPages = bookData.getList("pages");

        String[] sPages = new String[nPages.size()];
        for (int i = 0; i < nPages.size(); i++) {
            // TODO: BOOK:URLENCODE
            sPages[i] = nPages.get(i).toString();
        }
        this.pages = sPages;
    }

    public Book(String title, String author, String[] pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String sAuthor) {
        this.author = sAuthor;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getPages() {
        return this.pages;
    }

    public void setPage(int page, String text) {
        this.pages[page] = text;
    }

    public ItemStack generateItemStack() {
        CraftItemStack newbook = new CraftItemStack(Material.WRITTEN_BOOK);

        NBTTagCompound newBookData = new NBTTagCompound();

        newBookData.setString("author", this.author);
        newBookData.setString("title", this.title);

        NBTTagList nPages = new NBTTagList();
        for (int i = 0; i < this.pages.length; i++) {
            //TODO:BOOK:URLDECODE
            nPages.add(new NBTTagString(this.pages[i], this.pages[i]));
        }

        newBookData.set("pages", nPages);
        newbook.getHandle().tag = newBookData;

        return newbook;
    }
}