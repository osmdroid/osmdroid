package org.osmdroid.samplefragments.layouts.rec;

/**
 * created on 1/13/2017.
 *
 * @author Alex O'Ree
 */

/**
 * This class content Data
 * <p>
 * TypeLayout: type of layout to generate in recyclerview
 */

public class Info {


    public String TypeLayout;
    public String Title;
    public String Content;

    public Info(String typeLayout, String title, String content) {
        TypeLayout = typeLayout;
        Title = title;
        Content = content;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTypeLayout() {
        return TypeLayout;
    }

    public void setTypeLayout(String typeLayout) {
        TypeLayout = typeLayout;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

}
