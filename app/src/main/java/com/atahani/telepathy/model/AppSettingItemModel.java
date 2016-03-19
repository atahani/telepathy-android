package com.atahani.telepathy.model;

/**
 * used for Application Settings Recycler view adapter
 */
public class AppSettingItemModel {

    public final static int HEADER_ITEM_TYPE = 0;
    public final static int ITEM_DIVIDER_TYPE = 1;
    public final static int ITEM_OPEN_ACTIVITY = 2;
    public final static int ITEM_WITH_CHECKBOX = 3;
    public final static int ITEM_TO_OPEN_DIALOG = 4;
    public final static int ITEM_TO_LOG_OUT = 5;

    public final static int DIALOG_SET_NOTIFICATION_SOUND = 1;
    public final static int DIALOG_EDIT_LOCALE = 2;
    public final static int DIALOG_EDIT_THEME = 3;
    public final static int DIALOG_ABOUT = 4;

    public final static int CHECKBOX_NOTIFICATION_WHEN_TELEPATHIES_MATCHED = 0;
    public final static int CHECKBOX_NOTIFICATION_WHEN_ADD_YOU_AS_FRIEND = 1;
    public final static int CHECKBOX_VIBRATE = 2;

    private String title;
    private String status;
    private int type;
    private boolean isItemChecked;
    private String itemLabel;
    private int dialogSettingType;
    private int checkBoxType;
    private Class targetActivityClass;


    /**
     * initial group divider or item divider
     *
     * @param type int item type like ITEM_DIVIDER_TYPE
     */
    public AppSettingItemModel(int type) {
        this.type = type;
    }

    /**
     * initial header item
     *
     * @param type  int type of item
     * @param title String title
     */
    public AppSettingItemModel(int type, String title) {
        this.type = type;
        this.title = title;
        this.status = "";
    }

    /**
     * initial item with switch on/off
     *
     * @param type          int type of item
     * @param title         String title
     * @param isItemChecked boolean is switch checked
     */
    public AppSettingItemModel(int type, String title, int checkBoxType, boolean isItemChecked) {
        this.type = type;
        this.title = title;
        this.checkBoxType = checkBoxType;
        this.isItemChecked = isItemChecked;
    }

    /**
     * initial item to open dialog
     *
     * @param type              int type of item
     * @param title             String title
     * @param status            String status can be empty
     * @param dialogSettingType int dialog setting type
     */
    public AppSettingItemModel(int type, String title, String status, int dialogSettingType) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.dialogSettingType = dialogSettingType;
    }

    /**
     * initial item for ITEM_OPEN_ACTIVITY
     *
     * @param type                String Type
     * @param title               String Title
     * @param targetActivityClass Class TargetActivity
     * @param status              String status
     */
    public AppSettingItemModel(int type, String title, Class targetActivityClass, String status) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.targetActivityClass = targetActivityClass;
    }

    /* GETTER and SETTER  */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isItemChecked() {
        return isItemChecked;
    }

    public void setItemChecked(boolean isItemChecked) {
        this.isItemChecked = isItemChecked;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public int getDialogSettingType() {
        return dialogSettingType;
    }

    public void setDialogSettingType(int dialogSettingType) {
        this.dialogSettingType = dialogSettingType;
    }

    public int getCheckBoxType() {
        return checkBoxType;
    }

    public void setCheckBoxType(int checkBoxType) {
        this.checkBoxType = checkBoxType;
    }

    public Class getTargetActivityClass() {
        return targetActivityClass;
    }

    public void setTargetActivityClass(Class targetActivityClass) {
        this.targetActivityClass = targetActivityClass;
    }
}
