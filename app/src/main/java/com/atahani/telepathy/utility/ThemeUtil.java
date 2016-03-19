package com.atahani.telepathy.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.model.ChooseItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The helper class to change application Theme
 */
public class ThemeUtil {

    public static final String PURPLE_THEME = "purple_theme";
    public static final String RED_THEME = "red_theme";
    public static final String GREEN_THEME = "green_theme";
    public static final String BLUE_THEME = "blue_theme";
    public static final String INDIGO_THEME = "indigo_theme";
    public static final String GREY_THEME = "grey_theme";
    public static final String BROWN_THEME = "brown_theme";

    private Context mContext;
    private SharedPreferences mPreferences;

    public ThemeUtil(Context context) {
        this.mContext = context;
        this.mPreferences = this.mContext.getSharedPreferences("app_preference", Context.MODE_PRIVATE);
    }

    public void setCurrentTheme(String themeName) {
        Integer themeResId = R.style.Indigo_AppTheme;
        Integer themeChildResId = R.style.Indigo_AppTheme_Child;
        Integer primaryColor = ContextCompat.getColor(mContext, R.color.indigo_theme_primary);
        Integer primaryDarkColor = ContextCompat.getColor(mContext, R.color.indigo_theme_primary_dark);
        Integer primaryLightColor = ContextCompat.getColor(mContext, R.color.indigo_theme_primary_light);
        Integer accentColor = ContextCompat.getColor(mContext, R.color.indigo_theme_accent);
        switch (themeName) {
            case BLUE_THEME:
                themeResId = R.style.Blue_AppTheme;
                themeChildResId = R.style.Blue_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.blue_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.blue_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.blue_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.blue_theme_accent);
                break;
            case BROWN_THEME:
                themeResId = R.style.Brown_AppTheme;
                themeChildResId = R.style.Brown_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.brown_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.brown_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.brown_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.brown_theme_accent);
                break;
            case GREEN_THEME:
                themeResId = R.style.Green_AppTheme;
                themeChildResId = R.style.Green_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.green_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.green_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.green_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.green_theme_accent);
                break;
            case INDIGO_THEME:
                themeResId = R.style.Indigo_AppTheme;
                themeChildResId = R.style.Indigo_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.indigo_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.indigo_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.indigo_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.indigo_theme_accent);
                break;
            case GREY_THEME:
                themeResId = R.style.Grey_AppTheme;
                themeChildResId = R.style.Grey_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.grey_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.grey_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.grey_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.grey_theme_accent);
                break;
            case PURPLE_THEME:
                themeResId = R.style.Purple_AppTheme;
                themeChildResId = R.style.Purple_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.purple_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.purple_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.purple_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.purple_theme_accent);
                break;
            case RED_THEME:
                themeResId = R.style.Red_AppTheme;
                themeChildResId = R.style.Red_AppTheme_Child;
                primaryColor = ContextCompat.getColor(mContext, R.color.red_theme_primary);
                primaryDarkColor = ContextCompat.getColor(mContext, R.color.red_theme_primary_dark);
                primaryLightColor = ContextCompat.getColor(mContext, R.color.red_theme_primary_light);
                accentColor = ContextCompat.getColor(mContext, R.color.red_theme_accent);
                break;
        }
        //save it to preference
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_profile_theme_name), themeName)
                .putInt(mContext.getString(R.string.pref_profile_theme_res_id), themeResId)
                .putInt(mContext.getString(R.string.pref_profile_theme_child_res_id), themeChildResId)
                .putInt(mContext.getString(R.string.pref_theme_primary_color), primaryColor)
                .putInt(mContext.getString(R.string.pref_theme_primary_dark_color), primaryDarkColor)
                .putInt(mContext.getString(R.string.pref_theme_primary_light_color), primaryLightColor)
                .putInt(mContext.getString(R.string.pref_theme_accent_color), accentColor)
                .apply();
    }

    /**
     * get the theme resourceId by themeName
     *
     * @param themeName String themeName like INDIGO_THEME
     * @return int the themeResourceId
     */
    public int getThemeResourceIdByThemeName(String themeName) {
        Integer themeResId = R.style.Indigo_AppTheme;
        switch (themeName) {
            case BLUE_THEME:
                themeResId = R.style.Blue_AppTheme;
                break;
            case BROWN_THEME:
                themeResId = R.style.Brown_AppTheme;
                break;
            case GREEN_THEME:
                themeResId = R.style.Green_AppTheme;
                break;
            case INDIGO_THEME:
                themeResId = R.style.Indigo_AppTheme;
                break;
            case GREY_THEME:
                themeResId = R.style.Grey_AppTheme;
                break;
            case PURPLE_THEME:
                themeResId = R.style.Purple_AppTheme;
                break;
            case RED_THEME:
                themeResId = R.style.Red_AppTheme;
                break;
        }
        return themeResId;
    }

    /**
     * get theme child theme base by theme name
     *
     * @param themeName String themeName like INDIGO_THEME
     * @return int the childThemeResourceId
     */
    public int getThemeChildResourceIdByThemeName(String themeName) {
        Integer themeChildResId = R.style.Indigo_AppTheme_Child;
        switch (themeName) {
            case BLUE_THEME:
                themeChildResId = R.style.Blue_AppTheme_Child;
                break;
            case BROWN_THEME:
                themeChildResId = R.style.Brown_AppTheme_Child;
                break;
            case GREEN_THEME:
                themeChildResId = R.style.Green_AppTheme_Child;
                break;
            case INDIGO_THEME:
                themeChildResId = R.style.Indigo_AppTheme_Child;
                break;
            case GREY_THEME:
                themeChildResId = R.style.Grey_AppTheme_Child;
                break;
            case PURPLE_THEME:
                themeChildResId = R.style.Purple_AppTheme_Child;
                break;
            case RED_THEME:
                themeChildResId = R.style.Red_AppTheme_Child;
                break;
        }
        return themeChildResId;
    }

    /**
     * get default Theme of application
     *
     * @return String default theme
     */
    public String getDefaultTheme() {
        return INDIGO_THEME;
    }


    /**
     * get the list of Primary Color of theme
     *
     * @param chooseItemType the ChooseItemType
     * @return List of Integer of colors
     */
    public List<ChooseItemModel> getColorThemeItemList(int chooseItemType) {
        List<ChooseItemModel> colorThemeItemList = new ArrayList<>();
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.purple_theme_primary), PURPLE_THEME));
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.red_theme_primary), RED_THEME));
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.green_theme_primary), GREEN_THEME));
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.blue_theme_primary), BLUE_THEME));
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.indigo_theme_primary), INDIGO_THEME));
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.grey_theme_primary), GREY_THEME));
        colorThemeItemList.add(new ChooseItemModel(chooseItemType, ContextCompat.getColor(mContext, R.color.brown_theme_primary), BROWN_THEME));
        return colorThemeItemList;
    }

    /**
     * get the primary color by theme name
     *
     * @param themeName String theme name
     * @return int primary color , if not found return default one
     */
    public int getPrimaryColorByThemeName(String themeName) {
        switch (themeName) {
            case BLUE_THEME:
                return ContextCompat.getColor(mContext, R.color.blue_theme_primary);
            case BROWN_THEME:
                return ContextCompat.getColor(mContext, R.color.brown_theme_primary);
            case GREEN_THEME:
                return ContextCompat.getColor(mContext, R.color.green_theme_primary);
            case INDIGO_THEME:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_primary);
            case GREY_THEME:
                return ContextCompat.getColor(mContext, R.color.grey_theme_primary);
            case PURPLE_THEME:
                return ContextCompat.getColor(mContext, R.color.purple_theme_primary);
            case RED_THEME:
                return ContextCompat.getColor(mContext, R.color.red_theme_primary);
            default:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_primary);
        }
    }

    /**
     * get the primary dark color by theme name
     *
     * @param themeName String theme name
     * @return int primary dark color , if not found return default one
     */
    public int getPrimaryDarkColorByThemeName(String themeName) {
        switch (themeName) {
            case BLUE_THEME:
                return ContextCompat.getColor(mContext, R.color.blue_theme_primary_dark);
            case BROWN_THEME:
                return ContextCompat.getColor(mContext, R.color.brown_theme_primary_dark);
            case GREEN_THEME:
                return ContextCompat.getColor(mContext, R.color.green_theme_primary_dark);
            case INDIGO_THEME:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_primary_dark);
            case GREY_THEME:
                return ContextCompat.getColor(mContext, R.color.grey_theme_primary_dark);
            case PURPLE_THEME:
                return ContextCompat.getColor(mContext, R.color.purple_theme_primary_dark);
            case RED_THEME:
                return ContextCompat.getColor(mContext, R.color.red_theme_primary_dark);
            default:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_primary_dark);
        }
    }

    /**
     * get the primary light color by theme name
     *
     * @param themeName String theme
     * @return int primary light color , if not found return default one
     */
    public int getPrimaryLightColorByThemeName(String themeName) {
        switch (themeName) {
            case BLUE_THEME:
                return ContextCompat.getColor(mContext, R.color.blue_theme_primary_light);
            case BROWN_THEME:
                return ContextCompat.getColor(mContext, R.color.brown_theme_primary_light);
            case GREEN_THEME:
                return ContextCompat.getColor(mContext, R.color.green_theme_primary_light);
            case INDIGO_THEME:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_primary_light);
            case GREY_THEME:
                return ContextCompat.getColor(mContext, R.color.grey_theme_primary_light);
            case PURPLE_THEME:
                return ContextCompat.getColor(mContext, R.color.purple_theme_primary_light);
            case RED_THEME:
                return ContextCompat.getColor(mContext, R.color.red_theme_primary_light);
            default:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_primary_light);
        }
    }

    /**
     * get accent color of theme by name
     *
     * @param themeName String theme name
     * @return int accent color , if not found return default one
     */
    public int getAccentColorByThemeName(String themeName) {
        switch (themeName) {
            case BLUE_THEME:
                return ContextCompat.getColor(mContext, R.color.blue_theme_accent);
            case BROWN_THEME:
                return ContextCompat.getColor(mContext, R.color.brown_theme_accent);
            case GREEN_THEME:
                return ContextCompat.getColor(mContext, R.color.green_theme_accent);
            case INDIGO_THEME:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_accent);
            case GREY_THEME:
                return ContextCompat.getColor(mContext, R.color.grey_theme_accent);
            case PURPLE_THEME:
                return ContextCompat.getColor(mContext, R.color.purple_theme_accent);
            case RED_THEME:
                return ContextCompat.getColor(mContext, R.color.red_theme_accent);
            default:
                return ContextCompat.getColor(mContext, R.color.indigo_theme_accent);
        }
    }
}
