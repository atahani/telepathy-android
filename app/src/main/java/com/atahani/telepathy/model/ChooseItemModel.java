package com.atahani.telepathy.model;

import mobi.atahani.telepathy.R;

/**
 * bottom sheet item model
 */
public class ChooseItemModel {

    public final static int DEFAULT_ICON_COLOR = 0;

    public final static int ITEM_WITH_ICON = 1;
    public final static int ITEM_ITEM_WITH_OUT_ICON = 2;
    public final static int ITEM_THEME_COLOR = 3;
    public final static int ITEM_THEME_HR_COLOR = 4;

    public final static int ACTION_DO_NOTHING = 0;
    public final static int ACTION_TAKE_PHOTO = 1;
    public final static int ACTION_CHOOSE_FROM_GALLERY = 2;
    public final static int ACTION_REMOVE = 3;

    private int iconMimpmapResourceId;
    private String title;
    private int iconColorResourceId;
    private int type;
    private int actionType;
    private String valueCode;
    private int color;
    private String themeName;

    /**
     * initial for vertical item type or grid item type
     *
     * @param type                  int type of item
     * @param title                 String title
     * @param iconColorResourceId   int color resource id
     * @param iconMimpmapResourceId int icon Mimpmap resource id
     * @param actionType            int action type
     */
    public ChooseItemModel(int type, String title, int iconColorResourceId, int iconMimpmapResourceId, int actionType) {
        this.type = type;
        this.title = title;
        this.iconMimpmapResourceId = iconMimpmapResourceId;
        this.iconColorResourceId = iconColorResourceId;
        this.actionType = actionType;
    }

    /**
     * initial method for item with out icon for language
     * @param type
     * @param title
     * @param valueCode
     */
    public ChooseItemModel(int type, String title, String valueCode) {
        this.type = type;
        this.title = title;
        this.valueCode = valueCode;
    }

    /**
     * initial method for theme color item with themeName
     * @param color Int color
     * @param themeName  the String name of theme like blue_theme
     */
    public ChooseItemModel(int type,int color,String themeName){
        this.type = type;
        this.color = color;
        this.themeName = themeName;
    }

    /* generated setter and getter method */

    public int getIconMimpmapResourceId() {
        return iconMimpmapResourceId;
    }

    public void setIconMimpmapResourceId(int iconMimpmapResourceId) {
        this.iconMimpmapResourceId = iconMimpmapResourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconColorResourceId() {
        if (this.iconColorResourceId == DEFAULT_ICON_COLOR) {
            return R.color.theme_primary_tint_icon_color;
        } else {
            return iconColorResourceId;
        }
    }

    public void setIconColorResourceId(int iconColorResourceId) {
        this.iconColorResourceId = iconColorResourceId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public String getValueCode() {
        return valueCode;
    }

    public void setValueCode(String valueCode) {
        this.valueCode = valueCode;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }
}
